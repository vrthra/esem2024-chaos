package sbst.benchmark.pitest;

// import org.apache.commons.io.FileUtils;

import org.pitest.mutationtest.engine.MutationDetails;
import org.pitest.mutationtest.engine.MutationIdentifier;
import sbst.benchmark.FileUtils;
import sbst.benchmark.Main;
import sbst.benchmark.Util;
import sbst.benchmark.coverage.JacocoResult;
import sbst.benchmark.coverage.RemoteTestExecutionTask;

import java.io.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class MutationsEvaluator {
    public static final int MAX_THREAD;
    private static final long GLOBAL_TIMEOUT = 3600L;

    public static final Set<Integer> BLACK_LIST;

    static {
        int parallelism = 1;
        try {
            parallelism = Integer.parseInt(System.getProperty("sbst.benchmark.parallelism", "1"));
        } catch (NumberFormatException numberFormatException) {
        }

        MAX_THREAD = parallelism;

        Set<Integer> mutationsToSkip = new HashSet<Integer>();

        if (System.getProperties().containsKey("sbst.benchmark.skip")) {
            try {
                Arrays.asList(System.getProperty("sbst.benchmark.skip").split(","))
                        .forEach(id -> mutationsToSkip.add(Integer.parseInt(id)));
            } catch (NumberFormatException numberFormatException) {
            }
        }

        BLACK_LIST = mutationsToSkip;
    }

    public static final boolean ENABLE_REMOTE_EXECUTION = Boolean
            .parseBoolean(System.getProperty("sbst.benchmark.remoting", "false"));

    private String tempFolder;

    private String classPath;

    private String classToMutate;

    private List<String> targetTest;

    private MutationAnalysis mutationResults;

    private MutationSet allTheMutants;

    private Set<TestInfo> flakyTests;

    private boolean timeoutReached = false;

    public MutationsEvaluator(String pClassPath, String pClassToMutate, List<String> pTargetTest,
                              Set<TestInfo> pFlakyTests) {
        this.classPath = pClassPath;
        this.classToMutate = pClassToMutate;
        this.targetTest = pTargetTest;
        this.flakyTests = pFlakyTests;
    }

    public void computeCoveredMutants(MutationSet set, Optional<JacocoResult> jacoco) {
        this.allTheMutants = set;
        this.mutationResults = new MutationAnalysis(set, jacoco);
    }

    public void runMutations(String tempFolder, String path2SUT)
            throws ClassNotFoundException, IOException, InterruptedException, ExecutionException {
        this.tempFolder = tempFolder;
        if (tempFolder.equals(path2SUT)) {
            throw new IllegalArgumentException(
                    "Source and target directories should be different: \n source directory = " + path2SUT
                            + "target directory = " + tempFolder);
        }

        String newSUT = this.tempFolder + "/SUT";

        String newSutCP = createSUTCopy(path2SUT, newSUT);

        String CUT = newSUT + "/" + this.classToMutate.replace('.', '/') + ".class";
        File fcut = new File(CUT);
        if (fcut.exists()) {
            FileUtils.deleteQuietly(fcut);
        }

        ExecutorService service = Executors.newScheduledThreadPool(MAX_THREAD);

        long start_time = System.currentTimeMillis();

        // Always generate and executed all the mutants
        MutationSet coveredMutants = this.mutationResults.getCoveredMutants();

        Set<MutationIdentifier> mutationsToRun = coveredMutants.getMutationIDs();

        LinkedList<RemoteTestExecutionTask> task_list = new LinkedList<>();

        int mutation_counter = -1;
        AtomicInteger sharedCounter = new AtomicInteger(0);
        // Iterate over all the the mutants
        for (MutationIdentifier mutation : this.allTheMutants.getMutationIDs()) {

            sharedCounter.incrementAndGet();

            mutation_counter++;

            //            byte[] mu = coveredMutants.getMutantion(mutation).getBytes();
            byte[] mu = this.allTheMutants.getMutantion(mutation).getBytes();

            String newCUT = this.tempFolder + "/CUT" + mutation_counter;

            // write to disk all the details that we have about this mutation
            writeMutationOnDisk(mu, newCUT, this.allTheMutants.getMutantionDetails(mutation));

            String newCP = this.classPath.replace(path2SUT, newSutCP);

            newCP = newCUT + File.pathSeparator + newCP;

            List<String> testClasses = new ArrayList<>();
            testClasses.addAll(this.targetTest);

            if (BLACK_LIST.contains(new Integer(mutation_counter))) {
                Main.info("Mutant " + mutation_counter + " was black listed.");

            }
            else if (mutationsToRun.contains(mutation)) {
                Main.info("Mutant " + mutation_counter + " scheduled for execution.");

                RemoteTestExecutionTask executor = new RemoteTestExecutionTask(newCP, testClasses);

                executor.setMutation(mutation, mutation_counter);

                executor.setOutputLocation(new File(newCUT));

                executor.setSharedCounter(sharedCounter);

                task_list.addLast(executor);
            }
            else {
                Main.info("Mutant " + mutation_counter + " not covered.");
            }
        }

        // Alessio: Reduce this to a few minutes - All or nothing: all the tasks must be
        // completed in 180 seconds?
        List<Future<Map<String, Object>>> all = service.invokeAll((Collection) task_list); // , 600, TimeUnit.SECONDS);
        // //3600L,
        // TimeUnit.SECONDS);
        service.shutdown();

        for (Future<Map<String, Object>> future : all) {

            if (future.isCancelled()) {
                Main.info("\n Execution was cancelled. Timeout triggered for a mutant !!!");
                this.timeoutReached = true;
                continue;
            }

            Map<String, Object> testExecutionResults;
            try {
                testExecutionResults = future.get(600, TimeUnit.SECONDS);
                MutationIdentifier mutation = (MutationIdentifier) testExecutionResults.remove("MUTATION");
                int i = ((Integer) testExecutionResults.remove("MUTATION_ID")).intValue();
                Main.info("Mutant " + i + " Done");
            } catch (InterruptedException | ExecutionException | TimeoutException e) {
                Main.info("Execution did not get any result in timeout");
                this.timeoutReached = true;
            } catch (Exception e) {
                Main.info("Generic exception raised. We need to skip this mutant");
                e.printStackTrace();
            }
        }
    }

    public static String createSUTCopy(String path2SUT, String tempFolder) {
        File source = new File(path2SUT);
        File target = new File(tempFolder);

        Util.CPBuilder cpb = new Util.CPBuilder();
        cpb.and(target);
        try {
            if (path2SUT.contains(":")) {
                String[] libraries = path2SUT.split(":");
                for (String lib : libraries) {
                    lib = lib.replace(":", "");
                    if (lib.length() > 0) {
                        File fileLib = new File(lib);
                        if (fileLib.isDirectory()) {
                            FileUtils.copyDirectory(fileLib, target);
                        }
                        else {

                            FileUtils.copyFileToDirectory(fileLib, target);
                            cpb.and(new File(target, fileLib.getName()));
                        }
                    }
                }
            }
            else {

                if (!source.exists()) {
                    throw new FileNotFoundException();
                }
                FileUtils.copyDirectory(source, target);
                cpb.and(new File(target, source.getName()));
            }

        } catch (IOException e) {
            Main.debug("Could not make a copy of the SUT " + path2SUT);
            e.printStackTrace(Main.debugStr);
            return null;
        }

        return cpb.build();
    }

    public void writeMutationOnDisk(byte[] mu, String location, MutationDetails mutationDetails) {

        String newCUT = location + "/" + this.classToMutate.replace('.', '/') + ".class";

        File locationFile = new File(location);
        if (!locationFile.exists()) {
            Main.debug("Create folder " + locationFile.getAbsolutePath());
            locationFile.mkdirs();
        }

        File mutationDescriptionFile = new File(locationFile, "mutation.txt");

        try (PrintStream ps = new PrintStream(mutationDescriptionFile)) {
            // This also outputs the MutationIdentifier
            ps.println(mutationDetails.toString());
            Main.debug("Writing generated mutation to " + mutationDescriptionFile);
        } catch (Throwable t) {
            Main.info("Failed to write the generated mutation to " + mutationDescriptionFile + " - " + t.getMessage());
        }

        File changed_code = new File(newCUT);
        changed_code.getParentFile().mkdirs();

        try {
            FileOutputStream output = new FileOutputStream(changed_code);
            output.write(mu);
            output.flush();
            output.close();
        } catch (FileNotFoundException e) {

            e.printStackTrace();
        } catch (IOException e) {

            e.printStackTrace();
        }
    }

    public MutationAnalysis getMutationCoverage() {
        return this.mutationResults;
    }

    public boolean isTimeoutReached() {
        return this.timeoutReached;
    }

    public void setTimeoutReached() {
        this.timeoutReached = true;
    }
}