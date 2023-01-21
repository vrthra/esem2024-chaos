/*     */ package sbst.benchmark;
/*     */ 
/*     */ import java.io.BufferedOutputStream;
/*     */ import java.io.File;
/*     */ import java.io.FileOutputStream;
/*     */ import java.io.IOException;
/*     */ import java.io.OutputStream;
/*     */ import java.io.OutputStreamWriter;
/*     */ import java.io.PrintStream;
/*     */ import java.io.Writer;
/*     */ import org.apache.commons.configuration.ConfigurationException;
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
/*     */ public class Main
/*     */ {
/*     */   private static final String USAGE = "sbstcontest <toolname> <benchmark> <tooldirectory> <run_number> <timebudget> [--only-compute-metrics|--only-generate-tests] [TestCaseCopyDirectory]";
/*     */   public static final String TOOLEXECUTABLE = "runtool";
/*     */   
/*     */   public static class NullOutputStream
/*     */     extends OutputStream
/*     */   {
/*     */     public void write(int b) throws IOException {}
/*     */   }
/*     */   
/*  38 */   public static final String BENCHMARK_CONFIG = System.getProperty("sbst.benchmark.config");
/*  39 */   public static final String JAVAC = System.getProperty("sbst.benchmark.javac");
/*  40 */   public static final String JAVA = System.getProperty("sbst.benchmark.java");
/*     */   
/*  42 */   public static final String REMOTE_JAVA = System.getProperty("sbst.benchmark.remote.java");
/*  43 */   public static final String PITEST_JAR = System.getProperty("sbst.benchmark.pitest");
/*  44 */   public static final String JUNIT_JAR = System.getProperty("sbst.benchmark.junit");
/*  45 */   public static final String JUNIT_DEP_JAR = System.getProperty("sbst.benchmark.junit.dependency");
/*  46 */   public static final String JACOCO_JAR = System.getProperty("sbst.benchmark.jacoco");
/*     */   
/*     */   private static final String ONLY_COMPUTE_METRICS = "--only-compute-metrics";
/*     */   private static final String ONLY_GENERATE_TESTS = "--only-generate-tests";
/*     */   public static PrintStream debugStr;
/*     */   public static PrintStream infoStr;
/*     */   public static boolean debugFlag = false;
/*  53 */   public static String testCaseCopyDir = null;
/*     */   
/*     */   public static void info(String info) {
/*  56 */     System.out.println(info);
/*  57 */     infoStr.println(info);
/*  58 */     debug(info);
/*     */   }
/*     */   
/*     */   public static void debug(String info) {
/*  62 */     debugStr.println(info);
/*     */   }
/*     */   
/*     */   public static void main(String[] args) throws IOException {
/*  66 */     BenchmarkCollection bmcollection = null;
/*  67 */     String toolName = null, benchmark = null;
/*  68 */     IBenchmarkTask task = null;
/*  69 */     File toolDirectory = null;
/*  70 */     int runNumber = -1;
/*  71 */     int timeBudget = -1;
/*  72 */     String mode = null;
/*     */     
/*  74 */     if (BENCHMARK_CONFIG != null) {
/*     */       try {
/*  76 */         bmcollection = new BenchmarkCollection(new File(BENCHMARK_CONFIG));
/*  77 */       } catch (ConfigurationException e) {
/*  78 */         e.printStackTrace();
/*     */       } 
/*     */     } else {
/*  81 */       System.err.println("Undefined property: sbst.benchmark.config");
/*     */     } 
/*  83 */     if (args.length >= 1) {
/*  84 */       toolName = args[0];
/*  85 */       if (toolName == null || toolName.isEmpty()) {
/*  86 */         System.out.println("Invalid tool name!");
/*     */         return;
/*     */       } 
/*     */     } 
/*  90 */     if (args.length >= 2) {
/*  91 */       benchmark = args[1];
/*  92 */       if (bmcollection != null)
/*  93 */         task = bmcollection.forName(benchmark); 
/*     */     } 
/*  95 */     if (args.length >= 3)
/*  96 */       toolDirectory = new File(args[2]); 
/*  97 */     if (args.length >= 4) {
/*     */       try {
/*  99 */         runNumber = (new Integer(args[3])).intValue();
/* 100 */       } catch (NumberFormatException nfe) {
/* 101 */         System.out.println("Invalid run number: " + args[3]);
/*     */         
/*     */         return;
/*     */       } 
/*     */     }
/* 106 */     if (args.length >= 5) {
/*     */       try {
/* 108 */         timeBudget = (new Integer(args[4])).intValue();
/* 109 */       } catch (NumberFormatException nfe) {
/* 110 */         System.out.println("Invalid run number: " + args[3]);
/*     */         
/*     */         return;
/*     */       } 
/*     */     }
/* 115 */     if (args.length >= 6) {
/* 116 */       mode = args[5];
/* 117 */       if (!mode.equals("--only-generate-tests") && !mode.equals("--only-compute-metrics")) {
/* 118 */         System.out.println("Invalid mode type " + mode);
/* 119 */         System.out.println("Allowed mode types are --only-generate-tests and --only-compute-metrics");
/*     */         
/*     */         return;
/*     */       } 
/* 123 */       if (mode.equals("--only-compute-metrics") && args.length < 7) {
/* 124 */         System.out.println("Using mode --only-compute-metrics requires setting an archive folder to read the test cases");
/*     */         
/*     */         return;
/*     */       } 
/* 128 */       if (args.length >= 7) {
/* 129 */         testCaseCopyDir = args[6];
/*     */       }
/*     */     } 
/*     */     
/* 133 */     if (bmcollection == null || benchmark == null || task == null || toolDirectory == null) {
/* 134 */       System.err.println("sbstcontest <toolname> <benchmark> <tooldirectory> <run_number> <timebudget> [--only-compute-metrics|--only-generate-tests] [TestCaseCopyDirectory]");
/* 135 */       if (bmcollection != null) {
/* 136 */         if (benchmark != null && task == null) {
/* 137 */           System.out.println("Invalid benchmark name: " + benchmark);
/*     */         }
/* 139 */         System.out.println("Available benchmarks: " + bmcollection.getBenchmarks());
/*     */       } 
/*     */       
/*     */       return;
/*     */     } 
/* 144 */     debugStr = new PrintStream(new BufferedOutputStream(new FileOutputStream(new File(toolDirectory, "log_detailed.txt"), true)), true, "UTF-8");
/*     */ 
/*     */     
/* 147 */     infoStr = new PrintStream(new BufferedOutputStream(new FileOutputStream(new File(toolDirectory, "log.txt"), true)), true, "UTF-8");
/*     */ 
/*     */ 
/*     */     
/*     */     try {
/* 152 */       if (toolDirectory.exists() && toolDirectory.isDirectory()) {
/* 153 */         File executable = new File(toolDirectory, "runtool");
/* 154 */         if (executable.exists() && executable.isFile() && executable.canExecute()) {
/* 155 */           info("\n>>> TOOL NAME:  " + toolName);
/* 156 */           info("\n>>> BENCHMARK:  " + benchmark);
/* 157 */           info("\n>>> RUN NUMBER: " + runNumber);
/* 158 */           if (mode != null) {
/* 159 */             info("\n>>> MODE: " + mode);
/*     */           }
/* 161 */           File transcriptFile = new File(toolDirectory, "transcript.csv");
/* 162 */           Writer transcriptWriter = new OutputStreamWriter(new FileOutputStream(transcriptFile, true));
/* 163 */           TranscriptWriter transcript = new TranscriptWriter(task, transcriptWriter);
/* 164 */           transcript.start(toolName, timeBudget, benchmark, runNumber);
/* 165 */           RunTool tool = new RunTool(toolDirectory, executable, transcript, timeBudget);
/*     */           
/* 167 */           if (mode != null && mode.equals("--only-generate-tests")) {
/* 168 */             transcript.enableGenerateTests();
/* 169 */             tool.enableGenerateTests();
/* 170 */             transcript.disableComputeMetrics();
/* 171 */           } else if (mode != null && mode.equals("--only-compute-metrics")) {
/* 172 */             transcript.disableGenerateTests();
/* 173 */             tool.disableGenerateTests();
/* 174 */             transcript.enableComputeMetrics();
/*     */           } else {
/* 176 */             transcript.enableGenerateTests();
/* 177 */             tool.enableGenerateTests();
/* 178 */             transcript.enableComputeMetrics();
/*     */           } 
/* 180 */           tool.run(task);
/* 181 */           transcript.finish();
/* 182 */           transcriptWriter.close();
/*     */         } else {
/* 184 */           info("No executable found at: '" + executable + "'");
/*     */           return;
/*     */         } 
/*     */       } else {
/* 188 */         info("Not a valid directory: '" + args[2] + "'");
/*     */         return;
/*     */       } 
/*     */     } finally {
/* 192 */       debugStr.flush();
/* 193 */       debugStr.close();
/*     */     } 
/*     */ 
/*     */     
/* 197 */     System.exit(0);
/*     */   }
/*     */ }


/* Location:              /Users/gambi/killmatrix/repo/sbstcontest/benchmarktool/1.0.0/benchmarktool-1.0.0.jar!/sbst/benchmark/Main.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */