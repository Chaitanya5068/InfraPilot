package com.infrapilot.repository;

import com.infrapilot.domain.StageResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface StageResultRepository extends JpaRepository<StageResult, UUID> {
    List<StageResult> findByRunIdOrderByCreatedAtAsc(UUID runId);
    Optional<StageResult> findByRunIdAndStage(UUID runId, String stage);
}
