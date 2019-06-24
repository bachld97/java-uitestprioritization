package testprioritization.core;

import java.util.Map;

public class ExecutionResult {
    private final TestSuite suiteUnderExecution;
    private Map<String, Boolean> testCaseToResultMap;

    public ExecutionResult(TestSuite suite, Map<String, Boolean> testCaseToResultMap) {
        this.suiteUnderExecution = suite;
        this.testCaseToResultMap = testCaseToResultMap;
    }

    public boolean resultForTestWith(String testId) {
        return testCaseToResultMap.getOrDefault(testId, false);
    }

    public TestSuite getSuiteUnderExecution() {
        return this.suiteUnderExecution;
    }

}
