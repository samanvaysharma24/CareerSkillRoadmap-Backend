package com.careerskillroadmap.assessment;

import java.util.List;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record AssessmentRequest(
        @NotEmpty List<@Pattern(regexp = "ece|embedded|iot|vlsi|robotics|electrical|software|ai|cybersecurity|cloud|data|product|custom") String> domains,
        @NotBlank @Size(max = 1000) String interests,
        @Size(max = 300) String customInterest,
        @NotBlank @Pattern(regexp = "Beginner|Intermediate|Advanced") String skillLevel,
        @NotEmpty List<@Pattern(regexp = "projects|hands-on|research|design|analysis|career-ready") String> goals) {

    public AssessmentRequest {
        interests = interests == null ? "" : interests.trim();
        customInterest = customInterest == null ? "" : customInterest.trim();
    }
}
