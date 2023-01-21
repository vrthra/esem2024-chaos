/*     */ package sbst.benchmark;
/*     */ 
/*     */ import java.io.BufferedOutputStream;
/*     */ import java.io.BufferedReader;
/*     */ import java.io.File;
/*     */ import java.io.FileOutputStream;
/*     */ import java.io.FileReader;
/*     */ import java.io.IOException;
/*     */ import java.io.OutputStream;
/*     */ import java.io.PrintStream;
/*     */ import java.io.PrintWriter;
/*     */ import java.util.ArrayList;
/*     */ import java.util.Arrays;
/*     */ import java.util.Collections;
/*     */ import java.util.HashSet;
/*     */ import java.util.List;
/*     */ import java.util.Optional;
/*     */ import java.util.Set;
/*     */ import java.util.concurrent.Callable;
/*     */ import java.util.concurrent.ExecutorService;
/*     */ import java.util.concurrent.Executors;
/*     */ import java.util.concurrent.FutureTask;
/*     */ import java.util.concurrent.TimeUnit;
/*     */ import org.apache.commons.exec.CommandLine;
/*     */ import org.apache.commons.exec.DefaultExecutor;
/*     */ import org.apache.commons.exec.ExecuteStreamHandler;
/*     */ import org.apache.commons.exec.PumpStreamHandler;
/*     */ import org.apache.tools.ant.DirectoryScanner;
/*     */ import org.junit.runner.Result;
/*     */ import org.junit.runner.notification.Failure;
/*     */ import sbst.benchmark.coverage.JaCoCoLauncher;
/*     */ import sbst.benchmark.coverage.JacocoResult;
/*     */ import sbst.benchmark.coverage.TestExecutionTask;
/*     */ import sbst.benchmark.pitest.MutationAnalysis;
/*     */ import sbst.benchmark.pitest.MutationsEvaluator;
/*     */ import sbst.benchmark.pitest.PITWrapper;
/*     */ import sbst.benchmark.pitest.TestInfo;
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ public class TestSuite
/*     */ {
/*     */   public static final int TEST_TIMEOUT = 60000;
/*     */   public static final int MAX_THREAD = 1;
/*     */   private static final String DUMMY_JUNIT_CLASS = "SBSTDummyForCoverageAndMutationCalculation";
/*     */   private final IBenchmarkTask task;
/*     */   private final String toolName;
/*     */   private final String benchmark;
/*     */   private final File testCaseDir;
/*     */   private JacocoResult jacoco_result;
/*  63 */   private Set<TestInfo> flakyTests = new HashSet<>();
/*     */   
/*     */   private final List<File> extraClassPath;
/*     */   
/*     */   private long execTime;
/*     */   
/*     */   private List<String> compiledTestSrcFiles;
/*     */   private int d4jBrokenTests;
/*     */   private int d4jFailTests;
/*     */   private int d4jLinesTotal;
/*     */   private int d4jLinesCovered;
/*     */   private int d4jConditionsTotal;
/*     */   
/*     */   public TestSuite(IBenchmarkTask task, String toolName, String benchmark, File testCaseDir, List<File> extraCP, String cut) {
/*  77 */     this.task = task;
/*  78 */     this.toolName = toolName;
/*  79 */     this.benchmark = benchmark;
/*  80 */     this.testCaseDir = testCaseDir;
/*     */     
/*  82 */     if (extraCP == null) {
/*  83 */       this.extraClassPath = Collections.emptyList();
/*     */     } else {
/*  85 */       this.extraClassPath = new ArrayList<>(extraCP);
/*     */     } 
/*     */     
/*  88 */     this.compiledTestSrcFiles = new ArrayList<>();
/*     */     try {
/*  90 */       File testCaseBinDir = getTestCaseBinDir(testCaseDir);
/*  91 */       Util.cleanDirectory(testCaseBinDir);
/*  92 */     } catch (IOException ioe) {
/*  93 */       Main.info("Unable to clean bin directory! Aborting test execution!");
/*     */       return;
/*     */     } 
/*  96 */     compile();
/*  97 */     run();
/*  98 */     findFlakyTests();
/*  99 */     jacoco(cut);
/* 100 */     long time = System.currentTimeMillis();
/* 101 */     mutationAnalysis(cut);
/* 102 */     System.out.println("Time for mutation analysis = " + (System.currentTimeMillis() - time));
/*     */   }
/*     */   private int d4jConditionsCovered; private int d4jMutantsGenerated; private int d4jMutantsCovered; private int d4jMutantsKilled; private int d4jMutantsLive; private int numberOfUncompilableTestClasses; private int numberOfTestClasses; private int d4jMutantsIgnored;
/*     */   public List<File> getExtraCP() {
/* 106 */     return this.extraClassPath;
/*     */   }
/*     */   
/*     */   public IBenchmarkTask getTask() {
/* 110 */     return this.task;
/*     */   }
/*     */   
/*     */   public File getTestCaseDir() {
/* 114 */     return this.testCaseDir;
/*     */   }
/*     */   
/*     */   public File getTestCaseBinDir(File testCaseDir) {
/* 118 */     return new File(testCaseDir, "bin");
/*     */   }
/*     */   
/*     */   public List<String> getTestSrcFiles(File testCaseDir) {
/* 122 */     DirectoryScanner scanner = new DirectoryScanner();
/* 123 */     scanner.setFollowSymlinks(false);
/* 124 */     scanner.setIncludes(new String[] { "**/*.java" });
/* 125 */     scanner.setBasedir(testCaseDir);
/* 126 */     scanner.setCaseSensitive(false);
/* 127 */     scanner.scan();
/* 128 */     String[] files = scanner.getIncludedFiles();
/* 129 */     List<String> testCaseFiles = Arrays.asList(files);
/* 130 */     return testCaseFiles;
/*     */   }
/*     */   
/*     */   public int getTestcaseNumber(File testCaseDir) {
/* 134 */     int numberTC = 0;
/* 135 */     List<String> testSrc = getTestSrcFiles(testCaseDir);
/*     */     
/* 137 */     for (String src : testSrc) {
/*     */       
/*     */       try {
/* 140 */         BufferedReader br = new BufferedReader(new FileReader(testCaseDir.getAbsolutePath() + File.separator + src)); try {
/*     */           String line;
/* 142 */           while ((line = br.readLine()) != null) {
/* 143 */             if (line.contains("@Test") || line.contains("@org.junit.Test"))
/* 144 */               numberTC++; 
/*     */           } 
/*     */         } finally {
/* 147 */           br.close();
/*     */         } 
/* 149 */       } catch (Throwable t) {
/* 150 */         Main.info("Could not calculate number of est cases!");
/* 151 */         t.printStackTrace(Main.debugStr);
/*     */       } 
/*     */     } 
/* 154 */     return numberTC;
/*     */   }
/*     */   
/*     */   public List<String> getTestClassNames() {
/* 158 */     List<String> result = new ArrayList<>();
/* 159 */     for (String testcaseFile : getTestSrcFiles(this.testCaseDir))
/* 160 */       result.add(fileNameToClassName(testcaseFile)); 
/* 161 */     return result;
/*     */   }
/*     */   
/*     */   public int getNumberOfUncompilableTestClasses() {
/* 165 */     return this.numberOfUncompilableTestClasses;
/*     */   }
/*     */   
/*     */   public List<String> getCompiledTestSrcFiles() {
/* 169 */     return this.compiledTestSrcFiles;
/*     */   }
/*     */   
/*     */   public List<String> getCompiledTestClassNames() {
/* 173 */     List<String> result = new ArrayList<>();
/* 174 */     for (String testcaseFile : getCompiledTestSrcFiles()) {
/* 175 */       String className = fileNameToClassName(testcaseFile);
/* 176 */       result.add(className);
/*     */     } 
/* 178 */     return result;
/*     */   }
/*     */   
/*     */   private static String fileNameToClassName(String fname) {
/* 182 */     String result = fname;
/* 183 */     if (fname.endsWith(".java")) {
/* 184 */       result = result.substring(0, result.length() - 5);
/* 185 */     } else if (fname.endsWith(".class")) {
/* 186 */       result = result.substring(0, result.length() - 6);
/*     */     } 
/* 188 */     return result.replace('/', '.');
/*     */   }
/*     */   
/*     */   public static String classsNameFileName(String cName, String suffix) {
/* 192 */     return cName.replace('.', '/') + suffix;
/*     */   }
/*     */ 
/*     */   
/*     */   private void compile() {
/*     */     try {
/* 198 */       Main.debug("Creating dummy JUnit test file");
/* 199 */       String junitDummyCode = "import org.junit.Test;\n\npublic class SBSTDummyForCoverageAndMutationCalculation{\n\n@org.junit.Test\npublic void test(){}\n}";
/*     */       
/* 201 */       File dummyFile = new File(this.testCaseDir.getAbsolutePath(), "SBSTDummyForCoverageAndMutationCalculation.java");
/* 202 */       if (dummyFile.exists()) {
/* 203 */         dummyFile.delete();
/*     */       }
/* 205 */       PrintStream dummyWriter = new PrintStream(new BufferedOutputStream(new FileOutputStream(dummyFile, true)), true, "UTF-8");
/*     */       
/* 207 */       dummyWriter.println(junitDummyCode);
/* 208 */       dummyWriter.close();
/* 209 */     } catch (IOException ioe) {
/* 210 */       Main.info("ERROR: Unable to compile dummy class! Aborting compilation!");
/*     */       
/*     */       return;
/*     */     } 
/* 214 */     Main.info("\n---Compilation---");
/* 215 */     List<String> srcFiles = getTestSrcFiles(this.testCaseDir);
/*     */     
/* 217 */     if (srcFiles.isEmpty()) {
/* 218 */       Main.info("Did not find any JUnit source files! Skipping compilation!");
/*     */       
/*     */       return;
/*     */     } 
/* 222 */     this.numberOfTestClasses = srcFiles.size() - 1;
/* 223 */     this.numberOfUncompilableTestClasses = srcFiles.size() - 1;
/*     */ 
/*     */ 
/*     */     
/* 227 */     String cp = (new Util.CPBuilder()).and(getTask().getClassPath()).and(Main.JUNIT_JAR).and(Main.JUNIT_DEP_JAR).and(getExtraCP()).build();
/*     */     
/* 229 */     File testCaseBinDir = getTestCaseBinDir(this.testCaseDir);
/*     */ 
/*     */     
/* 232 */     List<String> scaffoldingFiles = new ArrayList<>();
/* 233 */     for (String file : srcFiles) {
/* 234 */       if (file.contains("scaffolding")) {
/* 235 */         scaffoldingFiles.add(file);
/* 236 */         this.numberOfUncompilableTestClasses--;
/*     */       } 
/*     */     } 
/*     */     
/* 240 */     for (String f : srcFiles) {
/* 241 */       String commandLine = null;
/* 242 */       if (scaffoldingFiles.size() == 0 || f.contains("SBSTDummyForCoverageAndMutationCalculation")) {
/* 243 */         commandLine = String.format("%s -sourcepath %s -cp %s -d %s %s", new Object[] { Main.JAVAC, this.testCaseDir
/* 244 */               .getAbsolutePath(), cp, testCaseBinDir, this.testCaseDir
/* 245 */               .getAbsolutePath() + File.separator + f });
/* 246 */         Main.debug("\n===\njavac command line:\n" + commandLine + "\n");
/*     */       } else {
/*     */         
/* 249 */         Main.info("Compiling with scaffolding tests");
/*     */         
/* 251 */         if (scaffoldingFiles.contains(f)) {
/*     */           continue;
/*     */         }
/* 254 */         String files = "";
/* 255 */         for (String scaffTest : scaffoldingFiles) {
/* 256 */           files = files + " " + this.testCaseDir.getAbsolutePath() + File.separator + scaffTest;
/*     */         }
/*     */         
/* 259 */         files = files + " " + this.testCaseDir.getAbsolutePath() + File.separator + f;
/*     */         
/* 261 */         Main.info(">> " + files);
/*     */         
/* 263 */         commandLine = String.format("%s -sourcepath %s -cp %s -d %s %s", new Object[] { Main.JAVAC, this.testCaseDir
/* 264 */               .getAbsolutePath(), cp, testCaseBinDir, files });
/* 265 */         Main.debug("\n===\njavac command line:\n" + commandLine + "\n");
/*     */       } 
/*     */       
/*     */       try {
/* 269 */         PumpStreamHandler psh = new PumpStreamHandler(Main.debugStr);
/* 270 */         CommandLine cl = CommandLine.parse(commandLine);
/* 271 */         DefaultExecutor exec = new DefaultExecutor();
/* 272 */         exec.setStreamHandler((ExecuteStreamHandler)psh);
/* 273 */         exec.execute(cl);
/* 274 */         this.compiledTestSrcFiles.add(f);
/* 275 */         if (!f.contains("SBSTDummyForCoverageAndMutationCalculation")) {
/* 276 */           Main.info("Compiled '" + f + "'");
/* 277 */           this.numberOfUncompilableTestClasses--;
/*     */         } 
/* 279 */       } catch (Throwable t) {
/* 280 */         Main.info("Failed to compile '" + f + "'!");
/* 281 */         t.printStackTrace(Main.debugStr);
/*     */       } 
/*     */     } 
/*     */   }
/*     */   
/*     */   private void run() {
/* 287 */     Main.info("\n---Timing Information---");
/*     */     try {
/* 289 */       List<String> testClasses = getCompiledTestClassNames();
/*     */       
/* 291 */       if (testClasses.isEmpty()) {
/* 292 */         Main.info("There are no executable test classes! Skipping time calculation!");
/*     */         
/*     */         return;
/*     */       } 
/*     */       
/* 297 */       File testCaseBinDir = getTestCaseBinDir(this.testCaseDir);
/*     */       
/* 299 */       String cp = (new Util.CPBuilder()).and(getTask().getClassPath()).and(Main.JUNIT_JAR).and(Main.JUNIT_DEP_JAR).and(testCaseBinDir).and(getExtraCP()).build();
/*     */       
/* 301 */       Main.debug("Running tests with the following classpath: \n" + cp);
/* 302 */       long startTime = System.currentTimeMillis();
/*     */ 
/*     */       
/* 305 */       ExecutorService service = Executors.newFixedThreadPool(1);
/*     */       
/* 307 */       TestExecutionTask executor = new TestExecutionTask(cp, testClasses);
/* 308 */       FutureTask<List<Result>> task = (FutureTask)service.submit((Callable<?>)executor);
/* 309 */       List<Result> executionResults = task.get(60000L, TimeUnit.MILLISECONDS);
/* 310 */       updateFlakyTests(executionResults);
/*     */       
/* 312 */       service.shutdown();
/* 313 */       service.awaitTermination(1000L, TimeUnit.MILLISECONDS);
/*     */       
/* 315 */       for (Result res : executionResults) {
/* 316 */         if (res.getFailureCount() > 0) {
/* 317 */           Main.debug("Failure: " + res.getFailures());
/* 318 */           for (Failure fail : res.getFailures()) {
/* 319 */             Main.debug("Failing Tests: " + fail.getTestHeader() + "\n" + fail.getTrace());
/*     */           }
/*     */         } 
/*     */       } 
/*     */ 
/*     */       
/* 325 */       this.d4jBrokenTests = this.flakyTests.size();
/*     */       
/* 327 */       this.execTime = System.currentTimeMillis() - startTime;
/* 328 */     } catch (Throwable t) {
/* 329 */       Main.info("Could not run the compiled tests!");
/* 330 */       t.printStackTrace(Main.debugStr);
/*     */     } 
/*     */   }
/*     */   
/*     */   private void findFlakyTests() {
/* 335 */     Main.info("\n---The tests are re-executed other four times to find the flaky ones---");
/*     */     try {
/* 337 */       List<String> testClasses = getCompiledTestClassNames();
/*     */       
/* 339 */       if (testClasses.isEmpty()) {
/* 340 */         Main.info("There are no executable test classes! Skipping time calculation!");
/*     */         
/*     */         return;
/*     */       } 
/*     */       
/* 345 */       File testCaseBinDir = getTestCaseBinDir(this.testCaseDir);
/*     */       
/* 347 */       String cp = (new Util.CPBuilder()).and(getTask().getClassPath()).and(Main.JUNIT_JAR).and(Main.JUNIT_DEP_JAR).and(testCaseBinDir).and(getTask().getClassPath()).and(getExtraCP()).build();
/* 348 */       long startTime = System.currentTimeMillis();
/*     */ 
/*     */       
/* 351 */       ExecutorService service = Executors.newFixedThreadPool(1);
/* 352 */       List<FutureTask<List<Result>>> tasks = new ArrayList<>();
/* 353 */       for (int i = 0; i < 4; i++) {
/* 354 */         TestExecutionTask executor = new TestExecutionTask(cp, testClasses);
/* 355 */         FutureTask<List<Result>> task = (FutureTask)service.submit((Callable<?>)executor);
/* 356 */         tasks.add(task);
/*     */       } 
/*     */       
/* 359 */       for (FutureTask<List<Result>> task : tasks) {
/* 360 */         List<Result> executionResults = task.get(60000L, TimeUnit.MILLISECONDS);
/* 361 */         updateFlakyTests(task.get());
/*     */         
/* 363 */         for (Result res : executionResults) {
/* 364 */           if (res.getFailureCount() > 0) {
/* 365 */             Main.debug("Failure: " + res.getFailures());
/* 366 */             for (Failure fail : res.getFailures()) {
/* 367 */               Main.debug("Failing Tests: " + fail.getTestHeader() + "\n" + fail.getTrace());
/*     */             }
/*     */           } 
/*     */         } 
/*     */       } 
/* 372 */       service.shutdown();
/* 373 */       service.awaitTermination(1000L, TimeUnit.MILLISECONDS);
/*     */ 
/*     */       
/* 376 */       this.d4jBrokenTests = this.flakyTests.size();
/*     */       
/* 378 */       this.execTime = System.currentTimeMillis() - startTime;
/* 379 */     } catch (Throwable t) {
/* 380 */       Main.info("Could not run the compiled tests!");
/* 381 */       t.printStackTrace(Main.debugStr);
/*     */     } 
/*     */   }
/*     */   
/*     */   private void updateFlakyTests(List<Result> results) {
/* 386 */     for (Result result : results) {
/*     */       
/* 388 */       if (result.getFailures().size() > 0) {
/* 389 */         for (Failure fail : result.getFailures()) {
/* 390 */           String header = fail.getTestHeader();
/*     */           
/* 392 */           if (header.contains("(")) {
/* 393 */             String testMethod = header.substring(0, header.indexOf('('));
/* 394 */             String testClass = header.substring(header.indexOf('(') + 1, header.length());
/* 395 */             TestInfo info = new TestInfo(testClass, testMethod);
/* 396 */             this.flakyTests.add(info);
/*     */           } 
/*     */         } 
/*     */       }
/*     */     } 
/*     */   }
/*     */   
/*     */   public long getExecTime() {
/* 404 */     return this.execTime;
/*     */   }
/*     */   
/*     */   private void cmdExec(String cmdLine, OutputStream outStr) throws Throwable {
/* 408 */     PumpStreamHandler psh = new PumpStreamHandler(outStr);
/* 409 */     CommandLine cl = CommandLine.parse(cmdLine);
/* 410 */     DefaultExecutor exec = new DefaultExecutor();
/* 411 */     exec.setStreamHandler((ExecuteStreamHandler)psh);
/* 412 */     exec.execute(cl);
/*     */   }
/*     */   
/*     */   private void d4jTestArchiving(File testCaseDir, String archiveFileName) {
/* 416 */     String commandLine = "tar -cjf " + archiveFileName + " -C " + testCaseDir.getAbsolutePath() + " .";
/* 417 */     Main.debug("\n===\nDefects4J test cases archiving command line:\n\n" + commandLine + "\n\n");
/*     */     try {
/* 419 */       cmdExec(commandLine, Main.debugStr);
/* 420 */     } catch (Throwable t) {
/* 421 */       Main.info("Could not archive test cases!");
/* 422 */       t.printStackTrace(Main.debugStr);
/*     */     } 
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   private void jacoco(String cut) {
/* 430 */     Main.info("\n=== Run Jacoco for Coverage ===");
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */     
/*     */     try {
/* 439 */       JaCoCoLauncher launcher = new JaCoCoLauncher(getTestCaseBinDir(this.testCaseDir).getParent());
/*     */       
/* 441 */       launcher.setTestFolder(getTestCaseBinDir(this.testCaseDir).getAbsolutePath());
/*     */       
/* 443 */       List<String> testClasses = getCompiledTestClassNames();
/* 444 */       launcher.setTestCase(testClasses);
/*     */       
/* 446 */       launcher.setJarInstrument(getTask().getClassPath());
/*     */       
/* 448 */       String cp = (new Util.CPBuilder()).and(Main.JUNIT_DEP_JAR).and(Main.JUNIT_JAR).and(getExtraCP()).build();
/* 449 */       cp = cp.replaceAll("::", ":");
/* 450 */       launcher.setClassPath(cp);
/* 451 */       launcher.setTargetClass(cut);
/* 452 */       launcher.runJaCoCo();
/*     */       
/* 454 */       JacocoResult result = launcher.getResults();
/* 455 */       result.printResults();
/*     */ 
/*     */       
/* 458 */       this.d4jFailTests = 0;
/*     */ 
/*     */       
/* 461 */       if (result != null) {
/* 462 */         this.d4jLinesTotal = result.getLinesTotal();
/* 463 */         this.d4jLinesCovered = result.getLinesCovered();
/* 464 */         this.d4jConditionsTotal = result.getBranchesTotal();
/* 465 */         this.d4jConditionsCovered = result.getBranchesCovered();
/*     */       } 
/*     */       
/* 468 */       this.jacoco_result = result;
/*     */     }
/* 470 */     catch (Throwable t) {
/* 471 */       Main.info("Could not calculate coverage metrics!");
/* 472 */       t.printStackTrace(Main.debugStr);
/*     */     } 
/*     */   }
/*     */ 
/*     */ 
/*     */   
/*     */   private void mutationAnalysis(String cut) {
/* 479 */     Main.info("\n=== Run PIT ===");
/*     */ 
/*     */ 
/*     */     
/*     */     try {
/* 484 */       String cp = (new Util.CPBuilder()).and(Main.JUNIT_JAR).and(Main.JUNIT_DEP_JAR).and(getTask().getClassPath()).and(getExtraCP()).and(getTestCaseBinDir(this.testCaseDir).getAbsolutePath()).build();
/*     */       
/* 486 */       List<String> testClasses = getTestSrcFiles(this.testCaseDir);
/*     */       
/* 488 */       List<String> fixedTestClasses = new ArrayList<>();
/* 489 */       for (int index = 0; index < testClasses.size(); index++) {
/* 490 */         if (!((String)testClasses.get(index)).contains("SBSTDummyForCoverageAndMutationCalculation")) {
/* 491 */           String element = testClasses.get(index);
/* 492 */           element = element.replace("/", ".");
/* 493 */           if (element.startsWith("testcases."))
/* 494 */             element = element.replace("testcases.", ""); 
/* 495 */           element = element.substring(0, element.length() - 5);
/* 496 */           fixedTestClasses.add(element);
/*     */         } 
/*     */       } 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */       
/* 505 */       PITWrapper wrapper = new PITWrapper(cp, cut, fixedTestClasses);
/*     */ 
/*     */       
/* 508 */       MutationsEvaluator evaluator = new MutationsEvaluator(cp, cut, fixedTestClasses, this.flakyTests);
/*     */ 
/*     */       
/* 511 */       evaluator.computeCoveredMutants(wrapper.getGeneratedMutants(), Optional.ofNullable(this.jacoco_result));
/*     */ 
/*     */       
/* 514 */       Main.info("Executing Mutation Analysis using " + wrapper.getGeneratedMutants().getNumberOfMutations() + " mutants ");
/*     */       
/* 516 */       String mutated = getTestCaseBinDir(this.testCaseDir).getParent() + "/mutated_code";
/*     */ 
/*     */ 
/*     */ 
/*     */       
/*     */       try {
/* 522 */         evaluator.runMutations(mutated, (new Util.CPBuilder()).and(getTask().getClassPath()).build());
/* 523 */       } catch (InterruptedException e) {
/* 524 */         Main.info("ERROR runMutation was interrupted !");
/* 525 */         e.printStackTrace(Main.infoStr);
/* 526 */         evaluator.setTimeoutReached();
/*     */       } 
/*     */ 
/*     */       
/* 530 */       if (evaluator.isTimeoutReached()) {
/* 531 */         File time_out = new File(getTestCaseDir().getAbsolutePath() + File.separator + "TIMEOUT.txt");
/* 532 */         time_out.createNewFile();
/* 533 */         Main.info("ERROR Evaluation not completed ignore it.");
/*     */         
/*     */         return;
/*     */       } 
/*     */       
/* 538 */       MutationAnalysis cov = evaluator.getMutationCoverage();
/*     */ 
/*     */ 
/*     */       
/* 542 */       cov.deleteFlakyTest(this.flakyTests);
/*     */       
/* 544 */       if (cov != null) {
/*     */         
/* 546 */         this.d4jMutantsGenerated = cov.getNumberOfMutations() - cov.numberOfIgnoredMutation();
/* 547 */         this.d4jMutantsLive = this.d4jMutantsGenerated - cov.numberOfKilledMutation();
/*     */         
/* 549 */         this.d4jMutantsCovered = cov.getNumberOfCoveredMutants() - cov.numberOfIgnoredMutation();
/*     */         
/* 551 */         this.d4jMutantsIgnored = cov.numberOfIgnoredMutation();
/* 552 */         this.d4jMutantsKilled = cov.numberOfKilledMutation();
/*     */       } 
/*     */ 
/*     */ 
/*     */       
/* 557 */       PrintWriter out = new PrintWriter(getTestCaseBinDir(this.testCaseDir).getParent() + "/mutation_results.txt");
/* 558 */       out.println(cov.toString());
/* 559 */       out.close();
/*     */     }
/* 561 */     catch (Throwable t) {
/* 562 */       Main.info("Could not calculate mutation metrics!");
/* 563 */       t.printStackTrace(Main.debugStr);
/*     */     } 
/*     */   }
/*     */   
/*     */   public int getD4jMutantsIgnored() {
/* 568 */     return this.d4jMutantsIgnored;
/*     */   }
/*     */   
/*     */   public int getD4jBrokenTests() {
/* 572 */     return this.d4jBrokenTests;
/*     */   }
/*     */   
/*     */   public int getD4jFailTests() {
/* 576 */     return this.d4jFailTests;
/*     */   }
/*     */   
/*     */   public int getD4jLinesTotal() {
/* 580 */     return this.d4jLinesTotal;
/*     */   }
/*     */   
/*     */   public int getD4jLinesCovered() {
/* 584 */     return this.d4jLinesCovered;
/*     */   }
/*     */   
/*     */   public int getD4jConditionsTotal() {
/* 588 */     return this.d4jConditionsTotal;
/*     */   }
/*     */   
/*     */   public int getD4jConditionsCovered() {
/* 592 */     return this.d4jConditionsCovered;
/*     */   }
/*     */   
/*     */   public int getD4jMutantsGenerated() {
/* 596 */     return this.d4jMutantsGenerated;
/*     */   }
/*     */   
/*     */   public int getD4jMutantsCovered() {
/* 600 */     return this.d4jMutantsCovered;
/*     */   }
/*     */   
/*     */   public int getD4jMutantsKilled() {
/* 604 */     return this.d4jMutantsKilled;
/*     */   }
/*     */   
/*     */   public int getD4jMutantsLive() {
/* 608 */     return this.d4jMutantsLive;
/*     */   }
/*     */   
/*     */   public int getNumberOfTestClasses() {
/* 612 */     return this.numberOfTestClasses;
/*     */   }
/*     */ }


/* Location:              /Users/gambi/killmatrix/repo/sbstcontest/benchmarktool/1.0.0/benchmarktool-1.0.0.jar!/sbst/benchmark/TestSuite.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */