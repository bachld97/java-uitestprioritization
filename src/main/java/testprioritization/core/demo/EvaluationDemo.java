package testprioritization.core.demo;

import testprioritization.core.*;
import testprioritization.core.katalonstudio.KatalonPathMapper;
import testprioritization.core.katalonstudio.KatalonReportsParser;
import testprioritization.core.katalonstudio.KatalonScriptParser;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


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

    public EvaluationDemo(String projectBasePath) {
        this.projectBasePath = projectBasePath;
        scriptsPath = pathMapper.getScriptPathFromBasePath(projectBasePath);
        reportsPath = pathMapper.getReportsPathFromBasePath(projectBasePath);
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
        TestSuite lastExecutedSuiteFromScript = scriptParser.readTestSuiteFromInputAt(scriptsPath);
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
