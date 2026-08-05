package com.adil.supportdesk.application.ticket.changepriority;

import com.adil.supportdesk.domain.ticket.model.TicketPriority;

public record ChangeTicketPriorityCommand(
        String ticketId,
        TicketPriority newPriority
) {

    public ChangeTicketPriorityCommand {
        if (
                ticketId == null
                        || ticketId.isBlank()
        ) {
            throw new IllegalArgumentException(
                    "TicketId cannot be null or empty"
            );
        }

        if (newPriority == null) {
            throw new IllegalArgumentException(
                    "New priority cannot be null"
            );
        }
    }
}