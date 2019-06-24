package testprioritization.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class RLTCPRankingAlgorithm implements RankingAlgorithm {
    private final Options options;
    private final CommandGraphSaver saver;
    private final CommandGraphLoader loader;

    private final Stopper stopper = new Stopper();
    private final WeightPolicy weightPolicy = new WeightPolicy();
    private final Predictor predictor = new Predictor();

    private CommandGraph currentGraph = null;

    public RLTCPRankingAlgorithm(
            CommandGraphSaver saver,
            CommandGraphLoader loader
    ) {
        this.saver = saver;
        this.loader = loader;
        this.options = new Options();
    }

    public RLTCPRankingAlgorithm(
        CommandGraphSaver saver,
        CommandGraphLoader loader,
        Options options
    ) {
        this.saver = saver;
        this.loader = loader;
        this.options = options;
    }

    @Override
    public List<TestCase> rankTestCasesIn(TestSuite suite) {
        if (currentGraph == null) {
            currentGraph = loader.loadCommandGraph();
        }

        ArrayList<TestCaseWrapper> wrappedTestCases = new ArrayList<>();
        for (TestCase testCase : suite.getTestCases()) {
            // Calculate weight

            // Create wrappedTestCases
        }

        Collections.sort(wrappedTestCases);
        return wrappedTestCases.stream()
                .map(tc -> tc.testCase)
                .collect(Collectors.toList());
    }

    @Override
    public void onTestExecutionResult(ExecutionResult result) {
        boolean firstLoop = true;
        boolean notFinished = true;
        List<TestCase> previouslyRankedTestCases =
                loader.loadRankedTestCaseForSuite(result.getSuiteUnderExecution());

    }

    class Options {
        public final float graphMergeDiscount;
        public final float penaltyDecayRate;
        public final int maxTrainingIteration;

        public Options() {
            this.graphMergeDiscount = 0.8f;
            this.penaltyDecayRate = 0.8f;
            this.maxTrainingIteration = 200;
        }

        public Options(
            float graphMergeDiscount,
            float penaltyDecayRate,
            int maxTrainingIteration
        ) {
            this.graphMergeDiscount = graphMergeDiscount;
            this.penaltyDecayRate = penaltyDecayRate;
            this.maxTrainingIteration = maxTrainingIteration;
        }

    }

    private class TestCaseWrapper implements Comparable<TestCaseWrapper> {

        private final TestCase testCase;
        private final float weight;

        public TestCaseWrapper(TestCase testCase, float weight) {
            this.testCase = testCase;
            this.weight = weight;
        }

        public TestCase getTestCase() {
            return testCase;
        }

        @Override
        public int compareTo(TestCaseWrapper testCaseWrapper) {
            return (int) (weight - testCaseWrapper.weight);
        }
    }

    private class Stopper {

    }

    private class WeightPolicy {

    }

    class Predictor {

    }
}
