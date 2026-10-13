package com.careerskillroadmap.projects;

import java.util.List;

import jakarta.validation.constraints.NotBlank;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Validated
@RequestMapping("/api/projects")
public class ProjectController {

    private final ProjectProgressService projectService;

    public ProjectController(ProjectProgressService projectService) {
        this.projectService = projectService;
    }

    @GetMapping
    public List<ProjectProgressService.ProjectResponse> list(Authentication authentication) {
        return projectService.list(authentication.getName());
    }

    @PostMapping("/{projectKey}/start")
    public ProjectProgressService.ProjectResponse start(
            @PathVariable @NotBlank String projectKey, Authentication authentication) {
        return projectService.start(authentication.getName(), projectKey);
    }

    @PostMapping("/{projectKey}/milestones/{milestoneKey}/complete")
    public ProjectProgressService.ProjectResponse completeMilestone(
            @PathVariable @NotBlank String projectKey,
            @PathVariable @NotBlank String milestoneKey,
            Authentication authentication) {
        return projectService.completeMilestone(authentication.getName(), projectKey, milestoneKey);
    }
}
