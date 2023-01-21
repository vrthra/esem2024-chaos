/*    */ package sbst.benchmark.pitest;
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
/*    */ 
/*    */ 
/*    */ public class TestInfo
/*    */ {
/*    */   public String testClass;
/*    */   public String testMethod;
/*    */   
/*    */   public TestInfo(String tClass, String tMethod) {
/* 22 */     this.testClass = tClass;
/* 23 */     this.testMethod = tMethod;
/*    */   }
/*    */   
/*    */   public String getTestClass() {
/* 27 */     return this.testClass;
/*    */   }
/*    */   
/*    */   public String getTestMethod() {
/* 31 */     return this.testMethod;
/*    */   }
/*    */ 
/*    */   
/*    */   public String toString() {
/* 36 */     return "(" + this.testClass + "," + this.testMethod + ")";
/*    */   }
/*    */ 
/*    */   
/*    */   public boolean equals(Object o) {
/* 41 */     if (o == null) {
/* 42 */       return false;
/*    */     }
/* 44 */     if (o instanceof TestInfo) {
/* 45 */       TestInfo other = (TestInfo)o;
/* 46 */       if (this.testClass.equals(other.testClass) && this.testMethod.equals(other.testMethod)) {
/* 47 */         return true;
/*    */       }
/* 49 */       return false;
/*    */     } 
/* 51 */     return false;
/*    */   }
/*    */ 
/*    */   
/*    */   public int hashCode() {
/* 56 */     int result = 17;
/* 57 */     result = 31 * result + this.testClass.hashCode();
/* 58 */     result = 31 * result + this.testMethod.hashCode();
/* 59 */     return result;
/*    */   }
/*    */ }


/* Location:              /Users/gambi/killmatrix/repo/sbstcontest/benchmarktool/1.0.0/benchmarktool-1.0.0.jar!/sbst/benchmark/pitest/TestInfo.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */