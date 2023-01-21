package sbst.benchmark;

import java.io.File;
import java.util.List;

public interface IToolListener {
  void finish();
  
  void startPreprocess();
  
  void endPreprocess();
  
  void startClass(String paramString);
  
  void endClass(String paramString, File paramFile, List<File> paramList);
  
  void start(String paramString1, int paramInt1, String paramString2, int paramInt2);
}


/* Location:              /Users/gambi/killmatrix/repo/sbstcontest/benchmarktool/1.0.0/benchmarktool-1.0.0.jar!/sbst/benchmark/IToolListener.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */