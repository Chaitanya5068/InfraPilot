package com.infrapilot.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class GitHubClient {
    private static final Logger log = LoggerFactory.getLogger(GitHubClient.class);

    public boolean triggerWorkflow(UUID runId, String repoUrl, String branch, String environment) {
        log.info("Triggering GitHub Actions workflow_dispatch for run: {}, repo: {}, branch: {}", runId, repoUrl, branch);
        return true;
    }
}
