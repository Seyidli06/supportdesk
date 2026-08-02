package com.adil.supportdesk.adapter.in.web.dto;

import com.adil.supportdesk.application.ticket.get.TicketResult;

import java.time.Instant;

public record TicketResponse(
        String id,
        String title,
        String description,
        String priority,
        String status,
        String requesterId,
        String assignedAgentId,
        Instant createdAt,
        Instant updatedAt,
        Instant resolvedAt,
        Instant closedAt,
        Instant slaDueAt
) {

    public static TicketResponse fromResult(
            TicketResult result
    ) {
        return new TicketResponse(
                result.id(),
                result.title(),
                result.description(),
                result.priority().name(),
                result.status().name(),
                result.requesterId(),
                result.assignedAgentId(),
                result.createdAt(),
                result.updatedAt(),
                result.resolvedAt(),
                result.closedAt(),
                result.slaDueAt()
        );
    }
}