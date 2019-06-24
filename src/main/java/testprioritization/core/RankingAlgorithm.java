package testprioritization.core;

import java.util.List;

public interface RankingAlgorithm {
    public List<TestCase> rankTestCasesIn(TestSuite suite);
    public void onTestExecutionResult(ExecutionResult result);
}
