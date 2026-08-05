package com.adil.supportdesk.adapter.out.persistence.ticket;

import com.adil.supportdesk.application.port.out.TicketMutationRepository;
import com.adil.supportdesk.domain.ticket.model.Ticket;
import com.adil.supportdesk.domain.ticket.model.TicketEvent;
import com.adil.supportdesk.domain.ticket.model.TicketEventType;
import com.adil.supportdesk.domain.ticket.model.TicketPriority;
import com.adil.supportdesk.domain.ticket.valueobject.TicketId;
import com.adil.supportdesk.domain.user.valueobject.UserId;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
class JpaTicketMutationRepositoryAdapterIntegrationTest {

    private static final Instant NOW =
            Instant.parse(
                    "2026-08-05T12:00:00Z"
            );

    @Autowired
    private TicketMutationRepository
            ticketMutationRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private UUID requesterId;
    private UUID ticketId;

    @BeforeEach
    void setUp() {
        requesterId = UUID.randomUUID();
        ticketId = UUID.randomUUID();

        insertRequester();
    }

    @AfterEach
    void tearDown() {
        jdbcTemplate.update(
                """
                DELETE FROM ticket_events
                WHERE ticket_id = ?
                """,
                ticketId
        );

        jdbcTemplate.update(
                """
                DELETE FROM tickets
                WHERE id = ?
                """,
                ticketId
        );

        jdbcTemplate.update(
                """
                DELETE FROM users
                WHERE id = ?
                """,
                requesterId
        );
    }

    @Test
    @DisplayName(
            "Ticket and event should be stored "
                    + "in the same transaction"
    )
    void ticketAndEventShouldBeStoredTogether() {
        Ticket ticket = createTicket();

        TicketEvent event =
                TicketEvent.create(
                        ticket.getId(),
                        UserId.of(requesterId),
                        TicketEventType.TICKET_CREATED,
                        null,
                        "OPEN",
                        NOW
                );

        ticketMutationRepository.saveWithEvent(
                ticket,
                event
        );

        assertEquals(
                1,
                countTickets()
        );

        assertEquals(
                1,
                countEvents()
        );
    }

    @Test
    @DisplayName(
            "Ticket insert should roll back "
                    + "when event insert fails"
    )
    void ticketShouldRollBackWhenEventInsertFails() {
        Ticket ticket = createTicket();

        UserId missingActorId =
                UserId.generate();

        TicketEvent invalidEvent =
                TicketEvent.create(
                        ticket.getId(),
                        missingActorId,
                        TicketEventType.TICKET_CREATED,
                        null,
                        "OPEN",
                        NOW
                );

        assertThrows(
                DataIntegrityViolationException.class,
                () ->
                        ticketMutationRepository
                                .saveWithEvent(
                                        ticket,
                                        invalidEvent
                                )
        );

        assertEquals(
                0,
                countTickets()
        );

        assertEquals(
                0,
                countEvents()
        );
    }

    private Ticket createTicket() {
        return new Ticket(
                new TicketId(ticketId),
                UserId.of(requesterId),
                "Transactional audit test",
                "Ticket and event must be saved atomically",
                TicketPriority.HIGH,
                NOW
        );
    }

    private void insertRequester() {
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
                requesterId,
                "transaction-audit-"
                        + requesterId
                        + "@integration.test",
                "integration-password-hash",
                "Transaction Audit User",
                Timestamp.from(NOW),
                0L
        );
    }

    private int countTickets() {
        Integer count =
                jdbcTemplate.queryForObject(
                        """
                        SELECT COUNT(*)
                        FROM tickets
                        WHERE id = ?
                        """,
                        Integer.class,
                        ticketId
                );

        return count == null ? 0 : count;
    }

    private int countEvents() {
        Integer count =
                jdbcTemplate.queryForObject(
                        """
                        SELECT COUNT(*)
                        FROM ticket_events
                        WHERE ticket_id = ?
                        """,
                        Integer.class,
                        ticketId
                );

        return count == null ? 0 : count;
    }
}