package com.infrapilot.repository;

import com.infrapilot.domain.TestResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TestResultRepository extends JpaRepository<TestResult, UUID> {
    List<TestResult> findByRunId(UUID runId);
    Optional<TestResult> findByRunIdAndKind(UUID runId, String kind);
}
