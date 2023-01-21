
package sbst.benchmark.junit;

import java.io.FileWriter;
import java.io.PrintWriter;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.Test;
import org.junit.runner.Description;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunListener;
import org.junit.runner.notification.RunNotifier;
import org.junit.runner.notification.StoppedByUserException;
import sbst.benchmark.Main;
import sbst.benchmark.pitest.TestInfo;

public class StoppingJUnitCore {
    final AtomicBoolean globalTimeout = new AtomicBoolean(false);

    class StoppingListener extends RunListener {
        private RunNotifier runNotifier = null;
        private Set<TestInfo> flakyTests = null;
        private RunListener listener = null;

        public StoppingListener(RunNotifier runNotifier, Set<TestInfo> flakyTests, Result theResult) {
            this.runNotifier = runNotifier;
            this.flakyTests = flakyTests;

            this.listener = theResult.createListener();
        }

        public void testFailure(Failure failure) throws Exception {
            super.testFailure(failure);

            String header = failure.getTestHeader();
            Main.info("Failed test " + header);
            if (header.contains("(")) {
                String testMethod = header.substring(0, header.indexOf('('));
                String tc = header.substring(header.indexOf('(') + 1, header.length());
                TestInfo info = new TestInfo(tc, testMethod);

                if (this.flakyTests.contains(info)) {

                    Main.debug("\t flaky test, ignore !");
                    this.listener.testAssumptionFailure(failure);

                    return;
                }
                if (failure.getTrace().contains("java.lang.Exception: test timed out after")) {

                    Main.debug("\t timeout test, ignore !");
                    this.listener.testAssumptionFailure(failure);
                } else if (failure.getException() instanceof InterruptedException) {

                    Main.debug(
                            "\t test execution reached (probably GLOABL timeout), will stop test execution but do not report killing test !");

                    StoppingJUnitCore.this.globalTimeout.set(true);
                    this.runNotifier.pleaseStop();
                } else {
                    Main.debug("\t actual test, will stop test execution !");
                    this.listener.testFailure(failure);
                    this.runNotifier.pleaseStop();
                }
            }
        }

        public void testStarted(Description description) throws Exception {
            super.testStarted(description);
            this.listener.testRunStarted(description);
        }

        public void testFinished(Description description) throws Exception {
            super.testFinished(description);
            this.listener.testFinished(description);
        }

        public void testAssumptionFailure(Failure failure) {
            super.testAssumptionFailure(failure);
            this.listener.testAssumptionFailure(failure);
        }

        public void testIgnored(Description description) throws Exception {
            super.testIgnored(description);
            this.listener.testIgnored(description);
        }
    }

    public static Object changeAnnotationValue(Annotation annotation, String key, Object newValue) {
        Field f;
        Map<String, Object> memberValues;
        Object handler = Proxy.getInvocationHandler(annotation);

        try {
            f = handler.getClass().getDeclaredField("memberValues");
        } catch (NoSuchFieldException | SecurityException e) {
            throw new IllegalStateException(e);
        }
        f.setAccessible(true);

        try {
            memberValues = (Map<String, Object>) f.get(handler);
        } catch (IllegalArgumentException | IllegalAccessException e) {
            throw new IllegalStateException(e);
        }
        Object oldValue = memberValues.get(key);
        if (oldValue == null || oldValue.getClass() != newValue.getClass()) {
            throw new IllegalArgumentException();
        }
        memberValues.put(key, newValue);
        return oldValue;
    }

    public Result run(Class testClass, long timeout, Set<TestInfo> flakyTests) {
        return run(Collections.singletonList(testClass), timeout, flakyTests);
    }

    public Result run(List<Class> testClasses, long timeout, Set<TestInfo> flakyTests) {
        JUnitCore junit = new JUnitCore();
        junit.addListener((RunListener) new SuppressingOutputTextListener(Main.infoStr));

        Result theResult = new Result();

        try {
            for (Field field : junit.getClass().getDeclaredFields()) {
                if (field.getName().equals("notifier") || field.getName().equals("fNotifier")) {
                    field.setAccessible(true);

                    RunNotifier runNotifier = (RunNotifier) field.get(junit);
                    junit.addListener(new StoppingListener(runNotifier, flakyTests, theResult));
                }
            }

            for (Class testClass : testClasses) {
                for (Method testMethod : testClass.getDeclaredMethods()) {
                    Test methodAnnotation = testMethod.<Test>getAnnotation(Test.class);

                    if (methodAnnotation != null && methodAnnotation.timeout() == 0L) {
                        changeAnnotationValue((Annotation) methodAnnotation, "timeout", Long.valueOf(timeout));
                    }
                }
            }

            return junit.run((Class[]) testClasses.toArray(new Class[0]));
        } catch (StoppedByUserException e) {

            if (this.globalTimeout.get()) {
                Main.info("Global timeout reached while executing tests.");
                return null;
            }

            return theResult;
        } catch (Throwable t) {
            Main.info("Failed Test execution with JUNIT CORE: ");
            t.printStackTrace(Main.infoStr);
            t.printStackTrace();
            throw new RuntimeException("Cannot execute test with StoppingJUnitCore");
        }
    }

    public static void main(String[] args) throws NoSuchFieldException, SecurityException, IllegalArgumentException,
            IllegalAccessException, InvocationTargetException {
        Main.debugStr = System.out;
        Main.infoStr = System.out;

        Main.debug("StoppingJUnitCore.main() CP: " + System.getProperty("java.class.path"));
        Main.debug("StoppingJUnitCore.main() ARGS : " + Arrays.toString((Object[]) args));

        String outputTo = null;
        List<String> testClasses = null;

        int i = 0;
        if (args[i].equals("--output-to")) {
            i++;
            outputTo = args[i];
        } else {
            System.exit(-120);
        }

        Main.debug("Output result of test execution to : " + outputTo);

        i++;
        if (args[i].equals("--tests")) {
            i++;
            testClasses = new ArrayList<>();
            while (!args[i].trim().equals("--flaky-tests") && i < args.length) {

                testClasses.add(args[i]);
                i++;
            }
        } else {
            System.exit(-120);
        }
        Main.debug("StoppingJUnitCore.main() TEST CLASSES " + testClasses);

        Set<TestInfo> flakyTests = new HashSet<>();
        if (args[i].equals("--flaky-tests") && i < args.length) {
            for (int index = ++i; index < args.length; index++) {
                if (!args[i].equals("--flaky-tests")) {
                    Main.debug("TODO Flaky test " + args[index]);
                }
            }
        }

        Main.debug("StoppingJUnitCore.main() FLAKY-TESTS " + flakyTests);

        List<Class> classes = new ArrayList<>();

        for (String each : testClasses) {
            try {
                classes.add(Class.forName(each));
            } catch (ClassNotFoundException e) {
                Main.info(" ERROR: Could not find test class: " + each);
                System.exit(2);
            }
        }

        StoppingJUnitCore junit = new StoppingJUnitCore();

        long timeout = 5000L;

        try {
            Result result = junit.run(classes, timeout, flakyTests);

            if (result.getFailureCount() > 0) {
                boolean killed = false;
                for (Failure fail : result.getFailures()) {

                    String header = fail.getTestHeader();

                    if (fail.getTrace().contains("java.lang.Exception: test timed out after")
                            || fail.getTrace().contains("java.io.FileNotFoundException")) {
                        Main.debug("\n Discard test execution");

                        continue;
                    }
                    if (header.contains("(")) {
                        String testMethod = header.substring(0, header.indexOf('('));
                        String testClass = header.substring(header.indexOf('(') + 1, header.length());
                        killed = true;

                        break;
                    }
                }
                if (!killed) {
                    Main.debug("\n Mutant Ignored !\n");
                } else {
                    Main.debug("\n Mutant Killed !\n");
                }
            }

            Main.debug("Writing serialized JUnit results to " + outputTo);

            try (FileWriter fileWriter = new FileWriter(outputTo);
                    PrintWriter printWriter = new PrintWriter(fileWriter)) {
                printWriter.println(result.getRunCount());
                printWriter.println(result.getFailureCount());
                for (Failure failure : result.getFailures()) {
                    printWriter.println(failure.getTestHeader() + ": " + failure.getTrace());

                    printWriter.println("");
                }
            }
            System.exit(0);
        } catch (Throwable e) {
            Main.info("ERROR WHILE RUNNING TESTS !");
            e.printStackTrace();
            System.exit(2);
        }
    }
}

/*
 * Location:
 * /Users/gambi/killmatrix/repo/sbstcontest/benchmarktool/1.0.0/benchmarktool-1.
 * 0.0.jar!/sbst/benchmark/junit/StoppingJUnitCore.class Java compiler version:
 * 8 (52.0) JD-Core Version: 1.1.3
 */