package com.careerskillroadmap.assessment;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.dao.DataAccessException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestControllerAdvice(assignableTypes = AssessmentController.class)
public class AssessmentExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(AssessmentExceptionHandler.class);

    @ExceptionHandler(AssessmentNotFoundException.class)
    public ProblemDetail handleAssessmentNotFound(AssessmentNotFoundException exception) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, exception.getMessage());
        problem.setTitle("Assessment not found");
        return problem;
    }

    @ExceptionHandler(InvalidRoadmapSelectionException.class)
    public ProblemDetail handleInvalidRoadmapSelection(InvalidRoadmapSelectionException exception) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, exception.getMessage());
        problem.setTitle("Invalid roadmap selection");
        return problem;
    }

    @ExceptionHandler(DataAccessException.class)
    public ProblemDetail handleAssessmentPersistenceError(DataAccessException exception) {
        logger.error("Database failure while processing an assessment request", exception);
        ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.INTERNAL_SERVER_ERROR);
        problem.setTitle("Assessment persistence failed");
        problem.setDetail("The assessment could not be saved. Check the server log for details.");
        return problem;
    }

    @ExceptionHandler({
            MethodArgumentNotValidException.class,
            HttpMessageNotReadableException.class,
            MethodArgumentTypeMismatchException.class
    })
    public ProblemDetail handleInvalidAssessmentRequest(Exception exception) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST, "Check the assessment ID and request body.");
        problem.setTitle("Invalid assessment request");
        return problem;
    }

    @ExceptionHandler(Exception.class)
    public ProblemDetail handleUnexpectedAssessmentError(Exception exception) {
        logger.error("Unexpected failure while processing an assessment request", exception);
        ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.INTERNAL_SERVER_ERROR);
        problem.setTitle("Assessment request failed");
        problem.setDetail("The assessment request could not be completed. Check the server log for details.");
        return problem;
    }
}
