package testprioritization.core.katalonstudio;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import testprioritization.core.TestCase;
import testprioritization.core.TestStep;
import testprioritization.core.TestSuite;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

public class KatalonScriptParser {
    public TestSuite readTestSuiteFromInputAt(String path) {
        try {
            File testSuite = new File(path);

            // use XML parser to read .ts file
            DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
            Document document = documentBuilder.parse(testSuite);

            // get test suite name
            String testSuiteName = document.getElementsByTagName("name").item(0).getTextContent();

            // get all the testCaseId tags
            NodeList testCaseIdTags = document.getElementsByTagName("testCaseId");
            List<String> rawTestCaseIds = new ArrayList<>();
            for (int index = 0; index < testCaseIdTags.getLength(); index++) {
                Node tagId = testCaseIdTags.item(index);
                // collect the raw id
                rawTestCaseIds.add(tagId.getTextContent());
            }

            // get path strings
            List<String> testScriptPaths = convertToPaths(rawTestCaseIds);

            // make test cases
            List<TestCase> testCases = new ArrayList<>();
            for (int index = 0; index < testScriptPaths.size(); index++) {
                testCases.add(readTestCaseFromScript(rawTestCaseIds.get(index), testScriptPaths.get(index)));
            }

            return new TestSuite(testSuiteName, testCases);
        } catch (Exception e) {
            System.err.format("Cannot read test suite file '%s", path);
            e.printStackTrace();
            return null;
        }
    }

    private List<String> convertToPaths(List<String> testCaseIds) {
        // help me :(
        return new ArrayList<>();
    }

    private String convertToCsvCommand(String rawCommand) {
        return rawCommand.replace("\'", "\"\"").replace("WebUI.", "");
    }

    private TestCase readTestCaseFromScript(String testCaseId, String scriptPath) {
        try {
            FileReader fileReader = new FileReader(scriptPath);
            BufferedReader bufferedReader = new BufferedReader(fileReader);

            List<TestStep> stepsInCurrentTestCase = new ArrayList<>();
            String line = bufferedReader.readLine();
            boolean notEOF = (line != null);
            while (notEOF) {
                // ignore import and empty statements
                if (!line.startsWith("import") && !line.isEmpty()) {
                    String command = convertToCsvCommand(line);
                    TestStep step = new TestStep(command);
                    stepsInCurrentTestCase.add(step);
                }

                line = bufferedReader.readLine();
                notEOF = (line != null);
            }

            return new TestCase(testCaseId, stepsInCurrentTestCase);

        } catch (Exception e) {
            System.err.format("Cannot read test case script file '%s'.", scriptPath);
            e.printStackTrace();
            return null;
        }
    }
}
