package com.careerskillroadmap.assessment;

import java.util.List;

import com.careerskillroadmap.career.CareerResponse;

public record AssessmentResponse(
        Long id,
        List<String> domains,
        String interests,
        String customInterest,
        String skillLevel,
        List<String> goals,
        List<RecommendationResponse> recommendations,
        Long selectedCareerId,
        List<CareerResponse.SkillResponse> roadmap) {

    public record RecommendationResponse(
            Long careerId,
            String title,
            String domain,
            String description,
            int matchScore,
            String reason,
            List<CareerResponse.SkillResponse> skills) {
    }
}
