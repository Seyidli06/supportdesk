package com.adil.supportdesk.adapter.out.persistence.ticket;

import com.adil.supportdesk.domain.ticket.model.TicketPriority;
import com.adil.supportdesk.domain.ticket.model.TicketStatus;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "tickets")
public class TicketJpaEntity {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(
            name = "title",
            nullable = false,
            length = 255
    )
    private String title;

    @Column(
            name = "description",
            nullable = false,
            columnDefinition = "TEXT"
    )
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "status",
            nullable = false,
            length = 30
    )
    private TicketStatus status;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "priority",
            nullable = false,
            length = 30
    )
    private TicketPriority priority;

    @Column(
            name = "requester_id",
            nullable = false
    )
    private UUID requesterId;

    @Column(name = "assigned_agent_id")
    private UUID assignedAgentId;

    @Version
    @Column(name = "version", nullable = false)
    private Long version;

    @Column(
            name = "created_at",
            nullable = false
    )
    private Instant createdAt;

    @Column(
            name = "updated_at",
            nullable = false
    )
    private Instant updatedAt;

    @Column(name = "resolved_at")
    private Instant resolvedAt;

    @Column(name = "closed_at")
    private Instant closedAt;

    @Column(name = "sla_due_at")
    private Instant slaDueAt;

    @OneToMany(
            mappedBy = "ticket",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    @OrderBy("createdAt ASC, id ASC")
    private List<TicketCommentJpaEntity> comments =
            new ArrayList<>();

    protected TicketJpaEntity() {
    }

    TicketJpaEntity(UUID id) {
        this.id = id;
    }

    void addComment(
            TicketCommentJpaEntity comment
    ) {
        comment.setTicket(this);
        comments.add(comment);
    }

    UUID getId() {
        return id;
    }

    void setTitle(String title) {
        this.title = title;
    }

    String getTitle() {
        return title;
    }

    void setDescription(String description) {
        this.description = description;
    }

    String getDescription() {
        return description;
    }

    void setStatus(TicketStatus status) {
        this.status = status;
    }

    TicketStatus getStatus() {
        return status;
    }

    void setPriority(TicketPriority priority) {
        this.priority = priority;
    }

    TicketPriority getPriority() {
        return priority;
    }

    void setRequesterId(UUID requesterId) {
        this.requesterId = requesterId;
    }

    UUID getRequesterId() {
        return requesterId;
    }

    void setAssignedAgentId(UUID assignedAgentId) {
        this.assignedAgentId = assignedAgentId;
    }

    UUID getAssignedAgentId() {
        return assignedAgentId;
    }

    Long getVersion() {
        return version;
    }

    void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    Instant getCreatedAt() {
        return createdAt;
    }

    void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

    Instant getUpdatedAt() {
        return updatedAt;
    }

    void setResolvedAt(Instant resolvedAt) {
        this.resolvedAt = resolvedAt;
    }

    Instant getResolvedAt() {
        return resolvedAt;
    }

    void setClosedAt(Instant closedAt) {
        this.closedAt = closedAt;
    }

    Instant getClosedAt() {
        return closedAt;
    }

    void setSlaDueAt(Instant slaDueAt) {
        this.slaDueAt = slaDueAt;
    }

    Instant getSlaDueAt() {
        return slaDueAt;
    }

    List<TicketCommentJpaEntity> getComments() {
        return comments;
    }
}