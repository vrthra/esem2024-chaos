package sbst.benchmark.coverage;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.runner.RemoteJUnitCore;
import org.pitest.mutationtest.engine.MutationIdentifier;

import sbst.benchmark.Main;

public class RemoteTestExecutionTask implements Callable<Map<String, Object>> {
    private String cp;
    private List<String> testClasses;
    private Map<String, Object> results = new HashMap<>();
    private MutationIdentifier mutation;
    private File outputDir;
    private int id;
    private AtomicInteger sharedCounter;

    
    // TODO Not sure where to put this one!
    public static long UNIT_TEST_TIMEOUT = 10000; 
    
    public RemoteTestExecutionTask(String cp, List<String> pTestClasses) {
        this.cp = cp;
        this.testClasses = pTestClasses;
    }

    public void setOutputLocation(File outputDir) {
        this.outputDir = outputDir;
    }

    public void setMutation(MutationIdentifier mutation, int id) {
        this.mutation = mutation;
        this.id = id;
    }

    private File createTempFile() throws IOException {
        if (this.outputDir == null) {
            Path temp = Files.createTempFile("junit-remote", ".txt", (FileAttribute<?>[]) new FileAttribute[0]);
            temp.toFile().deleteOnExit();
            return temp.toFile();
        }
        return File.createTempFile("test-esecution-", ".txt", this.outputDir);
    }

    public String executeJunitCore() throws IOException, InterruptedException {
        File outputFile = createTempFile();
        // Moved to MutationsEvaluator
//        File mutationDescriptionFile = new File(outputFile.getParentFile(), "mutation.txt");

        List<String> _args = new ArrayList<>();

        if (Main.REMOTE_JAVA == null) {
            _args.add(Main.JAVA);
        } else {
            _args.addAll(Arrays.asList((String[]) Main.REMOTE_JAVA.split(":")));
        }

        if (System.getProperty(RemoteJUnitCore.REMOTE_JUNIT_DEBUG) != null) {
            _args.add("-D"+RemoteJUnitCore.REMOTE_JUNIT_DEBUG + "=true");
        }
        
        _args.add("-D"+RemoteJUnitCore.REMOTE_JUNIT_MUTANT_ID+"=" + this.id);
        _args.add("-D"+RemoteJUnitCore.REMOTE_JUNIT_OUTPUT_TO+"=" + outputFile.getAbsolutePath());

        // Force a resonable timeout to avoid spinnig until the watchdog triggers
        _args.add("-D"+RemoteJUnitCore.REMOTE_JUNIT_TIMEOUT+ "=" + UNIT_TEST_TIMEOUT);
        
        if (System.getProperty(RemoteJUnitCore.REMOTE_JUNIT_SKIP_JUNIT_3) != null) {
            // Forward the option to the remote executor. Since this is a java option must be -D...
            _args.add("-D"+RemoteJUnitCore.REMOTE_JUNIT_SKIP_JUNIT_3 + "="
                    + System.getProperty(RemoteJUnitCore.REMOTE_JUNIT_SKIP_JUNIT_3));
            //
            System.out.println("Setting additional property: " + RemoteJUnitCore.REMOTE_JUNIT_SKIP_JUNIT_3 + "="
                    + System.getProperty(RemoteJUnitCore.REMOTE_JUNIT_SKIP_JUNIT_3));
        }

        
        
        _args.add("-cp");

        _args.add(this.cp + File.pathSeparatorChar + System.getProperty("java.class.path"));

        _args.add(RemoteJUnitCore.class.getName());
        for (String testClass : this.testClasses) {
            _args.add(testClass);
        }

        // Use
        ProcessBuilder pb = (new ProcessBuilder(_args)).inheritIO();

        final Process process = pb.start();

        final int mutantId = this.id;
        Thread watchdog = new Thread(new Runnable() {

            @Override
            public void run() {
                //
                System.out.println(mutantId + " Watchdog set to 5 minutes");
                try {
                    Thread.sleep(5 * 60 * 1000L);
                    System.out.println(mutantId + " Watchdog Triggers <<<< ");
                    // Kill the process
                    process.destroyForcibly();
                } catch (InterruptedException e) {
                } finally {
                    System.out.println(mutantId + " Watchdog stopping");
                }
            }
        }, "Watchdog for mutation " + this.id);

        watchdog.start();
        int exitCode = process.waitFor();

        if (watchdog.isAlive()) {
            Main.info(this.id + "Stopping watchdog");
            watchdog.interrupt();
        }

        // Moved to MutationsEvaluator
//        if (this.mutation != null) {
//            try (PrintStream ps = new PrintStream(mutationDescriptionFile)) {
//                ps.println(this.mutation.toString());
//            }
//        }

        Main.info(
                this.id + " ========================================================================================");
        Main.info(this.id + " Remote Execution Done "
                + ((this.mutation != null) ? (" for PIT mutant " + this.mutation) : ""));
        Main.info(this.id + " Exit code : " + exitCode);
        if (this.sharedCounter != null) {
            Main.info(this.id + " Remaining executions " + this.sharedCounter.decrementAndGet());
        }
        String stdOut = new String(Files.readAllBytes(outputFile.toPath()));
        Main.info(
                this.id + " ========================================================================================");
//        Main.info("STDOUT: ");
//        Main.info(stdOut);
//        Main.info("========================================================================================");

        return stdOut;
    }

    public Map<String, Object> call() throws ClassNotFoundException, IOException, InterruptedException {
        Main.info("Running tests from " + this.testClasses.size() + " test classes"
                + ((this.mutation != null) ? (" against mutant " + this.mutation) : ""));

        try {
            String[] outputLines = executeJunitCore().split(System.lineSeparator());
            //
            for (int i = 0; i < outputLines.length && !outputLines[i].trim().isEmpty(); i++) {

                String[] tokens = outputLines[i].split(" ");

//            Main.debug("Got Tokens: " + Arrays.toString( tokens ));

                this.results.put(tokens[0], tokens[1]);
            }

            if (this.mutation != null) {
                this.results.put("MUTATION", this.mutation);
                this.results.put("MUTATION_ID", Integer.valueOf(this.id));
            }
            return this.results;
        } catch (Throwable e) {
            e.printStackTrace();
            Main.info("ERROR: Failed execution from " + this.testClasses.size() + " test classes"
                    + ((this.mutation != null) ? (" against mutant " + this.mutation) : ""));
            return this.results;
            // TODO: handle exception
        }
    }

    public void setSharedCounter(AtomicInteger sharedCounter) {
        this.sharedCounter = sharedCounter;
    }
}