package testprioritization.core;

import testprioritization.core.util.Pair;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class RLTCPRankingAlgorithm implements RankingAlgorithm {
    private final Options options;
    private final CommandGraphPersistence graphPersistence;

    private final Stopper stopper = new Stopper();

    private CommandGraph currentGraph = null;

    public RLTCPRankingAlgorithm(
        CommandGraphPersistence graphPersistence
    ) {
        this.graphPersistence = graphPersistence;
        this.options = new Options();
    }

    public RLTCPRankingAlgorithm(
        CommandGraphPersistence graphPersistence,
        Options options
    ) {
        this.graphPersistence = graphPersistence;
        this.options = options;
    }

    @Override
    public String stringToDisplayApfd(float apfd) {
        return "RLTCP - " + options.toString() + " - APFD: " + apfd;
    }

    @Override
    public List<TestCase> rankTestCasesIn(TestSuite suite) {
        if (currentGraph == null) {
            currentGraph = graphPersistence.loadCommandGraph();
        }

        ArrayList<TestCaseWrapper> wrappedTestCases = new ArrayList<>();
        for (TestCase testCase : suite.getTestCases()) {
            float weight = calculateWeightFor(testCase);
            TestCaseWrapper wrapper = new TestCaseWrapper(testCase, weight);
            wrappedTestCases.add(wrapper);
        }

        Collections.sort(wrappedTestCases);
        List<TestCase> rankedTestCases = wrappedTestCases.stream()
                .map(TestCaseWrapper::getTestCase)
                .collect(Collectors.toList());

        graphPersistence.saveRankedTestCasesForSuite(rankedTestCases, suite);
        return rankedTestCases;
    }

    private float calculateWeightFor(TestCase testCase) {
        List<TestStep> steps = testCase.getTestSteps();
        float totalWeight = 0f;

        for (int toStepIndex = 1; toStepIndex < steps.size(); ++toStepIndex) {
            int fromStepIndex = toStepIndex - 1;
            TestStep fromStep = steps.get(fromStepIndex);
            TestStep toStep = steps.get(toStepIndex);
            Pair<TestStep, TestStep> testStepPair = Pair.of(fromStep, toStep);
            totalWeight += currentGraph.getWeightFor(testStepPair);
        }

        return totalWeight;
    }

    @Override
    public void onTestExecutionResult(ExecutionResult executionResult) {
        boolean firstLoop = true;
        boolean notDone = true;
        TestSuite suiteUnderExecution = executionResult.getSuiteUnderExecution();
        List<TestCase> previouslyRankedTestCases = null;
        int currentIterationCount = 0;

        while (notDone && currentIterationCount < options.maxTrainingIteration) {
            float failPenalty = (firstLoop) ? options.penaltyForFailedTestCases : 0f;
            float outOfOrderPenalty = options.penaltyForMisplacedTestCases;

            previouslyRankedTestCases = this.rankTestCasesIn(suiteUnderExecution);
            notDone = stopper.shouldContinueTraining(executionResult, previouslyRankedTestCases);

            WeightPolicy weightPolicy = new WeightPolicy(
                executionResult, previouslyRankedTestCases, failPenalty, outOfOrderPenalty, options.penaltyDecayRate
            );

            CommandGraph rewardGraph = weightPolicy.buildPenaltyCommandGraph();
            currentGraph.mergeWith(rewardGraph);

            firstLoop = false;
            currentIterationCount += 1;
        }

         graphPersistence.save(currentGraph);
    }

    public static class Options {
        public final float graphMergeDiscount;
        public final float penaltyDecayRate;
        public final int maxTrainingIteration;
        public final float penaltyForFailedTestCases;
        public final float penaltyForMisplacedTestCases;


        public Options() {
            this.graphMergeDiscount = 0.8f;
            this.penaltyDecayRate = 0.8f;
            this.maxTrainingIteration = 200;
            this.penaltyForFailedTestCases = 10f;
            this.penaltyForMisplacedTestCases = 5f;
        }

        public Options(float graphMergeDiscount, float penaltyDecayRate) {
            this.graphMergeDiscount = graphMergeDiscount;
            this.penaltyDecayRate = penaltyDecayRate;
            this.maxTrainingIteration = 200;
            this.penaltyForFailedTestCases = 10f;
            this.penaltyForMisplacedTestCases = 5f;
        }

        public Options(
            float graphMergeDiscount,
            float penaltyDecayRate,
            int maxTrainingIteration,
            float penaltyForFailedTestCases,
            float penaltyForMisplacedTestCases
        ) {
            this.graphMergeDiscount = graphMergeDiscount;
            this.penaltyDecayRate = penaltyDecayRate;
            this.maxTrainingIteration = maxTrainingIteration;
            this.penaltyForFailedTestCases = penaltyForFailedTestCases;
            this.penaltyForMisplacedTestCases = penaltyForMisplacedTestCases;
        }

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();
            builder.append(graphMergeDiscount);
            builder.append("_");
            builder.append(penaltyDecayRate);
            return builder.toString();
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
            return Float.compare(testCaseWrapper.weight, weight);
        }
    }

    private class Stopper {
        public boolean shouldContinueTraining(ExecutionResult executionResult, List<TestCase> previousOrder) {
            // If all tests pass => return false
            // If all tests fail => return false
            // return firstPass < lastFail
            boolean atLeastOneTestPass = false;
            boolean atLeastOneTestFail = false;
            boolean foundPassingTestBeforeFailingTest = false;

            for (TestCase testCase : previousOrder) {
                boolean testDidFail = executionResult.getDidFailForTestCaseWithId(testCase.getId());
                if (testDidFail) {
                    atLeastOneTestFail = true;
                    foundPassingTestBeforeFailingTest = atLeastOneTestPass;
                } else {
                    atLeastOneTestPass = true;
                }
            }
            return atLeastOneTestFail && atLeastOneTestPass && foundPassingTestBeforeFailingTest;
        }
    }

    private class WeightPolicy {

        private final ExecutionResult executionResult;
        private final List<TestCase> previouslyRankedTestCases;
        private final float failPenalty;
        private final float outOfOrderPenalty;
        private final float decayRateForTestFailPenalty;

        private WeightPolicy(
            ExecutionResult executionResult,
            List<TestCase> previouslyRankedTestCases,
            float failPenalty,
            float outOfOrderPenalty,
            float decayRateForTestFailPenalty
        ) {
            this.executionResult = executionResult;
            this.previouslyRankedTestCases = previouslyRankedTestCases;
            this.failPenalty = failPenalty;
            this.outOfOrderPenalty = outOfOrderPenalty;
            this.decayRateForTestFailPenalty = decayRateForTestFailPenalty;
        }

        public CommandGraph buildPenaltyCommandGraph() {
            // Penalty policy:
            // If testCase fails:
            // .... increase weight for contained pairs of test steps until failing step
            // .... the increment amount = failPenalty
            // else:
            // .... decrease weight for contained pairs of test steps
            // .... the decrement amount = number of failed tests after current test case * outOfOrderPenalty

            CommandGraph finalGraph = CommandGraph.empty();
            int numTestFailBefore = 0;
            int numTest = executionResult.getNumberOfFailedTests();
            for (TestCase testCase : previouslyRankedTestCases) {
                boolean testDidFail = executionResult.getDidFailForTestCaseWithId(testCase.getId());
                CommandGraph penaltyGraph;

                if (testDidFail) {
                    numTestFailBefore += 1;
                    penaltyGraph = rewardFailingTestCase(testCase);
                } else {
                    int numTestFailAfterThisTest = numTest - numTestFailBefore;
                    penaltyGraph = penalizeMisplacedTestCase(testCase, numTestFailAfterThisTest);
                }

                finalGraph = finalGraph.mergeWith(penaltyGraph);
            }

            return finalGraph;
        }

        private CommandGraph rewardFailingTestCase(TestCase testCase) {
            int indexOfFailingStep = getIndexOfFailingStep(testCase);
            List<TestStep> steps = testCase.getTestSteps();
            float actualWeight = failPenalty;

            List<CommandGraph.Edge> edges = new ArrayList<>();
            for (int stepIndex = indexOfFailingStep; stepIndex > 0; stepIndex--) {
                TestStep currentStep = steps.get(stepIndex);
                TestStep previousStep = steps.get(stepIndex - 1);
                CommandGraph.Edge newEdge = new CommandGraph.Edge(previousStep, currentStep, actualWeight);
                edges.add(newEdge);

                actualWeight *= decayRateForTestFailPenalty;
            }

            return CommandGraph.fromEdges(edges);
        }

        private int getIndexOfFailingStep(TestCase testCase) {
            List<TestStep> steps = testCase.getTestSteps();
            String testCaseId = testCase.getId();
            String failingStepCommand = executionResult.getFailingStepCommandForTestCaseWithId(testCaseId);

            int indexOfFailingStep = -1;
            for (int stepIndex = 0; stepIndex < steps.size(); stepIndex++) {
                TestStep currentStep = steps.get(stepIndex);
                String stepCommand = currentStep.getCommand();

                if (stepCommand.equals(failingStepCommand)) {
                    indexOfFailingStep = stepIndex;
                    break;
                }
            }

            if (indexOfFailingStep == -1) {
                indexOfFailingStep = steps.size() - 1;
            }

            return indexOfFailingStep;
        }

        private CommandGraph penalizeMisplacedTestCase(TestCase testCase, int numTestFailAfterThisTest) {
            // Penalty must be negative
            float penaltyAmount = numTestFailAfterThisTest * outOfOrderPenalty;
            penaltyAmount = Math.min(penaltyAmount, penaltyAmount * -1f);

            List<TestStep> steps = testCase.getTestSteps();

            List<CommandGraph.Edge> edges = new ArrayList<>();
            for (int testStepIndex = 1; testStepIndex < steps.size(); testStepIndex++) {
                TestStep currentStep = steps.get(testStepIndex);
                TestStep previousStep = steps.get(testStepIndex - 1);
                CommandGraph.Edge newEdge = new CommandGraph.Edge(previousStep, currentStep, penaltyAmount);
                edges.add(newEdge);
            }

            return CommandGraph.fromEdges(edges);
        }
    }
}
