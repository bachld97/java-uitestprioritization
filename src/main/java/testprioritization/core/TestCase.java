package testprioritization.core;

import java.util.List;

public class TestCase {
    private final String id;
    private final List<TestStep> testSteps;

    public TestCase(String id, List<TestStep> testSteps) {
        this.id = id;
        this.testSteps = testSteps;
    }

    public String getId() {
        return id;
    }

    public List<TestStep> getTestSteps() {
        return testSteps;
    }
}
