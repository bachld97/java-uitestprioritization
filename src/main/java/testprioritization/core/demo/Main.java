package testprioritization.core.demo;

import testprioritization.core.ExecutionResult;
import testprioritization.core.katalonstudio.KatalonReportsParser;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

public class Main {
    static public void main(String[] argv) {
        KatalonReportsParser parser = new KatalonReportsParser();
        String reportPath = "C:\\Users\\bachld\\Desktop\\Code\\thesis\\SampleProject\\Reports\\All\\20190302_230028\\20190302_230028.csv";
        ExecutionResult result = parser.readOneExecutionResultFromReport(reportPath);
        System.out.println(result.getNumberOfFailedTests());
    }
}
