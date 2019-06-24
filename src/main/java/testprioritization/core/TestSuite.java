package testprioritization.core;

import java.util.List;

public class TestSuite {
    private final String id;

    private final List<TestCase> testCases;

    TestSuite(String id, List<TestCase> testCases) {
        this.id = id;
        this.testCases = testCases;
    }

    public List<TestCase> getTestCases() {
        return testCases;
    }
}
