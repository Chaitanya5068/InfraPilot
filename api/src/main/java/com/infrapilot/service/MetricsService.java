package com.infrapilot.service;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

@Service
public class MetricsService {

    private final MeterRegistry meterRegistry;
    private final Map<String, AtomicReference<Double>> costGauges = new ConcurrentHashMap<>();

    public MetricsService(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    public void incrementRuns(String status, String environment) {
        Counter.builder("infrapilot_runs_total")
                .description("Total number of InfraPilot CI/CD pipeline runs")
                .tags("status", status != null ? status : "unknown", "environment", environment != null ? environment : "unknown")
                .register(meterRegistry)
                .increment();
    }

    public void recordStageDuration(String stage, long seconds) {
        Timer.builder("infrapilot_stage_duration_seconds")
                .description("Duration of pipeline stages in seconds")
                .tags("stage", stage != null ? stage : "unknown")
                .register(meterRegistry)
                .record(seconds, TimeUnit.SECONDS);
    }

    public void incrementScanFindings(String tool, String severity, int count) {
        Counter.builder("infrapilot_scan_findings_total")
                .description("Security scan findings count by tool and severity")
                .tags("tool", tool != null ? tool : "unknown", "severity", severity != null ? severity : "unknown")
                .register(meterRegistry)
                .increment(count);
    }

    public void updateCostEstimate(String runId, double costUsd) {
        costGauges.computeIfAbsent(runId, id -> {
            AtomicReference<Double> val = new AtomicReference<>(costUsd);
            Gauge.builder("infrapilot_cost_estimate_usd", val, AtomicReference::get)
                    .description("Estimated monthly infrastructure cost in USD")
                    .tags("run_id", id)
                    .register(meterRegistry);
            return val;
        }).set(costUsd);
    }
}
