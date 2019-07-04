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
        String sampleProjectPath = "/home/bachld/roadmap/thesis/SampleProject";
        EvaluationDemo demo = new EvaluationDemo(sampleProjectPath);
        demo.execute();
    }
}
