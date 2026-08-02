package com.adil.supportdesk.domain.ticket.model;

import com.adil.supportdesk.domain.ticket.exception.InvalidStatusTransitionException;
import com.adil.supportdesk.domain.ticket.exception.TicketClosedException;
import com.adil.supportdesk.domain.ticket.policy.TicketStatusPolicy;
import com.adil.supportdesk.domain.ticket.valueobject.CommentId;
import com.adil.supportdesk.domain.ticket.valueobject.TicketId;


import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class Ticket {
    private final TicketId id;
    private String title;
    private String description;
    private TicketStatus status;
    private final List<Comment> comments;
    private final Instant createdAt;
    private Instant updatedAt;

    public Ticket(TicketId id, String title, String description, Instant createdAt) {
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("Ticket title cannot be empty");
        }
        this.id = Objects.requireNonNull(id, "TicketId cannot be null");
        this.title = title;
        this.description = description != null ? description : "";
        this.status = TicketStatus.OPEN;
        this.comments = new ArrayList<>();
        this.createdAt = Objects.requireNonNull(createdAt, "CreatedAt cannot be null");
        this.updatedAt = createdAt;
    }

    public void transitionTo(TicketStatus newStatus, Instant now) {
        validateNotClosed("change status");
        if (!TicketStatusPolicy.canTransition(this.status, newStatus)) {
            throw new InvalidStatusTransitionException(this.status, newStatus);
        }
        this.status = newStatus;
        this.updatedAt = now;
    }

    public void addComment(String author, String content, Instant now) {
        validateNotClosed("add comment");
        Comment comment = new Comment(CommentId.generate(), author, content, now);
        this.comments.add(comment);
        this.updatedAt = now;
    }

    private void validateNotClosed(String action) {
        if (this.status == TicketStatus.CLOSED) {
            throw new TicketClosedException(action);
        }
    }

    public TicketId getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public TicketStatus getStatus() { return status; }
    public List<Comment> getComments() { return Collections.unmodifiableList(comments); }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}