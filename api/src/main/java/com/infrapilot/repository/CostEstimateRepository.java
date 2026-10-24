package com.infrapilot.repository;

import com.infrapilot.domain.CostEstimate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface CostEstimateRepository extends JpaRepository<CostEstimate, UUID> {
    Optional<CostEstimate> findByRunId(UUID runId);
}
