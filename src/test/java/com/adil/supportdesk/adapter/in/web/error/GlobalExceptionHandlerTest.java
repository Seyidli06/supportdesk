package com.adil.supportdesk.adapter.in.web.error;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.ProblemDetail;

import java.net.URI;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler =
            new GlobalExceptionHandler();

    @Test
    @DisplayName(
            "Optimistic locking failure should return 409 Problem Details"
    )
    void optimisticLockingFailureShouldReturn409() {
        OptimisticLockingFailureException exception =
                new OptimisticLockingFailureException(
                        "Database optimistic lock failure"
                );

        ProblemDetail problemDetail =
                handler.handleOptimisticLockingFailure(
                        exception
                );

        assertEquals(
                409,
                problemDetail.getStatus()
        );

        assertEquals(
                "Concurrent Modification",
                problemDetail.getTitle()
        );

        assertEquals(
                "The resource was modified by another request. "
                        + "Reload it and try again.",
                problemDetail.getDetail()
        );

        assertEquals(
                URI.create(
                        "https://supportdesk.com/errors/"
                                + "concurrent-modification"
                ),
                problemDetail.getType()
        );

        assertNotNull(
                problemDetail.getProperties()
        );

        assertNotNull(
                problemDetail
                        .getProperties()
                        .get("timestamp")
        );
    }
}