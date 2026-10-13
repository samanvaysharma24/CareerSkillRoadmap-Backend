package com.careerskillroadmap.assessment;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/assessments")
public class AssessmentController {

    private final AssessmentService assessmentService;

    public AssessmentController(AssessmentService assessmentService) {
        this.assessmentService = assessmentService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AssessmentResponse create(@Valid @RequestBody AssessmentRequest request, Authentication authentication) {
        return assessmentService.create(request, authentication.getName());
    }

    @GetMapping("/{id}")
    public AssessmentResponse findById(@PathVariable Long id, Authentication authentication) {
        return assessmentService.findById(id, authentication.getName());
    }

    @PutMapping("/recommendations")
    public AssessmentResponse refreshRecommendations(Authentication authentication) {
        return assessmentService.refreshRecommendations(authentication.getName());
    }

    @PutMapping("/{id}/roadmap")
    public AssessmentResponse selectRoadmap(
            @PathVariable Long id,
            @Valid @RequestBody RoadmapSelectionRequest request,
            Authentication authentication) {
        return assessmentService.selectRoadmap(id, request.careerId(), authentication.getName());
    }
}
