package com.infrapilot.domain;

import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "scan_results")
public class ScanResult {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(name = "run_id", nullable = false)
    private UUID runId;

    @Column(nullable = false, length = 30)
    private String tool; // gitleaks | trivy | checkov | sonar | depcheck

    private Integer critical = 0;
    private Integer high = 0;
    private Integer medium = 0;
    private Integer low = 0;

    @Column(columnDefinition = "jsonb")
    private String findings;

    @Column(name = "created_at", updatable = false)
    private OffsetDateTime createdAt = OffsetDateTime.now();

    public ScanResult() {}

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getRunId() { return runId; }
    public void setRunId(UUID runId) { this.runId = runId; }

    public String getTool() { return tool; }
    public void setTool(String tool) { this.tool = tool; }

    public Integer getCritical() { return critical; }
    public void setCritical(Integer critical) { this.critical = critical; }

    public Integer getHigh() { return high; }
    public void setHigh(Integer high) { this.high = high; }

    public Integer getMedium() { return medium; }
    public void setMedium(Integer medium) { this.medium = medium; }

    public Integer getLow() { return low; }
    public void setLow(Integer low) { this.low = low; }

    public String getFindings() { return findings; }
    public void setFindings(String findings) { this.findings = findings; }

    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }
}
