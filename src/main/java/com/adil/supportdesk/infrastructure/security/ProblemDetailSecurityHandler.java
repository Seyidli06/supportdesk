package com.adil.supportdesk.infrastructure.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Objects;

public class ProblemDetailSecurityHandler
        implements AuthenticationEntryPoint,
        AccessDeniedHandler {

    private static final String ERROR_BASE_URL =
            "https://supportdesk.com/errors/";

    private final ObjectMapper objectMapper;

    public ProblemDetailSecurityHandler(
            ObjectMapper objectMapper
    ) {
        this.objectMapper = Objects.requireNonNull(
                objectMapper,
                "ObjectMapper cannot be null"
        );
    }

    @Override
    public void commence(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException exception
    ) throws IOException {

        writeProblem(
                request,
                response,
                HttpStatus.UNAUTHORIZED,
                "Authentication Required",
                "Authentication is required to access this resource",
                "authentication-required"
        );
    }

    @Override
    public void handle(
            HttpServletRequest request,
            HttpServletResponse response,
            org.springframework.security.access.AccessDeniedException exception
    ) throws IOException, ServletException {

        writeProblem(
                request,
                response,
                HttpStatus.FORBIDDEN,
                "Access Denied",
                "You do not have permission to access this resource",
                "access-denied"
        );
    }

    private void writeProblem(
            HttpServletRequest request,
            HttpServletResponse response,
            HttpStatus status,
            String title,
            String detail,
            String errorType
    ) throws IOException {

        ProblemDetail problemDetail =
                ProblemDetail.forStatusAndDetail(
                        status,
                        detail
                );

        problemDetail.setTitle(title);
        problemDetail.setType(
                URI.create(
                        ERROR_BASE_URL + errorType
                )
        );
        problemDetail.setInstance(
                URI.create(
                        request.getRequestURI()
                )
        );
        problemDetail.setProperty(
                "timestamp",
                Instant.now()
        );

        response.setStatus(status.value());
        response.setContentType(
                MediaType.APPLICATION_PROBLEM_JSON_VALUE
        );
        response.setCharacterEncoding(
                StandardCharsets.UTF_8.name()
        );

        objectMapper.writeValue(
                response.getOutputStream(),
                problemDetail
        );
    }
}