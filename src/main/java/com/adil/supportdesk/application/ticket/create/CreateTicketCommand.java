package com.adil.supportdesk.application.ticket.create;

public record CreateTicketCommand(
        String title,
        String description
) {
    public CreateTicketCommand {
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("Title cannot be null or empty");
        }
    }
}