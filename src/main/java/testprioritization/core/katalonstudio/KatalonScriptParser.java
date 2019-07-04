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
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class KatalonScriptParser {
    private final String scriptBasePath;

    public KatalonScriptParser(String scriptBasePath) {
        this.scriptBasePath = scriptBasePath;
    }

    public TestSuite readTestSuiteFromInputAt(String testSuitePath) {
        try {
            File testSuite = new File(testSuitePath);

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
            System.err.format("Cannot read test suite file '%s", testSuitePath);
            e.printStackTrace();
            return null;
        }
    }

    private List<String> convertToPaths(List<String> testCaseIds) {
        List<String> pathsForTestScripts = new ArrayList<>();

        int testCasePartLength = "Test Cases/".length();
        for (String testCaseId : testCaseIds) {
            // must trim the first part (Test Cases/)
            String testCaseName = testCaseId.substring(testCasePartLength);
            String scriptFolder = scriptBasePath + testCaseName + "/";
            String scriptFile = getGroovyScriptFullPathInFolder(scriptFolder);
            pathsForTestScripts.add(scriptFile);
        }

        return pathsForTestScripts;
    }

    private String getGroovyScriptFullPathInFolder(String folderPath) {

        List<String> scripts = null;
        try {
            scripts = Files.walk(Paths.get(folderPath)).filter(
                Files::isRegularFile
            ).filter(
                path -> path.toString().endsWith(".groovy")
            ).map(
                Path::toString
            ).collect(Collectors.toList());
        } catch (IOException e) {
            e.printStackTrace();
            return "error";
        }

        if (scripts.size() == 0) {
            return "error";
        } else {
            return scripts.get(0);
        }
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
