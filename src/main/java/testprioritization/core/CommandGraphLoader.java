package testprioritization.core;

import java.util.List;

public interface CommandGraphLoader {
    public CommandGraph loadCommandGraph();
    public List<TestCase> loadRankedTestCaseForSuite(TestSuite suite);
}
