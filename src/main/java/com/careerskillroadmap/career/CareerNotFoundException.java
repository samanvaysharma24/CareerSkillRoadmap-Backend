package com.careerskillroadmap.career;

public class CareerNotFoundException extends RuntimeException {

    public CareerNotFoundException(Long id) {
        super("Career with ID " + id + " was not found.");
    }
}
