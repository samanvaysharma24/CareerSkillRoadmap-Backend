package com.careerskillroadmap.career;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class CareerExceptionHandler {

    @ExceptionHandler(CareerNotFoundException.class)
    public ProblemDetail handleCareerNotFound(CareerNotFoundException exception) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, exception.getMessage());
        problem.setTitle("Career not found");
        return problem;
    }
}
