package testprioritization.core.demo;

import testprioritization.core.*;
import testprioritization.core.katalonstudio.KatalonPathMapper;
import testprioritization.core.katalonstudio.KatalonReportsParser;
import testprioritization.core.katalonstudio.KatalonScriptParser;

import java.util.*;


/*
 * Demo scenario:
 * - Training: Run prioritization on (n - 1) existing test reports [Like paper implementation]
 * - Last evaluation: Use script parser --> prioritize current test scripts
 * - - Last iteration in test report == execution for latest test script
 * - - Evaluate APFD  against the last iteration
 */
public class EvaluationDemo {
    private final KatalonScriptParser scriptParser = new KatalonScriptParser();
    private final KatalonReportsParser reportsParser = new KatalonReportsParser();

    private final String projectBasePath;

    private final KatalonPathMapper pathMapper = new KatalonPathMapper();

    private final String scriptsPath;
    private final String reportsPath;
    private final String testSuitePath;

    public EvaluationDemo(String projectBasePath) {
        this.projectBasePath = projectBasePath;
        scriptsPath = pathMapper.getScriptPathFromBasePath(projectBasePath);
        reportsPath = pathMapper.getReportsPathFromBasePath(projectBasePath);
        testSuitePath = pathMapper.getTestSuiteFromBasePath(projectBasePath);
    }


    private List<RankingAlgorithm> rankingAlgorithms = Arrays.asList(
        new RLTCPRankingAlgorithm(
            new DemoGraphPersistence(),
            new RLTCPRankingAlgorithm.Options(1.0f, 1.0f)
        ),
        new RLTCPRankingAlgorithm(
                new DemoGraphPersistence(),
                new RLTCPRankingAlgorithm.Options(1.0f, 0.8f)
        ),
        new RLTCPRankingAlgorithm(
                new DemoGraphPersistence(),
                new RLTCPRankingAlgorithm.Options(1.0f, 0.6f)
        ),
        new RLTCPRankingAlgorithm(
                new DemoGraphPersistence(),
                new RLTCPRankingAlgorithm.Options(0.8f, 1.0f)
        ),
        new RLTCPRankingAlgorithm(
                new DemoGraphPersistence(),
                new RLTCPRankingAlgorithm.Options(0.8f, 0.8f)
        ),
        new RLTCPRankingAlgorithm(
                new DemoGraphPersistence(),
                new RLTCPRankingAlgorithm.Options(0.8f, 0.6f)
        ),
        new RLTCPRankingAlgorithm(
                new DemoGraphPersistence(),
                new RLTCPRankingAlgorithm.Options(0.6f, 1.0f)
        ),
        new RLTCPRankingAlgorithm(
                new DemoGraphPersistence(),
                new RLTCPRankingAlgorithm.Options(0.6f, 1.0f)
        ),
        new RLTCPRankingAlgorithm(
                new DemoGraphPersistence(),
                new RLTCPRankingAlgorithm.Options(0.6f, 1.0f)
        )
    );


    public void execute() {
        // Read all reports for all previous run
        // Read script for current iteration
        // For each method
        // .... For each report (until the last one)
        // ........ Perform prioritization on report
        // .... Perform prioritization on script
        // .... Perform APFD evaluation for this method

        List<ExecutionResult> executionResults = reportsParser.readAllExecutionResultsFromReportsAt(reportsPath);
        TestSuite lastExecutedSuiteFromScript = scriptParser.readTestSuiteFromInputAt(testSuitePath);

        for (RankingAlgorithm rankingAlgorithm : rankingAlgorithms) {
            rankHistoricalExecutions(rankingAlgorithm, executionResults);
            List<TestCase> lastOrder = rankingAlgorithm.rankTestCasesIn(lastExecutedSuiteFromScript);
            ExecutionResult lastResult = executionResults.get(executionResults.size() - 1);
            float apfd = calculateAPFD(lastOrder, lastResult);
            System.out.println(rankingAlgorithm.getClass().toString() + ": " + apfd);
        }
    }

    private List<List<TestCase>> rankHistoricalExecutions(RankingAlgorithm rankingAlgorithm, List<ExecutionResult> executionResults) {
        List<List<TestCase>> historicalOrders = new ArrayList<>();
        for (ExecutionResult result : executionResults) { // n - 1 ??
            List<TestCase> order = rankingAlgorithm.rankTestCasesIn(result.getSuiteUnderExecution());
            rankingAlgorithm.onTestExecutionResult(result);
            historicalOrders.add(order);
        }

        return historicalOrders;
    }

    private List<Float> calculatePercentageOfFaultsFoundPerTestCase(List<TestCase> order, ExecutionResult result) {
        float numberOfFails = result.getNumberOfFailedTests();
        float numberOfFailsFound = 0;
        int numberOfTestCases = order.size();
        List<Float> results = new ArrayList<>();

        if (numberOfFails == 0) {
            for (int index = 0; index < numberOfTestCases; index++) {
                results.add(100f);
            }
        }

        for (int index = 0; index < numberOfTestCases; index++) {
            String testId = order.get(index).getId();
            if (result.getDidFailForTestCaseWithId(testId)) {
                numberOfFailsFound += 1;
            }

            results.add(numberOfFailsFound / numberOfFails * 100f);
        }

        return results;
    }

    public Float calculateAPFD(List<TestCase> order, ExecutionResult result) {
        float numberOfFails = result.getNumberOfFailedTests();
        float tfSum = 0;
        int numberOfTestCases = order.size();

        if (numberOfFails == 0) {
            return 100f;
        }

        for (int index = 0; index < numberOfTestCases; index++) {
            String testId = order.get(index).getId();
            if (result.getDidFailForTestCaseWithId(testId)) {
                tfSum += index + 1;
            }
        }

        float nm = numberOfFails * numberOfTestCases;
        return (1 - tfSum / nm + 1 / (2 * numberOfTestCases)) * 100;
    }

    private class DemoGraphPersistence implements CommandGraphPersistence {


        private Map<String, List<TestCase>> suiteIdToRankedTestCasesMap = new HashMap<>();

        @Override
        public void save(CommandGraph graph) {
            // Ignore
        }

        @Override
        public CommandGraph loadCommandGraph() {
            return CommandGraph.empty();
        }

        @Override
        public void saveRankedTestCasesForSuite(List<TestCase> rankedTestCases, TestSuite containingSuite) {
            suiteIdToRankedTestCasesMap.put(containingSuite.getId(), rankedTestCases);
        }

        @Override
        public List<TestCase> loadRankedTestCaseForSuite(TestSuite suite) {
            return suiteIdToRankedTestCasesMap.getOrDefault(suite.getId(), suite.getTestCases());
        }
    }
}
