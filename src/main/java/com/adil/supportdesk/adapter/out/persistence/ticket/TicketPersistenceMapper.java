package com.adil.supportdesk.adapter.out.persistence.ticket;

import com.adil.supportdesk.domain.ticket.model.Comment;
import com.adil.supportdesk.domain.ticket.model.Ticket;
import com.adil.supportdesk.domain.ticket.valueobject.CommentId;
import com.adil.supportdesk.domain.ticket.valueobject.TicketId;
import com.adil.supportdesk.domain.user.valueobject.UserId;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class TicketPersistenceMapper {

    public void updateEntity(
            Ticket source,
            TicketJpaEntity target
    ) {
        target.setTitle(source.getTitle());
        target.setDescription(source.getDescription());
        target.setStatus(source.getStatus());
        target.setPriority(source.getPriority());

        target.setRequesterId(
                source.getRequesterId().value()
        );

        target.setAssignedAgentId(
                source.getAssignedAgentId() == null
                        ? null
                        : source.getAssignedAgentId().value()
        );

        target.setCreatedAt(source.getCreatedAt());
        target.setUpdatedAt(source.getUpdatedAt());
        target.setResolvedAt(source.getResolvedAt());
        target.setClosedAt(source.getClosedAt());
        target.setSlaDueAt(source.getSlaDueAt());

        synchronizeComments(source, target);
    }

    public Ticket toDomain(
            TicketJpaEntity source
    ) {
        List<Comment> comments = source
                .getComments()
                .stream()
                .map(this::toDomainComment)
                .toList();

        UserId assignedAgentId =
                source.getAssignedAgentId() == null
                        ? null
                        : UserId.of(
                        source.getAssignedAgentId()
                );

        return Ticket.restore(
                new TicketId(source.getId()),
                UserId.of(source.getRequesterId()),
                assignedAgentId,
                source.getTitle(),
                source.getDescription(),
                source.getPriority(),
                source.getStatus(),
                comments,
                source.getCreatedAt(),
                source.getUpdatedAt(),
                source.getResolvedAt(),
                source.getClosedAt(),
                source.getSlaDueAt()
        );
    }

    private void synchronizeComments(
            Ticket source,
            TicketJpaEntity target
    ) {
        Set<UUID> domainCommentIds = source
                .getComments()
                .stream()
                .map(comment ->
                        comment.getId().getValue()
                )
                .collect(Collectors.toSet());

        target.getComments().removeIf(
                comment ->
                        !domainCommentIds.contains(
                                comment.getId()
                        )
        );

        Map<UUID, TicketCommentJpaEntity>
                existingComments = target
                .getComments()
                .stream()
                .collect(
                        Collectors.toMap(
                                TicketCommentJpaEntity::getId,
                                Function.identity()
                        )
                );

        for (Comment comment : source.getComments()) {
            UUID commentId =
                    comment.getId().getValue();

            if (existingComments.containsKey(commentId)) {
                continue;
            }

            TicketCommentJpaEntity newComment =
                    new TicketCommentJpaEntity(
                            commentId,
                            comment.getAuthorId().value(),
                            comment.getContent(),
                            comment.getCreatedAt()
                    );

            target.addComment(newComment);
        }
    }

    private Comment toDomainComment(
            TicketCommentJpaEntity source
    ) {
        return new Comment(
                new CommentId(source.getId()),
                UserId.of(source.getAuthorId()),
                source.getContent(),
                source.getCreatedAt()
        );
    }
}