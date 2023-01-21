/*     */ package sbst.benchmark;
/*     */ 
/*     */ import java.io.BufferedReader;
/*     */ import java.io.File;
/*     */ import java.io.FileNotFoundException;
/*     */ import java.io.FileReader;
/*     */ import java.io.IOException;
/*     */ import java.io.PrintStream;
/*     */ import java.util.HashMap;
/*     */ import java.util.Map;
/*     */ import java.util.Vector;
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
/*     */ public class Score
/*     */ {
/*     */   private static final double WEIGHT_LINE_COVERAGE = 1.0D;
/*     */   private static final double WEIGHT_CONDITION_COVERAGE = 2.0D;
/*     */   private static final double WEIGHT_MUTATION_SCORE = 4.0D;
/*     */   private static final double WEIGHT_REAL_FAULT_WAS_FOUND = 4.0D;
/*     */   
/*     */   static class ToolBudgetKey
/*     */   {
/*     */     final String toolName;
/*     */     final int timeBudget;
/*     */     
/*     */     public ToolBudgetKey(String toolName, int timeBudget) {
/*  35 */       this.toolName = toolName;
/*  36 */       this.timeBudget = timeBudget;
/*     */     }
/*     */ 
/*     */     
/*     */     public int hashCode() {
/*  41 */       int prime = 31;
/*  42 */       int result = 1;
/*  43 */       result = 31 * result + this.timeBudget;
/*  44 */       result = 31 * result + ((this.toolName == null) ? 0 : this.toolName.hashCode());
/*  45 */       return result;
/*     */     }
/*     */ 
/*     */     
/*     */     public boolean equals(Object obj) {
/*  50 */       if (this == obj)
/*  51 */         return true; 
/*  52 */       if (obj == null)
/*  53 */         return false; 
/*  54 */       if (getClass() != obj.getClass())
/*  55 */         return false; 
/*  56 */       ToolBudgetKey other = (ToolBudgetKey)obj;
/*  57 */       if (this.timeBudget != other.timeBudget)
/*  58 */         return false; 
/*  59 */       if (this.toolName == null) {
/*  60 */         if (other.toolName != null)
/*  61 */           return false; 
/*  62 */       } else if (!this.toolName.equals(other.toolName)) {
/*  63 */         return false;
/*  64 */       }  return true;
/*     */     }
/*     */ 
/*     */     
/*     */     public String toString() {
/*  69 */       return "[toolName=" + this.toolName + ", timeBudget=" + this.timeBudget + "]";
/*     */     }
/*     */   }
/*     */   
/*     */   static class ToolBudgetBenchmarkKey { final String toolName;
/*     */     
/*     */     public ToolBudgetBenchmarkKey(String toolName, int timeBudget, String benchmarkName) {
/*  76 */       this.toolName = toolName;
/*  77 */       this.timeBudget = timeBudget;
/*  78 */       this.benchmarkName = benchmarkName;
/*     */     }
/*     */ 
/*     */     
/*     */     final int timeBudget;
/*     */     
/*     */     final String benchmarkName;
/*     */     
/*     */     public int hashCode() {
/*  87 */       int prime = 31;
/*  88 */       int result = 1;
/*  89 */       result = 31 * result + ((this.benchmarkName == null) ? 0 : this.benchmarkName.hashCode());
/*  90 */       result = 31 * result + this.timeBudget;
/*  91 */       result = 31 * result + ((this.toolName == null) ? 0 : this.toolName.hashCode());
/*  92 */       return result;
/*     */     }
/*     */ 
/*     */     
/*     */     public boolean equals(Object obj) {
/*  97 */       if (this == obj)
/*  98 */         return true; 
/*  99 */       if (obj == null)
/* 100 */         return false; 
/* 101 */       if (getClass() != obj.getClass())
/* 102 */         return false; 
/* 103 */       ToolBudgetBenchmarkKey other = (ToolBudgetBenchmarkKey)obj;
/* 104 */       if (this.benchmarkName == null) {
/* 105 */         if (other.benchmarkName != null)
/* 106 */           return false; 
/* 107 */       } else if (!this.benchmarkName.equals(other.benchmarkName)) {
/* 108 */         return false;
/* 109 */       }  if (this.timeBudget != other.timeBudget)
/* 110 */         return false; 
/* 111 */       if (this.toolName == null) {
/* 112 */         if (other.toolName != null)
/* 113 */           return false; 
/* 114 */       } else if (!this.toolName.equals(other.toolName)) {
/* 115 */         return false;
/* 116 */       }  return true;
/*     */     }
/*     */ 
/*     */     
/*     */     public String toString() {
/* 121 */       return "[toolName=" + this.toolName + ", timeBudget=" + this.timeBudget + ", benchmarkName=" + this.benchmarkName + "]";
/*     */     } }
/*     */   
/*     */   static class ToolBudgetBenchmarkRunIdKey {
/*     */     private final String toolName;
/*     */     private final int timeBudget;
/*     */     
/*     */     public ToolBudgetBenchmarkRunIdKey(String toolName, int timeBudget, String benchmarkName, int runId) {
/* 129 */       this.toolName = toolName;
/* 130 */       this.timeBudget = timeBudget;
/* 131 */       this.benchmarkName = benchmarkName;
/* 132 */       this.runId = runId;
/*     */     }
/*     */ 
/*     */     
/*     */     private final String benchmarkName;
/*     */     
/*     */     private final int runId;
/*     */ 
/*     */     
/*     */     public int hashCode() {
/* 142 */       int prime = 31;
/* 143 */       int result = 1;
/* 144 */       result = 31 * result + ((this.benchmarkName == null) ? 0 : this.benchmarkName.hashCode());
/* 145 */       result = 31 * result + this.runId;
/* 146 */       result = 31 * result + this.timeBudget;
/* 147 */       result = 31 * result + ((this.toolName == null) ? 0 : this.toolName.hashCode());
/* 148 */       return result;
/*     */     }
/*     */ 
/*     */     
/*     */     public boolean equals(Object obj) {
/* 153 */       if (this == obj)
/* 154 */         return true; 
/* 155 */       if (obj == null)
/* 156 */         return false; 
/* 157 */       if (getClass() != obj.getClass())
/* 158 */         return false; 
/* 159 */       ToolBudgetBenchmarkRunIdKey other = (ToolBudgetBenchmarkRunIdKey)obj;
/* 160 */       if (this.benchmarkName == null) {
/* 161 */         if (other.benchmarkName != null)
/* 162 */           return false; 
/* 163 */       } else if (!this.benchmarkName.equals(other.benchmarkName)) {
/* 164 */         return false;
/* 165 */       }  if (this.runId != other.runId)
/* 166 */         return false; 
/* 167 */       if (this.timeBudget != other.timeBudget)
/* 168 */         return false; 
/* 169 */       if (this.toolName == null) {
/* 170 */         if (other.toolName != null)
/* 171 */           return false; 
/* 172 */       } else if (!this.toolName.equals(other.toolName)) {
/* 173 */         return false;
/* 174 */       }  return true;
/*     */     }
/*     */ 
/*     */     
/*     */     public String toString() {
/* 179 */       return "[toolName=" + this.toolName + ", timeBudget=" + this.timeBudget + ", benchmarkName=" + this.benchmarkName + ", runId=" + this.runId + "]";
/*     */     }
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/* 189 */   private static Map<String, Double> standardDeviations = new HashMap<>();
/*     */   
/*     */   public static void log(String msg) {
/* 192 */     System.out.println(msg);
/*     */   }
/*     */   
/*     */   public static void main(String[] args) throws IOException {
/* 196 */     if (args.length != 2) {
/* 197 */       System.out.println("Usage java " + Score.class.getName() + " <FILENAME>.csv <SCORE.csv>");
/*     */       
/*     */       return;
/*     */     } 
/* 201 */     String csvFileName = args[0];
/*     */     
/* 203 */     File csvFile = new File(csvFileName);
/* 204 */     if (!csvFile.exists()) {
/* 205 */       System.err.println("File " + csvFileName + " was not found");
/*     */       
/*     */       return;
/*     */     } 
/* 209 */     String csvScoreFileName = args[1];
/* 210 */     File csvScoreFile = new File(csvScoreFileName);
/* 211 */     if (csvScoreFile.exists()) {
/* 212 */       log("Deleting existing " + csvScoreFileName);
/* 213 */       csvScoreFile.delete();
/*     */     } 
/* 215 */     Map<ToolBudgetKey, Double> scoreMap = computeScore(csvFile);
/* 216 */     PrintStream writer = new PrintStream(csvScoreFile);
/* 217 */     log("Writing results to new file " + csvScoreFileName);
/* 218 */     printScore(writer, scoreMap);
/*     */   }
/*     */   
/*     */   static void printScore(PrintStream out, Map<ToolBudgetKey, Double> scoreMap) {
/* 222 */     String headline = String.format("%1$10s, %2$10s, %3$7s, %4$13s", new Object[] { "TOOL", "TIMEBUDGET", "SCORE", "STD.DEVIATION" });
/* 223 */     out.println(headline);
/* 224 */     Map<String, Double> toolScore = new HashMap<>();
/*     */     
/* 226 */     Map<String, Double> toolStdDev = new HashMap<>();
/* 227 */     for (Map.Entry<ToolBudgetKey, Double> entry : scoreMap.entrySet()) {
/* 228 */       String toolName = ((ToolBudgetKey)entry.getKey()).toolName;
/* 229 */       int timeBudget = ((ToolBudgetKey)entry.getKey()).timeBudget;
/* 230 */       double score = ((Double)entry.getValue()).doubleValue();
/* 231 */       Double timeScore = toolScore.get(toolName);
/* 232 */       if (timeScore == null) {
/* 233 */         timeScore = new Double(score);
/*     */       } else {
/* 235 */         timeScore = Double.valueOf(timeScore.doubleValue() + score);
/* 236 */       }  toolScore.put(toolName, timeScore);
/* 237 */       String line = String.format("%1$10s, %2$10s, %3$7g, %4$13g", new Object[] { toolName, Integer.valueOf(timeBudget), Double.valueOf(score), standardDeviations.get(toolName + timeBudget) });
/* 238 */       out.println(line);
/* 239 */       Double stdev = toolStdDev.get(toolName);
/* 240 */       if (stdev == null) {
/* 241 */         stdev = new Double(0.0D);
/*     */       } else {
/* 243 */         stdev = new Double(stdev.doubleValue() + ((Double)standardDeviations.get(toolName + timeBudget)).doubleValue());
/* 244 */       }  toolStdDev.put(toolName, stdev);
/*     */     } 
/* 246 */     out.println("--");
/* 247 */     for (String tool : toolScore.keySet())
/* 248 */       out.println(tool + " scores: " + ((Double)toolScore.get(tool)).doubleValue() + " [std.deviation = " + toolStdDev.get(tool) + "]"); 
/*     */   }
/*     */   
/*     */   static Map<ToolBudgetKey, Double> computeScore(File csvFile) throws FileNotFoundException, IOException {
/* 252 */     log("");
/* 253 */     log("Reading .csv execution results");
/* 254 */     log("==============================");
/* 255 */     Map<ToolBudgetBenchmarkRunIdKey, ExecutionResult> m = readResults(csvFile);
/*     */     
/* 257 */     log("");
/* 258 */     log("Computing score per execution result");
/* 259 */     log("====================================");
/* 260 */     Map<ToolBudgetBenchmarkRunIdKey, Double> scorePerRun = computeScorePerRun(m);
/*     */     
/* 262 */     log("");
/* 263 */     log("Computing avergare of execution results");
/* 264 */     log("=======================================");
/* 265 */     Map<ToolBudgetBenchmarkKey, Double> scorePerBenchmark = computeBenchmarkAverage(scorePerRun);
/*     */     
/* 267 */     log("");
/* 268 */     log("Adding all averages per tool");
/* 269 */     log("============================");
/* 270 */     Map<ToolBudgetKey, Double> score = aggregateScorePerTool(scorePerBenchmark);
/* 271 */     return score;
/*     */   }
/*     */   
/*     */   private static class SumCountPair {
/* 275 */     private double sum = 0.0D;
/* 276 */     private int count = 0;
/*     */     private Vector<Double> scores;
/*     */     
/*     */     public SumCountPair(double sum) {
/* 280 */       this.sum = sum;
/* 281 */       this.count = 1;
/* 282 */       this.scores = new Vector<>();
/*     */     }
/*     */     
/*     */     public void add(double new_sum) {
/* 286 */       this.sum += new_sum;
/* 287 */       this.count++;
/* 288 */       this.scores.add(new Double(new_sum));
/*     */     }
/*     */     
/*     */     public double getAverage() {
/* 292 */       return this.sum / this.count;
/*     */     }
/*     */     
/*     */     public double getStdDeviation() {
/* 296 */       double avgScore = getAverage();
/* 297 */       double stdDevScore = 0.0D;
/* 298 */       for (Double score : this.scores)
/* 299 */         stdDevScore += Math.pow(avgScore - score.doubleValue(), 2.0D); 
/* 300 */       return Math.sqrt(stdDevScore / (this.count - 1));
/*     */     }
/*     */   }
/*     */ 
/*     */ 
/*     */   
/*     */   static Map<ToolBudgetBenchmarkKey, Double> computeBenchmarkAverage(Map<ToolBudgetBenchmarkRunIdKey, Double> scorePerRun) {
/* 307 */     Map<ToolBudgetBenchmarkKey, SumCountPair> sumCountPairs = new HashMap<>();
/* 308 */     for (Map.Entry<ToolBudgetBenchmarkRunIdKey, Double> entry : scorePerRun.entrySet()) {
/*     */       
/* 310 */       double score = ((Double)entry.getValue()).doubleValue();
/* 311 */       String toolName = (entry.getKey()).toolName;
/* 312 */       int timeBudget = (entry.getKey()).timeBudget;
/* 313 */       String benchmarkName = (entry.getKey()).benchmarkName;
/* 314 */       ToolBudgetBenchmarkKey toolBenchmarkKey = new ToolBudgetBenchmarkKey(toolName, timeBudget, benchmarkName);
/* 315 */       if (!sumCountPairs.containsKey(toolBenchmarkKey)) {
/* 316 */         sumCountPairs.put(toolBenchmarkKey, new SumCountPair(score)); continue;
/*     */       } 
/* 318 */       SumCountPair sumCountPair = sumCountPairs.get(toolBenchmarkKey);
/* 319 */       sumCountPair.add(score);
/*     */     } 
/*     */ 
/*     */ 
/*     */ 
/*     */     
/* 325 */     Map<ToolBudgetBenchmarkKey, Double> scorePerBenchmark = new HashMap<>();
/* 326 */     for (Map.Entry<ToolBudgetBenchmarkKey, SumCountPair> entry : sumCountPairs.entrySet()) {
/* 327 */       ToolBudgetBenchmarkKey k = entry.getKey();
/* 328 */       SumCountPair v = entry.getValue();
/* 329 */       double avg = v.getAverage();
/* 330 */       log("New Avg Score for " + k + " is " + avg + " [std. deviation = " + v.getStdDeviation() + "]");
/* 331 */       scorePerBenchmark.put(k, Double.valueOf(avg));
/* 332 */       String key = k.toolName + k.timeBudget;
/* 333 */       Double stdev = standardDeviations.get(key);
/* 334 */       if (stdev == null) {
/* 335 */         stdev = new Double(0.0D);
/*     */       } else {
/* 337 */         stdev = new Double(stdev.doubleValue() + v.getStdDeviation());
/* 338 */       }  standardDeviations.put(key, stdev);
/*     */     } 
/* 340 */     return scorePerBenchmark;
/*     */   }
/*     */ 
/*     */   
/*     */   static Map<ToolBudgetBenchmarkRunIdKey, Double> computeScorePerRun(Map<ToolBudgetBenchmarkRunIdKey, ExecutionResult> results) {
/* 345 */     Map<ToolBudgetBenchmarkRunIdKey, Double> scorePerRun = new HashMap<>();
/* 346 */     for (Map.Entry<ToolBudgetBenchmarkRunIdKey, ExecutionResult> entry : results.entrySet()) {
/* 347 */       ToolBudgetBenchmarkRunIdKey k = entry.getKey();
/* 348 */       ExecutionResult v = entry.getValue();
/* 349 */       double score = computeScorePerRun(v);
/* 350 */       log("New Run Score for " + k + " is " + score);
/* 351 */       scorePerRun.put(k, Double.valueOf(score));
/*     */     } 
/* 353 */     return scorePerRun;
/*     */   }
/*     */   
/*     */   private static double computeScorePerRun(ExecutionResult r) {
/* 357 */     double overtime_generation_penalty, uncompilableFlakyPenalty, lineCoverageRatio = r.linesCoverageRatio;
/* 358 */     double conditionCoverageRatio = r.conditionsCoverageRatio;
/* 359 */     double mutationScore = r.mutantsKillRatio;
/* 360 */     int generationTime = r.generationTime;
/* 361 */     int uncompilableTestClasses = r.uncompilableNumber;
/* 362 */     int flakyTests = r.brokenTests;
/* 363 */     int faultRevelingTests = r.failTests;
/* 364 */     int testSuiteSize = r.testcaseNumber;
/* 365 */     int timeBudget = r.timeBudget;
/* 366 */     int totalNumberOfTestClasses = r.totalTestClasses;
/*     */     
/* 368 */     double coverageScore = 0.0D;
/* 369 */     coverageScore += 1.0D * lineCoverageRatio;
/* 370 */     coverageScore += 2.0D * conditionCoverageRatio;
/* 371 */     coverageScore += 4.0D * mutationScore;
/* 372 */     if (faultRevelingTests > 0) {
/* 373 */       coverageScore += 4.0D;
/*     */     }
/*     */     
/* 376 */     if (generationTime == 0) {
/* 377 */       overtime_generation_penalty = 1.0D;
/*     */     } else {
/* 379 */       int timeBudgetMillis = timeBudget * 1000;
/* 380 */       double generationTimeRatio = computeRatio(timeBudgetMillis, generationTime);
/* 381 */       overtime_generation_penalty = Math.min(1.0D, generationTimeRatio);
/*     */     } 
/*     */ 
/*     */     
/* 385 */     if (testSuiteSize == 0)
/*     */     {
/* 387 */       return 0.0D;
/*     */     }
/*     */     
/* 390 */     if (uncompilableTestClasses == totalNumberOfTestClasses) {
/*     */       
/* 392 */       uncompilableFlakyPenalty = 2.0D;
/*     */     } else {
/*     */       
/* 395 */       double flakyTestRatio = computeRatio(flakyTests, testSuiteSize);
/*     */       
/* 397 */       double uncompilableTestClassesRatio = computeRatio(uncompilableTestClasses, totalNumberOfTestClasses);
/*     */       
/* 399 */       uncompilableFlakyPenalty = flakyTestRatio + uncompilableTestClassesRatio;
/*     */     } 
/*     */     
/* 402 */     double score = coverageScore * overtime_generation_penalty - uncompilableFlakyPenalty;
/* 403 */     return score;
/*     */   }
/*     */ 
/*     */   
/*     */   private static double computeRatio(int a, int b) {
/* 408 */     return a / b;
/*     */   }
/*     */ 
/*     */   
/*     */   static Map<ToolBudgetBenchmarkRunIdKey, ExecutionResult> readResults(File csvFile) throws FileNotFoundException, IOException {
/* 413 */     BufferedReader br = new BufferedReader(new FileReader(csvFile));
/*     */     try {
/* 415 */       Map<ToolBudgetBenchmarkRunIdKey, ExecutionResult> results = new HashMap<>();
/*     */       
/* 417 */       br.readLine(); String line;
/* 418 */       while ((line = br.readLine()) != null) {
/*     */         ExecutionResult r;
/* 420 */         String trimmedLine = line.trim();
/* 421 */         if (trimmedLine.equals("")) {
/*     */           continue;
/*     */         }
/*     */         
/* 425 */         String regexp = "[,\\t]+";
/* 426 */         String[] fields = trimmedLine.split("[,\\t]+");
/* 427 */         if (fields.length != 25) {
/* 428 */           throw new IllegalStateException("Incorrect number of fields " + fields.length + "(expected was " + '\031' + ")");
/*     */         }
/*     */ 
/*     */ 
/*     */         
/*     */         try {
/* 434 */           r = readFields(fields);
/* 435 */         } catch (IllegalArgumentException e) {
/* 436 */           throw new IllegalArgumentException("Offending line is " + trimmedLine, e);
/*     */         } 
/* 438 */         ToolBudgetBenchmarkRunIdKey key = new ToolBudgetBenchmarkRunIdKey(r.tool, r.timeBudget, r.benchmark, r.runId);
/*     */         
/* 440 */         results.put(key, r);
/*     */       } 
/* 442 */       log(results.keySet().size() + " execution results were successfully retrieved.");
/* 443 */       return results;
/*     */     } finally {
/* 445 */       br.close();
/*     */     } 
/*     */   }
/*     */   
/*     */   private static ExecutionResult readFields(String[] fields) throws IllegalArgumentException {
/* 450 */     ExecutionResult r = new ExecutionResult();
/* 451 */     r.tool = fields[0];
/* 452 */     r.timeBudget = Integer.valueOf(fields[23]).intValue();
/* 453 */     r.benchmark = fields[1];
/* 454 */     r.className = fields[2];
/* 455 */     r.runId = Integer.valueOf(fields[3]).intValue();
/* 456 */     if (!fields[4].trim().equals("?")) {
/* 457 */       r.preparationTime = Integer.valueOf(fields[4]).intValue();
/* 458 */       r.generationTime = Integer.valueOf(fields[5]).intValue();
/* 459 */       r.executionTime = Integer.valueOf(fields[6]).intValue();
/* 460 */       r.testcaseNumber = Integer.valueOf(fields[7]).intValue();
/* 461 */       r.uncompilableNumber = Integer.valueOf(fields[8]).intValue();
/* 462 */       r.brokenTests = Integer.valueOf(fields[9]).intValue();
/* 463 */       r.failTests = Integer.valueOf(fields[10]).intValue();
/* 464 */       r.linesTotal = Integer.valueOf(fields[11]).intValue();
/* 465 */       r.linesCovered = Integer.valueOf(fields[12]).intValue();
/* 466 */       r.linesCoverageRatio = Double.valueOf(fields[13]).doubleValue() / 100.0D;
/* 467 */       r.conditionsTotal = Integer.valueOf(fields[14]).intValue();
/* 468 */       r.conditionsCovered = Integer.valueOf(fields[15]).intValue();
/* 469 */       r.conditionsCoverageRatio = Double.valueOf(fields[16]).doubleValue() / 100.0D;
/* 470 */       r.mutantsTotal = Integer.valueOf(fields[17]).intValue();
/* 471 */       r.mutantsCovered = Integer.valueOf(fields[18]).intValue();
/* 472 */       r.mutantsCoverageRatio = Double.valueOf(fields[19]).doubleValue() / 100.0D;
/* 473 */       r.mutantsKilled = Integer.valueOf(fields[20]).intValue();
/* 474 */       r.mutantsKillRatio = Double.valueOf(fields[21]).doubleValue() / 100.0D;
/* 475 */       r.mutantsAlive = Integer.valueOf(fields[22]).intValue();
/* 476 */       r.totalTestClasses = Integer.valueOf(fields[24]).intValue();
/*     */       
/* 478 */       checkValidRow(r);
/*     */     } 
/* 480 */     return r;
/*     */   }
/*     */   
/*     */   private static void checkValidRow(ExecutionResult r) throws IllegalArgumentException {
/* 484 */     if (r.preparationTime < 0) {
/* 485 */       throw new IllegalArgumentException("preparationTime cannot be negative!");
/*     */     }
/* 487 */     if (r.generationTime < 0) {
/* 488 */       throw new IllegalArgumentException("generationTime cannot be negative!");
/*     */     }
/* 490 */     if (r.executionTime < 0) {
/* 491 */       throw new IllegalArgumentException("executionTime cannot be negative!");
/*     */     }
/* 493 */     if (r.testcaseNumber < 0) {
/* 494 */       throw new IllegalArgumentException("testcaseNumber cannot be negative!");
/*     */     }
/* 496 */     if (r.uncompilableNumber < 0) {
/* 497 */       throw new IllegalArgumentException("uncompilableNumber cannot be negative!");
/*     */     }
/* 499 */     if (r.brokenTests < 0) {
/* 500 */       throw new IllegalArgumentException("brokenTests cannot be negative!");
/*     */     }
/* 502 */     if (r.failTests < 0) {
/* 503 */       throw new IllegalArgumentException("failTests cannot be negative!");
/*     */     }
/* 505 */     if (r.linesTotal < 0) {
/* 506 */       throw new IllegalArgumentException("linesTotal cannot be negative!");
/*     */     }
/* 508 */     if (r.linesCovered < 0) {
/* 509 */       throw new IllegalArgumentException("linesCovered cannot be negative!");
/*     */     }
/* 511 */     if (r.linesCoverageRatio < 0.0D || r.linesCoverageRatio > 1.0D) {
/* 512 */       throw new IllegalArgumentException("linesCoverageRatio is not a ratio!");
/*     */     }
/* 514 */     if (r.conditionsTotal < 0) {
/* 515 */       throw new IllegalArgumentException("conditionsTotal cannot be negative!");
/*     */     }
/* 517 */     if (r.conditionsCovered < 0) {
/* 518 */       throw new IllegalArgumentException("conditionsCovered cannot be negative!");
/*     */     }
/* 520 */     if (r.conditionsCoverageRatio < 0.0D || r.conditionsCoverageRatio > 1.0D) {
/* 521 */       throw new IllegalArgumentException("conditionsCoverageRatio is not a ratio!");
/*     */     }
/* 523 */     if (r.mutantsTotal < 0) {
/* 524 */       throw new IllegalArgumentException("mutantsTotal cannot be negative!");
/*     */     }
/* 526 */     if (r.mutantsCovered < 0) {
/* 527 */       throw new IllegalArgumentException("mutantsCovered cannot be negative!");
/*     */     }
/* 529 */     if (r.mutantsCoverageRatio < 0.0D || r.mutantsCoverageRatio > 1.0D) {
/* 530 */       throw new IllegalArgumentException("mutantsCoverageRatio is not a ratio!");
/*     */     }
/* 532 */     if (r.mutantsKilled < 0) {
/* 533 */       throw new IllegalArgumentException("mutantsKilled cannot be negative!");
/*     */     }
/* 535 */     if (r.mutantsKillRatio < 0.0D || r.mutantsKillRatio > 1.0D) {
/* 536 */       throw new IllegalArgumentException("mutantsKillRatio is not a ratio!");
/*     */     }
/* 538 */     if (r.mutantsAlive < 0) {
/* 539 */       throw new IllegalArgumentException("mutantsAlive cannot be negative!");
/*     */     }
/* 541 */     if (r.totalTestClasses < 0) {
/* 542 */       throw new IllegalArgumentException("totalTestClasses cannot be negative!");
/*     */     }
/* 544 */     if (r.brokenTests + r.failTests > r.testcaseNumber)
/*     */     {
/* 546 */       if (r.testcaseNumber == 0 && r.brokenTests == 1 && r.failTests == 1 && r.totalTestClasses == 1) {
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */         
/* 552 */         r.brokenTests = 0;
/* 553 */         r.failTests = 0;
/* 554 */         log("Warning, assuming no test cases in single test class for " + r.tool + "," + r.timeBudget + "," + r.benchmark + "," + r.runId);
/*     */       }
/*     */       else {
/*     */         
/* 558 */         throw new IllegalArgumentException("number of broken tests and failed tests cannot be greater than total number of test cases");
/*     */       } 
/*     */     }
/*     */ 
/*     */     
/* 563 */     if (r.uncompilableNumber > r.totalTestClasses) {
/* 564 */       throw new IllegalArgumentException("number of uncompilable test classes cannot be greater than total number of test classes!");
/*     */     }
/*     */ 
/*     */     
/* 568 */     if (r.testcaseNumber <= 0 || r.uncompilableNumber == r.totalTestClasses);
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
/*     */   public static Map<ToolBudgetKey, Double> aggregateScorePerTool(Map<ToolBudgetBenchmarkKey, Double> scorePerBenchmark) {
/* 580 */     Map<ToolBudgetKey, Double> scorePerTool = new HashMap<>();
/* 581 */     for (Map.Entry<ToolBudgetBenchmarkKey, Double> entry : scorePerBenchmark.entrySet()) {
/* 582 */       double new_score; String toolName = ((ToolBudgetBenchmarkKey)entry.getKey()).toolName;
/* 583 */       int timeBudget = ((ToolBudgetBenchmarkKey)entry.getKey()).timeBudget;
/* 584 */       ToolBudgetKey key = new ToolBudgetKey(toolName, timeBudget);
/*     */       
/* 586 */       if (!scorePerTool.containsKey(key)) {
/* 587 */         new_score = ((Double)entry.getValue()).doubleValue();
/*     */       } else {
/* 589 */         double old_score = ((Double)scorePerTool.get(key)).doubleValue();
/* 590 */         new_score = old_score + ((Double)entry.getValue()).doubleValue();
/*     */       } 
/* 592 */       scorePerTool.put(key, Double.valueOf(new_score));
/*     */     } 
/* 594 */     for (Map.Entry<ToolBudgetKey, Double> entry : scorePerTool.entrySet()) {
/* 595 */       log("Final score for " + entry.getKey() + " is " + entry.getValue() + " [std. deviation = " + ((Double)standardDeviations.get(((ToolBudgetKey)entry.getKey()).toolName + ((ToolBudgetKey)entry.getKey()).timeBudget)).doubleValue() + "]");
/*     */     }
/*     */     
/* 598 */     return scorePerTool;
/*     */   }
/*     */ }


/* Location:              /Users/gambi/killmatrix/repo/sbstcontest/benchmarktool/1.0.0/benchmarktool-1.0.0.jar!/sbst/benchmark/Score.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */