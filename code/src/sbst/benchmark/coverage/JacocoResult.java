/*     */ package sbst.benchmark.coverage;
/*     */ 
/*     */ import java.util.HashSet;
/*     */ import java.util.Set;
/*     */ import org.jacoco.core.analysis.IClassCoverage;
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
/*     */ public class JacocoResult
/*     */ {
/*     */   private int linesTotal;
/*     */   private int linesCovered;
/*     */   private int branchesTotal;
/*     */   private int branchesCovered;
/*     */   private int instructionsTotal;
/*     */   private int instructionsCovered;
/*     */   private int methodsTotal;
/*     */   private int methodsCovered;
/*     */   private int complexityTotal;
/*     */   private int complexityCovered;
/*     */   private Set<Integer> coveredLines;
/*     */   private Set<Integer> uncoveredLines;
/*     */   
/*     */   public JacocoResult(IClassCoverage cc) {
/*  44 */     this.linesTotal = cc.getLineCounter().getTotalCount();
/*  45 */     this.linesCovered = cc.getLineCounter().getCoveredCount();
/*  46 */     this.branchesTotal = cc.getBranchCounter().getTotalCount();
/*  47 */     this.branchesCovered = cc.getBranchCounter().getCoveredCount();
/*  48 */     this.instructionsTotal = cc.getInstructionCounter().getTotalCount();
/*  49 */     this.instructionsCovered = cc.getInstructionCounter().getCoveredCount();
/*  50 */     this.methodsTotal = cc.getMethodCounter().getTotalCount();
/*  51 */     this.methodsCovered = cc.getMethodCounter().getCoveredCount();
/*  52 */     this.complexityTotal = cc.getComplexityCounter().getTotalCount();
/*  53 */     this.complexityCovered = cc.getComplexityCounter().getCoveredCount();
/*     */ 
/*     */     
/*  56 */     this.coveredLines = new HashSet<>();
/*  57 */     this.uncoveredLines = new HashSet<>();
/*  58 */     for (int line = cc.getFirstLine(); line <= cc.getLastLine(); line++) {
/*  59 */       if (cc.getLine(line).getStatus() == 2 || cc.getLine(line).getStatus() == 3) {
/*  60 */         this.coveredLines.add(Integer.valueOf(line));
/*  61 */       } else if (cc.getLine(line).getStatus() == 1) {
/*  62 */         this.uncoveredLines.add(Integer.valueOf(line));
/*     */       } 
/*     */     } 
/*     */   }
/*     */   public int getLinesTotal() {
/*  67 */     return this.linesTotal;
/*     */   }
/*     */   
/*     */   public int getLinesCovered() {
/*  71 */     return this.linesCovered;
/*     */   }
/*     */   
/*     */   public int getBranchesTotal() {
/*  75 */     return this.branchesTotal;
/*     */   }
/*     */   
/*     */   public int getBranchesCovered() {
/*  79 */     return this.branchesCovered;
/*     */   }
/*     */   
/*     */   public int getInstructionsTotal() {
/*  83 */     return this.instructionsTotal;
/*     */   }
/*     */   
/*     */   public int getInstructionsCovered() {
/*  87 */     return this.instructionsCovered;
/*     */   }
/*     */   
/*     */   public int getMethodsTotal() {
/*  91 */     return this.methodsTotal;
/*     */   }
/*     */   
/*     */   public int getMethodsCovered() {
/*  95 */     return this.methodsCovered;
/*     */   }
/*     */   
/*     */   public int getComplexityTotal() {
/*  99 */     return this.complexityTotal;
/*     */   }
/*     */   
/*     */   public int getComplexityCovered() {
/* 103 */     return this.complexityCovered;
/*     */   }
/*     */   
/*     */   public Set<Integer> getUncoveredLines() {
/* 107 */     return this.uncoveredLines;
/*     */   }
/*     */   
/*     */   public Set<Integer> getCoveredLines() {
/* 111 */     return this.coveredLines;
/*     */   }
/*     */   
/*     */   public void printResults() {
/* 115 */     System.out.println("Method coverage = " + (this.methodsCovered / this.methodsTotal));
/* 116 */     System.out.println("Line coverage = " + (this.linesCovered / this.linesTotal));
/* 117 */     System.out.println("Branch coverage = " + (this.branchesCovered / this.branchesTotal));
/* 118 */     System.out.println("Complexity coverage = " + (this.complexityCovered / this.complexityTotal));
/*     */   }
/*     */ }


/* Location:              /Users/gambi/killmatrix/repo/sbstcontest/benchmarktool/1.0.0/benchmarktool-1.0.0.jar!/sbst/benchmark/coverage/JacocoResult.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */