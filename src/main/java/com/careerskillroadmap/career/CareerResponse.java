package com.careerskillroadmap.career;

import java.util.List;

public record CareerResponse(
        Long id,
        String title,
        String domain,
        String description,
        List<SkillResponse> skills) {

    public static CareerResponse from(Career career) {
        return new CareerResponse(
                career.getId(),
                career.getTitle(),
                career.getDomain(),
                career.getDescription(),
                career.getSkills().stream()
                        .map(skill -> new SkillResponse(skill.getId(), skill.getTitle(), skill.getDescription(), skill.getPosition()))
                        .toList());
    }

    public record SkillResponse(Long id, String title, String description, int position) {
    }
}
