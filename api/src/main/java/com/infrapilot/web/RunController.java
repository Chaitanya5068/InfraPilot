package com.infrapilot.web;

import com.infrapilot.domain.*;
import com.infrapilot.repository.UserRepository;
import com.infrapilot.service.CostService;
import com.infrapilot.service.RunService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/runs")
public class RunController {

    private final RunService runService;
    private final CostService costService;
    private final UserRepository userRepository;

    public RunController(RunService runService, CostService costService, UserRepository userRepository) {
        this.runService = runService;
        this.costService = costService;
        this.userRepository = userRepository;
    }

    public record StartRunRequest(UUID projectId, String environment, String ciSystem) {}

    @PostMapping
    public ResponseEntity<?> startRun(@RequestBody StartRunRequest req, Authentication auth) {
        UUID userId = null;
        if (auth != null) {
            userRepository.findByEmail(auth.getName()).ifPresent(u -> {});
        }
        Run run = runService.startRun(req.projectId(), req.environment(), req.ciSystem(), userId);
        return ResponseEntity.ok(run);
    }

    @GetMapping
    public ResponseEntity<List<Run>> listRuns() {
        return ResponseEntity.ok(runService.listRuns());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getRun(@PathVariable UUID id) {
        return runService.getRun(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}/stages")
    public ResponseEntity<List<StageResult>> getStages(@PathVariable UUID id) {
        return ResponseEntity.ok(runService.getStages(id));
    }

    @PostMapping("/{id}/approve")
    public ResponseEntity<?> approveRun(@PathVariable UUID id, Authentication auth) {
        String email = auth != null ? auth.getName() : "devops@infrapilot.io";
        UUID userId = userRepository.findByEmail(email).map(User::getId).orElse(null);
        Run updated = runService.approveRun(id, userId, email);
        return ResponseEntity.ok(Map.of("message", "Run approved successfully", "run", updated));
    }

    @PostMapping("/{id}/reject")
    public ResponseEntity<?> rejectRun(@PathVariable UUID id, Authentication auth) {
        String email = auth != null ? auth.getName() : "devops@infrapilot.io";
        UUID userId = userRepository.findByEmail(email).map(User::getId).orElse(null);
        Run updated = runService.rejectRun(id, userId, email);
        return ResponseEntity.ok(Map.of("message", "Run rejected", "run", updated));
    }

    @PostMapping("/{id}/destroy")
    public ResponseEntity<?> destroyRun(@PathVariable UUID id, Authentication auth) {
        String email = auth != null ? auth.getName() : "devops@infrapilot.io";
        UUID userId = userRepository.findByEmail(email).map(User::getId).orElse(null);
        Run updated = runService.destroyRun(id, userId);
        return ResponseEntity.ok(Map.of("message", "Infrastructure destruction triggered", "run", updated));
    }

    @GetMapping("/{id}/scans")
    public ResponseEntity<List<ScanResult>> getScans(@PathVariable UUID id) {
        return ResponseEntity.ok(runService.getScans(id));
    }

    @GetMapping("/{id}/tests")
    public ResponseEntity<List<TestResult>> getTests(@PathVariable UUID id) {
        return ResponseEntity.ok(runService.getTests(id));
    }

    @GetMapping("/{id}/cost")
    public ResponseEntity<?> getCost(@PathVariable UUID id) {
        return costService.getCostByRunId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
