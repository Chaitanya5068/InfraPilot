package com.infrapilot.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Service
public class RepoAnalyzer {

    private static final Logger log = LoggerFactory.getLogger(RepoAnalyzer.class);
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Yaml yaml = new Yaml();

    public static class AnalysisResult {
        private String detectedStack;
        private Map<String, Object> config;
        private List<String> readinessNotes = new ArrayList<>();
        private boolean hasDockerfile;
        private boolean hasTests;
        private boolean hasInfraPilotYml;
        private String error;

        public String getDetectedStack() { return detectedStack; }
        public void setDetectedStack(String detectedStack) { this.detectedStack = detectedStack; }

        public Map<String, Object> getConfig() { return config; }
        public void setConfig(Map<String, Object> config) { this.config = config; }

        public List<String> getReadinessNotes() { return readinessNotes; }
        public void setReadinessNotes(List<String> readinessNotes) { this.readinessNotes = readinessNotes; }

        public boolean isHasDockerfile() { return hasDockerfile; }
        public void setHasDockerfile(boolean hasDockerfile) { this.hasDockerfile = hasDockerfile; }

        public boolean isHasTests() { return hasTests; }
        public void setHasTests(boolean hasTests) { this.hasTests = hasTests; }

        public boolean isHasInfraPilotYml() { return hasInfraPilotYml; }
        public void setHasInfraPilotYml(boolean hasInfraPilotYml) { this.hasInfraPilotYml = hasInfraPilotYml; }

        public String getError() { return error; }
        public void setError(String error) { this.error = error; }
    }

    /**
     * Analyzes a repository URL.
     * Enforces security rule: must begin with https://github.com/ or https://gitlab.com/
     */
    public AnalysisResult analyze(String repoUrl, String branch) {
        AnalysisResult result = new AnalysisResult();

        // 1. Validate URL to prevent SSRF and command injection
        if (repoUrl == null || (!repoUrl.startsWith("https://github.com/") && !repoUrl.startsWith("https://gitlab.com/"))) {
            result.setError("Invalid repository URL. Must start with https://github.com/ or https://gitlab.com/");
            return result;
        }

        String safeBranch = (branch != null && !branch.trim().isEmpty()) ? branch.trim() : "main";

        // Handle special local demo repo simulation if URL is infrapilot/demo-app
        if (repoUrl.contains("infrapilot/demo-app") || repoUrl.contains("demo-app")) {
            return analyzeDemoApp();
        }

        Path tempDir = null;
        try {
            tempDir = Files.createTempDirectory("infrapilot-clone-");

            // 2. Safe shallow clone with ProcessBuilder
            List<String> command = List.of(
                    "git", "clone", "--depth", "1", "--branch", safeBranch, repoUrl, tempDir.toAbsolutePath().toString()
            );

            ProcessBuilder pb = new ProcessBuilder(command);
            pb.redirectErrorStream(true);
            Process process = pb.start();

            boolean finished = process.waitFor(45, TimeUnit.SECONDS);
            if (!finished || process.exitValue() != 0) {
                process.destroyForcibly();
                result.setError("Failed to clone repository or operation timed out.");
                return result;
            }

            File dir = tempDir.toFile();
            inspectDirectory(dir, result);

        } catch (Exception e) {
            log.error("Error during repo analysis: {}", e.getMessage(), e);
            result.setError("Repository analysis failed: " + e.getMessage());
        } finally {
            // 6. Delete temporary clone after analysis
            if (tempDir != null) {
                deleteRecursively(tempDir.toFile());
            }
        }

        return result;
    }

    public AnalysisResult analyzeDemoApp() {
        AnalysisResult result = new AnalysisResult();
        result.setHasInfraPilotYml(true);
        result.setHasDockerfile(true);
        result.setHasTests(true);
        result.setDetectedStack("Java Maven");

        Map<String, Object> cfg = new LinkedHashMap<>();
        cfg.put("version", 1);
        cfg.put("app", Map.of(
                "name", "demo-app",
                "type", "fullstack",
                "port", 8080,
                "health_path", "/actuator/health",
                "metrics_path", "/actuator/prometheus"
        ));
        cfg.put("build", Map.of(
                "command", "./mvnw -B package -DskipTests",
                "dockerfile", "./Dockerfile"
        ));
        cfg.put("test", Map.of(
                "unit", "./mvnw -B test",
                "api", "newman run tests/postman/collection.json --env-var baseUrl={{APP_URL}}",
                "load", "k6 run tests/k6/smoke.js"
        ));
        cfg.put("deploy", Map.of(
                "target", "aws-ec2",
                "compose_file", "docker-compose.yml",
                "env", Map.of("SPRING_PROFILES_ACTIVE", "prod"),
                "secrets", List.of("DB_PASSWORD")
        ));
        cfg.put("thresholds", Map.of(
                "max_critical_vulns", 0,
                "max_high_vulns", 5,
                "min_coverage_percent", 60,
                "p95_latency_ms", 500
        ));
        result.setConfig(cfg);
        result.getReadinessNotes().add("Found valid infrapilot.yml configuration contract.");
        result.getReadinessNotes().add("Found Dockerfile for containerized deployment.");
        result.getReadinessNotes().add("Found comprehensive unit, Newman API, and k6 load tests.");
        result.getReadinessNotes().add("Actuator health & Prometheus metrics enabled.");
        return result;
    }

    private void inspectDirectory(File root, AnalysisResult result) {
        File configFile = new File(root, "infrapilot.yml");
        if (configFile.exists()) {
            result.setHasInfraPilotYml(true);
            try (InputStream in = new FileInputStream(configFile)) {
                Map<String, Object> yamlMap = yaml.load(in);
                result.setConfig(yamlMap);
                result.getReadinessNotes().add("infrapilot.yml contract verified successfully.");
            } catch (Exception e) {
                result.getReadinessNotes().add("Warning: infrapilot.yml syntax error. Falling back to stack detection.");
            }
        }

        File dockerFile = new File(root, "Dockerfile");
        result.setHasDockerfile(dockerFile.exists());
        if (!dockerFile.exists()) {
            result.getReadinessNotes().add("No Dockerfile detected in repository root. Starter Dockerfile proposed.");
        }

        // Auto-detection rules (Section 6 & 7)
        if (new File(root, "pom.xml").exists()) {
            result.setDetectedStack("Java Maven");
            if (result.getConfig() == null) {
                result.setConfig(generateDefaultConfig("java-app", "./mvnw -B package -DskipTests", "./mvnw -B test", 8080));
            }
        } else if (new File(root, "build.gradle").exists()) {
            result.setDetectedStack("Java Gradle");
            if (result.getConfig() == null) {
                result.setConfig(generateDefaultConfig("gradle-app", "./gradlew build", "./gradlew test", 8080));
            }
        } else if (new File(root, "package.json").exists()) {
            result.setDetectedStack("Node");
            if (result.getConfig() == null) {
                result.setConfig(generateDefaultConfig("node-app", "npm ci && npm run build", "npm test", 3000));
            }
        } else if (new File(root, "requirements.txt").exists()) {
            result.setDetectedStack("Python");
            if (result.getConfig() == null) {
                result.setConfig(generateDefaultConfig("python-app", "pip install -r requirements.txt", "pytest", 8000));
            }
        } else if (dockerFile.exists()) {
            result.setDetectedStack("Generic container");
            if (result.getConfig() == null) {
                result.setConfig(generateDefaultConfig("container-app", "docker build .", "", 8080));
            }
        } else {
            result.setDetectedStack("Unknown");
            result.getReadinessNotes().add("Could not determine tech stack. Please provide infrapilot.yml.");
        }

        // Check for tests
        boolean testsFound = new File(root, "src/test").exists() ||
                new File(root, "tests").exists() ||
                new File(root, "test").exists();
        result.setHasTests(testsFound);
        if (!testsFound) {
            result.getReadinessNotes().add("No unit/API tests found: test stages will report as skipped.");
        }
    }

    private Map<String, Object> generateDefaultConfig(String appName, String buildCmd, String testCmd, int port) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("version", 1);
        map.put("app", Map.of(
                "name", appName,
                "type", "backend",
                "port", port,
                "health_path", "/health"
        ));
        map.put("build", Map.of(
                "command", buildCmd,
                "dockerfile", "./Dockerfile"
        ));
        map.put("test", Map.of(
                "unit", testCmd
        ));
        map.put("deploy", Map.of(
                "target", "aws-ec2",
                "compose_file", "docker-compose.yml"
        ));
        map.put("thresholds", Map.of(
                "max_critical_vulns", 0,
                "max_high_vulns", 5,
                "min_coverage_percent", 50,
                "p95_latency_ms", 500
        ));
        return map;
    }

    private void deleteRecursively(File file) {
        if (file.isDirectory()) {
            File[] children = file.listFiles();
            if (children != null) {
                for (File child : children) {
                    deleteRecursively(child);
                }
            }
        }
        file.delete();
    }
}
