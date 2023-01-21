/*     */ package sbst.benchmark.pitest;
/*     */ 
/*     */ import java.io.File;
/*     */ import java.io.IOException;
/*     */ import java.util.ArrayList;
/*     */ import java.util.List;
/*     */ import java.util.Scanner;
/*     */ import java.util.Set;
/*     */ import org.apache.commons.exec.CommandLine;
/*     */ import org.apache.commons.exec.DefaultExecutor;
/*     */ import org.apache.commons.exec.ExecuteStreamHandler;
/*     */ import org.apache.commons.exec.PumpStreamHandler;
/*     */ import org.pitest.mutationtest.engine.MutationIdentifier;
/*     */ import sbst.benchmark.Main;
/*     */ import sbst.benchmark.junit.StoppingJUnitCore;
/*     */ 
/*     */ 
/*     */ 
/*     */ public class RemoteTestExec4MutationTask
/*     */   extends TestExec4MutationTask
/*     */ {
/*     */   public RemoteTestExec4MutationTask(String cp, List<String> pTestClasses, Set<TestInfo> pFlakyTests, MutationIdentifier id) {
/*  23 */     super(cp, pTestClasses, pFlakyTests, id);
/*     */   }
/*     */ 
/*     */ 
/*     */   
/*     */   public MutationResults call() throws ClassNotFoundException, IOException {
/*  29 */     Main.debug("Evaluating mutant (using remote test executor) " + this.results.getMutation_id().hashCode() + " using tests: " + this.testClasses);
/*     */ 
/*     */ 
/*     */ 
/*     */     
/*  34 */     File tempResultFile = File.createTempFile("sbst-", ".pitest");
/*  35 */     tempResultFile.deleteOnExit();
/*     */     
/*  37 */     List<String> actualTestClasses = new ArrayList<>();
/*  38 */     for (String test : this.testClasses) {
/*  39 */       if (test.contains("_scaffolding")) {
/*  40 */         Main.debug("Skipped scaffolding test " + test);
/*     */         
/*     */         continue;
/*     */       } 
/*     */       
/*  45 */       if (test.startsWith("testcases.")) {
/*  46 */         test = test.replaceFirst("testcases.", "");
/*     */       }
/*  48 */       actualTestClasses.add(test);
/*     */     } 
/*     */     
/*  51 */     String cmdLine = String.format("%s -cp %s %s --output-to %s --tests %s --flaky-tests %s", new Object[] { Main.JAVA, this.cp + File.pathSeparatorChar + 
/*  52 */           System.getProperty("java.class.path"), StoppingJUnitCore.class
/*  53 */           .getName(), tempResultFile.getAbsolutePath(), actualTestClasses
/*  54 */           .toString().replaceAll(",", " ").replace("[", "").replace("]", ""), this.flakyTests
/*  55 */           .toString().replaceAll(",", " ").replace("[", "").replace("]", "") });
/*     */ 
/*     */     
/*  58 */     CommandLine cl = CommandLine.parse(cmdLine);
/*     */     
/*  60 */     Main.debug("\n===\njava command line:\n" + cmdLine + "\n");
/*     */     
/*  62 */     PumpStreamHandler psh = new PumpStreamHandler(Main.debugStr);
/*  63 */     DefaultExecutor exec = new DefaultExecutor();
/*     */     
/*  65 */     psh.start();
/*  66 */     exec.setStreamHandler((ExecuteStreamHandler)psh);
/*     */     
/*  68 */     int exitValud = exec.execute(cl);
/*     */     
/*  70 */     psh.stop();
/*     */     
/*  72 */     if (exitValud == 120) {
/*     */       
/*  74 */       Main.info("ERROR ! Cannot execute remote command !");
/*     */ 
/*     */       
/*  77 */       this.results.setState(MutationResults.State.IGNORED);
/*  78 */       return this.results;
/*     */     } 
/*  80 */     Main.debug("Read JUnit result from " + tempResultFile);
/*     */     
/*  82 */     MockResult result = new MockResult();
/*     */     
/*  84 */     try (Scanner sc = new Scanner(tempResultFile)) {
/*     */       
/*  86 */       int totalCount = Integer.parseInt(sc.nextLine());
/*     */       
/*  88 */       if (totalCount == 0) {
/*  89 */         this.results.setState(MutationResults.State.NEVER_RUN);
/*  90 */         Main.debug("RemoteTestExec4MutationTask: No test? Mutant survived " + this.results
/*  91 */             .getMutation_id().hashCode());
/*  92 */         return this.results;
/*     */       } 
/*     */       
/*  95 */       result.setRunCount(totalCount);
/*  96 */       this.results.setState(MutationResults.State.SURVIVED);
/*     */       
/*  98 */       int totalFail = Integer.parseInt(sc.nextLine());
/*     */ 
/*     */       
/* 101 */       String h = null;
/* 102 */       String t = null;
/* 103 */       while (sc.hasNextLine()) {
/* 104 */         String line = sc.nextLine();
/* 105 */         if (line.isEmpty()) {
/*     */           
/* 107 */           if (h == null) {
/* 108 */             Main.debug("Empty line !"); continue;
/*     */           } 
/* 110 */           Main.debug("Register a Fail: " + h);
/* 111 */           result.addFailure(h, t);
/* 112 */           h = null;
/* 113 */           t = null; continue;
/*     */         } 
/* 115 */         if (h == null) {
/* 116 */           h = line.split(":")[0];
/* 117 */           t = line.split(":")[1] + "\n"; continue;
/*     */         } 
/* 119 */         t = t + line + "\n";
/*     */       } 
/*     */       
/* 122 */       this.results.addJUnitResult(result);
/*     */       
/* 124 */       return processTestResults(result);
/*     */     } 
/*     */   }
/*     */ }


/* Location:              /Users/gambi/killmatrix/repo/sbstcontest/benchmarktool/1.0.0/benchmarktool-1.0.0.jar!/sbst/benchmark/pitest/RemoteTestExec4MutationTask.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */