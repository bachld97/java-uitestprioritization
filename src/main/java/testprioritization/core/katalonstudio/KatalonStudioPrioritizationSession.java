package testprioritization.core.katalonstudio;

import testprioritization.core.*;

import java.util.List;

// For use in real project
public class KatalonStudioPrioritizationSession implements PrioritizationSession {

    private KatalonScriptParser scriptParser = new KatalonScriptParser();
    private KatalonReportsParser reportsParser = new KatalonReportsParser();

    private RankingAlgorithm rankingAlgorithm;

    public KatalonStudioPrioritizationSession(
        CommandGraphPersistence graphPersistence, RLTCPRankingAlgorithm.Options options
    ) {
        this.rankingAlgorithm = new RLTCPRankingAlgorithm(graphPersistence, options);
    }

    @Override
    public List<TestCase> prioritizeSuiteAtPath(String path) {
        TestSuite suiteUnderPrioritization = scriptParser.readTestSuiteFromInputAt(path);
        return rankingAlgorithm.rankTestCasesIn(suiteUnderPrioritization);
    }

    @Override
    public void evaluateExecutionInstanceWithReportAtPath(String path) {
        ExecutionResult executionResult = reportsParser.readExecutionInfoFromReportAt(path);
        rankingAlgorithm.onTestExecutionResult(executionResult);
    }
}
