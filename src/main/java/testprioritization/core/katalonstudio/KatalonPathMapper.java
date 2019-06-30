package testprioritization.core.katalonstudio;

public class KatalonPathMapper {

    public String getScriptPathFromBasePath(String projectBasePath) {
        StringBuilder pathBuilder = new StringBuilder();
        pathBuilder.append(projectBasePath);
        if (! projectBasePath.endsWith("/")) {
            pathBuilder.append("/");
        }
        pathBuilder.append("Scripts/");

        return pathBuilder.toString();
    }

    public String getReportsPathFromBasePath(String projectBasePath) {
        StringBuilder pathBuilder = new StringBuilder();
        pathBuilder.append(projectBasePath);
        if (! projectBasePath.endsWith("/")) {
            pathBuilder.append("/");
        }
        pathBuilder.append("Reports/");

        return pathBuilder.toString();
    }
}
