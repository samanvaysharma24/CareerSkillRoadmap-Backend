package com.careerskillroadmap.auth;

public class InvalidRegistrationException extends RuntimeException {

    public InvalidRegistrationException() {
        super("The password must be at most 72 UTF-8 bytes.");
    }
}
