package sbst.benchmark.pitest;

import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import org.pitest.classinfo.ClassByteArraySource;
import org.pitest.classinfo.ClassName;
import org.pitest.classpath.ClassloaderByteArraySource;
import org.pitest.coverage.CoverageGenerator;
import org.pitest.coverage.TestInfo;
import org.pitest.functional.SideEffect1;
import org.pitest.mutationtest.EngineArguments;
import org.pitest.mutationtest.HistoryStore;
import org.pitest.mutationtest.MutationConfig;
import org.pitest.mutationtest.MutationResultListenerFactory;
import org.pitest.mutationtest.build.MutationInterceptor;
import org.pitest.mutationtest.build.MutationSource;
import org.pitest.mutationtest.build.TestPrioritiser;
import org.pitest.mutationtest.commandline.OptionsParser;
import org.pitest.mutationtest.commandline.ParseResult;
import org.pitest.mutationtest.commandline.PluginFilter;
import org.pitest.mutationtest.config.PluginServices;
import org.pitest.mutationtest.config.ReportOptions;
import org.pitest.mutationtest.config.SettingsFactory;
import org.pitest.mutationtest.engine.Mutater;
import org.pitest.mutationtest.engine.MutationDetails;
import org.pitest.mutationtest.engine.MutationEngine;
import org.pitest.mutationtest.engine.MutationIdentifier;
import org.pitest.mutationtest.tooling.MutationStrategies;
import org.pitest.plugin.Feature;
import org.pitest.plugin.FeatureParameter;
import org.pitest.util.Log;
import org.pitest.util.ResultOutputStrategy;

import edu.emory.mathcs.backport.java.util.Collections;
import sbst.benchmark.Main;
import sbst.benchmark.coverage.TestUtil;

public class PITWrapper {
    private MutationSet generatedMutants;

    private SideEffect1<Feature> asInfo(final String leader) {
        return a -> {
            Log.getLogger().info(String.format("%1$-16s", leader + a.name()) + a.description());
            for (final FeatureParameter each : a.params()) {
                Log.getLogger().info(String.format("%1$-18s", "  [" + each.name() + "]") + each.description());
            }
        };
    }

    public PITWrapper(String pClassPath, String pClassToMutate, List<String> pTargetTest) {

        String testList = pTargetTest.get(0);
        for (int index = 1; index < pTargetTest.size(); index++) {
            testList = testList + "," + (String) pTargetTest.get(index);
        }

        // Diverse inputs jere
        String[] args4Pit = { "--classPath", pClassPath, "--targetClasses", pClassToMutate, "--targetTests", testList };

        final PluginServices plugins = PluginServices.makeForContextLoader();
//        PluginServices plugins = new PluginServices(getClass().getClassLoader());

        OptionsParser parser = new OptionsParser((Predicate) new PluginFilter(plugins));
        ParseResult pr = parser.parse(args4Pit);
        ReportOptions options = pr.getOptions();

        //
        // TODO: For some reason, in commons-io and a few other projects, the line
        // numbers/blocks/indexes reported by PIT ran as below, do not match the ones in
        // the XML and the ones reported by JaCoCo
        // The suspect is that PITest changes the byte to enable some mutations: negated
        // conditions, removal of something
        final SettingsFactory settings = new SettingsFactory(options, plugins);
//        Log.getLogger().info("---------------------------------------------------------------------------");
//        Log.getLogger().info("Enabled (+) and disabled (-) features.");
//        Log.getLogger().info("-----------------------------------------");
//        settings.describeFeatures(asInfo("+"), asInfo("-"));
//        Log.getLogger().info("---------------------------------------------------------------------------");

        final HistoryStore history = null;
        final CoverageGenerator coverage = null;
        final MutationResultListenerFactory listenerFactory = null;
        final ResultOutputStrategy output = null;

        // This is taken from PITest command line
        final MutationStrategies strategies = new MutationStrategies(settings.createEngine(), history, coverage,
                listenerFactory, output);

        final EngineArguments args = EngineArguments.arguments().withExcludedMethods(options.getExcludedMethods())
                .withMutators(options.getMutators());
        final MutationEngine engine = strategies.factory().createEngine(args);

        int count = 0;

        try {
            URL[] urls = TestUtil.createURLs(pClassPath);
            URLClassLoader cl = URLClassLoader.newInstance(urls, getClass().getClassLoader());

            ClassloaderByteArraySource classloaderByteArraySource = new ClassloaderByteArraySource(cl);

            Mutater mutater = engine.createMutator((ClassByteArraySource) classloaderByteArraySource);

            ClassName classToMutate = ClassName.fromString(pClassToMutate);

            final MutationConfig mutationConfig = new MutationConfig(engine, null); // strategies.coverage().getLaunchOptions());

            // Alessio: This seems to be the culprit - Of course, if this is used we cannot
            // get the right coverage of mutants
            final MutationInterceptor interceptor = settings.getInterceptor().createInterceptor(options,
                    classloaderByteArraySource);

            MutationSource ms = new MutationSource(mutationConfig, new TestPrioritiser() {

                @Override
                public List<TestInfo> assignTests(MutationDetails mutation) {
                    // Execute NO test at this point...
                    return Collections.emptyList();
                }
            }, classloaderByteArraySource, interceptor);

//            List<MutationDetails> list = mutater.findMutations(classToMutate);
//            Main.info("Original Mutations " + list.size() );
            // The interceptor is the keypoint to get the same number of mutants!
            List<MutationDetails> list = new ArrayList(ms.createMutations(ClassName.fromString(pClassToMutate)));
            Main.info("Generated Mutations " + list.size());

            this.generatedMutants = new MutationSet();

            for (MutationDetails mutantDetail : list) {
                count++;
                MutationIdentifier id = mutantDetail.getId();
                this.generatedMutants.addMutant(id, mutater.getMutation(id), mutantDetail);
            }

            cl.close();
        } catch (

        IOException e) {
            Main.info("Exception while generating mutants " + count + " for " + pClassToMutate);
            e.printStackTrace();
        } catch (Exception e) {
            Main.info("Exception while generating mutants " + count + " for " + pClassToMutate);
            e.printStackTrace();
        } finally {
            Main.info("Generated Mutants: " + this.generatedMutants.getNumberOfMutations() + " for " + pClassToMutate);
        }

    }

    public MutationSet getGeneratedMutants() {
        return this.generatedMutants;
    }
}
