package testprioritization.core;

import java.util.ArrayList;
import java.util.Map;

public class ExecutionResult {
    private final TestSuite suiteUnderExecution;
    private Map<String, Boolean> testCaseToDidFailMap;
    private Map<String, String> testCaseToFailingTestStepCommandMap;

    public static ExecutionResult empty() {
        TestSuite noneSuite = new TestSuite("NoneSuite", new ArrayList<>());
        Map<String, Boolean> noneDidFailMap = null;
        Map<String, String> noneFailingStepMap = null;
        return new ExecutionResult(noneSuite, noneDidFailMap, noneFailingStepMap);
    }

    public ExecutionResult(
            TestSuite suite, Map<String, Boolean> testCaseToDidFailMap, Map<String, String> testCaseToFailingTestStepCommandMap
    ) {
        this.suiteUnderExecution = suite;
        this.testCaseToDidFailMap = testCaseToDidFailMap;
        this.testCaseToFailingTestStepCommandMap = testCaseToFailingTestStepCommandMap;
    }

    public boolean getDidFailForTestCaseWithId(String testCaseId) {
        // Test case which is not executed is considered false
        // Empty list indicates all failed test is not executed
        if (testCaseToDidFailMap == null) {
            return true;
        }
        return testCaseToDidFailMap.getOrDefault(testCaseId, true);
    }

    public String getFailingStepCommandForTestCaseWithId(String testCaseId) {
        // This function should always be called on failing testCaseId
        // The default is to protect the call site
        if (testCaseToFailingTestStepCommandMap == null) {
            return "None";
        }
        return testCaseToFailingTestStepCommandMap.getOrDefault(testCaseId, "None");
    }

    public int getNumberOfFailedTests() {
        if (testCaseToDidFailMap == null) {
            return 0;
        }
        return (int) testCaseToDidFailMap.values().stream().filter(Boolean::booleanValue).count();
    }


    public TestSuite getSuiteUnderExecution() {
        return this.suiteUnderExecution;
    }

}
