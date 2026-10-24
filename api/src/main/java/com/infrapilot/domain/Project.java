package com.infrapilot.domain;

import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "projects")
public class Project {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(name = "repo_url", nullable = false, length = 500)
    private String repoUrl;

    @Column(name = "repo_host", nullable = false, length = 10)
    private String repoHost; // github | gitlab

    @Column(length = 100)
    private String branch = "main";

    @Column(name = "detected_stack", length = 30)
    private String detectedStack;

    @Column(columnDefinition = "jsonb")
    private String config; // parsed infrapilot.yml as json string

    @Column(name = "is_demo")
    private Boolean isDemo = false;

    @Column(name = "created_by")
    private UUID createdBy;

    @Column(name = "created_at", updatable = false)
    private OffsetDateTime createdAt = OffsetDateTime.now();

    public Project() {}

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getRepoUrl() { return repoUrl; }
    public void setRepoUrl(String repoUrl) { this.repoUrl = repoUrl; }

    public String getRepoHost() { return repoHost; }
    public void setRepoHost(String repoHost) { this.repoHost = repoHost; }

    public String getBranch() { return branch; }
    public void setBranch(String branch) { this.branch = branch; }

    public String getDetectedStack() { return detectedStack; }
    public void setDetectedStack(String detectedStack) { this.detectedStack = detectedStack; }

    public String getConfig() { return config; }
    public void setConfig(String config) { this.config = config; }

    public Boolean getIsDemo() { return isDemo; }
    public void setIsDemo(Boolean isDemo) { this.isDemo = isDemo; }

    public UUID getCreatedBy() { return createdBy; }
    public void setCreatedBy(UUID createdBy) { this.createdBy = createdBy; }

    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }
}
