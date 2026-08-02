package com.adil.supportdesk.application.ticket.comment;

public record AddCommentCommand(
        String ticketId,
        String author,
        String content
) {
    public AddCommentCommand {
        if (ticketId == null || ticketId.isBlank()) {
            throw new IllegalArgumentException("TicketId cannot be null or empty");
        }
        if (author == null || author.isBlank()) {
            throw new IllegalArgumentException("Author cannot be null or empty");
        }
        if (content == null || content.isBlank()) {
            throw new IllegalArgumentException("Content cannot be null or empty");
        }
    }
}