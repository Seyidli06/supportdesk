package com.adil.supportdesk.application.ticket.assign;

public record AssignTicketCommand(
        String ticketId,
        String agentId
) {

    public AssignTicketCommand {
        if (ticketId == null || ticketId.isBlank()) {
            throw new IllegalArgumentException(
                    "Ticket id cannot be empty"
            );
        }

        if (agentId == null || agentId.isBlank()) {
            throw new IllegalArgumentException(
                    "Agent id cannot be empty"
            );
        }
    }
}