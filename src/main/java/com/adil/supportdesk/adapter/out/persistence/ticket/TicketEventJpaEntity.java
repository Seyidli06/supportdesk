package com.adil.supportdesk.adapter.out.persistence.ticket;

import com.adil.supportdesk.domain.ticket.model.TicketEventType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "ticket_events")
public class TicketEventJpaEntity {

    @Id
    @Column(
            name = "id",
            nullable = false
    )
    private UUID id;

    @Column(
            name = "ticket_id",
            nullable = false
    )
    private UUID ticketId;

    @Column(
            name = "actor_id",
            nullable = false
    )
    private UUID actorId;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "event_type",
            nullable = false,
            length = 40
    )
    private TicketEventType eventType;

    @Column(
            name = "previous_value",
            length = 255
    )
    private String previousValue;

    @Column(
            name = "new_value",
            length = 255
    )
    private String newValue;

    @Column(
            name = "created_at",
            nullable = false
    )
    private Instant createdAt;

    protected TicketEventJpaEntity() {
    }

    TicketEventJpaEntity(
            UUID id,
            UUID ticketId,
            UUID actorId,
            TicketEventType eventType,
            String previousValue,
            String newValue,
            Instant createdAt
    ) {
        this.id = id;
        this.ticketId = ticketId;
        this.actorId = actorId;
        this.eventType = eventType;
        this.previousValue = previousValue;
        this.newValue = newValue;
        this.createdAt = createdAt;
    }

    UUID getId() {
        return id;
    }

    UUID getTicketId() {
        return ticketId;
    }

    UUID getActorId() {
        return actorId;
    }

    TicketEventType getEventType() {
        return eventType;
    }

    String getPreviousValue() {
        return previousValue;
    }

    String getNewValue() {
        return newValue;
    }

    Instant getCreatedAt() {
        return createdAt;
    }
}