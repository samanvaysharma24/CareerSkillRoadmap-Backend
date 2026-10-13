package com.careerskillroadmap.projects;

public class InvalidProjectMilestoneException extends RuntimeException {

    public InvalidProjectMilestoneException() {
        super("This milestone is not part of the selected project.");
    }
}
