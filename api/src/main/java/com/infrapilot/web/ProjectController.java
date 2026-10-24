package com.infrapilot.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.infrapilot.domain.Project;
import com.infrapilot.repository.ProjectRepository;
import com.infrapilot.service.RepoAnalyzer;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/projects")
public class ProjectController {

    private final ProjectRepository projectRepository;
    private final RepoAnalyzer repoAnalyzer;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public ProjectController(ProjectRepository projectRepository, RepoAnalyzer repoAnalyzer) {
        this.projectRepository = projectRepository;
        this.repoAnalyzer = repoAnalyzer;
    }

    public record CreateProjectRequest(String name, String repoUrl, String branch, Boolean isDemo) {}

    @PostMapping
    public ResponseEntity<?> createProject(@RequestBody CreateProjectRequest req, Authentication auth) {
        if (req.repoUrl() == null || req.repoUrl().isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Repository URL is required"));
        }

        String branch = (req.branch() != null && !req.branch().isBlank()) ? req.branch() : "main";
        RepoAnalyzer.AnalysisResult analysis = repoAnalyzer.analyze(req.repoUrl(), branch);

        if (analysis.getError() != null) {
            return ResponseEntity.badRequest().body(Map.of("error", analysis.getError()));
        }

        String host = req.repoUrl().contains("gitlab") ? "gitlab" : "github";
        String projName = (req.name() != null && !req.name().isBlank()) ? req.name() :
                (req.repoUrl().contains("demo-app") ? "demo-app" : "repo-project-" + System.currentTimeMillis() % 1000);

        Project project = new Project();
        project.setName(projName);
        project.setRepoUrl(req.repoUrl());
        project.setRepoHost(host);
        project.setBranch(branch);
        project.setDetectedStack(analysis.getDetectedStack());
        project.setIsDemo(Boolean.TRUE.equals(req.isDemo()) || req.repoUrl().contains("demo-app"));

        try {
            project.setConfig(objectMapper.writeValueAsString(analysis.getConfig()));
        } catch (Exception ignored) {}

        Project saved = projectRepository.save(project);

        return ResponseEntity.ok(Map.of(
                "project", saved,
                "readinessReport", analysis.getReadinessNotes(),
                "detectedStack", analysis.getDetectedStack(),
                "hasDockerfile", analysis.isHasDockerfile(),
                "hasTests", analysis.isHasTests()
        ));
    }

    @GetMapping
    public ResponseEntity<List<Project>> listProjects() {
        return ResponseEntity.ok(projectRepository.findAllByOrderByCreatedAtDesc());
    }

    @GetMapping("/{id}/config")
    public ResponseEntity<?> getProjectConfig(@PathVariable UUID id) {
        return projectRepository.findById(id)
                .map(p -> ResponseEntity.ok(p.getConfig()))
                .orElse(ResponseEntity.notFound().build());
    }
}
