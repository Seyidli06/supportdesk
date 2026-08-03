package com.adil.supportdesk.adapter.in.web.ticket.dto;

import com.adil.supportdesk.application.ticket.query.TicketSummaryResult;

import java.time.Instant;

public record TicketSummaryResponse(
        String id,
        String title,
        String priority,
        String status,
        String requesterId,
        String assignedAgentId,
        Instant createdAt,
        Instant updatedAt,
        Instant slaDueAt
) {

    public static TicketSummaryResponse fromResult(
            TicketSummaryResult result
    ) {
        return new TicketSummaryResponse(
                result.id(),
                result.title(),
                result.priority().name(),
                result.status().name(),
                result.requesterId(),
                result.assignedAgentId(),
                result.createdAt(),
                result.updatedAt(),
                result.slaDueAt()
        );
    }
}