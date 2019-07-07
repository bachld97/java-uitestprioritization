package testprioritization.core.katalonstudio;

import testprioritization.core.ExecutionResult;
import testprioritization.core.TestCase;
import testprioritization.core.TestStep;
import testprioritization.core.TestSuite;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
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
            ).sorted();
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
                if (thisRowIsHeader) {
                    // Skip this row
                    thisRowIsHeader = false;
                } else if (thisRowIsSuiteInfo) {
                    thisRowIsSuiteInfo = false;
                    suiteId = getIdStringFrom(line);
                } else if (isEmptyRow(line)) {
                    // Conclude previous test case result
                    thisRowIsTestCaseHeader = true;

                    if (isNotFirstEmptyRow) {
                        TestCase tc = new TestCase(currentTestCaseId, stepsInCurrentTestCase);
                        testCases.add(tc);
                        currentTestCaseId = null;
                        stepsInCurrentTestCase = null;
                    } else {
                        isNotFirstEmptyRow = true;
                    }
                } else if (thisRowIsTestCaseHeader) {
                    // Begin new test case result
                    thisRowIsTestCaseHeader = false;
                    currentTestCaseId = getIdStringFrom(line);
                    stepsInCurrentTestCase = new ArrayList<>();

                    String statusString = getStatusStringFrom(line);
                    boolean testCaseDidFail = (
                        statusString.equalsIgnoreCase("failed") ||
                        statusString.equalsIgnoreCase("error")
                    );
                    didFailMap.put(currentTestCaseId, testCaseDidFail);
                } else {
                    // Continue expanding current test case result
                    String command = convertToCommand(getCommandStringFrom(line));
                    String statusString = getStatusStringFrom(line);
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

            testCases.add(new TestCase(currentTestCaseId, stepsInCurrentTestCase));

            List<TestCase> uniqueTestCases = removeDuplicatedKeepLastTestCaseForEachId(testCases);
            TestSuite suiteUnderExecution = new TestSuite(suiteId, uniqueTestCases);
            return new ExecutionResult(suiteUnderExecution, didFailMap, failStepMap);
        } catch (Exception e) {
            System.err.format("Cannot read report file '%s'.", reportPath);
            e.printStackTrace();
            return ExecutionResult.empty();
        }
    }

    private List<TestCase> removeDuplicatedKeepLastTestCaseForEachId(List<TestCase> testCases) {
        List<TestCase> uniqueTestCases = new ArrayList<>();
        List<String> includedTestCaseId = new ArrayList<>();

        for (int testCaseIndex = 0; testCaseIndex < testCases.size(); ++testCaseIndex) {
            TestCase testCase = testCases.get(testCaseIndex);
            String testCaseId = testCase.getId();
            if (includedTestCaseId.contains(testCaseId)) {
                continue;
            } else {
                includedTestCaseId.add(testCaseId);
                uniqueTestCases.add(testCase);
            }
        }

        return uniqueTestCases;
    }

    private String getIdStringFrom(String lineFromCsv) {
        return getFirstCsvElement(lineFromCsv);
    }

    private String getCommandStringFrom(String lineFromCsv) {
        return getFirstCsvElement(lineFromCsv);
    }

    private String getFirstCsvElement(String lineFromCsv) {
        char CHAR_QUOTE = '\"';
        char CHAR_COMMA = ',';

        // The lineFromCsv must at least be ""
        if (lineFromCsv.length() < 2) {
            return lineFromCsv;
        }

        int startIndex = lineFromCsv.charAt(0) == CHAR_QUOTE ? 1 : 0;
        int endIndex = startIndex;
        boolean isNumberOfQuotesBalanced = lineFromCsv.charAt(0) != CHAR_QUOTE;
        boolean notFoundCommaOutsideQuote = true;
        int lineLength = lineFromCsv.length();

        // Find first comma outside enclosing quote
        while (endIndex < lineLength && notFoundCommaOutsideQuote) {
            char currentChar = lineFromCsv.charAt(endIndex);
            if (currentChar == CHAR_QUOTE) {
                isNumberOfQuotesBalanced = ! isNumberOfQuotesBalanced;
            } else if (currentChar == CHAR_COMMA) {
                notFoundCommaOutsideQuote = ! isNumberOfQuotesBalanced;
            }

            if (notFoundCommaOutsideQuote) {
                endIndex += 1;
            }
        }

        if (lineFromCsv.charAt(endIndex - 1) == CHAR_QUOTE) {
            endIndex -= 1;
        }

        String result =  lineFromCsv.substring(startIndex, endIndex);
        return result;
    }

    // This works as expected because the status does not contains special characters
    private String getStatusStringFrom(String lineFromCsv) {
        String[] lineAfterSplit = lineFromCsv.split(CSV_DELIM);

        if (lineAfterSplit.length == 0) {
            return lineFromCsv;
        }

        return lineAfterSplit[lineAfterSplit.length - 1];
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
