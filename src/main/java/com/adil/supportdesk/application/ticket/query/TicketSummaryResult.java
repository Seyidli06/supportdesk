package com.adil.supportdesk.application.ticket.query;

import com.adil.supportdesk.domain.ticket.model.TicketPriority;
import com.adil.supportdesk.domain.ticket.model.TicketStatus;

import java.time.Instant;

public record TicketSummaryResult(
        String id,
        String title,
        TicketPriority priority,
        TicketStatus status,
        String requesterId,
        String assignedAgentId,
        Instant createdAt,
        Instant updatedAt,
        Instant slaDueAt
) {
}