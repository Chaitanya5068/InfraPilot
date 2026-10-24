package com.infrapilot.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.infrapilot.domain.CostEstimate;
import com.infrapilot.repository.CostEstimateRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

@Service
public class CostService {

    private static final Logger log = LoggerFactory.getLogger(CostService.class);
    private final CostEstimateRepository costEstimateRepository;
    private final MetricsService metricsService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${infrapilot.cost.budget-cap-usd:25.0}")
    private double budgetCapUsd;

    public CostService(CostEstimateRepository costEstimateRepository, MetricsService metricsService) {
        this.costEstimateRepository = costEstimateRepository;
        this.metricsService = metricsService;
    }

    public CostEstimate saveInfracostReport(UUID runId, String infracostJson) {
        BigDecimal totalMonthlyCost = BigDecimal.ZERO;
        try {
            JsonNode root = objectMapper.readTree(infracostJson);
            if (root.has("totalMonthlyCost")) {
                totalMonthlyCost = new BigDecimal(root.get("totalMonthlyCost").asText("0.00"));
            }
        } catch (Exception e) {
            log.error("Failed to parse Infracost breakdown json: {}", e.getMessage());
        }

        CostEstimate estimate = costEstimateRepository.findByRunId(runId).orElse(new CostEstimate());
        estimate.setRunId(runId);
        estimate.setMonthlyCost(totalMonthlyCost);
        estimate.setBreakdown(infracostJson);
        estimate.setCurrency("USD");
        CostEstimate saved = costEstimateRepository.save(estimate);

        metricsService.updateCostEstimate(runId.toString(), totalMonthlyCost.doubleValue());
        return saved;
    }

    public Optional<CostEstimate> getCostByRunId(UUID runId) {
        return costEstimateRepository.findByRunId(runId);
    }

    public boolean isWithinBudget(BigDecimal monthlyCost) {
        return monthlyCost != null && monthlyCost.doubleValue() <= budgetCapUsd;
    }
}
