package com.careerskillroadmap.projects;

import java.util.List;
import java.util.Set;

public record ProjectDefinition(
        String key,
        String title,
        String description,
        String domain,
        String difficulty,
        List<String> skillsAndTools,
        List<MilestoneDefinition> milestones,
        String expectedOutcome,
        Set<String> goals) {

    public record MilestoneDefinition(String key, String title, String learningStep) {
    }
}
