package com.adil.supportdesk.adapter.in.web.common;

import com.adil.supportdesk.application.auth.EmailAlreadyExistsException;
import com.adil.supportdesk.application.auth.InvalidCredentialsException;
import com.adil.supportdesk.application.security.UnauthorizedAccessException;
import com.adil.supportdesk.application.ticket.assign.InvalidAssigneeException;
import com.adil.supportdesk.application.user.UserNotFoundException;
import com.adil.supportdesk.domain.ticket.exception.DomainException;
import com.adil.supportdesk.domain.ticket.exception.InvalidStatusTransitionException;
import com.adil.supportdesk.domain.ticket.exception.TicketClosedException;
import com.adil.supportdesk.domain.ticket.exception.TicketNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.net.URI;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(TicketNotFoundException.class)
    public ProblemDetail handleTicketNotFound(
            TicketNotFoundException exception
    ) {
        return createProblem(
                HttpStatus.NOT_FOUND,
                "Ticket Not Found",
                exception.getMessage(),
                "ticket-not-found"
        );
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ProblemDetail handleUserNotFound(
            UserNotFoundException exception
    ) {
        return createProblem(
                HttpStatus.NOT_FOUND,
                "User Not Found",
                exception.getMessage(),
                "user-not-found"
        );
    }

    @ExceptionHandler(UnauthorizedAccessException.class)
    public ProblemDetail handleUnauthorizedAccess(
            UnauthorizedAccessException exception
    ) {
        return createProblem(
                HttpStatus.FORBIDDEN,
                "Access Denied",
                exception.getMessage(),
                "access-denied"
        );
    }

    @ExceptionHandler(InvalidAssigneeException.class)
    public ProblemDetail handleInvalidAssignee(
            InvalidAssigneeException exception
    ) {
        return createProblem(
                HttpStatus.UNPROCESSABLE_ENTITY,
                "Invalid Assignee",
                exception.getMessage(),
                "invalid-assignee"
        );
    }

    @ExceptionHandler({
            InvalidStatusTransitionException.class,
            TicketClosedException.class
    })
    public ProblemDetail handleDomainViolation(
            DomainException exception
    ) {
        return createProblem(
                HttpStatus.UNPROCESSABLE_ENTITY,
                "Domain Rule Violation",
                exception.getMessage(),
                "domain-violation"
        );
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ProblemDetail handleIllegalArgument(
            IllegalArgumentException exception
    ) {
        return createProblem(
                HttpStatus.BAD_REQUEST,
                "Invalid Request",
                exception.getMessage(),
                "invalid-request"
        );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidationExceptions(
            MethodArgumentNotValidException exception
    ) {
        Map<String, String> errors = new HashMap<>();

        exception.getBindingResult()
                .getFieldErrors()
                .forEach(error ->
                        errors.put(
                                error.getField(),
                                error.getDefaultMessage()
                        )
                );

        ProblemDetail problemDetail =
                ProblemDetail.forStatusAndDetail(
                        HttpStatus.BAD_REQUEST,
                        "Validation failed"
                );

        problemDetail.setTitle("Bad Request");
        problemDetail.setType(
                URI.create(
                        "https://supportdesk.com/errors/validation"
                )
        );
        problemDetail.setProperty("errors", errors);
        problemDetail.setProperty(
                "timestamp",
                Instant.now()
        );

        return problemDetail;
    }

    private ProblemDetail createProblem(
            HttpStatus status,
            String title,
            String detail,
            String errorType
    ) {
        ProblemDetail problemDetail =
                ProblemDetail.forStatusAndDetail(
                        status,
                        detail
                );

        problemDetail.setTitle(title);
        problemDetail.setType(
                URI.create(
                        "https://supportdesk.com/errors/"
                                + errorType
                )
        );
        problemDetail.setProperty(
                "timestamp",
                Instant.now()
        );

        return problemDetail;
    }
    @ExceptionHandler(EmailAlreadyExistsException.class)
    public ProblemDetail handleEmailAlreadyExists(
            EmailAlreadyExistsException exception
    ) {
        return createProblem(
                HttpStatus.CONFLICT,
                "Email Already Exists",
                exception.getMessage(),
                "email-already-exists"
        );
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    public ProblemDetail handleInvalidCredentials(
            InvalidCredentialsException exception
    ) {
        return createProblem(
                HttpStatus.UNAUTHORIZED,
                "Invalid Credentials",
                exception.getMessage(),
                "invalid-credentials"
        );
    }


}