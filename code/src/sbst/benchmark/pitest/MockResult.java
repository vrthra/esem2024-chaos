/*    */ package sbst.benchmark.pitest;
/*    */ 
/*    */ import java.util.ArrayList;
/*    */ import java.util.List;
/*    */ import org.junit.runner.Result;
/*    */ import org.junit.runner.notification.Failure;
/*    */ 
/*    */ public class MockResult
/*    */   extends Result {
/*    */   private static final long serialVersionUID = 1L;
/* 11 */   List<Failure> failures = new ArrayList<>();
/* 12 */   private int runCount = 0;
/*    */   
/*    */   public void addFailure(String header, String trace) {
/* 15 */     MockFailure f = new MockFailure(null, null);
/* 16 */     f.setTrace(trace);
/* 17 */     f.setTestHeader(header);
/* 18 */     this.failures.add(f);
/*    */   }
/*    */ 
/*    */   
/*    */   public int getFailureCount() {
/* 23 */     return this.failures.size();
/*    */   }
/*    */   
/*    */   public void setRunCount(int runCount) {
/* 27 */     this.runCount = runCount;
/*    */   }
/*    */ 
/*    */   
/*    */   public int getRunCount() {
/* 32 */     return this.runCount;
/*    */   }
/*    */ 
/*    */   
/*    */   public List<Failure> getFailures() {
/* 37 */     return this.failures;
/*    */   }
/*    */ }


/* Location:              /Users/gambi/killmatrix/repo/sbstcontest/benchmarktool/1.0.0/benchmarktool-1.0.0.jar!/sbst/benchmark/pitest/MockResult.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */