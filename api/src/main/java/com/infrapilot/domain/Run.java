package com.infrapilot.domain;

import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "runs")
public class Run {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(name = "project_id", nullable = false)
    private UUID projectId;

    @Column(nullable = false, length = 20)
    private String environment; // dev | staging | prod

    @Column(nullable = false, length = 20)
    private String status = "queued"; 
    // queued | running | awaiting_approval | deploying | deployed | failed | destroyed

    @Column(name = "commit_sha", length = 40)
    private String commitSha;

    @Column(name = "ci_system", length = 20)
    private String ciSystem = "jenkins"; // jenkins | github_actions | gitlab_ci

    @Column(name = "ci_ref", length = 200)
    private String ciRef;

    @Column(name = "endpoint_url", length = 500)
    private String endpointUrl;

    @Column(name = "grafana_url", length = 500)
    private String grafanaUrl;

    @Column(name = "requested_by")
    private UUID requestedBy;

    @Column(name = "approved_by")
    private UUID approvedBy;

    @Column(name = "started_at")
    private OffsetDateTime startedAt;

    @Column(name = "finished_at")
    private OffsetDateTime finishedAt;

    @Column(name = "created_at", updatable = false)
    private OffsetDateTime createdAt = OffsetDateTime.now();

    public Run() {}

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getProjectId() { return projectId; }
    public void setProjectId(UUID projectId) { this.projectId = projectId; }

    public String getEnvironment() { return environment; }
    public void setEnvironment(String environment) { this.environment = environment; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getCommitSha() { return commitSha; }
    public void setCommitSha(String commitSha) { this.commitSha = commitSha; }

    public String getCiSystem() { return ciSystem; }
    public void setCiSystem(String ciSystem) { this.ciSystem = ciSystem; }

    public String getCiRef() { return ciRef; }
    public void setCiRef(String ciRef) { this.ciRef = ciRef; }

    public String getEndpointUrl() { return endpointUrl; }
    public void setEndpointUrl(String endpointUrl) { this.endpointUrl = endpointUrl; }

    public String getGrafanaUrl() { return grafanaUrl; }
    public void setGrafanaUrl(String grafanaUrl) { this.grafanaUrl = grafanaUrl; }

    public UUID getRequestedBy() { return requestedBy; }
    public void setRequestedBy(UUID requestedBy) { this.requestedBy = requestedBy; }

    public UUID getApprovedBy() { return approvedBy; }
    public void setApprovedBy(UUID approvedBy) { this.approvedBy = approvedBy; }

    public OffsetDateTime getStartedAt() { return startedAt; }
    public void setStartedAt(OffsetDateTime startedAt) { this.startedAt = startedAt; }

    public OffsetDateTime getFinishedAt() { return finishedAt; }
    public void setFinishedAt(OffsetDateTime finishedAt) { this.finishedAt = finishedAt; }

    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }
}
