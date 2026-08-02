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
        this(
                id,
                requesterId,
                null,
                title,
                description,
                priority,
                TicketStatus.OPEN,
                List.of(),
                createdAt,
                createdAt,
                null,
                null,
                null
        );
    }

    private Ticket(
            TicketId id,
            UserId requesterId,
            UserId assignedAgentId,
            String title,
            String description,
            TicketPriority priority,
            TicketStatus status,
            List<Comment> comments,
            Instant createdAt,
            Instant updatedAt,
            Instant resolvedAt,
            Instant closedAt,
            Instant slaDueAt
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

        this.status = Objects.requireNonNull(
                status,
                "Ticket status cannot be null"
        );

        this.createdAt = Objects.requireNonNull(
                createdAt,
                "CreatedAt cannot be null"
        );

        this.updatedAt = Objects.requireNonNull(
                updatedAt,
                "UpdatedAt cannot be null"
        );

        if (updatedAt.isBefore(createdAt)) {
            throw new IllegalArgumentException(
                    "UpdatedAt cannot be before CreatedAt"
            );
        }

        this.assignedAgentId = assignedAgentId;
        this.title = title.trim();
        this.description = description.trim();
        this.comments = new ArrayList<>(
                Objects.requireNonNull(
                        comments,
                        "Comments cannot be null"
                )
        );
        this.resolvedAt = resolvedAt;
        this.closedAt = closedAt;
        this.slaDueAt = slaDueAt;
    }

    public static Ticket restore(
            TicketId id,
            UserId requesterId,
            UserId assignedAgentId,
            String title,
            String description,
            TicketPriority priority,
            TicketStatus status,
            List<Comment> comments,
            Instant createdAt,
            Instant updatedAt,
            Instant resolvedAt,
            Instant closedAt,
            Instant slaDueAt
    ) {
        return new Ticket(
                id,
                requesterId,
                assignedAgentId,
                title,
                description,
                priority,
                status,
                comments,
                createdAt,
                updatedAt,
                resolvedAt,
                closedAt,
                slaDueAt
        );
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

        if (agentId.equals(assignedAgentId)) {
            return;
        }

        assignedAgentId = agentId;
        updatedAt = now;
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

        if (newStatus == status) {
            return;
        }

        if (!TicketStatusPolicy.canTransition(
                status,
                newStatus
        )) {
            throw new InvalidStatusTransitionException(
                    status,
                    newStatus
            );
        }

        TicketStatus previousStatus = status;

        status = newStatus;
        updatedAt = now;

        if (newStatus == TicketStatus.RESOLVED) {
            resolvedAt = now;
        }

        if (
                previousStatus == TicketStatus.RESOLVED
                        && newStatus == TicketStatus.IN_PROGRESS
        ) {
            resolvedAt = null;
        }

        if (newStatus == TicketStatus.CLOSED) {
            closedAt = now;
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

        comments.add(comment);
        updatedAt = now;
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

        if (newPriority == priority) {
            return;
        }

        priority = newPriority;
        updatedAt = now;
    }

    public void defineSlaDueAt(Instant dueAt) {
        validateNotClosed("define SLA due date");

        Objects.requireNonNull(
                dueAt,
                "SLA due date cannot be null"
        );

        if (!dueAt.isAfter(createdAt)) {
            throw new IllegalArgumentException(
                    "SLA due date must be after ticket creation time"
            );
        }

        slaDueAt = dueAt;
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

    private static void validateDescription(
            String description
    ) {
        if (
                description == null
                        || description.isBlank()
        ) {
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