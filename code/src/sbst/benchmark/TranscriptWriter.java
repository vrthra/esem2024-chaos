/*     */ package sbst.benchmark;
/*     */ 
/*     */ import java.io.BufferedReader;
/*     */ import java.io.BufferedWriter;
/*     */ import java.io.File;
/*     */ import java.io.FileNotFoundException;
/*     */ import java.io.FileReader;
/*     */ import java.io.FileWriter;
/*     */ import java.io.IOException;
/*     */ import java.io.PrintWriter;
/*     */ import java.io.Writer;
/*     */ import java.util.List;
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
/*     */ public class TranscriptWriter
/*     */   implements IToolListener
/*     */ {
/*     */   private static final String PREPARATION_TIME = "prepTime";
/*     */   private static final String GENERATION_TIME = "genTime";
/*     */   private static final String TIMING_FILENAME = "timing.txt";
/*     */   private final PrintWriter transcript;
/*     */   private long time;
/*     */   private final IBenchmarkTask task;
/*     */   private String toolName;
/*     */   private String benchmark;
/*     */   private int runNumber;
/*     */   private long prepTime;
/*     */   private boolean computeMetrics = true;
/*     */   
/*     */   public void enableComputeMetrics() {
/*  41 */     this.computeMetrics = true;
/*     */   }
/*     */   
/*     */   public void disableComputeMetrics() {
/*  45 */     this.computeMetrics = false;
/*     */   }
/*     */   
/*     */   private boolean generateTests = true;
/*     */   private int timeBudget;
/*     */   
/*     */   public void enableGenerateTests() {
/*  52 */     this.generateTests = true;
/*     */   }
/*     */   
/*     */   public void disableGenerateTests() {
/*  56 */     this.generateTests = false;
/*     */   }
/*     */   
/*     */   public TranscriptWriter(IBenchmarkTask task, Writer transcriptWriter) {
/*  60 */     this.task = task;
/*  61 */     this.transcript = new PrintWriter(transcriptWriter);
/*     */   }
/*     */ 
/*     */   
/*     */   public void startPreprocess() {
/*  66 */     this.time = System.currentTimeMillis();
/*     */   }
/*     */ 
/*     */   
/*     */   public void endPreprocess() {
/*  71 */     this.prepTime = System.currentTimeMillis() - this.time;
/*     */     
/*  73 */     this.transcript.println("tool,benchmark,class,run,preparationTime,generationTime,executionTime,testcaseNumber,uncompilableNumber,brokenTests,failTests,linesTotal,linesCovered,linesCoverageRatio,conditionsTotal,conditionsCovered,conditionsCoverageRatio,mutantsTotal,mutantsCovered,mutantsCoverageRatio,mutantsKilled,mutantsKillRatio,mutantsAlive,timeBudget,totalTestClasses");
/*     */   }
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
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public void startClass(String cname) {
/* 111 */     this.time = System.currentTimeMillis();
/*     */   }
/*     */ 
/*     */ 
/*     */   
/*     */   public void endClass(String cname, File testcaseDir, List<File> extraCP) {
/* 117 */     int numTestCases = 0;
/*     */     
/* 119 */     int d4jBrokenTests = 0, d4jFailTests = 0, d4jLinesTotal = 0, d4jLinesCovered = 0, d4jConditionsTotal = 0;
/* 120 */     int d4jConditionsCovered = 0, d4jMutantsGenerated = 0, d4jMutantsCovered = 0, d4jMutantsKilled = 0;
/* 121 */     int d4jMutantsLive = 0;
/*     */     
/* 123 */     long execTime = 0L, genTime = execTime;
/*     */ 
/*     */ 
/*     */     
/*     */     try {
/* 128 */       Util.pause(1.0D);
/*     */       
/* 130 */       if (this.generateTests && !this.computeMetrics) {
/*     */         
/* 132 */         genTime = System.currentTimeMillis() - this.time;
/* 133 */         writeTimingFile(testcaseDir, genTime, this.prepTime);
/*     */         
/* 135 */         if (Main.testCaseCopyDir != null) {
/* 136 */           File archiveDir = new File(Main.testCaseCopyDir);
/* 137 */           if (archiveDir.exists())
/*     */           {
/*     */             
/* 140 */             Util.CopyToDirectory(testcaseDir, archiveDir, cname);
/*     */           }
/*     */         }
/*     */       
/* 144 */       } else if (!this.generateTests && this.computeMetrics) {
/*     */         
/* 146 */         testcaseDir = new File(Main.testCaseCopyDir);
/*     */         
/* 148 */         TimingData t = readTimingFile(testcaseDir);
/* 149 */         this.prepTime = t.preparationTime.longValue();
/* 150 */         genTime = t.generationTime.longValue();
/*     */       } else {
/*     */         
/* 153 */         genTime = System.currentTimeMillis() - this.time;
/*     */       } 
/*     */       
/* 156 */       if (this.computeMetrics) {
/* 157 */         TestSuite testSuite = new TestSuite(this.task, this.toolName, this.benchmark, testcaseDir, extraCP, cname);
/*     */ 
/*     */ 
/*     */         
/* 161 */         numTestCases = testSuite.getTestcaseNumber(testcaseDir) - 1;
/* 162 */         execTime = testSuite.getExecTime();
/*     */         
/* 164 */         int uncompilableNumber = testSuite.getNumberOfUncompilableTestClasses();
/* 165 */         d4jBrokenTests = testSuite.getD4jBrokenTests();
/* 166 */         d4jFailTests = testSuite.getD4jFailTests();
/* 167 */         d4jLinesTotal = testSuite.getD4jLinesTotal();
/* 168 */         d4jLinesCovered = testSuite.getD4jLinesCovered();
/* 169 */         d4jConditionsTotal = testSuite.getD4jConditionsTotal();
/* 170 */         d4jConditionsCovered = testSuite.getD4jConditionsCovered();
/* 171 */         d4jMutantsGenerated = testSuite.getD4jMutantsGenerated();
/* 172 */         d4jMutantsCovered = testSuite.getD4jMutantsCovered();
/* 173 */         d4jMutantsKilled = testSuite.getD4jMutantsKilled();
/* 174 */         d4jMutantsLive = testSuite.getD4jMutantsLive();
/*     */         
/* 176 */         this.transcript.println(this.toolName + "," + this.benchmark + "," + cname + "," + this.runNumber + "," + this.prepTime + "," + genTime + "," + execTime + "," + numTestCases + "," + uncompilableNumber + "," + d4jBrokenTests + "," + d4jFailTests + "," + d4jLinesTotal + "," + d4jLinesCovered + "," + ((d4jLinesTotal == 0) ? 0.0F : (d4jLinesCovered / d4jLinesTotal * 100.0F)) + "," + d4jConditionsTotal + "," + d4jConditionsCovered + "," + ((d4jConditionsTotal == 0) ? 0.0F : (d4jConditionsCovered / d4jConditionsTotal * 100.0F)) + "," + d4jMutantsGenerated + "," + d4jMutantsCovered + "," + ((d4jMutantsGenerated == 0) ? 0.0F : (d4jMutantsCovered / d4jMutantsGenerated * 100.0F)) + "," + d4jMutantsKilled + "," + ((d4jMutantsGenerated == 0) ? 0.0F : (d4jMutantsKilled / d4jMutantsGenerated * 100.0F)) + "," + d4jMutantsLive + "," + this.timeBudget + "," + testSuite
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
/* 189 */             .getNumberOfTestClasses());
/*     */         
/* 191 */         this.transcript.flush();
/*     */         
/* 193 */         Main.info("\n>>> RESULTS:");
/* 194 */         Main.info("\tTool name: " + this.toolName);
/* 195 */         Main.info("\tBenchmark: " + this.benchmark);
/* 196 */         Main.info("\tClass Under Test: " + cname);
/* 197 */         Main.info("\tRun number: " + this.runNumber);
/* 198 */         Main.info("\tTool preparation time (ms): " + this.prepTime);
/* 199 */         Main.info("\tTool test cases generation time (ms): " + genTime);
/* 200 */         Main.info("\tTest cases execution time (ms): " + execTime);
/* 201 */         Main.info("\tTest case number: " + numTestCases);
/* 202 */         Main.info("\tUncompilable Test Classes: " + uncompilableNumber);
/* 203 */         Main.info("\tBroken tests number: " + d4jBrokenTests);
/* 204 */         Main.info("\tFailing tests number: " + d4jFailTests);
/* 205 */         Main.info("\tLines total: " + d4jLinesTotal);
/* 206 */         Main.info("\tLines covered: " + d4jLinesCovered);
/* 207 */         Main.info("\tLines coverage ratio (%): " + ((d4jLinesTotal == 0) ? 0.0F : (d4jLinesCovered / d4jLinesTotal * 100.0F)));
/*     */         
/* 209 */         Main.info("\tConditions total: " + d4jConditionsTotal);
/* 210 */         Main.info("\tConditions covered: " + d4jConditionsCovered);
/* 211 */         Main.info("\tConditions coverage ratio (%): " + ((d4jConditionsTotal == 0) ? 0.0F : (d4jConditionsCovered / d4jConditionsTotal * 100.0F)));
/*     */         
/* 213 */         Main.info("\tMutants total: " + d4jMutantsGenerated);
/* 214 */         Main.info("\tMutants covered: " + d4jMutantsCovered);
/* 215 */         Main.info("\tMutants coverage ratio (%): " + ((d4jMutantsGenerated == 0) ? 0.0F : (d4jMutantsCovered / d4jMutantsGenerated * 100.0F)));
/*     */         
/* 217 */         Main.info("\tMutants killed: " + d4jMutantsKilled);
/* 218 */         Main.info("\tMutants killed ratio (%): " + ((d4jMutantsGenerated == 0) ? 0.0F : (d4jMutantsKilled / d4jMutantsGenerated * 100.0F)));
/*     */         
/* 220 */         Main.info("\tMutants alive: " + d4jMutantsLive);
/*     */       } 
/* 222 */     } catch (Throwable e) {
/* 223 */       Main.info("ERROR: Something went wrong! Consult log.txt for more infos!");
/* 224 */       e.printStackTrace(Main.debugStr);
/*     */     } 
/*     */   }
/*     */   
/*     */   private static class TimingData {
/*     */     Long generationTime;
/*     */     Long preparationTime;
/*     */     
/*     */     private TimingData() {} }
/*     */   
/*     */   private TimingData readTimingFile(File testcaseDir) throws FileNotFoundException, IOException {
/* 235 */     String generationTimeFilename = testcaseDir.getAbsolutePath() + File.separator + "timing.txt";
/* 236 */     BufferedReader bfr = new BufferedReader(new FileReader(new File(generationTimeFilename)));
/*     */     try {
/* 238 */       String line = bfr.readLine();
/* 239 */       TimingData t = new TimingData();
/* 240 */       while (line != null) {
/* 241 */         if (!line.contains("=")) {
/* 242 */           throw new RuntimeException("Malformed line " + line);
/*     */         }
/* 244 */         String[] parts = line.split("=");
/* 245 */         if (parts.length != 2) {
/* 246 */           throw new RuntimeException("Malformed line " + line);
/*     */         }
/* 248 */         String key = parts[0];
/* 249 */         String value = parts[1];
/* 250 */         if (key.equals("genTime")) {
/* 251 */           long generationTime = Long.valueOf(value).longValue();
/* 252 */           t.generationTime = Long.valueOf(generationTime);
/* 253 */         } else if (key.equals("prepTime")) {
/* 254 */           long preparationTime = Long.valueOf(value).longValue();
/* 255 */           t.preparationTime = Long.valueOf(preparationTime);
/*     */         } else {
/* 257 */           throw new RuntimeException("Unknown key= " + key);
/*     */         } 
/*     */ 
/*     */         
/* 261 */         line = bfr.readLine();
/*     */       } 
/* 263 */       return t;
/*     */     } finally {
/* 265 */       bfr.close();
/*     */     } 
/*     */   }
/*     */   
/*     */   private void writeTimingFile(File testcaseDir, long genTime, long preparationTime) throws IOException {
/* 270 */     String generationTimeFilename = testcaseDir.getAbsolutePath() + File.separator + "timing.txt";
/* 271 */     BufferedWriter out = new BufferedWriter(new FileWriter(generationTimeFilename));
/* 272 */     out.write(String.format("%s=%s\n", new Object[] { "genTime", String.valueOf(genTime) }));
/* 273 */     out.write(String.format("%s=%s\n", new Object[] { "prepTime", String.valueOf(preparationTime) }));
/* 274 */     out.close();
/*     */   }
/*     */ 
/*     */   
/*     */   public void start(String toolName, int timeBudget, String benchmark, int runNumber) {
/* 279 */     this.toolName = toolName;
/* 280 */     this.benchmark = benchmark;
/* 281 */     this.timeBudget = timeBudget;
/* 282 */     this.runNumber = runNumber;
/*     */   }
/*     */ 
/*     */   
/*     */   public void finish() {
/* 287 */     this.transcript.println(" \n \n \n");
/* 288 */     this.transcript.flush();
/*     */   }
/*     */ }


/* Location:              /Users/gambi/killmatrix/repo/sbstcontest/benchmarktool/1.0.0/benchmarktool-1.0.0.jar!/sbst/benchmark/TranscriptWriter.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */