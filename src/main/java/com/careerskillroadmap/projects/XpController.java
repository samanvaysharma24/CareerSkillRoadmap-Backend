package com.careerskillroadmap.projects;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/xp")
public class XpController {

    private final ProjectProgressService projectService;

    public XpController(ProjectProgressService projectService) {
        this.projectService = projectService;
    }

    @GetMapping
    public ProjectProgressService.XpSummary summary(Authentication authentication) {
        return projectService.summary(authentication.getName());
    }
}
