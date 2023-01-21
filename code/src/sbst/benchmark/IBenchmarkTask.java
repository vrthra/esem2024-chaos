package sbst.benchmark;

import java.io.File;
import java.util.List;

public interface IBenchmarkTask {
  File getSourceDirectory();
  
  File getBinDirectory();
  
  List<File> getClassPath();
  
  List<String> getClassNames();
}


/* Location:              /Users/gambi/killmatrix/repo/sbstcontest/benchmarktool/1.0.0/benchmarktool-1.0.0.jar!/sbst/benchmark/IBenchmarkTask.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */