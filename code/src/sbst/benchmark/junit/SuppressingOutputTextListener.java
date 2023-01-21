
package sbst.benchmark.junit;

import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Optional;

import org.junit.internal.TextListener;
import org.junit.runner.Description;
import org.junit.runner.notification.Failure;

public class SuppressingOutputTextListener extends TextListener {
    private PrintStream writer;

    public SuppressingOutputTextListener(PrintStream writer) {
        super(new PrintStream(new OutputStream() {
            public void write(int b) {
            }
        }));
        //
        this.writer = writer;

    }

    public void testAssumptionFailure(Failure failure) {
        writer.println("\t - Skipped test: " + failure.getTestHeader() + " : " + failure.getMessage());
    }

    public void testStarted(Description description) {
        writer.println("\t - Started test: " + description.getClassName() + "." + description.getMethodName());
    }

    @Override
    public void testFinished(Description description) throws Exception {
        writer.println("\t - Finished test: " + description.getClassName() + "." + description.getMethodName());
    }

    @Override
    public void testIgnored(Description description) {
        writer.println("\t - Ignored test: " + description.getClassName() + "." + description.getMethodName());
    }

    public void testFailure(Failure failure) {
//        writer.println("\t - Failed test !");
        // In case this class has no test methods, i.e., not a test we skip it
        Throwable exception = failure.getException();

        if (exception != null && exception.getMessage() != null
                && (exception.getMessage()
                        .contains("Test class should have exactly one public zero-argument constructor")
                        || exception.getMessage().contains("No runnable methods"))) {
            writer.println("\t - (Forced) Skipped test: " + failure.getTestHeader());
        } else if (failure.getTestHeader().contains("initializationError") && failure.getMessage() != null
                && failure.getMessage().contains("No runnable methods")) {
            writer.println("\t - (Forced) Skipped test: " + failure.getTestHeader());
        } else {
            writer.println("\t - Failed test: " + failure.getTestHeader() + " : " + failure.getMessage());
//            String[] traceElements = failure.getTrace().split(System.lineSeparator());
//            writer.println("\t - Failed test: " + failure.getTestHeader() + " : " + failure.getMessage());
//            writer.println(failure.getTrace());
//            failure.getTestHeader() + " : " + traceElements[0] + " "
//                    + traceElements[1] + " " + traceElements[2]);
        }
    }
}