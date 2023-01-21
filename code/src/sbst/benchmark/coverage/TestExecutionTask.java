/*     */ package sbst.benchmark.coverage;
/*     */ 
/*     */ import java.io.IOException;
/*     */ import java.net.MalformedURLException;
/*     */ import java.net.URL;
/*     */ import java.net.URLClassLoader;
/*     */ import java.util.ArrayList;
/*     */ import java.util.List;
/*     */ import java.util.concurrent.Callable;
/*     */ import org.junit.runner.JUnitCore;
/*     */ import org.junit.runner.Result;
/*     */ import org.junit.runner.notification.RunListener;
/*     */ import sbst.benchmark.Main;
/*     */ import sbst.benchmark.junit.SuppressingOutputTextListener;
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
/*     */ public class TestExecutionTask
/*     */   implements Callable<List<Result>>
/*     */ {
/*     */   private URL[] urls;
/*     */   private List<String> testClasses;
/*  34 */   private List<Result> results = new ArrayList<>();
/*     */ 
/*     */   
/*     */   public TestExecutionTask(String cp, List<String> pTestClasses) {
/*     */     try {
/*  39 */       this.urls = TestUtil.createURLs(cp);
/*  40 */       this.testClasses = pTestClasses;
/*  41 */     } catch (MalformedURLException e) {
/*     */       
/*  43 */       e.printStackTrace();
/*     */     } 
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public List<Result> call() throws ClassNotFoundException, IOException {
/*  51 */     URLClassLoader cl = URLClassLoader.newInstance(this.urls, getClass().getClassLoader());
/*     */     
/*  53 */     Main.info("Running the tests: " + this.testClasses);
/*  54 */     for (String test : this.testClasses) {
/*     */       
/*  56 */       if (test.contains("_scaffolding")) {
/*  57 */         Main.debug("Skipped scaffolding test " + test);
/*     */         
/*     */         continue;
/*     */       } 
/*     */       
/*  62 */       if (test.startsWith("testcases.")) {
/*  63 */         test = test.replaceFirst("testcases.", "");
/*     */       }
/*     */       
/*  66 */       Class<?> testClass = cl.loadClass(test);
/*     */ 
/*     */       
/*  69 */       JUnitCore junit = new JUnitCore();
/*     */       
/*  71 */       junit.addListener((RunListener)new SuppressingOutputTextListener(Main.infoStr));
/*     */       
/*  73 */       Result result = junit.run(new Class[] { testClass });
/*     */       
/*  75 */       this.results.add(result);
/*     */     } 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */     
/*  83 */     Main.debug("Executions terminated");
/*     */     try {
/*  85 */       cl.close();
/*  86 */     } catch (SecurityException ex) {
/*     */ 
/*     */       
/*  89 */       Main.debug("Was unable to close the URLClassLoader after execution of the tests due to security policy!");
/*  90 */       Main.debug("Will continue execution");
/*  91 */     } catch (IOException ex) {
/*     */ 
/*     */       
/*  94 */       Main.debug("An exception occurred during closing of the URLClassLoader!");
/*  95 */       Main.debug("Will continue execution");
/*     */     } 
/*  97 */     return this.results;
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public int countFailingTests() {
/* 106 */     int count = 0;
/*     */     
/* 108 */     for (Result result : this.results) {
/* 109 */       count += result.getFailureCount();
/*     */     }
/* 111 */     return count;
/*     */   }
/*     */   
/*     */   public List<Result> getExecutionResults() {
/* 115 */     return this.results;
/*     */   }
/*     */ }


/* Location:              /Users/gambi/killmatrix/repo/sbstcontest/benchmarktool/1.0.0/benchmarktool-1.0.0.jar!/sbst/benchmark/coverage/TestExecutionTask.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */