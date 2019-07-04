package testprioritization.core.katalonstudio;

import testprioritization.core.*;

import java.util.List;

// For use in real project
public class KatalonStudioPrioritizationSession implements PrioritizationSession {

    private final KatalonScriptParser scriptParser;
    private final KatalonReportsParser reportsParser;

    private final RankingAlgorithm rankingAlgorithm;

    public KatalonStudioPrioritizationSession(
        CommandGraphPersistence graphPersistence, RLTCPRankingAlgorithm.Options options,
        KatalonScriptParser scriptParser, KatalonReportsParser reportsParser
    ) {
        this.rankingAlgorithm = new RLTCPRankingAlgorithm(graphPersistence, options);
        this.scriptParser = scriptParser;
        this.reportsParser = reportsParser;
    }

    @Override
    public List<TestCase> prioritizeSuiteAtPath(String path) {
        TestSuite suiteUnderPrioritization = scriptParser.readTestSuiteFromInputAt(path);
        return rankingAlgorithm.rankTestCasesIn(suiteUnderPrioritization);
    }

    @Override
    public void evaluateExecutionInstanceWithReportAtPath(String path) {
        ExecutionResult executionResult = reportsParser.readOneExecutionResultFromReport(path);
        rankingAlgorithm.onTestExecutionResult(executionResult);
    }
}
