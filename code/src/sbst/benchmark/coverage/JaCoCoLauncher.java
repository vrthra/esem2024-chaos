/*     */ package sbst.benchmark.coverage;
/*     */ 
/*     */ import java.io.File;
/*     */ import java.io.FileInputStream;
/*     */ import java.io.FileNotFoundException;
/*     */ import java.io.FileOutputStream;
/*     */ import java.io.InputStream;
/*     */ import java.io.OutputStream;
/*     */ import java.net.MalformedURLException;
/*     */ import java.util.HashMap;
/*     */ import java.util.LinkedList;
/*     */ import java.util.List;
/*     */ import java.util.Map;
/*     */ import java.util.concurrent.ExecutorService;
/*     */ import java.util.concurrent.Executors;
/*     */ import java.util.concurrent.FutureTask;
/*     */ import java.util.concurrent.TimeUnit;
// /*     */ import org.apache.commons.io.FileUtils;
// /*     */ import org.apache.commons.io.filefilter.TrueFileFilter;
/*     */ import org.jacoco.core.analysis.Analyzer;
/*     */ import org.jacoco.core.analysis.CoverageBuilder;
/*     */ import org.jacoco.core.analysis.IClassCoverage;
/*     */ import org.jacoco.core.analysis.ICoverageVisitor;
/*     */ import org.jacoco.core.data.ExecutionDataStore;
/*     */ import org.jacoco.core.data.IExecutionDataVisitor;
/*     */ import org.jacoco.core.data.ISessionInfoVisitor;
/*     */ import org.jacoco.core.data.SessionInfoStore;
/*     */ import org.jacoco.core.instr.Instrumenter;
/*     */ import org.jacoco.core.runtime.IExecutionDataAccessorGenerator;
/*     */ import org.jacoco.core.runtime.LoggerRuntime;
/*     */ import org.jacoco.core.runtime.RuntimeData;
/*     */ import sbst.benchmark.FileUtils;
import sbst.benchmark.Main;
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
/*     */ 
/*     */ 
/*     */ public class JaCoCoLauncher
/*     */ {
/*     */   private String temp_folder;
/*     */   private String targetClass;
/*     */   private List<String> testCases;
/*     */   private String testFolder;
/*     */   private LinkedList<String> required_libraries;
/*  56 */   private List<File> jar_to_instrument = new LinkedList<>();
/*     */   
/*  58 */   private List<File> instrumented_jar = new LinkedList<>();
/*     */   
/*     */   private JacocoResult results;
/*     */   private Map<String, JacocoResult> allResults;
/*     */   
/*     */   public JaCoCoLauncher(String pTempFile) {
/*  64 */     this.temp_folder = pTempFile;
/*  65 */     this.allResults = new HashMap<>();
/*     */   }
/*     */   
/*     */   public void setTestCase(List<String> testCases) {
/*  69 */     this.testCases = testCases;
/*     */   }
/*     */   
/*     */   public void setTestFolder(String testFolder) {
/*  73 */     this.testFolder = testFolder;
/*     */   }
/*     */   
/*     */   public void setTargetClass(String CUT) {
/*  77 */     this.targetClass = CUT.replace('.', '/');
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public void setClassPath(String classpath) {
/*  86 */     this.required_libraries = new LinkedList<>();
/*     */     
/*  88 */     String[] libraries = classpath.split(":");
/*  89 */     for (String s : libraries) {
/*  90 */       s = s.replace(":", "");
/*  91 */       if (s.length() > 0) {
/*  92 */         this.required_libraries.addLast(s);
/*     */       }
/*     */     } 
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public void setJarInstrument(List<File> jars) {
/* 103 */     for (File file : jars) {
/*     */       
/* 105 */       if (!file.exists()) {
/*     */         try {
/* 107 */           throw new FileNotFoundException();
/* 108 */         } catch (FileNotFoundException e) {
/*     */           
/* 110 */           e.printStackTrace();
/*     */         } 
/*     */       }
/* 113 */       this.jar_to_instrument.add(file);
/*     */ 
/*     */       
/* 116 */       String jarName = file.getName();
/* 117 */       if (jarName.endsWith(".jar")) {
/* 118 */         jarName = jarName.replaceAll(".jar", "_instrumented.jar");
/*     */       } else {
/* 120 */         jarName = jarName + "_instrumented";
/* 121 */       }  this.instrumented_jar.add(new File(this.temp_folder + "/" + jarName));
/*     */     } 
/*     */   }
/*     */   
/*     */   private String generateClassPath() throws MalformedURLException {
/* 126 */     String cp = "";
/*     */     
/* 128 */     cp = cp + Main.JACOCO_JAR + ":";
/*     */     
/*     */     int index;
/* 131 */     for (index = 0; index < this.instrumented_jar.size(); index++) {
/* 132 */       cp = cp + ((File)this.instrumented_jar.get(index)).getAbsolutePath() + ":";
/*     */     }
/*     */ 
/*     */     
/* 136 */     for (index = 0; index < this.required_libraries.size(); index++) {
/* 137 */       cp = cp + (String)this.required_libraries.get(index) + ":";
/*     */     }
/*     */     
/* 140 */     cp = cp + this.testFolder;
/* 141 */     return cp;
/*     */   }
/*     */ 
/*     */ 
/*     */   
/*     */   public void runJaCoCo() throws Exception {
/* 147 */     LoggerRuntime loggerRuntime = new LoggerRuntime();
/* 148 */     Instrumenter instr = new Instrumenter((IExecutionDataAccessorGenerator)loggerRuntime);
/*     */     
/* 150 */     int tot_instrumented_class = 0;
/* 151 */     for (int index = 0; index < this.jar_to_instrument.size(); index++) {
/*     */ 
/*     */ 
/*     */       
/* 155 */       if (((File)this.jar_to_instrument.get(index)).isDirectory()) {
/*     */         
/* 157 */         ((File)this.instrumented_jar.get(index)).mkdir();
/*     */ 
/*     */         
/* 160 */         FileUtils.copyDirectory(this.jar_to_instrument.get(index), this.instrumented_jar.get(index));
/*     */ 
/*     */         
/* 163 */         //List<File> files = (List<File>)FileUtils.listFiles(this.jar_to_instrument.get(index), TrueFileFilter.INSTANCE, TrueFileFilter.INSTANCE);
/* 163 */         List<File> files = FileUtils.listFiles(this.jar_to_instrument.get(index));
/*     */
/*     */         
/* 166 */         for (File f : files) {
/* 167 */           if (f.getName().endsWith(".class")) {
/*     */ 
/*     */             
/* 170 */             String fileName = f.getAbsolutePath();
/* 171 */             fileName = fileName.replace(((File)this.jar_to_instrument.get(index)).getAbsolutePath(), ((File)this.instrumented_jar
/* 172 */                 .get(index)).getAbsolutePath());
/*     */ 
/*     */             
/* 175 */             InputStream input = new FileInputStream(f.getAbsolutePath());
/*     */ 
/*     */             
/* 178 */             OutputStream output = new FileOutputStream(fileName);
/* 179 */             tot_instrumented_class += instr.instrumentAll(input, output, "");
/*     */           } 
/*     */         } 
/*     */       } 
/*     */     } 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */     
/* 192 */     Main.info("Number of instrumented file = " + tot_instrumented_class);
/*     */ 
/*     */ 
/*     */     
/* 196 */     RuntimeData data = new RuntimeData();
/* 197 */     loggerRuntime.startup(data);
/*     */     
/* 199 */     String cp = generateClassPath();
/*     */     
/* 201 */     Main.info("Running tests with the following classpath: \n" + cp);
/*     */     
/* 203 */     ExecutorService service = Executors.newFixedThreadPool(1);
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */     
/* 209 */     RemoteTestExecutionTask executor = new RemoteTestExecutionTask(cp, this.testCases);
/* 210 */     FutureTask<Map<String, Object>> task = (FutureTask)service.submit(executor);
/* 211 */     task.get(60000L, TimeUnit.MILLISECONDS);
/*     */     
/* 213 */     service.shutdown();
/* 214 */     service.awaitTermination(5L, TimeUnit.MINUTES);
/*     */ 
/*     */ 
/*     */     
/* 218 */     ExecutionDataStore executionData = new ExecutionDataStore();
/* 219 */     SessionInfoStore sessionInfos = new SessionInfoStore();
/* 220 */     data.collect((IExecutionDataVisitor)executionData, (ISessionInfoVisitor)sessionInfos, false);
/* 221 */     loggerRuntime.shutdown();
/*     */     
/* 223 */     for (int i = 0; i < this.jar_to_instrument.size(); i++) {
/*     */ 
/*     */       
/* 226 */       CoverageBuilder coverageBuilder = new CoverageBuilder();
/* 227 */       Analyzer analyzer = new Analyzer(executionData, (ICoverageVisitor)coverageBuilder);
/* 228 */       int n = analyzer.analyzeAll(this.jar_to_instrument.get(i));
/* 229 */       Main.info("Number of file with coverage information = " + n);
/*     */ 
/*     */       
/* 232 */       for (IClassCoverage cc : coverageBuilder.getClasses()) {
/* 233 */         JacocoResult jcr = new JacocoResult(cc);
/* 234 */         this.allResults.put(cc.getName(), jcr);
/*     */         
/* 236 */         Main.info("Extracted coverage data for the class " + cc.getName() + " ");
/* 237 */         jcr.printResults();
/*     */         
/* 239 */         if (cc.getName().equals(this.targetClass)) {
/* 240 */           Main.debug("Extracted coverage data for the class " + this.targetClass);
/* 241 */           this.results = jcr;
/*     */         } 
/*     */       } 
/*     */     } 
/*     */     
/* 246 */     for (File file : this.instrumented_jar) {
/* 247 */       if (file.exists()) {
/* 248 */         if (file.isDirectory()) {
/* 249 */           FileUtils.deleteDirectory(file); continue;
/*     */         } 
/* 251 */         file.deleteOnExit();
/*     */       } 
/*     */     } 
/*     */   }
/*     */   
/*     */   public Map<String, JacocoResult> getAllResults() {
/* 257 */     return this.allResults;
/*     */   }
/*     */   
/*     */   public JacocoResult getResults() {
/* 261 */     return this.results;
/*     */   }
/*     */ }


/* Location:              /Users/gambi/killmatrix/repo/sbstcontest/benchmarktool/1.0.0/benchmarktool-1.0.0.jar!/sbst/benchmark/coverage/JaCoCoLauncher.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */