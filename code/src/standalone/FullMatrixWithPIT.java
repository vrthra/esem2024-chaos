
package standalone;

import com.lexicalscope.jewel.cli.CliFactory;
import com.lexicalscope.jewel.cli.Option;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import sbst.benchmark.Main;

public class FullMatrixWithPIT {
    public static interface CLI {
        @Option(longName = { "output-folder" }, defaultToNull = true)
        File getOutputFolder();

        @Option(longName = { "test-cases-dir" })
        File getTestCaseDir();

        @Option(longName = { "compiled-test-cases-dir" }, defaultToNull = true)
        File getCompiledTestsDir();

        @Option(longName = { "jacoco-exec-file" })
        File getJacocoExecFile();

        @Option(longName = { "classpath" })
        List<File> getClassPath();

        @Option(longName = { "extra-classpath" })
        List<File> getExtraClassPath();

        @Option(longName = { "list-of-cut" })
        List<String> getCuts();
    }

    public static void main(String[] args) throws IOException {
        if (System.getProperty("sbst.benchmark.javac") == null) {
            System.setProperty("sbst.benchmark.javac", "javac");
        }

        if (System.getProperty("sbst.benchmark.java") == null) {
            System.setProperty("sbst.benchmark.java", "java");
        }

        if (System.getProperty("sbst.benchmark.jacoco") == null) {
            System.setProperty("sbst.benchmark.jacoco", "/Users/gambi/junitcontest/bin/lib/jacocoagent.jar");
        }

        CLI parsedCommandLineArguments = (CLI) CliFactory.parseArguments(CLI.class, args);

        File logDir = (parsedCommandLineArguments.getOutputFolder() != null)
                ? parsedCommandLineArguments.getOutputFolder()
                : parsedCommandLineArguments.getTestCaseDir();

        Main.debugStr = new PrintStream(
                new BufferedOutputStream(new FileOutputStream(new File(logDir, "debug_log.txt"), true)), true, "UTF-8");

        Main.infoStr = new PrintStream(
                new BufferedOutputStream(new FileOutputStream(new File(logDir, "log.txt"), true)), true, "UTF-8");

        TestSuite ts = new TestSuite(parsedCommandLineArguments.getTestCaseDir(),
                parsedCommandLineArguments.getClassPath(), parsedCommandLineArguments.getExtraClassPath(),
                parsedCommandLineArguments.getCuts().<String>toArray(new String[0]));

        if (parsedCommandLineArguments.getCompiledTestsDir() != null) {

            String prefix = parsedCommandLineArguments.getCompiledTestsDir().toString().endsWith("/")
                    ? parsedCommandLineArguments.getCompiledTestsDir().toString()
                    : (parsedCommandLineArguments.getCompiledTestsDir().toString() + "/");
            try (Stream<Path> walk = Files.walk(parsedCommandLineArguments.getCompiledTestsDir().toPath(),
                    new java.nio.file.FileVisitOption[0])) {

                List<String> testFiles = (List<String>) walk.map(x -> x.toString())
                        .filter(f -> f.endsWith("Test.class")).map(f -> f.replace(prefix, ""))
                        .collect(Collectors.toList());

                ts.setCompiledTestSrcFiles(testFiles);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {

            ts.compileTests();
        }

        ts.computeCoverage(parsedCommandLineArguments.getJacocoExecFile());

        ts.mutationAnalysis();
    }

}
