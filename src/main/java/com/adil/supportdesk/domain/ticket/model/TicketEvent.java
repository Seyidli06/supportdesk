package com.adil.supportdesk.domain.ticket.model;

import com.adil.supportdesk.domain.ticket.valueobject.TicketId;
import com.adil.supportdesk.domain.user.valueobject.UserId;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public record TicketEvent(
        UUID id,
        TicketId ticketId,
        UserId actorId,
        TicketEventType type,
        String previousValue,
        String newValue,
        Instant createdAt
) {

    private static final int MAX_VALUE_LENGTH = 255;

    public TicketEvent {
        Objects.requireNonNull(
                id,
                "Ticket event id cannot be null"
        );

        Objects.requireNonNull(
                ticketId,
                "Ticket id cannot be null"
        );

        Objects.requireNonNull(
                actorId,
                "Actor id cannot be null"
        );

        Objects.requireNonNull(
                type,
                "Ticket event type cannot be null"
        );

        Objects.requireNonNull(
                createdAt,
                "Ticket event creation time cannot be null"
        );

        previousValue = normalizeValue(
                previousValue,
                "Previous value"
        );

        newValue = normalizeValue(
                newValue,
                "New value"
        );
    }

    public static TicketEvent create(
            TicketId ticketId,
            UserId actorId,
            TicketEventType type,
            String previousValue,
            String newValue,
            Instant createdAt
    ) {
        return new TicketEvent(
                UUID.randomUUID(),
                ticketId,
                actorId,
                type,
                previousValue,
                newValue,
                createdAt
        );
    }

    private static String normalizeValue(
            String value,
            String fieldName
    ) {
        if (value == null) {
            return null;
        }

        String normalizedValue = value.trim();

        if (normalizedValue.isEmpty()) {
            throw new IllegalArgumentException(
                    fieldName + " cannot be blank"
            );
        }

        if (
                normalizedValue.length()
                        > MAX_VALUE_LENGTH
        ) {
            throw new IllegalArgumentException(
                    fieldName
                            + " cannot exceed "
                            + MAX_VALUE_LENGTH
                            + " characters"
            );
        }

        return normalizedValue;
    }
}