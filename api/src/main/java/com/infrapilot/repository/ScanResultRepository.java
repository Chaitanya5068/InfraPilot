package com.infrapilot.repository;

import com.infrapilot.domain.ScanResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ScanResultRepository extends JpaRepository<ScanResult, UUID> {
    List<ScanResult> findByRunId(UUID runId);
    Optional<ScanResult> findByRunIdAndTool(UUID runId, String tool);
}
