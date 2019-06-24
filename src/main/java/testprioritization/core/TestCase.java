package testprioritization.core;

import java.util.List;

public class TestCase {
    private final String id;
    private List<TestStep> testSteps;

    public TestCase(String id, List<TestStep> testSteps) {
        this.id = id;
        this.testSteps = testSteps;
    }
}
