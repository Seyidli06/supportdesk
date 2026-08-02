package com.adil.supportdesk.application.ticket.changestatus;

import com.adil.supportdesk.domain.ticket.model.TicketStatus;

public record ChangeTicketStatusCommand(
        String ticketId,
        TicketStatus newStatus
) {
    public ChangeTicketStatusCommand {
        if (ticketId == null || ticketId.isBlank()) {
            throw new IllegalArgumentException("TicketId cannot be null or empty");
        }
        if (newStatus == null) {
            throw new IllegalArgumentException("New status cannot be null");
        }
    }
}