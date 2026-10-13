package com.careerskillroadmap.assessment;

public class InvalidRoadmapSelectionException extends RuntimeException {

    public InvalidRoadmapSelectionException(Long careerId) {
        super("Career with ID " + careerId + " is not a recommendation for this assessment.");
    }
}
