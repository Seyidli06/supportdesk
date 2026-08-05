package com.adil.supportdesk.application.ticket.event;

import com.adil.supportdesk.domain.ticket.model.TicketEvent;
import com.adil.supportdesk.domain.ticket.model.TicketEventType;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public record TicketEventResult(
        UUID id,
        String ticketId,
        String actorId,
        TicketEventType type,
        String previousValue,
        String newValue,
        Instant createdAt
) {

    public TicketEventResult {
        Objects.requireNonNull(
                id,
                "Event id cannot be null"
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
                "Event type cannot be null"
        );

        Objects.requireNonNull(
                createdAt,
                "CreatedAt cannot be null"
        );
    }

    public static TicketEventResult from(
            TicketEvent event
    ) {
        Objects.requireNonNull(
                event,
                "Ticket event cannot be null"
        );

        return new TicketEventResult(
                event.id(),
                event.ticketId()
                        .getValue()
                        .toString(),
                event.actorId()
                        .value()
                        .toString(),
                event.type(),
                event.previousValue(),
                event.newValue(),
                event.createdAt()
        );
    }
}