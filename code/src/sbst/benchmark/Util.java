/*     */ package sbst.benchmark;
/*     */ 
/*     */ import java.io.File;
/*     */ import java.io.FileInputStream;
/*     */ import java.io.FileOutputStream;
/*     */ import java.io.IOException;
/*     */ import java.nio.MappedByteBuffer;
/*     */ import java.nio.channels.FileChannel;
/*     */ import java.nio.charset.Charset;
/*     */ import java.util.Collection;
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
/*     */ public class Util
/*     */ {
/*     */   public static class CPBuilder
/*     */   {
/*  27 */     private final StringBuilder sb = new StringBuilder(); private final String seperator;
/*     */     
/*     */     public CPBuilder() {
/*  30 */       this(":");
/*     */     } public CPBuilder(String seperator) {
/*  32 */       this.seperator = seperator;
/*     */     }
/*     */ 
/*     */     
/*     */     private CPBuilder append(String s) {
/*  37 */       if (this.sb.length() > 0)
/*  38 */         this.sb.append(this.seperator); 
/*  39 */       this.sb.append(s);
/*  40 */       return this;
/*     */     }
/*     */     
/*  43 */     public CPBuilder and(String f) { return append(f); } public CPBuilder and(File f) {
/*  44 */       return append(f.getAbsolutePath());
/*     */     } public CPBuilder and(Collection<File> lf) {
/*  46 */       for (File f : lf)
/*  47 */         append(f.getAbsolutePath()); 
/*  48 */       return this;
/*     */     }
/*     */     public CPBuilder andStrings(Collection<String> lf) {
/*  51 */       for (String f : lf)
/*  52 */         append(f); 
/*  53 */       return this;
/*     */     } public String build() {
/*  55 */       return this.sb.toString();
/*     */     } }
/*     */   
/*     */   public static String readFile(File file) throws IOException {
/*  59 */     FileInputStream stream = new FileInputStream(file);
/*     */     try {
/*  61 */       FileChannel fc = stream.getChannel();
/*  62 */       MappedByteBuffer bb = fc.map(FileChannel.MapMode.READ_ONLY, 0L, fc.size());
/*  63 */       return Charset.defaultCharset().decode(bb).toString();
/*     */     } finally {
/*  65 */       stream.close();
/*     */     } 
/*     */   }
/*     */ 
/*     */   
/*     */   public static void cleanDirectory(File dir) throws IOException {
/*  71 */     if (dir.exists()) {
/*  72 */       delete(dir);
/*     */     }
/*  74 */     Main.debug("Creating directory " + dir);
/*  75 */     if (!dir.mkdir())
/*  76 */       throw new IOException("Could not create directory: " + dir); 
/*     */   }
/*     */   
/*     */   private static void delete(File f) throws IOException {
/*  80 */     if (f.isDirectory())
/*  81 */       for (File c : f.listFiles()) {
/*  82 */         delete(c);
/*     */       } 
/*  84 */     Main.debug("Deleting file or directory " + f);
/*  85 */     if (!f.delete())
/*  86 */       throw new IOException("Failed to delete file: " + f); 
/*     */   }
/*     */   
/*     */   public static void CopyToDirectory(File fileOrDirectory, File destDir, String targetName) throws IOException {
/*  90 */     Main.debug("Copying '" + fileOrDirectory + "' to '" + destDir + "'");
/*     */     
/*  92 */     if (targetName == null) {
/*  93 */       targetName = fileOrDirectory.getName();
/*     */     }
/*  95 */     if (!destDir.exists() && 
/*  96 */       !destDir.mkdirs()) {
/*  97 */       throw new IOException("Unable to create directory " + destDir.getAbsolutePath());
/*     */     }
/*     */     
/* 100 */     if (fileOrDirectory.isFile()) {
/* 101 */       File destFile = new File(destDir.getAbsolutePath() + File.separator + targetName);
/* 102 */       if (!destFile.exists() && 
/* 103 */         !destFile.createNewFile()) {
/* 104 */         throw new IOException("Unable to create file " + destFile.getAbsolutePath());
/*     */       }
/*     */       
/* 107 */       FileChannel source = null;
/* 108 */       FileChannel destination = null;
/*     */       
/*     */       try {
/* 111 */         source = (new FileInputStream(fileOrDirectory)).getChannel();
/* 112 */         destination = (new FileOutputStream(destFile)).getChannel();
/* 113 */         destination.transferFrom(source, 0L, source.size());
/*     */       } finally {
/* 115 */         if (source != null)
/* 116 */           source.close(); 
/* 117 */         if (destination != null)
/* 118 */           destination.close(); 
/*     */       } 
/* 120 */     } else if (fileOrDirectory.isDirectory()) {
/* 121 */       File copyDir = new File(destDir.getAbsolutePath() + File.separator + targetName);
/*     */       
/* 123 */       if (!copyDir.exists() && 
/* 124 */         !copyDir.mkdir()) {
/* 125 */         throw new IOException("Unable to create directory " + copyDir.getAbsolutePath());
/*     */       }
/*     */       
/* 128 */       File[] files = fileOrDirectory.listFiles();
/* 129 */       if (files != null)
/* 130 */         for (File f : files) {
/* 131 */           CopyToDirectory(f, copyDir, null);
/*     */         } 
/*     */     } 
/*     */   }
/*     */   
/*     */   public static void pause(double time) {
/*     */     try {
/* 138 */       Thread.sleep((int)(time * 1000.0D));
/* 139 */     } catch (Throwable throwable) {}
/*     */   }
/*     */ }


/* Location:              /Users/gambi/killmatrix/repo/sbstcontest/benchmarktool/1.0.0/benchmarktool-1.0.0.jar!/sbst/benchmark/Util.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */