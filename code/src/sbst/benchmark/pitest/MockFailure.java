/*    */ package sbst.benchmark.pitest;
/*    */ 
/*    */ import org.junit.runner.Description;
/*    */ import org.junit.runner.notification.Failure;
/*    */ 
/*    */ public class MockFailure
/*    */   extends Failure {
/*  8 */   private String trace = null;
/*  9 */   private String header = null;
/*    */   
/*    */   MockFailure(Description description, Throwable thrownException) {
/* 12 */     super(description, thrownException);
/*    */   }
/*    */   
/*    */   private static final long serialVersionUID = 1L;
/*    */   
/*    */   public void setTrace(String trace) {
/* 18 */     this.trace = trace;
/*    */   }
/*    */ 
/*    */   
/*    */   public String getTrace() {
/* 23 */     return this.trace;
/*    */   }
/*    */   
/*    */   public void setTestHeader(String header) {
/* 27 */     this.header = header;
/*    */   }
/*    */ 
/*    */   
/*    */   public String getTestHeader() {
/* 32 */     return this.header;
/*    */   }
/*    */ }


/* Location:              /Users/gambi/killmatrix/repo/sbstcontest/benchmarktool/1.0.0/benchmarktool-1.0.0.jar!/sbst/benchmark/pitest/MockFailure.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */