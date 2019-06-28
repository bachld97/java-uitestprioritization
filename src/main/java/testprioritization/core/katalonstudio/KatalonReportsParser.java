package testprioritization.core.katalonstudio;

import com.sun.org.apache.xpath.internal.operations.Bool;
import testprioritization.core.ExecutionResult;
import testprioritization.core.TestCase;
import testprioritization.core.TestStep;
import testprioritization.core.TestSuite;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class KatalonReportsParser {
    private static final String CSV_DELIM = ",";

    public List<ExecutionResult> readAllExecutionResultsFromReportsAt(String reportsBasePath) {
        Stream<String> reportFiles = listAllCsvInFolderAndSubFolder(reportsBasePath);
        return reportFiles.map(this::readOneExecutionResultFromReport).collect(Collectors.toList());
    }

    private Stream<String> listAllCsvInFolderAndSubFolder(String folderPath) {
        Stream<String> reportFilePaths;
        try {
            Stream<Path>  paths = Files.walk(Paths.get(folderPath));
            reportFilePaths = paths.filter(
                Files::isRegularFile
            ).filter(
                path -> path.toString().endsWith(".csv")
            ).map(
                Path::toString
            );
        } catch (IOException e) {
            reportFilePaths = Stream.empty();
        }

        return reportFilePaths;
    }

    public ExecutionResult readOneExecutionResultFromReport(String reportPath) {
        try {
            FileReader fileReader = new FileReader(reportPath);
            BufferedReader bufferedReader = new BufferedReader(fileReader);

            boolean thisRowIsHeader = true;
            boolean thisRowIsSuiteInfo = true;
            boolean thisRowIsTestCaseHeader = true;
            boolean isNotFirstEmptyRow = false;

            String suiteId = null;
            List<TestCase> testCases = new ArrayList<>();
            String currentTestCaseId = null;
            List<TestStep> stepsInCurrentTestCase = null;
            Map<String, Boolean> didFailMap = new HashMap<>();
            Map<String, String> failStepMap = new HashMap<>();

            String line = bufferedReader.readLine();
            boolean notEOF = (line != null);
            while (notEOF) {
                String[] lineAfterSplit =  line.split(CSV_DELIM);

                if (thisRowIsHeader) {
                    // Skip this row
                    thisRowIsHeader = false;
                } else if (thisRowIsSuiteInfo) {
                    thisRowIsSuiteInfo = false;
                    suiteId = line.split(CSV_DELIM)[0];
                } else if (isEmptyRow(line)) {
                    // Conclude previous test case result
                    thisRowIsTestCaseHeader = true;

                    if (isNotFirstEmptyRow) {
                        TestCase tc = new TestCase(currentTestCaseId, stepsInCurrentTestCase);
                        testCases.add(tc);
                    } else {
                        isNotFirstEmptyRow = true;
                    }
                } else if (thisRowIsTestCaseHeader) {
                    // Begin new test case result
                    thisRowIsTestCaseHeader = false;
                    currentTestCaseId = lineAfterSplit[0];
                    stepsInCurrentTestCase = new ArrayList<>();

                    String statusString = lineAfterSplit[lineAfterSplit.length - 1];
                    boolean testCaseDidFail = (
                        statusString.equalsIgnoreCase("failed") ||
                        statusString.equalsIgnoreCase("error")
                    );
                    didFailMap.put(currentTestCaseId, testCaseDidFail);
                } else {
                    // Continue expanding current test case result
                    String command = convertToCommand(lineAfterSplit[0]);
                    String statusString = lineAfterSplit[lineAfterSplit.length - 1];
                    TestStep currentStep = new TestStep(command);
                    boolean testStepDidFail = (
                        statusString.equalsIgnoreCase("failed") ||
                        statusString.equalsIgnoreCase("error")
                    );

                    if (testStepDidFail) {
                        failStepMap.put(currentTestCaseId, command);
                    }
                    stepsInCurrentTestCase.add(currentStep);
                }

                line = bufferedReader.readLine();
                notEOF = (line != null);
            }
            bufferedReader.close();

            TestSuite suiteUnderExecution = new TestSuite(suiteId, testCases);
            return new ExecutionResult(suiteUnderExecution, didFailMap, failStepMap);
        } catch (Exception e) {
            System.err.format("Cannot read report file '%s'.", reportPath);
            e.printStackTrace();
            return ExecutionResult.empty();
        }
    }

    private String convertToCommand(String rawCommandInCSV) {
        return rawCommandInCSV;
    }


    private boolean isEmptyRow(String line) {
        return (
            line.isEmpty() ||
            line.split(CSV_DELIM).length == 0 ||
            line.split(CSV_DELIM)[0].isEmpty()
        );
    }

}
