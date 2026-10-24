package com.infrapilot.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class GitLabClient {
    private static final Logger log = LoggerFactory.getLogger(GitLabClient.class);

    public boolean triggerPipeline(UUID runId, String repoUrl, String branch, String environment) {
        log.info("Triggering GitLab CI pipeline for run: {}, repo: {}, branch: {}", runId, repoUrl, branch);
        return true;
    }
}
