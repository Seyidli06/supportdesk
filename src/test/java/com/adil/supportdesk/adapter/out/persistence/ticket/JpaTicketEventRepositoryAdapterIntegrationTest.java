package com.adil.supportdesk.adapter.out.persistence.ticket;

import com.adil.supportdesk.application.port.out.TicketEventRepository;
import com.adil.supportdesk.domain.ticket.model.TicketEvent;
import com.adil.supportdesk.domain.ticket.model.TicketEventType;
import com.adil.supportdesk.domain.ticket.valueobject.TicketId;
import com.adil.supportdesk.domain.user.valueobject.UserId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

@SpringBootTest
@Transactional
class JpaTicketEventRepositoryAdapterIntegrationTest {

    private static final Instant BASE_TIME =
            Instant.parse(
                    "2026-08-05T08:00:00Z"
            );

    @Autowired
    private TicketEventRepository
            eventRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    @DisplayName(
            "Ticket events should be stored "
                    + "in chronological order"
    )
    void ticketEventsShouldBeStoredChronologically() {
        UUID actorId = UUID.randomUUID();
        UUID ticketId = UUID.randomUUID();

        insertUser(actorId);
        insertTicket(ticketId, actorId);

        TicketEvent createdEvent =
                TicketEvent.create(
                        new TicketId(ticketId),
                        UserId.of(actorId),
                        TicketEventType.TICKET_CREATED,
                        null,
                        "OPEN",
                        BASE_TIME.plusSeconds(1)
                );

        TicketEvent statusChangedEvent =
                TicketEvent.create(
                        new TicketId(ticketId),
                        UserId.of(actorId),
                        TicketEventType.STATUS_CHANGED,
                        "OPEN",
                        "IN_PROGRESS",
                        BASE_TIME.plusSeconds(2)
                );

        TicketEvent savedCreatedEvent =
                eventRepository.save(
                        createdEvent
                );

        TicketEvent savedStatusEvent =
                eventRepository.save(
                        statusChangedEvent
                );

        List<TicketEvent> events =
                eventRepository.findByTicketId(
                        new TicketId(ticketId)
                );

        assertEquals(
                2,
                events.size()
        );

        assertEquals(
                savedCreatedEvent.id(),
                events.get(0).id()
        );

        assertEquals(
                TicketEventType.TICKET_CREATED,
                events.get(0).type()
        );

        assertNull(
                events.get(0).previousValue()
        );

        assertEquals(
                "OPEN",
                events.get(0).newValue()
        );

        assertEquals(
                savedStatusEvent.id(),
                events.get(1).id()
        );

        assertEquals(
                TicketEventType.STATUS_CHANGED,
                events.get(1).type()
        );

        assertEquals(
                "OPEN",
                events.get(1).previousValue()
        );

        assertEquals(
                "IN_PROGRESS",
                events.get(1).newValue()
        );

        assertEquals(
                actorId,
                events.get(1)
                        .actorId()
                        .value()
        );
    }

    private void insertUser(
            UUID userId
    ) {
        jdbcTemplate.update(
                """
                INSERT INTO users (
                    id,
                    email,
                    password_hash,
                    full_name,
                    created_at,
                    token_version
                )
                VALUES (?, ?, ?, ?, ?, ?)
                """,
                userId,
                "ticket-event-"
                        + userId
                        + "@integration.test",
                "integration-password-hash",
                "Ticket Event Actor",
                Timestamp.from(BASE_TIME),
                0L
        );
    }

    private void insertTicket(
            UUID ticketId,
            UUID requesterId
    ) {
        jdbcTemplate.update(
                """
                INSERT INTO tickets (
                    id,
                    title,
                    description,
                    status,
                    priority,
                    requester_id,
                    version,
                    created_at,
                    updated_at
                )
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                """,
                ticketId,
                "Audit history test ticket",
                "Ticket used for audit history test",
                "OPEN",
                "MEDIUM",
                requesterId,
                0L,
                Timestamp.from(BASE_TIME),
                Timestamp.from(BASE_TIME)
        );
    }
}