package com.adil.supportdesk.adapter.in.web.ticket.dto;

import com.adil.supportdesk.application.ticket.get.TicketResult;

import java.time.Instant;
import java.util.List;

public record TicketResponse(
        String id,
        String title,
        String description,
        String priority,
        String status,
        String requesterId,
        String assignedAgentId,
        List<CommentResponse> comments,
        Instant createdAt,
        Instant updatedAt,
        Instant resolvedAt,
        Instant closedAt,
        Instant slaDueAt
) {

    public static TicketResponse fromResult(
            TicketResult result
    ) {
        List<CommentResponse> comments =
                result.comments()
                        .stream()
                        .map(comment ->
                                new CommentResponse(
                                        comment.id(),
                                        comment.authorId(),
                                        comment.content(),
                                        comment.createdAt()
                                )
                        )
                        .toList();

        return new TicketResponse(
                result.id(),
                result.title(),
                result.description(),
                result.priority().name(),
                result.status().name(),
                result.requesterId(),
                result.assignedAgentId(),
                comments,
                result.createdAt(),
                result.updatedAt(),
                result.resolvedAt(),
                result.closedAt(),
                result.slaDueAt()
        );
    }

    public record CommentResponse(
            String id,
            String authorId,
            String content,
            Instant createdAt
    ) {
    }
}