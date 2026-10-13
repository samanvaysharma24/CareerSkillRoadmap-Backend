package com.careerskillroadmap.projects;

public class ProjectNotFoundException extends RuntimeException {

    public ProjectNotFoundException(String key) {
        super("Project not found: " + key);
    }
}
