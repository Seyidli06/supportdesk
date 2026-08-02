package com.adil.supportdesk.domain.ticket.model;

import com.adil.supportdesk.domain.ticket.exception.InvalidStatusTransitionException;
import com.adil.supportdesk.domain.ticket.exception.TicketClosedException;
import com.adil.supportdesk.domain.ticket.policy.TicketStatusPolicy;
import com.adil.supportdesk.domain.ticket.valueobject.CommentId;
import com.adil.supportdesk.domain.ticket.valueobject.TicketId;
import com.adil.supportdesk.domain.user.valueobject.UserId;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class Ticket {

    private static final int MAX_TITLE_LENGTH = 100;

    private final TicketId id;
    private final UserId requesterId;

    private UserId assignedAgentId;

    private String title;
    private String description;

    private TicketPriority priority;
    private TicketStatus status;

    private final List<Comment> comments;

    private final Instant createdAt;
    private Instant updatedAt;
    private Instant resolvedAt;
    private Instant closedAt;
    private Instant slaDueAt;

    public Ticket(
            TicketId id,
            UserId requesterId,
            String title,
            String description,
            TicketPriority priority,
            Instant createdAt
    ) {
        validateTitle(title);
        validateDescription(description);

        this.id = Objects.requireNonNull(
                id,
                "TicketId cannot be null"
        );
        this.requesterId = Objects.requireNonNull(
                requesterId,
                "RequesterId cannot be null"
        );
        this.priority = Objects.requireNonNull(
                priority,
                "Ticket priority cannot be null"
        );
        this.createdAt = Objects.requireNonNull(
                createdAt,
                "CreatedAt cannot be null"
        );

        this.title = title.trim();
        this.description = description.trim();
        this.status = TicketStatus.OPEN;
        this.comments = new ArrayList<>();
        this.updatedAt = createdAt;
    }

    public void assignTo(
            UserId agentId,
            Instant now
    ) {
        validateNotClosed("assign ticket");

        Objects.requireNonNull(
                agentId,
                "AgentId cannot be null"
        );
        validateTimestamp(now);

        if (agentId.equals(this.assignedAgentId)) {
            return;
        }

        this.assignedAgentId = agentId;
        this.updatedAt = now;
    }

    public void transitionTo(
            TicketStatus newStatus,
            Instant now
    ) {
        validateNotClosed("change status");

        Objects.requireNonNull(
                newStatus,
                "New status cannot be null"
        );
        validateTimestamp(now);

        if (newStatus == this.status) {
            return;
        }

        if (!TicketStatusPolicy.canTransition(
                this.status,
                newStatus
        )) {
            throw new InvalidStatusTransitionException(
                    this.status,
                    newStatus
            );
        }

        TicketStatus previousStatus = this.status;

        this.status = newStatus;
        this.updatedAt = now;

        if (newStatus == TicketStatus.RESOLVED) {
            this.resolvedAt = now;
        }

        if (
                previousStatus == TicketStatus.RESOLVED
                        && newStatus == TicketStatus.IN_PROGRESS
        ) {
            this.resolvedAt = null;
        }

        if (newStatus == TicketStatus.CLOSED) {
            this.closedAt = now;
        }
    }

    public void addComment(
            UserId authorId,
            String content,
            Instant now
    ) {
        validateNotClosed("add comment");
        validateTimestamp(now);

        Comment comment = new Comment(
                CommentId.generate(),
                authorId,
                content,
                now
        );

        this.comments.add(comment);
        this.updatedAt = now;
    }

    public void changePriority(
            TicketPriority newPriority,
            Instant now
    ) {
        validateNotClosed("change priority");

        Objects.requireNonNull(
                newPriority,
                "New priority cannot be null"
        );
        validateTimestamp(now);

        if (newPriority == this.priority) {
            return;
        }

        this.priority = newPriority;
        this.updatedAt = now;
    }

    public void defineSlaDueAt(Instant slaDueAt) {
        validateNotClosed("define SLA due date");

        Objects.requireNonNull(
                slaDueAt,
                "SLA due date cannot be null"
        );

        if (!slaDueAt.isAfter(createdAt)) {
            throw new IllegalArgumentException(
                    "SLA due date must be after ticket creation time"
            );
        }

        this.slaDueAt = slaDueAt;
    }

    public boolean isRequestedBy(UserId userId) {
        return requesterId.equals(userId);
    }

    public boolean isAssignedTo(UserId userId) {
        return assignedAgentId != null
                && assignedAgentId.equals(userId);
    }

    public boolean isAssigned() {
        return assignedAgentId != null;
    }

    private void validateNotClosed(String action) {
        if (status == TicketStatus.CLOSED) {
            throw new TicketClosedException(action);
        }
    }

    private void validateTimestamp(Instant timestamp) {
        Objects.requireNonNull(
                timestamp,
                "Operation timestamp cannot be null"
        );

        if (timestamp.isBefore(createdAt)) {
            throw new IllegalArgumentException(
                    "Operation timestamp cannot be before ticket creation time"
            );
        }
    }

    private static void validateTitle(String title) {
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException(
                    "Ticket title cannot be empty"
            );
        }

        if (title.trim().length() > MAX_TITLE_LENGTH) {
            throw new IllegalArgumentException(
                    "Ticket title cannot exceed "
                            + MAX_TITLE_LENGTH
                            + " characters"
            );
        }
    }

    private static void validateDescription(String description) {
        if (description == null || description.isBlank()) {
            throw new IllegalArgumentException(
                    "Ticket description cannot be empty"
            );
        }
    }

    public TicketId getId() {
        return id;
    }

    public UserId getRequesterId() {
        return requesterId;
    }

    public UserId getAssignedAgentId() {
        return assignedAgentId;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public TicketPriority getPriority() {
        return priority;
    }

    public TicketStatus getStatus() {
        return status;
    }

    public List<Comment> getComments() {
        return Collections.unmodifiableList(comments);
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public Instant getResolvedAt() {
        return resolvedAt;
    }

    public Instant getClosedAt() {
        return closedAt;
    }

    public Instant getSlaDueAt() {
        return slaDueAt;
    }
}