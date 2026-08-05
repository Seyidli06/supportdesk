package com.adil.supportdesk.adapter.in.web.ticket.dto;

import com.adil.supportdesk.application.ticket.event.TicketEventResult;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public record TicketEventResponse(
        UUID id,
        String ticketId,
        String actorId,
        String type,
        String previousValue,
        String newValue,
        Instant createdAt
) {

    public static TicketEventResponse fromResult(
            TicketEventResult result
    ) {
        Objects.requireNonNull(
                result,
                "Ticket event result cannot be null"
        );

        return new TicketEventResponse(
                result.id(),
                result.ticketId(),
                result.actorId(),
                result.type().name(),
                result.previousValue(),
                result.newValue(),
                result.createdAt()
        );
    }
}