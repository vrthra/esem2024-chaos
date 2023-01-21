/*    */ package sbst.benchmark.coverage;
/*    */ 
/*    */ import java.io.File;
/*    */ import java.net.MalformedURLException;
/*    */ import java.net.URL;
/*    */ import java.util.ArrayList;
/*    */ import java.util.LinkedList;
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
/*    */ 
/*    */ 
/*    */ 
/*    */ public class TestUtil
/*    */ {
/*    */   public static URL[] createURLs(String cp) throws MalformedURLException {
/* 27 */     LinkedList<String> required_libraries = new LinkedList<>();
/*    */     
/* 29 */     String[] libraries = cp.split(":");
/* 30 */     for (String s : libraries) {
/* 31 */       s = s.replace(":", "");
/* 32 */       if (s.length() > 0) {
/* 33 */         required_libraries.addLast(s);
/*    */       }
/*    */     } 
/* 36 */     URL[] url = new URL[required_libraries.size()];
/*    */     
/* 38 */     for (int index = 0; index < required_libraries.size(); index++) {
/* 39 */       if (((String)required_libraries.get(index)).endsWith(".jar")) {
/* 40 */         url[index] = new URL("jar:file:" + (String)required_libraries.get(index) + "!/");
/*    */       } else {
/* 42 */         url[index] = (new File(required_libraries.get(index))).toURI().toURL();
/*    */       } 
/*    */     } 
/*    */ 
/*    */ 
/*    */ 
/*    */     
/* 49 */     return url;
/*    */   }
/*    */ 
/*    */   
/*    */   public static List<File> getCompiledFileList(File directory) {
/* 54 */     List<File> list = new ArrayList<>();
/* 55 */     if (directory.isDirectory()) {
/* 56 */       for (File f : directory.listFiles()) {
/* 57 */         list.addAll(getCompiledFileList(f));
/*    */       }
/*    */     }
/* 60 */     else if (directory.getName().endsWith(".class")) {
/* 61 */       list.add(directory);
/*    */     } 
/*    */     
/* 64 */     return list;
/*    */   }
/*    */ }


/* Location:              /Users/gambi/killmatrix/repo/sbstcontest/benchmarktool/1.0.0/benchmarktool-1.0.0.jar!/sbst/benchmark/coverage/TestUtil.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */