/*    */ package sbst.benchmark;
/*    */ 
/*    */ import java.util.ArrayList;
/*    */ import java.util.List;
/*    */ import org.apache.commons.exec.LogOutputStream;
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
/*    */ public class ProcessOutput
/*    */   extends LogOutputStream
/*    */ {
/* 21 */   private final List<String> lines = new ArrayList<>();
/*    */   boolean reroute;
/*    */   
/*    */   public ProcessOutput(boolean rerouteToStdOut) {
/* 25 */     this.reroute = rerouteToStdOut;
/*    */   }
/*    */   
/*    */   protected void processLine(String line, int level) {
/* 29 */     if (line != null) {
/* 30 */       this.lines.add(line);
/* 31 */       if (this.reroute) System.out.println(line); 
/*    */     } 
/*    */   }
/*    */   public List<String> getLines() {
/* 35 */     return this.lines;
/*    */   }
/*    */   public String toString() {
/* 38 */     StringBuilder sb = new StringBuilder();
/* 39 */     for (String line : getLines())
/* 40 */       sb.append(line).append("\n"); 
/* 41 */     return sb.toString();
/*    */   }
/*    */ }


/* Location:              /Users/gambi/killmatrix/repo/sbstcontest/benchmarktool/1.0.0/benchmarktool-1.0.0.jar!/sbst/benchmark/ProcessOutput.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */