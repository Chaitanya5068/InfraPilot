package com.infrapilot.web;

import com.infrapilot.service.CostService;
import com.infrapilot.service.RunService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/callbacks")
public class CallbackController {

    private final RunService runService;
    private final CostService costService;

    public CallbackController(RunService runService, CostService costService) {
        this.runService = runService;
        this.costService = costService;
    }

    public record StageCallbackRequest(UUID runId, String stage, String status, Integer durationSec, String logUrl) {}

    @PostMapping("/stage")
    public ResponseEntity<?> callbackStage(@RequestBody StageCallbackRequest req) {
        if (req.runId() == null || req.stage() == null || req.status() == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "runId, stage, and status are required"));
        }
        runService.recordStageCallback(req.runId(), req.stage(), req.status(), req.durationSec(), req.logUrl());
        return ResponseEntity.ok(Map.of("message", "Stage callback recorded"));
    }

    @PostMapping("/scan")
    public ResponseEntity<?> callbackScan(
            @RequestHeader(value = "X-Run-Id", required = false) String runIdHeader,
            @RequestHeader(value = "X-Scan-Type", required = false) String scanTypeHeader,
            @RequestParam(value = "runId", required = false) UUID runIdParam,
            @RequestParam(value = "type", required = false) String scanTypeParam,
            @RequestBody String reportJson
    ) {
        UUID runId = runIdHeader != null ? UUID.fromString(runIdHeader) : runIdParam;
        String tool = scanTypeHeader != null ? scanTypeHeader : scanTypeParam;

        if (runId == null || tool == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Run ID and scan type must be provided"));
        }

        runService.recordScanCallback(runId, tool, reportJson);
        return ResponseEntity.ok(Map.of("message", "Scan callback recorded"));
    }

    @PostMapping("/cost")
    public ResponseEntity<?> callbackCost(
            @RequestHeader(value = "X-Run-Id", required = false) String runIdHeader,
            @RequestParam(value = "runId", required = false) UUID runIdParam,
            @RequestBody String infracostJson
    ) {
        UUID runId = runIdHeader != null ? UUID.fromString(runIdHeader) : runIdParam;
        if (runId == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Run ID is required"));
        }
        var estimate = costService.saveInfracostReport(runId, infracostJson);
        return ResponseEntity.ok(Map.of("message", "Cost estimate saved", "estimate", estimate));
    }

    public record RunCallbackRequest(String runRef, String status) {}

    @PostMapping("/run")
    public ResponseEntity<?> callbackRun(@RequestBody RunCallbackRequest req) {
        return ResponseEntity.ok(Map.of("message", "Run callback acknowledged", "runRef", req.runRef()));
    }
}
