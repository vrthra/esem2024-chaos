/*    */ package sbst.benchmark;
/*    */ 
/*    */ import java.io.File;
/*    */ import java.util.ArrayList;
/*    */ import java.util.Arrays;
/*    */ import java.util.Collections;
/*    */ import java.util.HashMap;
/*    */ import java.util.List;
/*    */ import java.util.Map;
/*    */ import java.util.Random;
/*    */ import java.util.Set;
/*    */ import org.apache.commons.configuration.ConfigurationException;
/*    */ import org.apache.commons.configuration.SubnodeConfiguration;
/*    */ import org.apache.commons.configuration.plist.PropertyListConfiguration;
/*    */ import org.apache.commons.configuration.tree.ConfigurationNode;
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
/*    */ public class BenchmarkCollection
/*    */ {
/*    */   Map<String, IBenchmarkTask> benchmarks;
/*    */   
/*    */   public BenchmarkCollection(File file) throws ConfigurationException {
/* 37 */     PropertyListConfiguration benchmarkList = new PropertyListConfiguration();
/* 38 */     benchmarkList.load(file);
/* 39 */     this.benchmarks = new HashMap<>();
/* 40 */     for (ConfigurationNode child : benchmarkList.getRoot().getChildren()) {
/*    */       
/* 42 */       String key = child.getName();
/* 43 */       SubnodeConfiguration conf = benchmarkList.configurationAt(key);
/*    */       
/* 45 */       String src = conf.getString("src");
/* 46 */       if (src == null || src.isEmpty())
/* 47 */         throw new ConfigurationException("Missing field: src"); 
/* 48 */       String bin = conf.getString("bin");
/* 49 */       if (bin == null || bin.isEmpty())
/* 50 */         throw new ConfigurationException("Missing field: bin"); 
/* 51 */       String[] classpath = conf.getStringArray("classpath");
/* 52 */       if (classpath == null)
/* 53 */         classpath = new String[0]; 
/* 54 */       String[] classes = conf.getStringArray("classes");
/* 55 */       if (classes == null || classes.length == 0)
/* 56 */         throw new ConfigurationException("Missing or empty field: classes"); 
/* 57 */       List<File> cp = new ArrayList<>(classpath.length);
/* 58 */       for (String path : classpath)
/*    */       {
/* 60 */         cp.add(new File(path));
/*    */       }
/* 62 */       this.benchmarks.put(key, new Benchmark(new File(src), new File(bin), cp, Arrays.asList(classes)));
/*    */     } 
/*    */   }
/*    */ 
/*    */   
/*    */   public IBenchmarkTask forName(String benchmark) {
/* 68 */     return this.benchmarks.get(benchmark);
/*    */   }
/*    */   
/*    */   public IBenchmarkTask forName(String benchmark, int size, long seed) throws Exception {
/* 72 */     Random rand = new Random(seed);
/* 73 */     IBenchmarkTask result = forName(benchmark);
/* 74 */     if (result != null) {
/*    */       
/* 76 */       List<String> names = result.getClassNames();
/* 77 */       if (size > 0 && size < names.size()) {
/*    */         
/* 79 */         List<String> newnames = new ArrayList<>(names);
/* 80 */         Collections.shuffle(newnames, rand);
/* 81 */         newnames = new ArrayList<>(newnames.subList(0, size));
/*    */         
/* 83 */         result = new Benchmark(result.getSourceDirectory(), result.getBinDirectory(), result.getClassPath(), names);
/*    */       } 
/*    */     } 
/* 86 */     return result;
/*    */   }
/*    */ 
/*    */   
/*    */   public Set<String> getBenchmarks() {
/* 91 */     return this.benchmarks.keySet();
/*    */   }
/*    */ }


/* Location:              /Users/gambi/killmatrix/repo/sbstcontest/benchmarktool/1.0.0/benchmarktool-1.0.0.jar!/sbst/benchmark/BenchmarkCollection.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */