
package sbst.benchmark.junit;

import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.internal.TextListener;
import org.junit.runner.Description;
import org.junit.runner.notification.Failure;

public class ParsableTextListener extends TextListener {
    private boolean lastFailed = false;
    private boolean lastIgnored = false;
    private boolean lastAssumptionNotMet = false;
    private Optional<Failure> ifFailed = Optional.empty();

    private PrintStream writer;

    public ParsableTextListener(PrintStream writer) {
        super(new PrintStream(new OutputStream() {
            public void write(int b) {
            }
        }));
        this.writer = writer;
    }

    public void testStarted(Description description) {
        this.lastFailed = false;
        this.lastIgnored = false;
        this.lastAssumptionNotMet = false;
        this.ifFailed = Optional.empty();
    }

    public void testIgnored(Description description) {
        this.lastIgnored = true;
    }

    public void testAssumptionFailure(Failure failure) {
        this.lastAssumptionNotMet = true;
        this.ifFailed = Optional.of(failure);
    }

    public void testFailure(Failure failure) {

        // In case this class has no test methods, i.e., not a test we skip it
        Throwable exception = failure.getException();

        if (exception != null && exception.getMessage() != null
                && (exception.getMessage()
                        .contains("Test class should have exactly one public zero-argument constructor")
                        || exception.getMessage().contains("No runnable methods"))) {
            this.lastIgnored = true;
            this.ifFailed = Optional.empty();
            return;
        }

        if (failure.getTestHeader().contains("initializationError") && failure.getMessage() != null
                && failure.getMessage().contains("No runnable methods")) {
            this.lastIgnored = true;
            this.ifFailed = Optional.empty();
            return;
        }

        // Otherwise, we mark this as an actual failure
        this.lastFailed = true;
        this.ifFailed = Optional.of(failure);

    }

    public void testFinished(Description description) throws Exception {
        StringBuffer sb = new StringBuffer();
        sb.append(description.getDisplayName());
        if (this.lastFailed) {
            sb.append(" FAIL");
        } else if (this.lastIgnored) {
            sb.append(" IGNORED");
        } else if (this.lastAssumptionNotMet) {
            sb.append(" INVALID");
        } else {
            sb.append(" PASS");
        }

        List<String> out = new ArrayList<>();

        if (this.ifFailed.isPresent()) {
            sb.append(" ");
            out.add(((Failure) this.ifFailed.get()).getException().toString().trim());

            for (String line : ((Failure) this.ifFailed.get()).getTrace().split(System.lineSeparator())) {
                if (line.contains(description.getTestClass().getName())) {

                    if (line.contains(":")) {

                        out.add(line.trim().split(":")[1].replaceAll("\\D", ""));
                    } else {

                        out.add(line.trim());
                    }
                }
            }
            // Make sure we do not go into multiple lines here
            sb.append(out.toString().replaceAll("(\\t|\\r?\\n|\\n)+", " "));
        }

        this.writer.println(sb.toString());
    }
}

/*
 * Location:
 * /Users/gambi/killmatrix/repo/sbstcontest/benchmarktool/1.0.0/benchmarktool-1.
 * 0.0.jar!/sbst/benchmark/junit/ParsableTextListener.class Java compiler
 * version: 8 (52.0) JD-Core Version: 1.1.3
 */