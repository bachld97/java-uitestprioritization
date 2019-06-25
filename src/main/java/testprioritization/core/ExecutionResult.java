package testprioritization.core;

import java.util.Map;

public class ExecutionResult {
    private final TestSuite suiteUnderExecution;
    private Map<String, Boolean> testCaseToDidFailMap;
    private Map<String, String> testCaseToFailingTestStepCommandMap;

    public ExecutionResult(
            TestSuite suite, Map<String, Boolean> testCaseToDidFailMap, Map<String, String> testCaseToFailingTestStepCommandMap
    ) {
        this.suiteUnderExecution = suite;
        this.testCaseToDidFailMap = testCaseToDidFailMap;
        this.testCaseToFailingTestStepCommandMap = testCaseToFailingTestStepCommandMap;
    }

    public boolean getDidFailForTestCaseWithId(String testCaseId) {
        return testCaseToDidFailMap.getOrDefault(testCaseId, false);
    }

    public String getFailingStepCommandForTestCaseWithId(String testCaseId) {
        return testCaseToFailingTestStepCommandMap.getOrDefault(testCaseId, null);
    }

    public int getNumberOfFailedTests() {
        return (int) testCaseToDidFailMap.values().stream().filter(Boolean::booleanValue).count();
    }


    public TestSuite getSuiteUnderExecution() {
        return this.suiteUnderExecution;
    }

}
