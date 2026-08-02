package com.adil.supportdesk.application.ticket.get;

import com.adil.supportdesk.domain.ticket.model.Ticket;
import com.adil.supportdesk.domain.ticket.model.TicketStatus;

import java.time.Instant;
import java.util.List;

public record TicketResult(
        String id,
        String title,
        String description,
        TicketStatus status,
        List<CommentResult> comments,
        Instant createdAt,
        Instant updatedAt
) {
    public static TicketResult from(Ticket ticket) {
        List<CommentResult> commentResults = ticket.getComments().stream()
                .map(c -> new CommentResult(c.getId().getValue().toString(), c.getAuthor(), c.getContent(), c.getCreatedAt()))
                .toList();

        return new TicketResult(
                ticket.getId().getValue().toString(),
                ticket.getTitle(),
                ticket.getDescription(),
                ticket.getStatus(),
                commentResults,
                ticket.getCreatedAt(),
                ticket.getUpdatedAt()
        );
    }

    public record CommentResult(
            String id,
            String author,
            String content,
            Instant createdAt
    ) {}
}