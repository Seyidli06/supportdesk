package com.adil.supportdesk.adapter.out.persistence.ticket;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "ticket_comments")
public class TicketCommentJpaEntity {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @ManyToOne(
            fetch = FetchType.LAZY,
            optional = false
    )
    @JoinColumn(
            name = "ticket_id",
            nullable = false
    )
    private TicketJpaEntity ticket;

    @Column(
            name = "author_id",
            nullable = false
    )
    private UUID authorId;

    @Column(
            name = "content",
            nullable = false,
            columnDefinition = "TEXT"
    )
    private String content;

    @Column(
            name = "created_at",
            nullable = false
    )
    private Instant createdAt;

    protected TicketCommentJpaEntity() {
    }

    TicketCommentJpaEntity(
            UUID id,
            UUID authorId,
            String content,
            Instant createdAt
    ) {
        this.id = id;
        this.authorId = authorId;
        this.content = content;
        this.createdAt = createdAt;
    }

    void setTicket(TicketJpaEntity ticket) {
        this.ticket = ticket;
    }

    UUID getId() {
        return id;
    }

    UUID getAuthorId() {
        return authorId;
    }

    String getContent() {
        return content;
    }

    Instant getCreatedAt() {
        return createdAt;
    }
}