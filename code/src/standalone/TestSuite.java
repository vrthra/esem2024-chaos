package standalone;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileAttribute;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteStreamHandler;
import org.apache.commons.exec.PumpStreamHandler;
import org.apache.tools.ant.DirectoryScanner;
import org.jacoco.core.analysis.Analyzer;
import org.jacoco.core.analysis.CoverageBuilder;
import org.jacoco.core.analysis.IClassCoverage;
import org.jacoco.core.analysis.ICoverageVisitor;
import org.jacoco.core.data.ExecutionDataStore;
import org.jacoco.core.tools.ExecFileLoader;
import org.objectweb.asm.ClassReader;
import sbst.benchmark.Main;
import sbst.benchmark.Util;
import sbst.benchmark.coverage.JacocoResult;
import sbst.benchmark.pitest.MutationAnalysis;
import sbst.benchmark.pitest.MutationSet;
import sbst.benchmark.pitest.MutationsEvaluator;
import sbst.benchmark.pitest.PITWrapper;
import sbst.benchmark.pitest.TestInfo;

public class TestSuite {
    public static final int TEST_TIMEOUT = 60000;
    public static final int MAX_THREAD = 1;
    private static final String DUMMY_JUNIT_CLASS = "SBSTDummyForCoverageAndMutationCalculation";
    private final File testCaseDir;
    private List<String> compiledTestSrcFiles;
    private File testCaseBinDir;
    private Map<String, JacocoResult> jacocoResults;
    private Set<TestInfo> flakyTests = new HashSet<>();

    private final List<File> extraClassPath;

    private final List<File> classPath;

    private long execTime;

    private int d4jBrokenTests;

    private int d4jFailTests;
    private int d4jLinesTotal;
    private int d4jLinesCovered;
    private int d4jConditionsTotal;

    public TestSuite(File testCaseDir, List<File> CP, List<File> extraCP, String... cuts) {
        this.testCaseDir = testCaseDir;

        if (CP == null) {
            this.classPath = Collections.emptyList();
        } else {
            this.classPath = new ArrayList<>(CP);
        }

        if (extraCP == null) {
            this.extraClassPath = Collections.emptyList();
        } else {
            this.extraClassPath = new ArrayList<>(extraCP);
        }

        this.compiledTestSrcFiles = new ArrayList<>();

        try {
            Path path = Files.createTempDirectory("mutation-analysis-tests-bin-",
                    (FileAttribute<?>[]) new FileAttribute[0]);

            this.testCaseBinDir = path.toFile();
            this.testCaseBinDir.deleteOnExit();
            Main.debug("Bin folder for tests is " + this.testCaseBinDir);
            Util.cleanDirectory(this.testCaseBinDir);
        } catch (IOException ioe) {
            Main.info("Unable to clean bin directory! Aborting test execution!");

            return;
        }
        this.cuts = cuts;
    }

    private int d4jConditionsCovered;
    private int d4jMutantsGenerated;
    private int d4jMutantsCovered;
    private int d4jMutantsKilled;
    private int d4jMutantsLive;
    private int numberOfUncompilableTestClasses;
    private int numberOfTestClasses;
    private int d4jMutantsIgnored;
    private String[] cuts;

    public List<File> getClassPath() {
        return this.classPath;
    }

    public List<File> getExtraCP() {
        return this.extraClassPath;
    }

    public File getTestCaseDir() {
        return this.testCaseDir;
    }

    public File getTestCaseBinDir() {
        return this.testCaseBinDir;
    }

    public List<String> getTestSrcFiles(File testCaseDir) {
        DirectoryScanner scanner = new DirectoryScanner();
        scanner.setFollowSymlinks(false);
        scanner.setIncludes(new String[] { "**/*.java" });
        scanner.setBasedir(testCaseDir);
        scanner.setCaseSensitive(false);
        scanner.scan();
        String[] files = scanner.getIncludedFiles();
        List<String> testCaseFiles = Arrays.asList(files);

        Set<String> tests = new HashSet<>();

        for (String testCaseFile : testCaseFiles) {
            try {
                for (String line : Files
                        .readAllLines(Paths.get(testCaseDir.getAbsolutePath(), new String[] { testCaseFile }))) {
                    if (line.contains("@Test")) {
                        tests.add(testCaseFile);
                    }
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return new ArrayList<>(tests);
    }

    public int getTestcaseNumber(File testCaseDir) {
        int numberTC = 0;
        List<String> testSrc = getTestSrcFiles(testCaseDir);

        for (String src : testSrc) {

            try {
                BufferedReader br = new BufferedReader(
                        new FileReader(testCaseDir.getAbsolutePath() + File.separator + src));
                try {
                    String line;
                    while ((line = br.readLine()) != null) {
                        if (line.contains("@Test") || line.contains("@org.junit.Test"))
                            numberTC++;
                    }
                } finally {
                    br.close();
                }
            } catch (Throwable t) {
                Main.info("Could not calculate number of est cases!");
                t.printStackTrace(Main.debugStr);
            }
        }
        return numberTC;
    }

    public List<String> getTestClassNames() {
        List<String> result = new ArrayList<>();
        for (String testcaseFile : getTestSrcFiles(this.testCaseDir))
            result.add(fileNameToClassName(testcaseFile));
        return result;
    }

    public int getNumberOfUncompilableTestClasses() {
        return this.numberOfUncompilableTestClasses;
    }

    public List<String> getCompiledTestSrcFiles() {
        return this.compiledTestSrcFiles;
    }

    public void setCompiledTestSrcFiles(List<String> files) {
        this.compiledTestSrcFiles = files;
    }

    public List<String> getCompiledTestClassNames() {
        List<String> result = new ArrayList<>();
        for (String testcaseFile : getCompiledTestSrcFiles()) {
            String className = fileNameToClassName(testcaseFile);
            result.add(className);
        }
        return result;
    }

    private static String fileNameToClassName(String fname) {
        String result = fname;
        if (fname.endsWith(".java")) {
            result = result.substring(0, result.length() - 5);
        } else if (fname.endsWith(".class")) {
            result = result.substring(0, result.length() - 6);
        }
        return result.replace('/', '.');
    }

    public static String classsNameFileName(String cName, String suffix) {
        return cName.replace('.', '/') + suffix;
    }

    public void compileTests() {
        Main.info("\n---Recompile test tests---");
        Set<String> srcFiles = new HashSet<>(getTestSrcFiles(this.testCaseDir));

        if (srcFiles.isEmpty()) {
            Main.info("Did not find any JUnit source files! Skipping compilation!");

            return;
        }
        this.numberOfTestClasses = srcFiles.size() - 1;
        this.numberOfUncompilableTestClasses = srcFiles.size() - 1;

        String cp = (new Util.CPBuilder()).and(getClassPath()).and(getExtraCP()).build();

        File testCaseBinDir = getTestCaseBinDir();

        StringBuilder commandLine = new StringBuilder();
        commandLine.append(String.format("%s -sourcepath %s -cp %s -d %s ", new Object[] { Main.JAVAC, this.testCaseDir

                .getAbsolutePath(), cp, testCaseBinDir }));

        for (String testSrcFile : srcFiles) {
            if (testSrcFile.contains("Test.java")) {
                this.compiledTestSrcFiles.add(testSrcFile);
            }
            commandLine.append(String.format("%s ",
                    new Object[] { this.testCaseDir.getAbsolutePath() + File.separator + testSrcFile }));
        }

        Main.info("\n===\njavac command line:\n" + commandLine + "\n");
        try {
            PumpStreamHandler psh = new PumpStreamHandler(Main.infoStr);
            CommandLine cl = CommandLine.parse(commandLine.toString());
            DefaultExecutor exec = new DefaultExecutor();
            exec.setStreamHandler((ExecuteStreamHandler) psh);
            exec.execute(cl);
        } catch (Throwable t) {
            Main.info("Failed to compile !");
            t.printStackTrace();
        }
    }

    public long getExecTime() {
        return this.execTime;
    }

    private void cmdExec(String cmdLine, OutputStream outStr) throws Throwable {
        PumpStreamHandler psh = new PumpStreamHandler(outStr);
        CommandLine cl = CommandLine.parse(cmdLine);
        DefaultExecutor exec = new DefaultExecutor();
        exec.setStreamHandler((ExecuteStreamHandler) psh);
        exec.execute(cl);
    }

    private void d4jTestArchiving(File testCaseDir, String archiveFileName) {
        String commandLine = "tar -cjf " + archiveFileName + " -C " + testCaseDir.getAbsolutePath() + " .";
        Main.debug("\n===\nDefects4J test cases archiving command line:\n\n" + commandLine + "\n\n");
        try {
            cmdExec(commandLine, Main.debugStr);
        } catch (Throwable t) {
            Main.info("Could not archive test cases!");
            t.printStackTrace(Main.debugStr);
        }
    }

    public void computeCoverage(File executionDataFile) throws IOException {
        this.jacocoResults = new HashMap<>();

        ExecFileLoader execFileLoader = new ExecFileLoader();
        execFileLoader.load(executionDataFile);

        ExecutionDataStore executionDataStore = execFileLoader.getExecutionDataStore();

        CoverageBuilder coverageBuilder = new CoverageBuilder();
        Analyzer analyzer = new Analyzer(executionDataStore, (ICoverageVisitor) coverageBuilder);

        for (File f : getClassPath()) {
            if (f.isDirectory() && f.getName().endsWith("compiled-classes")) {
                try (Stream<Path> walk = Files.walk(f.toPath(), new java.nio.file.FileVisitOption[0])) {

                    List<String> result = (List<String>) walk.map(x -> x.toString())
                            .filter(y -> (y.endsWith(".class") && !y.contains("$"))).collect(Collectors.toList());

                    for (String classFile : result) {
                        Main.debug("Extracting coverage for " + classFile);
                        File file = new File(classFile);
                        try {
                            if (file.isFile()) {
                                analyzer.analyzeClass(new ClassReader(new FileInputStream(file)));
                            }
                        } catch (Throwable e) {
                            Main.info("Failed to analyze file: " + classFile);
                            e.printStackTrace();
                        }

                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        for (IClassCoverage cc : coverageBuilder.getClasses()) {
            String fullyQualifiedName = cc.getName().replace("/", ".");
            this.jacocoResults.put(fullyQualifiedName, new JacocoResult(cc));
            Main.debug("Adding coverage for " + fullyQualifiedName);
        }
    }

    private String generateDirectoryHostingMutationsForCUT(String cut) {
        return this.testCaseDir + File.separator + "mutated_code" + File.separator + cut;
    }

    public void mutationAnalysis() {
        int allMutant = 0;
        int coveredMutant = 0;
        int evaluatedMutants = 0;

        Map<String, MutationsEvaluator> mutationsEvaluators = new HashMap<>();
        Map<String, Integer> mutantCount = new HashMap<>();

        for (String cut : this.cuts) {

            if (!cut.trim().isEmpty()) {

                try {

                    String cp = (new Util.CPBuilder()).and(getClassPath()).and(getExtraCP())
                            .and(getTestCaseBinDir().getAbsolutePath()).build();

                    List<String> testClasses = getTestSrcFiles(this.testCaseDir);

                    List<String> fixedTestClasses = new ArrayList<>();

                    for (int index = 0; index < testClasses.size(); index++) {
                        if (!((String) testClasses.get(index)).contains("SBSTDummyForCoverageAndMutationCalculation")) {
                            String element = testClasses.get(index);
                            element = element.replace("/", ".");

                            if (element.startsWith("testcases.")) {
                                element = element.replace("testcases.", "");
                            }
                            element = element.substring(0, element.length() - 5);
                            fixedTestClasses.add(element);
                        }
                    }

                    PITWrapper wrapper = new PITWrapper(cp, cut, fixedTestClasses);

                    MutationSet mutants = wrapper.getGeneratedMutants();

                    MutationsEvaluator evaluator = new MutationsEvaluator(cp, cut, fixedTestClasses, this.flakyTests);

                    mutationsEvaluators.put(cut, evaluator);

                    ///
                    MutationAnalysis ma = new MutationAnalysis(mutants, getJacocoResultFor(cut));
                    allMutant += ma.getNumberOfMutations();
                    coveredMutant += ma.getNumberOfCoveredMutants();

                    Main.info("MUTANT-COVERAGE:class " + cut + " mutants " + ma.getNumberOfMutations() + " covered "
                            + ma.getNumberOfCoveredMutants());

                    evaluator.computeCoveredMutants(mutants, getJacocoResultFor(cut));
                } catch (Throwable t) {
                    t.printStackTrace();
                    Main.info("Failed to generate mutants for " + cut);
                    t.printStackTrace(Main.infoStr);
                }
            }
        }

        Main.info("PROGRESS: Starting mutation analysis");
        Main.info("PROGRESS: Total      mutants " + allMutant);
        Main.info("PROGRESS: Covered    mutants " + coveredMutant);

        int evaluatedCUT = 0;

        int totalCUT = mutationsEvaluators.size();
        for (Map.Entry<String, MutationsEvaluator> mutationToExecute : mutationsEvaluators.entrySet()) {

            try {
                Main.info("PROGRESS: Remaining class to evaluate " + (totalCUT - evaluatedCUT) + "/ " + totalCUT);

                String cut = mutationToExecute.getKey();
                MutationsEvaluator evaluator = mutationToExecute.getValue();

                String mutated = generateDirectoryHostingMutationsForCUT(cut);
                Main.info("Mutants will be stored under " + mutated);

                try {

                    evaluator.runMutations(mutated,
                            (new Util.CPBuilder()).and(getClassPath()).and(getExtraCP()).build());
                } catch (InterruptedException e) {
                    Main.info("ERROR runMutation was interrupted !");
                    e.printStackTrace(Main.infoStr);
                    evaluator.setTimeoutReached();
                }

            } catch (Throwable t) {
                Main.info("Could not calculate mutation metrics!");
                t.printStackTrace();
                t.printStackTrace(Main.debugStr);
            }
        }
    }

    private Optional<JacocoResult> getJacocoResultFor(String cut) {
        Optional<JacocoResult> res = Optional.ofNullable(this.jacocoResults.get(cut));
        return res;
    }

    public int getD4jMutantsIgnored() {
        return this.d4jMutantsIgnored;
    }

    public int getD4jBrokenTests() {
        return this.d4jBrokenTests;
    }

    public int getD4jFailTests() {
        return this.d4jFailTests;
    }

    public int getD4jLinesTotal() {
        return this.d4jLinesTotal;
    }

    public int getD4jLinesCovered() {
        return this.d4jLinesCovered;
    }

    public int getD4jConditionsTotal() {
        return this.d4jConditionsTotal;
    }

    public int getD4jConditionsCovered() {
        return this.d4jConditionsCovered;
    }

    public int getD4jMutantsGenerated() {
        return this.d4jMutantsGenerated;
    }

    public int getD4jMutantsCovered() {
        return this.d4jMutantsCovered;
    }

    public int getD4jMutantsKilled() {
        return this.d4jMutantsKilled;
    }

    public int getD4jMutantsLive() {
        return this.d4jMutantsLive;
    }

    public int getNumberOfTestClasses() {
        return this.numberOfTestClasses;
    }
}