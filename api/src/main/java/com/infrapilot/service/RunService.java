package com.infrapilot.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.infrapilot.domain.*;
import com.infrapilot.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.*;

@Service
public class RunService {

    private static final Logger log = LoggerFactory.getLogger(RunService.class);

    private final RunRepository runRepository;
    private final ProjectRepository projectRepository;
    private final StageResultRepository stageResultRepository;
    private final ScanResultRepository scanResultRepository;
    private final TestResultRepository testResultRepository;
    private final AuditEventRepository auditEventRepository;
    private final JenkinsClient jenkinsClient;
    private final GitHubClient gitHubClient;
    private final GitLabClient gitLabClient;
    private final MetricsService metricsService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public RunService(
            RunRepository runRepository,
            ProjectRepository projectRepository,
            StageResultRepository stageResultRepository,
            ScanResultRepository scanResultRepository,
            TestResultRepository testResultRepository,
            AuditEventRepository auditEventRepository,
            JenkinsClient jenkinsClient,
            GitHubClient gitHubClient,
            GitLabClient gitLabClient,
            MetricsService metricsService
    ) {
        this.runRepository = runRepository;
        this.projectRepository = projectRepository;
        this.stageResultRepository = stageResultRepository;
        this.scanResultRepository = scanResultRepository;
        this.testResultRepository = testResultRepository;
        this.auditEventRepository = auditEventRepository;
        this.jenkinsClient = jenkinsClient;
        this.gitHubClient = gitHubClient;
        this.gitLabClient = gitLabClient;
        this.metricsService = metricsService;
    }

    @Transactional
    public Run startRun(UUID projectId, String environment, String ciSystem, UUID userId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new IllegalArgumentException("Project not found with id: " + projectId));

        String targetEnv = (environment != null && !environment.isBlank()) ? environment.toLowerCase() : "dev";
        String targetCi = (ciSystem != null && !ciSystem.isBlank()) ? ciSystem.toLowerCase() : "jenkins";

        Run run = new Run();
        run.setProjectId(project.getId());
        run.setEnvironment(targetEnv);
        run.setCiSystem(targetCi);
        run.setStatus("running");
        run.setRequestedBy(userId);
        run.setStartedAt(OffsetDateTime.now());
        run.setCommitSha(UUID.randomUUID().toString().substring(0, 7));
        Run saved = runRepository.save(run);

        // Pre-create initial pipeline stages in 'pending' state
        List<String> pipelineStages = List.of(
                "checkout", "load_config", "build", "unit_tests", "sonar",
                "secret_scan", "dependency_scan", "iac_scan", "image_build", "image_scan",
                "infracost", "terraform_plan", "approval", "terraform_apply", "deploy", "smoke_load_test"
        );
        for (String stage : pipelineStages) {
            stageResultRepository.save(new StageResult(saved.getId(), stage, "pending", 0, null));
        }

        // Trigger chosen CI system
        if ("github_actions".equalsIgnoreCase(targetCi)) {
            gitHubClient.triggerWorkflow(saved.getId(), project.getRepoUrl(), project.getBranch(), targetEnv);
        } else if ("gitlab_ci".equalsIgnoreCase(targetCi)) {
            gitLabClient.triggerPipeline(saved.getId(), project.getRepoUrl(), project.getBranch(), targetEnv);
        } else {
            jenkinsClient.triggerRun(saved.getId(), project.getRepoUrl(), project.getBranch(), targetEnv);
        }

        // Record metrics & audit
        metricsService.incrementRuns(saved.getStatus(), saved.getEnvironment());
        auditEventRepository.save(new AuditEvent(userId, "RUN_STARTED", "RUN", saved.getId(),
                "{\"environment\": \"" + targetEnv + "\", \"ciSystem\": \"" + targetCi + "\"}"));

        return saved;
    }

    @Transactional
    public void recordStageCallback(UUID runId, String stageName, String status, Integer durationSec, String logUrl) {
        Run run = runRepository.findById(runId)
                .orElseThrow(() -> new IllegalArgumentException("Run not found: " + runId));

        StageResult stage = stageResultRepository.findByRunIdAndStage(runId, stageName)
                .orElse(new StageResult(runId, stageName, status, durationSec, logUrl));

        stage.setStatus(status);
        if (durationSec != null) {
            stage.setDurationSec(durationSec);
            metricsService.recordStageDuration(stageName, durationSec);
        }
        if (logUrl != null) stage.setLogUrl(logUrl);
        stageResultRepository.save(stage);

        // State machine progression
        if ("approval".equalsIgnoreCase(stageName) && "waiting".equalsIgnoreCase(status)) {
            run.setStatus("awaiting_approval");
            runRepository.save(run);
        } else if ("deploy".equalsIgnoreCase(stageName) && "passed".equalsIgnoreCase(status)) {
            run.setStatus("deployed");
            run.setFinishedAt(OffsetDateTime.now());
            run.setEndpointUrl("http://localhost:8081");
            run.setGrafanaUrl("http://localhost:3000");
            runRepository.save(run);
            metricsService.incrementRuns("deployed", run.getEnvironment());
        } else if ("failed".equalsIgnoreCase(status)) {
            run.setStatus("failed");
            run.setFinishedAt(OffsetDateTime.now());
            runRepository.save(run);
            metricsService.incrementRuns("failed", run.getEnvironment());
        }
    }

    @Transactional
    public void recordScanCallback(UUID runId, String tool, String findingsJson) {
        int critical = 0, high = 0, medium = 0, low = 0;
        try {
            var root = objectMapper.readTree(findingsJson);
            if (root.isArray()) {
                for (var node : root) {
                    String sev = node.has("severity") ? node.get("severity").asText().toUpperCase() : "LOW";
                    switch (sev) {
                        case "CRITICAL" -> critical++;
                        case "HIGH" -> high++;
                        case "MEDIUM" -> medium++;
                        default -> low++;
                    }
                }
            }
        } catch (Exception ignored) {}

        ScanResult scan = scanResultRepository.findByRunIdAndTool(runId, tool)
                .orElse(new ScanResult());
        scan.setRunId(runId);
        scan.setTool(tool);
        scan.setCritical(critical);
        scan.setHigh(high);
        scan.setMedium(medium);
        scan.setLow(low);
        scan.setFindings(findingsJson);
        scanResultRepository.save(scan);

        metricsService.incrementScanFindings(tool, "CRITICAL", critical);
        metricsService.incrementScanFindings(tool, "HIGH", high);
        metricsService.incrementScanFindings(tool, "MEDIUM", medium);
        metricsService.incrementScanFindings(tool, "LOW", low);
    }

    @Transactional
    public Run approveRun(UUID runId, UUID userId, String userEmail) {
        Run run = runRepository.findById(runId)
                .orElseThrow(() -> new IllegalArgumentException("Run not found: " + runId));
        run.setStatus("deploying");
        run.setApprovedBy(userId);
        Run saved = runRepository.save(run);

        jenkinsClient.approveStage(runId, userEmail);
        auditEventRepository.save(new AuditEvent(userId, "RUN_APPROVED", "RUN", runId, "{\"approver\": \"" + userEmail + "\"}"));
        return saved;
    }

    @Transactional
    public Run rejectRun(UUID runId, UUID userId, String userEmail) {
        Run run = runRepository.findById(runId)
                .orElseThrow(() -> new IllegalArgumentException("Run not found: " + runId));
        run.setStatus("failed");
        run.setFinishedAt(OffsetDateTime.now());
        Run saved = runRepository.save(run);

        jenkinsClient.rejectStage(runId, userEmail);
        auditEventRepository.save(new AuditEvent(userId, "RUN_REJECTED", "RUN", runId, "{\"rejecter\": \"" + userEmail + "\"}"));
        return saved;
    }

    @Transactional
    public Run destroyRun(UUID runId, UUID userId) {
        Run run = runRepository.findById(runId)
                .orElseThrow(() -> new IllegalArgumentException("Run not found: " + runId));
        run.setStatus("destroyed");
        run.setEndpointUrl(null);
        Run saved = runRepository.save(run);

        auditEventRepository.save(new AuditEvent(userId, "INFRA_DESTROYED", "RUN", runId, "{\"action\": \"terraform destroy\"}"));
        return saved;
    }

    public Optional<Run> getRun(UUID runId) {
        return runRepository.findById(runId);
    }

    public List<Run> listRuns() {
        return runRepository.findAllByOrderByCreatedAtDesc();
    }

    public List<StageResult> getStages(UUID runId) {
        return stageResultRepository.findByRunIdOrderByCreatedAtAsc(runId);
    }

    public List<ScanResult> getScans(UUID runId) {
        return scanResultRepository.findByRunId(runId);
    }

    public List<TestResult> getTests(UUID runId) {
        return testResultRepository.findByRunId(runId);
    }
}
