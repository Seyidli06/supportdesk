package com.adil.supportdesk.application.ticket.comment;

public record AddCommentCommand(
        String ticketId,
        String content
) {

    public AddCommentCommand {
        if (ticketId == null || ticketId.isBlank()) {
            throw new IllegalArgumentException(
                    "Ticket id cannot be empty"
            );
        }

        if (content == null || content.isBlank()) {
            throw new IllegalArgumentException(
                    "Comment content cannot be empty"
            );
        }
    }
}