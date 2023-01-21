/*    */ package sbst.benchmark;
/*    */ 
/*    */ import java.io.File;
/*    */ import java.util.ArrayList;
/*    */ import java.util.Collections;
/*    */ import java.util.List;
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ public class Benchmark
/*    */   implements IBenchmarkTask
/*    */ {
/*    */   private final File sourceDirectory;
/*    */   private final File binDirectory;
/*    */   private final List<File> classPath;
/*    */   private final List<String> classNames;
/*    */   
/*    */   public Benchmark(File source, File bin, List<File> cp, List<String> classes) {
/* 28 */     this.sourceDirectory = source;
/* 29 */     this.binDirectory = bin;
/* 30 */     this.classPath = new ArrayList<>(cp);
/* 31 */     this.classNames = new ArrayList<>(classes);
/* 32 */     Collections.sort(this.classNames);
/*    */   }
/*    */   
/*    */   public File getSourceDirectory() {
/* 36 */     return this.sourceDirectory;
/*    */   }
/*    */   public File getBinDirectory() {
/* 39 */     return this.binDirectory;
/*    */   }
/*    */   public List<File> getClassPath() {
/* 42 */     return this.classPath;
/*    */   }
/*    */   public List<String> getClassNames() {
/* 45 */     return this.classNames;
/*    */   }
/*    */ }


/* Location:              /Users/gambi/killmatrix/repo/sbstcontest/benchmarktool/1.0.0/benchmarktool-1.0.0.jar!/sbst/benchmark/Benchmark.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */