package com.careerskillroadmap.projects;

public class ProjectNotStartedException extends RuntimeException {

    public ProjectNotStartedException() {
        super("Start the project before completing its milestones.");
    }
}
