package com.infrapilot.repository;

import com.infrapilot.domain.Run;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface RunRepository extends JpaRepository<Run, UUID> {
    List<Run> findByProjectIdOrderByCreatedAtDesc(UUID projectId);
    List<Run> findAllByOrderByCreatedAtDesc();
    long countByStatus(String status);
}
