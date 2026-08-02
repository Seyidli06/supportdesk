package com.adil.supportdesk.adapter.in.web.dto;

import com.adil.supportdesk.application.ticket.get.TicketResult;

import java.time.Instant;

public record TicketResponse(
        String id,
        String title,
        String description,
        String status,
        Instant createdAt
) {
    public static TicketResponse fromDomain(TicketResult result) {
        return new TicketResponse(
                result.id(),
                result.title(),
                result.description(),
                result.status().name(),
                result.createdAt()
        );
    }
}