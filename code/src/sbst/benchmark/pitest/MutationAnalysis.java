package sbst.benchmark.pitest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.pitest.mutationtest.engine.MutationDetails;
import org.pitest.mutationtest.engine.MutationIdentifier;
import sbst.benchmark.Main;
import sbst.benchmark.coverage.JacocoResult;

public class MutationAnalysis {
    private MutationSet generatedMutants;
    private MutationSet uncoveredMutants;
    private MutationSet coveredMutants;
    private List<MutationIdentifier> ignoredMutants;
    private Map<MutationIdentifier, Boolean> killData;
    private Map<MutationIdentifier, List<TestInfo>> coveringTests;

    public MutationAnalysis(MutationSet set, Optional<JacocoResult> result) {
        this.generatedMutants = set;

        Set<Integer> coveredLines = result.isPresent() ? ((JacocoResult) result.get()).getCoveredLines()
                : new HashSet<>();

//        Main.debug("Matching Jacoco with PIT: find covered mutations");
        this.coveredMutants = new MutationSet();
        this.uncoveredMutants = new MutationSet();
//        
//        for (MutationIdentifier id : this.generatedMutants.getMutationIDs()) {
//            MutationDetails detail = this.generatedMutants.getMutantionDetails(id);
//            if (coveredLines.contains(Integer.valueOf(detail.getClassLine().getLineNumber()))) {
//                this.coveredMutants.addMutant(id, this.generatedMutants.getMutantion(id), this.generatedMutants.getMutantionDetails(id));
//                continue;
//            }
//            this.uncoveredMutants.addMutant(id, this.generatedMutants.getMutantion(id), this.generatedMutants.getMutantionDetails(id));
//        }

        Main.info("--- FORCE EXECUTION OF TESTS AND MUTANTS");
        for (MutationIdentifier id : this.generatedMutants.getMutationIDs()) {
            MutationDetails detail = this.generatedMutants.getMutantionDetails(id);
            this.coveredMutants.addMutant(id, this.generatedMutants.getMutantion(id),
                    this.generatedMutants.getMutantionDetails(id));
        }

        this.ignoredMutants = new ArrayList<>();

        this.killData = new HashMap<>();
        this.coveringTests = new HashMap<>();

        Main.debug(String.format("Mutants/coverage summary: covered %d, uncovered %d, ignored %d",
                new Object[] { Integer.valueOf(this.coveredMutants.getNumberOfMutations()),
                        Integer.valueOf(this.uncoveredMutants.getNumberOfMutations()),
                        Integer.valueOf(this.ignoredMutants.size()) }));
    }

    public void addKilledMutant(MutationDetails mutant, TestInfo info) {
        Main.info("* Killed mutant " + mutant);
        this.killData.put(mutant.getId(), Boolean.valueOf(true));
        if (this.coveringTests.containsKey(mutant.getId())) {

            ((List<TestInfo>) this.coveringTests.get(mutant.getId())).add(info);
        } else {

            List<TestInfo> list = new ArrayList<>();
            list.add(info);
            this.coveringTests.put(mutant.getId(), list);
        }
    }

    public void addIgnoreMutant(MutationDetails mutant) {
        Main.info("* Ignored mutant " + mutant);
        this.ignoredMutants.add(mutant.getId());
    }

    public void addAliveMutant(MutationDetails mutant) {
        Main.info("* Alived mutant " + mutant);
        Boolean killed = this.killData.get(mutant.getId());
        if (killed == null || !killed.booleanValue())
            this.killData.put(mutant.getId(), Boolean.valueOf(false));
    }

    public int getNumberOfMutations() {
        return this.generatedMutants.getNumberOfMutations();
    }

    public int numberOfKilledMutation() {
        int count = 0;
        for (MutationIdentifier id : this.generatedMutants.getMutationIDs()) {
            if (this.killData.containsKey(id) && ((Boolean) this.killData.get(id)).booleanValue()) {
                count++;
            }
        }
        return count;
    }

    public int numberOfIgnoredMutation() {
        return this.ignoredMutants.size();
    }

    public int numberOfUncoveredMutation() {
        return this.uncoveredMutants.getNumberOfMutations();
    }

    public int numberOfALiveMutation() {
        int count = 0;
        for (MutationIdentifier id : this.coveredMutants.getMutationIDs()) {
            if (!((Boolean) this.killData.get(id)).booleanValue())
                count++;
        }
        return count;
    }

    public void printCoverageInfo() {
        System.out.println(toString());
    }

    public String toString() {
        String info = "N. of generated mutants " + getNumberOfMutations() + "\n";
        info = info + "N. of covered mutants " + getNumberOfCoveredMutants() + "\n";
        info = info + "N. of killed mutants " + numberOfKilledMutation() + "\n";
        info = info + "N. of ignored mutants " + numberOfIgnoredMutation() + "\n";
        String mutation_list = "";
        String test_info = "";
        int count = 0;
        for (MutationIdentifier id : this.coveringTests.keySet()) {
            mutation_list = mutation_list + "Mutant: " + count + ", killed by tests " + this.coveringTests.get(id)
                    + "\n";

            test_info = test_info + "Mutant: " + count + "\t ---> " + this.generatedMutants.getMutantionDetails(id)
                    + "\n";

            count++;
        }
        info = info + "\n\n --- Tests killing mutants --- \n ";
        info = info + test_info;
        info = info + "\n\n --- Mutants details--- \n ";
        info = info + mutation_list;
        return info;
    }

    public boolean isMutantKilled(MutationIdentifier id) {
        if (this.killData.containsKey(id)) {
            return ((Boolean) this.killData.get(id)).booleanValue();
        }
        return false;
    }

    public void deleteFlakyTest(Set<TestInfo> flaky) {
        for (MutationIdentifier id : this.coveringTests.keySet()) {
            List<TestInfo> killingTests = this.coveringTests.get(id);

            for (TestInfo flaky_test : flaky) {
                if (killingTests.contains(flaky_test)) {
                    killingTests.remove(flaky_test);
                }
            }
            if (killingTests.size() == 0) {

                MutationDetails detail = this.generatedMutants.getMutantionDetails(id);
                addAliveMutant(detail);
            }
        }
    }

    public MutationSet getCoveredMutants() {
        return this.coveredMutants;
    }

    public int getNumberOfCoveredMutants() {
        return this.coveredMutants.getNumberOfMutations();
    }
}
