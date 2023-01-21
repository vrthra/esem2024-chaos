/*    */ package sbst.benchmark.pitest;
/*    */ 
/*    */ import java.util.ArrayList;
/*    */ import java.util.List;
/*    */ import org.junit.runner.Result;
/*    */ import org.pitest.mutationtest.engine.MutationIdentifier;
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
/*    */ public class MutationResults
/*    */ {
/*    */   public enum State
/*    */   {
/* 24 */     SURVIVED, KILLED, IGNORED, NEVER_RUN;
/*    */   }
/*    */   
/* 27 */   private List<Result> results = new ArrayList<>();
/*    */   
/*    */   private MutationIdentifier mutation_id;
/*    */   
/*    */   private State state;
/*    */ 
/*    */   
/*    */   public MutationResults(List<Result> pResults, MutationIdentifier id) {
/* 35 */     this.mutation_id = id;
/* 36 */     this.results = pResults;
/* 37 */     this.state = State.NEVER_RUN;
/*    */   }
/*    */   
/*    */   public State getState() {
/* 41 */     return this.state;
/*    */   }
/*    */   
/*    */   public void setState(State state) {
/* 45 */     this.state = state;
/*    */   }
/*    */   
/*    */   public List<Result> getJUnitResults() {
/* 49 */     return this.results;
/*    */   }
/*    */   
/*    */   public MutationIdentifier getMutation_id() {
/* 53 */     return this.mutation_id;
/*    */   }
/*    */   
/*    */   public void addJUnitResult(Result r) {
/* 57 */     this.results.add(r);
/*    */   }
/*    */ }


/* Location:              /Users/gambi/killmatrix/repo/sbstcontest/benchmarktool/1.0.0/benchmarktool-1.0.0.jar!/sbst/benchmark/pitest/MutationResults.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */