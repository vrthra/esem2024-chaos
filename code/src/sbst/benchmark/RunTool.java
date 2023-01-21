/*     */ package sbst.benchmark;
/*     */ 
/*     */ import java.io.BufferedInputStream;
/*     */ import java.io.BufferedReader;
/*     */ import java.io.File;
/*     */ import java.io.IOException;
/*     */ import java.io.InputStream;
/*     */ import java.io.InputStreamReader;
/*     */ import java.io.OutputStreamWriter;
/*     */ import java.io.StringReader;
/*     */ import java.lang.reflect.Field;
/*     */ import java.util.ArrayList;
/*     */ import java.util.HashMap;
/*     */ import java.util.HashSet;
/*     */ import java.util.List;
/*     */ import java.util.Map;
/*     */ import java.util.PriorityQueue;
/*     */ import java.util.Queue;
/*     */ import java.util.Set;
/*     */ import java.util.concurrent.Callable;
/*     */ import java.util.concurrent.ExecutorService;
/*     */ import java.util.concurrent.Executors;
/*     */ import java.util.concurrent.Future;
/*     */ import java.util.concurrent.TimeUnit;
/*     */ import java.util.concurrent.TimeoutException;
/*     */ import org.apache.commons.lang.StringUtils;
/*     */ import sbst.runtool.SBSTChannel;
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
/*     */ public class RunTool
/*     */ {
/*     */   private static final double EXTRA_TIME_FACTOR = 1.0D;
/*     */   private final File homeDir;
/*     */   private final File executable;
/*     */   private final IToolListener listener;
/*  50 */   private int timeBudget = -1;
/*     */   
/*     */   private boolean generateTests = true;
/*     */   
/*     */   public void enableGenerateTests() {
/*  55 */     this.generateTests = true;
/*     */   }
/*     */   
/*     */   public void disableGenerateTests() {
/*  59 */     this.generateTests = false;
/*     */   }
/*     */   
/*     */   public RunTool(File directory, File executable, IToolListener listener, int timeBudget) {
/*  63 */     this.homeDir = directory;
/*  64 */     this.executable = executable;
/*  65 */     this.listener = listener;
/*  66 */     this.timeBudget = timeBudget;
/*     */   }
/*     */   
/*     */   public void run(IBenchmarkTask task) throws IOException {
/*  70 */     File tempDir = new File(this.homeDir, "temp");
/*  71 */     Util.cleanDirectory(tempDir);
/*  72 */     File dataDir = new File(tempDir, "data");
/*  73 */     Util.cleanDirectory(dataDir);
/*  74 */     File testcasesDir = new File(tempDir, "testcases");
/*  75 */     Util.cleanDirectory(testcasesDir);
/*     */     
/*  77 */     ProcessBuilder pbuilder = new ProcessBuilder(new String[] { this.executable.getAbsolutePath() });
/*  78 */     pbuilder.redirectErrorStream(false);
/*  79 */     Process toolSubprocess = null;
/*  80 */     InputStreamReader stdout = null;
/*  81 */     InputStream stderr = null;
/*  82 */     OutputStreamWriter stdin = null;
/*     */     
/*  84 */     Main.debug("Executing " + this.executable.getAbsolutePath());
/*  85 */     toolSubprocess = pbuilder.start();
/*     */     try {
/*  87 */       stderr = toolSubprocess.getErrorStream();
/*  88 */       stdout = new InputStreamReader(toolSubprocess.getInputStream());
/*  89 */       stdin = new OutputStreamWriter(toolSubprocess.getOutputStream());
/*     */       
/*  91 */       Thread drainThread = new Thread(new Drain(stderr));
/*  92 */       drainThread.setDaemon(true);
/*  93 */       drainThread.start();
/*     */       
/*  95 */       execute(task, testcasesDir, new SBSTChannel(stdout, stdin, Main.debugStr));
/*     */     } finally {
/*     */       
/*  98 */       Main.debug("Stopping process...");
/*  99 */       killUnixProcess(toolSubprocess);
/* 100 */       Main.debug("Process was destroyed");
/*     */     } 
/*     */   }
/*     */   
/*     */   private void execute(IBenchmarkTask task, File testcases, SBSTChannel channel) throws IOException {
/* 105 */     channel.emit("BENCHMARK");
/* 106 */     this.listener.startPreprocess();
/* 107 */     channel.emit(task.getSourceDirectory());
/* 108 */     channel.emit(task.getBinDirectory());
/* 109 */     channel.emit(task.getClassPath().size());
/*     */     
/* 111 */     for (File cp_entry : task.getClassPath()) {
/* 112 */       channel.emit(cp_entry.getAbsolutePath());
/*     */     }
/* 114 */     if (!this.generateTests) {
/* 115 */       channel.emit(0);
/*     */     } else {
/* 117 */       channel.emit(task.getClassNames().size());
/*     */     } 
/* 119 */     Main.debug("expecting CLASSPATH or READY");
/* 120 */     String line = channel.readLine();
/* 121 */     List<File> extraCP = new ArrayList<>();
/* 122 */     if ("CLASSPATH".equals(line)) {
/* 123 */       int n = channel.number();
/* 124 */       for (int i = 0; i < n; i++) {
/* 125 */         File entry = channel.directory_jarfile();
/* 126 */         extraCP.add(entry);
/*     */       } 
/* 128 */       line = channel.readLine();
/*     */     } 
/*     */     
/* 131 */     if ("READY".equals(line)) {
/* 132 */       this.listener.endPreprocess();
/* 133 */       if (this.timeBudget != -1) {
/* 134 */         channel.emit(this.timeBudget);
/*     */       }
/* 136 */       if (this.generateTests) {
/* 137 */         for (String cname : task.getClassNames()) {
/* 138 */           Util.cleanDirectory(testcases);
/* 139 */           Main.info("\n\n### CLASS UNDER TEST ###: " + cname);
/* 140 */           channel.emit(cname);
/* 141 */           this.listener.startClass(cname);
/* 142 */           if (this.timeBudget != -1) {
/*     */             
/*     */             try {
/* 145 */               long budget_millis = (this.timeBudget * 1000);
/* 146 */               long extra_time_millis = (long)(budget_millis * 1.0D);
/* 147 */               long timeout_millis = budget_millis + extra_time_millis;
/*     */               
/* 149 */               Main.info(String.format("\n\n Executing with internal budget of %s sec and external budget of %s sec ", new Object[] {
/*     */                       
/* 151 */                       Integer.valueOf(this.timeBudget), Long.valueOf(timeout_millis / 1000L) }));
/* 152 */               token(channel, "READY", timeout_millis);
/* 153 */               Main.info("\n\n Execution finished with no timeout");
/* 154 */             } catch (TimeoutException ex) {
/* 155 */               Main.info("\n\nA timeout occurred waiting for signal READY");
/*     */             }
/*     */           
/*     */           } else {
/*     */             
/* 160 */             channel.token("READY");
/*     */           } 
/* 162 */           this.listener.endClass(cname, testcases, extraCP);
/*     */           
/* 164 */           Main.info(">> >>" + testcases.getAbsolutePath());
/*     */         } 
/*     */       } else {
/* 167 */         for (String cname : task.getClassNames()) {
/* 168 */           Main.info("\n\n### COMPUTING METRICS ###: " + cname);
/* 169 */           Main.debug("\n\nStarting class " + cname);
/* 170 */           this.listener.startClass(cname);
/* 171 */           Main.debug("\n\nEnding class " + cname);
/* 172 */           this.listener.endClass(cname, testcases, extraCP);
/* 173 */           Main.debug("\n\nClass " + cname + " ended.");
/*     */         } 
/*     */       } 
/*     */     } 
/*     */   }
/*     */   
/*     */   private static class Drain implements Runnable {
/*     */     private BufferedInputStream stream;
/*     */     
/*     */     private Drain(InputStream stream) {
/* 183 */       this.stream = new BufferedInputStream(stream);
/*     */     }
/*     */ 
/*     */     
/*     */     public void run() {
/* 188 */       byte[] buff = new byte[1024];
/*     */       try {
/*     */         int len;
/* 191 */         while ((len = this.stream.read(buff)) != -1) {
/* 192 */           System.err.write(buff, 0, len);
/* 193 */           Main.debugStr.write(buff, 0, len);
/*     */         } 
/* 195 */       } catch (IOException e) {
/* 196 */         e.printStackTrace();
/*     */       } 
/*     */     }
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   private void token(SBSTChannel channel, String string, long timeout_millis) throws IOException, TimeoutException {
/* 207 */     ExecutorService executor = Executors.newSingleThreadExecutor();
/* 208 */     TaskExpectReady future_task = new TaskExpectReady(channel, string);
/* 209 */     Future<Void> future = executor.submit(future_task);
/*     */     
/*     */     try {
/* 212 */       future.get(timeout_millis, TimeUnit.MILLISECONDS);
/* 213 */     } catch (Exception e) {
/* 214 */       for (StackTraceElement trace : e.getStackTrace())
/* 215 */         Main.debug("" + trace); 
/* 216 */       Main.debug("Error: \n" + e.getCause());
/* 217 */       e.printStackTrace();
/*     */     } 
/*     */   }
/*     */   
/*     */   private class TaskExpectReady implements Callable<Void> {
/*     */     private final String string;
/*     */     private final SBSTChannel channel;
/*     */     
/*     */     public TaskExpectReady(SBSTChannel channel, String expected_string) {
/* 226 */       this.string = expected_string;
/* 227 */       this.channel = channel;
/*     */     }
/*     */ 
/*     */     
/*     */     public Void call() throws Exception {
/* 232 */       this.channel.token(this.string);
/* 233 */       return null;
/*     */     }
/*     */   }
/*     */ 
/*     */   
/*     */   private static int getUnixPID(Process process) throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
/* 239 */     Class<?> cl = process.getClass();
/* 240 */     Field field = cl.getDeclaredField("pid");
/* 241 */     field.setAccessible(true);
/* 242 */     Object pidObject = field.get(process);
/* 243 */     return ((Integer)pidObject).intValue();
/*     */   }
/*     */   
/*     */   private static void killUnixProcess(Process process) {
/* 247 */     if (process.getClass().getName().equals("java.lang.UNIXProcess")) {
/*     */       try {
/* 249 */         Main.debug("Tool subprocess is an instance of java.lang.UNIXProcess");
/* 250 */         int toolSubprocessPID = getUnixPID(process);
/* 251 */         Main.debug("Tool subprocess Id is " + toolSubprocessPID);
/*     */ 
/*     */         
/* 254 */         String psOutput = executePS();
/*     */ 
/*     */         
/* 257 */         Map<Integer, Set<Integer>> children_of_pids = buildParentPIDToChildrenPIDMap(psOutput);
/*     */ 
/*     */         
/* 260 */         killSubprocesses(toolSubprocessPID, children_of_pids);
/*     */       }
/* 262 */       catch (InterruptedException|IOException|NoSuchFieldException|SecurityException|IllegalArgumentException|IllegalAccessException e) {
/*     */         
/* 264 */         Main.debug("An exception occurred while killing tool subprocess:");
/* 265 */         Main.debug(e.getMessage());
/*     */       } 
/*     */     } else {
/* 268 */       Main.debug("Tool subprocess is NOT an instance of java.lang.UNIXProcess");
/* 269 */       Main.debug("calling Process.destroy()");
/* 270 */       process.destroy();
/* 271 */       Main.debug("Process.destroy() finished");
/*     */     } 
/*     */   }
/*     */ 
/*     */   
/*     */   private static void killSubprocesses(int toolSubprocessPID, Map<Integer, Set<Integer>> children_of_pids) throws InterruptedException, IOException {
/* 277 */     Set<Integer> killed_pids = new HashSet<>();
/* 278 */     Queue<Integer> killing_queue = new PriorityQueue<>();
/* 279 */     killing_queue.add(Integer.valueOf(toolSubprocessPID));
/* 280 */     while (!killing_queue.isEmpty()) {
/* 281 */       int pid = ((Integer)killing_queue.poll()).intValue();
/* 282 */       if (killed_pids.contains(Integer.valueOf(pid))) {
/*     */         continue;
/*     */       }
/* 285 */       if (children_of_pids.containsKey(Integer.valueOf(pid))) {
/* 286 */         Set<Integer> children_pids = children_of_pids.get(Integer.valueOf(pid));
/* 287 */         killing_queue.addAll(children_pids);
/*     */       } 
/*     */       
/* 290 */       if (pid == toolSubprocessPID) {
/* 291 */         Main.debug("Killing master process " + pid);
/*     */       } else {
/* 293 */         Main.debug("Killing child process " + pid);
/*     */       } 
/*     */       
/* 296 */       String kill_cmd = "kill -9 " + pid;
/* 297 */       Main.debug("Executing command " + kill_cmd);
/* 298 */       Runtime.getRuntime().exec(kill_cmd).waitFor();
/* 299 */       Main.debug("Execution of command " + kill_cmd + " finished.");
/* 300 */       killed_pids.add(Integer.valueOf(pid));
/*     */     } 
/*     */   }
/*     */ 
/*     */   
/*     */   private static String executePS() throws IOException, InterruptedException {
/* 306 */     ProcessBuilder pBuilder = new ProcessBuilder(new String[0]);
/* 307 */     pBuilder.command(new String[] { "ps", "-eo", "pid,ppid" });
/* 308 */     Process psProcess = pBuilder.start();
/* 309 */     psProcess.waitFor();
/* 310 */     InputStream pOutput = psProcess.getInputStream();
/* 311 */     BufferedReader reader = new BufferedReader(new InputStreamReader(pOutput));
/* 312 */     String line = null;
/* 313 */     StringBuilder builder = new StringBuilder();
/* 314 */     while ((line = reader.readLine()) != null) {
/* 315 */       builder.append(line + "\n");
/*     */     }
/* 317 */     String psOutput = builder.toString();
/* 318 */     return psOutput;
/*     */   }
/*     */ 
/*     */   
/*     */   private static Map<Integer, Set<Integer>> buildParentPIDToChildrenPIDMap(String psOutput) throws IOException {
/* 323 */     Map<Integer, Set<Integer>> childs_of_pid = new HashMap<>();
/* 324 */     BufferedReader strReader = new BufferedReader(new StringReader(psOutput)); String line;
/* 325 */     while ((line = strReader.readLine()) != null) {
/* 326 */       String[] line_parts = StringUtils.split(line);
/*     */       
/* 328 */       if (line_parts.length < 2) {
/*     */         continue;
/*     */       }
/* 331 */       String child_pid_str = line_parts[0];
/* 332 */       String parent_pid_str = line_parts[1];
/*     */       
/* 334 */       if (child_pid_str.equals("PID")) {
/*     */         continue;
/*     */       }
/*     */       
/* 338 */       int child_pid = Integer.valueOf(child_pid_str).intValue();
/* 339 */       int parent_pid = Integer.valueOf(parent_pid_str).intValue();
/*     */       
/* 341 */       if (!childs_of_pid.containsKey(Integer.valueOf(parent_pid))) {
/* 342 */         childs_of_pid.put(Integer.valueOf(parent_pid), new HashSet<>());
/*     */       }
/* 344 */       ((Set<Integer>)childs_of_pid.get(Integer.valueOf(parent_pid))).add(Integer.valueOf(child_pid));
/*     */     } 
/* 346 */     return childs_of_pid;
/*     */   }
/*     */ }


/* Location:              /Users/gambi/killmatrix/repo/sbstcontest/benchmarktool/1.0.0/benchmarktool-1.0.0.jar!/sbst/benchmark/RunTool.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */