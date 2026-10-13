package com.careerskillroadmap.assessment;

public class AssessmentNotFoundException extends RuntimeException {

    public AssessmentNotFoundException(Long id) {
        super("Assessment with ID " + id + " was not found.");
    }
}
