
package org.junit.runner;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Proxy;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.junit.runner.notification.RunListener;

import junit.framework.TestCase;
import junit.framework.TestSuite;
import sbst.benchmark.junit.ParsableTextListener;
import sbst.benchmark.junit.SuppressingOutputTextListener;

public class RemoteJUnitCore {
    public static final String REMOTE_JUNIT_DEBUG = "remote.junit.debug";

    public static final String REMOTE_JUNIT_OUTPUT_TO = "remote.junit.output_to";
    public static final String REMOTE_JUNIT_OUTPUT_DEBUG_TO = "remote.junit.output_debug_to";
    public static final String REMOTE_JUNIT_TIMEOUT = "remote.junit.timeout";
    public static final String REMOTE_JUNIT_SKIP_JUNIT_3 = "remote.junit.skip.junit3";
    public static final String REMOTE_JUNIT_MUTANT_ID = "remote.junit.mutant_id";

    public static PrintStream outputStream = System.out;
    public static PrintStream debugStream = System.out;

    private static final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");

    public static long timeout = -1;
    public static boolean skip_Junit_3 = false;

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

    private static String mutant_id = "";

    public static void log(String msg) {
        debugStream.println(mutant_id + " REMOTE-JUNIT: " + msg);
    }

    public static void main(String[] args) throws ClassNotFoundException, FileNotFoundException {

        if (System.getProperty(REMOTE_JUNIT_OUTPUT_TO) != null) {
            outputStream = new PrintStream(new File(System.getProperty(REMOTE_JUNIT_OUTPUT_TO)));
        }

        if (System.getProperty(REMOTE_JUNIT_OUTPUT_DEBUG_TO) != null) {
            debugStream = new PrintStream(new File(System.getProperty(REMOTE_JUNIT_OUTPUT_DEBUG_TO)));
        }

        log("Starting remote test executions. ");

        if (System.getProperty("remote.junit.debug") != null) {
            log("Debug active. ");
        }

        if (System.getProperty(REMOTE_JUNIT_TIMEOUT) != null) {
            timeout = new Integer(System.getProperty(REMOTE_JUNIT_TIMEOUT));
            log("\t Forcing min timeout to " + timeout);
        }

        // If this flag is set, skip test in Junit 3 style
        skip_Junit_3 = System.getProperty(REMOTE_JUNIT_SKIP_JUNIT_3) != null;
        if (skip_Junit_3) {
            log("\t Forcing skip JUnit 3 tests");
        }

        mutant_id = System.getProperty(REMOTE_JUNIT_MUTANT_ID, "-1");

        log("Preparing classes for remote test executions. ");

        JUnitCore junit = new JUnitCore();

        List<Request> requests = new ArrayList<Request>();

        if (args.length == 1 && args[0].contains("#")) {
            String className = args[0].split("#")[0];
            String methodName = args[0].split("#")[1];
            //

            try {
                Class testClass = Class.forName(className);

                if (!Modifier.isAbstract(testClass.getModifiers())) {

                    for (Method testMethod : testClass.getDeclaredMethods()) {
                        Test methodAnnotation = testMethod.<Test>getAnnotation(Test.class);
                        if (methodAnnotation != null) {
                            if (timeout > 0 && methodAnnotation.timeout() < timeout) {
                                changeAnnotationValue((Annotation) methodAnnotation, "timeout", Long.valueOf(timeout));
                                log("   Update Timeout from " + methodAnnotation.timeout() + " to "
                                        + testMethod.getName());
                            }
                        }
                    }
                } else {
                    log("Skip class " + testClass.getName() + " because it is abstract.");

                }
            } catch (Throwable e) {
                log("Error instrumenting " + className + " - " + e.getMessage());
                e.printStackTrace(System.out);
            }
            //
            requests.add(Request.method(Class.forName(className), methodName));
        } else {

            int testClasses = 0;
            int testMethods = 0;

            List<String> argumentsAsList = new ArrayList<String>();
            //
            for (String className : args) {

                try {
                    Class testClass = Class.forName(className);

                    if (Modifier.isAbstract(testClass.getModifiers())) {
                        log("Skip class " + testClass.getName() + " because it is abstract.");
                        continue;
                    } else if (!isJUnitTest(testClass, skip_Junit_3)) {
                        log("Skip class " + testClass.getName() + " because it is not a recognized JUnit test.");
                        continue;
                    } else {
                        log("Preparing test class " + testClass.getName());
                        for (Method method : testClass.getDeclaredMethods()) {
                            Test methodAnnotation = method.<Test>getAnnotation(Test.class);

                            if (methodAnnotation != null) {
                                // The method is a JUnit 4 test method
                                testMethods++;

                                if (timeout > 0 && methodAnnotation.timeout() < timeout) {
                                    // Commenting this cose will trigger an WARNING about mockito and illegal
                                    // accesses
                                    changeAnnotationValue((Annotation) methodAnnotation, "timeout",
                                            Long.valueOf(timeout));
                                    log("   Update Timeout from " + methodAnnotation.timeout() + " to "
                                            + method.getName());
                                }

                            } else {
                                // Alessio Is this safe?
                                log("   Skip NON test method " + method.getName());
                            }
                        }

                        argumentsAsList.add(className);
                        testClasses++;

                        // Add the testing request
                        requests.add(Request.aClass(testClass));
                    }

                } catch (Throwable e) {
                    log("Error instrumenting " + className + " - " + e.getMessage());
                    e.printStackTrace(System.out);
                }
            }

            log("Summary:\n\tTest classes : " + testClasses + "\n\tTest methods: " + testMethods);
//            JUnitCommandLineParseResult jUnitCommandLineParseResult = JUnitCommandLineParseResult
//                    .parse(argumentsAsList.toArray(new String[] {}));
//            requests.add(jUnitCommandLineParseResult.createRequest(JUnitCore.defaultComputer()));
        }

        junit.addListener((RunListener) new ParsableTextListener(outputStream));
        if (System.getProperty("remote.junit.debug") != null) {
            junit.addListener((RunListener) new ParsableTextListener(debugStream));
            junit.addListener((RunListener) new SuppressingOutputTextListener(debugStream));
        }

        try {
            log("Starting remote test executions. ");
            long globalStartTime = System.currentTimeMillis();

            boolean noFail = true;
            int runCount = 0;
            int failCount = 0;
            int ignoreCount = 0;

            for (Request request : requests) {
                long localStartTime = System.currentTimeMillis();

                log("Starting request : " + request);
                Result result = junit.run(request);

                long localEndTime = System.currentTimeMillis();

                long elapsed = (localEndTime - localStartTime);

                int seconds = (int) (elapsed / 1000) % 60;
                int minutes = (int) ((elapsed / (1000 * 60)) % 60);
                int hours = (int) ((elapsed / (1000 * 60 * 60)) % 24);

                log("Summary of test class execution: Duration : "
                        + String.format("%02d hour %02d min %02d sec", hours, minutes, seconds) + " Test Executed: "
                        + result.getRunCount() + " Test Failed: " + result.getFailureCount() + " Test Ignored: "
                        + result.getIgnoreCount() + "\n\n\n");

                runCount = runCount + result.getRunCount();
                failCount = failCount + result.getFailureCount();
                ignoreCount = ignoreCount + result.getIgnoreCount();

                noFail = noFail && result.wasSuccessful();
            }

            long globalEndTime = System.currentTimeMillis();
            long elapsed = (globalEndTime - globalStartTime);

            int seconds = (int) (elapsed / 1000) % 60;
            int minutes = (int) ((elapsed / (1000 * 60)) % 60);
            int hours = (int) ((elapsed / (1000 * 60 * 60)) % 24);

            log("Summary of execution: Duration : "
                    + String.format("%02d hour %02d min %02d sec", hours, minutes, seconds) + " Test Executed: "
                    + runCount + " Test Failed: " + failCount + " Test Ignored: " + ignoreCount + "\n\n\n");

            System.exit(noFail ? 0 : 1);
        } catch (Throwable e) {
            log("Failed execution - " + e.getMessage());
            e.printStackTrace(System.out);
            System.exit(123);
        }
    }

    private static boolean isJUnitTest(Class testClass, boolean skipJunit3) {
        // TODO Auto-generated method stub
        if (TestCase.class.isAssignableFrom(testClass)) {
            if (!skipJunit3) {
                log(testClass + " is a JUnit 3 Test Class");
                return true;
            } else {
                log(testClass + " is a JUnit 3 Test Class. But " + REMOTE_JUNIT_SKIP_JUNIT_3 + " is ON");
                return false;
            }
        } else if (TestSuite.class.isAssignableFrom(testClass)) {
            if (!skipJunit3) {
                log(testClass + " is a JUnit 3 Test Suite");
                return true;
            } else {
                log(testClass + " is a JUnit 3 Test Suite. But " + REMOTE_JUNIT_SKIP_JUNIT_3 + " is ON");
                return false;
            }
        } else {
            for (Method m : testClass.getMethods()) {
                if (m.isAnnotationPresent(Test.class)) {
                    return true;
                }
            }
        }
        log(testClass + " contains no methods annotated with @Test");

        return false;
    }
}
