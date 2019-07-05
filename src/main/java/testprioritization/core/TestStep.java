package testprioritization.core;

public class TestStep {

    private final String command;

    public TestStep(String command) {
        this.command = command;
    }

    public String getCommand() {
        return command;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof TestStep) {
            return ((TestStep) obj).getCommand().equals(command);
        }
        return false;
    }
}
