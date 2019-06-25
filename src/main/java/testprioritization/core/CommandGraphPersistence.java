package testprioritization.core;

import java.util.List;

public interface CommandGraphPersistence {
    public void save(CommandGraph graph);
    public CommandGraph loadCommandGraph();
    public List<TestCase> loadRankedTestCaseForSuite(TestSuite suite);

    public void saveRankedTestCasesForSuite(
        List<TestCase> rankedTestCases, TestSuite containingSuite
    );
}
