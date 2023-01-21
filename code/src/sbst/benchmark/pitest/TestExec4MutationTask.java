/*     */
package sbst.benchmark.pitest;

/*     */
/*     */ import java.io.IOException;
/*     */ import java.io.PrintStream;
/*     */ import java.net.MalformedURLException;
/*     */ import java.net.URL;
/*     */ import java.net.URLClassLoader;
/*     */ import java.util.ArrayList;
/*     */ import java.util.List;
/*     */ import java.util.Set;
/*     */ import java.util.concurrent.Callable;
/*     */ import org.junit.internal.TextListener;
/*     */ import org.junit.runner.Description;
/*     */ import org.junit.runner.JUnitCore;
/*     */ import org.junit.runner.Result;
/*     */ import org.junit.runner.notification.Failure;
/*     */ import org.junit.runner.notification.RunListener;
/*     */ import org.pitest.mutationtest.engine.MutationIdentifier;
/*     */ import sbst.benchmark.Main;
/*     */ import sbst.benchmark.coverage.TestUtil;

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
/*     */
/*     */ public class TestExec4MutationTask/*     */ implements Callable<MutationResults>
/*     */ {
    /*     */ String cp;
    /*     */ URL[] urls;
    /*     */ List<String> testClasses;
    /*     */ MutationResults results;
    /*     */ Set<TestInfo> flakyTests;

    /*     */
    /*     */ public TestExec4MutationTask(String cp, List<String> pTestClasses, Set<TestInfo> pFlakyTests,
            MutationIdentifier id) {
        /*     */ try {
            /* 49 */ this.cp = cp;
            /* 50 */ this.urls = TestUtil.createURLs(cp);
            /* 51 */ this.testClasses = pTestClasses;
            /* 52 */ this.flakyTests = pFlakyTests;
            /* 53 */ this.results = new MutationResults(new ArrayList<>(), id);
            /* 54 */ } catch (MalformedURLException e) {
            /*     */
            /* 56 */ e.printStackTrace();
            /*     */ }
        /*     */ }

    /*     */
    /*     */ MutationResults processTestResults(Result result) {
        /* 61 */ if (result.getFailures().size() > 0) {
            /* 62 */ for (Failure fail : result.getFailures()) {
                /* 63 */ String header = fail.getTestHeader();
                /* 64 */ if (header.contains("(")) {
                    /* 65 */ String testMethod = header.substring(0, header.indexOf('('));
                    /* 66 */ String tc = header.substring(header.indexOf('(') + 1, header.length());
                    /* 67 */ TestInfo info = new TestInfo(tc, testMethod);
                    /* 68 */ if (!this.flakyTests.contains(info) &&
                    /* 69 */ !fail.getTrace().contains("java.lang.Exception: test timed out after")) {
                        /* 70 */ Main.debug("TestExec4MutationTask: Mutant " + this.results.getMutation_id()
                                + " killed by test " + info);
                        /*     */
                        /* 72 */ this.results.setState(MutationResults.State.KILLED);
                        /* 73 */ return this.results;
                        /*     */ }
                    /*     */ }
                /*     */ }
            /*     */
            /*     */
            /* 79 */ if (result.getRunCount() == result.getFailureCount()) {
                /* 80 */ Main.debug("TestExec4MutationTask: All the executed tests triggered timeout. Ignore mutant: "
                        + this.results/* 81 */ .getMutation_id().hashCode());
                /* 82 */ Main.debug("=======================================");
                /* 83 */ Main.debug("Ignored mutant " + this.results.getMutation_id());
                /* 84 */ Main.debug("CP " + this.cp);
                /* 85 */ Main.debug("=======================================");
                /* 86 */ this.results.setState(MutationResults.State.IGNORED);
                /* 87 */ return this.results;
                /*     */ }
            /*     */
            /* 90 */ Main.debug("TestExec4MutationTask: Only some tests triggered the timeout. Kill the mutant: "
                    + this.results/* 91 */ .getMutation_id().hashCode());
            /* 92 */ this.results.setState(MutationResults.State.KILLED);
            /* 93 */ return this.results;
            /*     */ }
        /*     */
        /*     */
        /*     */
        /*     */
        /*     */
        /* 100 */ this.results.setState(MutationResults.State.SURVIVED);
        /* 101 */ return this.results;
        /*     */ }

    /*     */
    /*     */
    /*     */
    /*     */
    /*     */ public MutationResults call() throws ClassNotFoundException, IOException {
        /* 108 */ try (URLClassLoader cl = URLClassLoader.newInstance(this.urls, getClass().getClassLoader())) {
            /*     */
            /*     */
            /* 111 */ List<Class> actualTestClasses = new ArrayList<>();
            /* 112 */ for (String test : this.testClasses) {
                /*     */
                /* 114 */ if (test.contains("_scaffolding")) {
                    /*     */ continue;
                    /*     */ }
                /*     */
                /*     */
                /*     */
                /* 120 */ if (test.startsWith("testcases.")) {
                    /* 121 */ test = test.replaceFirst("testcases.", "");
                    /*     */ }
                /*     */
                /*     */
                /* 125 */ Class testClass = cl.loadClass(test);
                /* 126 */ actualTestClasses.add(testClass);
                /*     */ }
            /*     */
            /* 129 */ Main.info("Evaluating mutant " + this.results/* 130 */ .getMutation_id().hashCode()
                    + " using tests: " + actualTestClasses);
            /* 131 */ Main.info("\n CP : " + this.cp + "\n");
            /*     */
            /*     */
            /*     */
            /*     */
            /*     */
            /*     */
            /*     */ try {
                /* 139 */ JUnitCore junit = new JUnitCore();
                /*     */
                /* 141 */ junit.addListener((RunListener) new TextListener(System.out)
                /*     */ {
                    /*     */ boolean killed = false;

                    /*     */
                    /*     */
                    /*     */ public void testStarted(Description description) {
                        /* 147 */ this.killed = false;
                        /*     */ }

                    /*     */
                    /*     */
                    /*     */
                    /*     */ public void testFailure(Failure failure) {
                        /* 153 */ TestExec4MutationTask.this.results.setState(MutationResults.State.KILLED);
                        /* 154 */ this.killed = true;
                        /*     */ }

                    /*     */
                    /*     */
                    /*     */
                    /*     */ public void testFinished(Description description) throws Exception {
                        /* 160 */ System.out.println("KILL-MATRIX:" + description.getDisplayName()
                                + (this.killed ? " KILLED " : " MISSED ") + "Mutant "
                                + TestExec4MutationTask.this.results/* 161 */ .getMutation_id().hashCode());
                        /*     */ }

                    /*     */
                    /*     */
                    /*     */
                    /*     */
                    /*     */
                    /*     */ public void testRunFinished(Result result) {
                    }

                    /*     */
                    /*     */
                    /*     */
                    /*     */
                    /*     */ public void testSuiteFinished(Description description) throws Exception {
                    }
                    /*     */ });
                /* 175 */ junit.run((Class[]) actualTestClasses.toArray(new Class[0]));
                /*     */ }
            /* 177 */ catch (SecurityException ex) {
                /*     */
                /*     */
                /* 180 */ Main.info(
                        "Was unable to close the URLClassLoader after execution of the tests due to security policy!");
                /*     */
                /* 182 */ Main.info("Will continue execution");
                /* 183 */ } catch (Throwable e) {
                /* 184 */ Main.info("ERROR Failed Evaluating mutant " + this.results.getMutation_id().hashCode()
                        + " using tests: " + this.testClasses);
                /*     */
                /* 186 */ e.printStackTrace();
                /* 187 */ this.results.setState(MutationResults.State.IGNORED);
                /*     */ }
            /*     */
            /* 190 */ return this.results;
            /*     */ }
        /*     */ }

    /*     */
    /*     */
    /*     */
    /*     */
    /*     */
    /*     */
    /*     */ public int countFailingTests() {
        /* 200 */ int count = 0;
        /*     */
        /* 202 */ for (Result result : this.results.getJUnitResults()) {
            /* 203 */ count += result.getFailureCount();
            /*     */ }
        /* 205 */ return count;
        /*     */ }

    /*     */
    /*     */ public List<Result> getExecutionResults() {
        /* 209 */ return this.results.getJUnitResults();
        /*     */ }
    /*     */ }

/*
 * Location:
 * /Users/gambi/killmatrix/repo/sbstcontest/benchmarktool/1.0.0/benchmarktool-1.
 * 0.0.jar!/sbst/benchmark/pitest/TestExec4MutationTask.class Java compiler
 * version: 8 (52.0) JD-Core Version: 1.1.3
 */