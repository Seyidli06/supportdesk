package com.adil.supportdesk.application.ticket.get;

import com.adil.supportdesk.domain.ticket.model.Ticket;
import com.adil.supportdesk.domain.ticket.model.TicketPriority;
import com.adil.supportdesk.domain.ticket.model.TicketStatus;

import java.time.Instant;
import java.util.List;

public record TicketResult(
        String id,
        String title,
        String description,
        TicketPriority priority,
        TicketStatus status,
        String requesterId,
        String assignedAgentId,
        List<CommentResult> comments,
        Instant createdAt,
        Instant updatedAt,
        Instant resolvedAt,
        Instant closedAt,
        Instant slaDueAt
) {

    public static TicketResult from(Ticket ticket) {
        List<CommentResult> commentResults =
                ticket.getComments()
                        .stream()
                        .map(comment -> new CommentResult(
                                comment.getId()
                                        .getValue()
                                        .toString(),
                                comment.getAuthorId()
                                        .value()
                                        .toString(),
                                comment.getContent(),
                                comment.getCreatedAt()
                        ))
                        .toList();

        String assignedAgentId =
                ticket.getAssignedAgentId() == null
                        ? null
                        : ticket.getAssignedAgentId()
                        .value()
                        .toString();

        return new TicketResult(
                ticket.getId().getValue().toString(),
                ticket.getTitle(),
                ticket.getDescription(),
                ticket.getPriority(),
                ticket.getStatus(),
                ticket.getRequesterId()
                        .value()
                        .toString(),
                assignedAgentId,
                commentResults,
                ticket.getCreatedAt(),
                ticket.getUpdatedAt(),
                ticket.getResolvedAt(),
                ticket.getClosedAt(),
                ticket.getSlaDueAt()
        );
    }

    public record CommentResult(
            String id,
            String authorId,
            String content,
            Instant createdAt
    ) {
    }
}