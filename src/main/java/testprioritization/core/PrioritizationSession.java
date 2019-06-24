package testprioritization.core;

import java.util.List;

public interface PrioritizationSession {
    public List<TestCase> prioritizeSuiteAtPath(String path);

    class Builder {

    }
}
