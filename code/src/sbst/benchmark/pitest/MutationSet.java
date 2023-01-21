/*    */ package sbst.benchmark.pitest;
/*    */ 
/*    */ import java.util.HashMap;
/*    */ import java.util.Map;
/*    */ import java.util.Set;
/*    */ import org.pitest.mutationtest.engine.Mutant;
/*    */ import org.pitest.mutationtest.engine.MutationDetails;
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
/*    */ public class MutationSet
/*    */ {
/* 36 */   private Map<MutationIdentifier, MutationDetails> generatedMutantDetails = new HashMap<>();
/* 37 */   private Map<MutationIdentifier, Mutant> generatedMutant = new HashMap<>();
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */   
/*    */   public void addMutant(MutationIdentifier id, Mutant mu, MutationDetails detail) {
/* 47 */     this.generatedMutant.put(id, mu);
/* 48 */     this.generatedMutantDetails.put(id, detail);
/*    */   }
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */   
/*    */   public Set<MutationIdentifier> getMutationIDs() {
/* 56 */     return this.generatedMutant.keySet();
/*    */   }
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */   
/*    */   public Mutant getMutantion(MutationIdentifier id) {
/* 65 */     return this.generatedMutant.get(id);
/*    */   }
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */   
/*    */   public MutationDetails getMutantionDetails(MutationIdentifier id) {
/* 74 */     return this.generatedMutantDetails.get(id);
/*    */   }
/*    */   
/*    */   public int getNumberOfMutations() {
/* 78 */     return this.generatedMutant.keySet().size();
/*    */   }
/*    */   
/*    */   public void remove(MutationIdentifier id) {
/* 82 */     this.generatedMutant.remove(id);
/* 83 */     this.generatedMutantDetails.remove(id);
/*    */   }
/*    */ }


/* Location:              /Users/gambi/killmatrix/repo/sbstcontest/benchmarktool/1.0.0/benchmarktool-1.0.0.jar!/sbst/benchmark/pitest/MutationSet.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */