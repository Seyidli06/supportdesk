package com.adil.supportdesk.adapter.out.persistence.ticket;

import com.adil.supportdesk.application.port.out.TicketRepository;
import com.adil.supportdesk.domain.ticket.model.Ticket;
import com.adil.supportdesk.domain.ticket.model.TicketPriority;
import com.adil.supportdesk.domain.ticket.model.TicketStatus;
import com.adil.supportdesk.domain.ticket.valueobject.TicketId;
import com.adil.supportdesk.domain.user.valueobject.UserId;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

@SpringBootTest
@Transactional
class JpaTicketRepositoryAdapterIntegrationTest {

    private static final Instant CREATED_AT =
            Instant.parse("2026-08-02T15:00:00Z");

    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private EntityManager entityManager;

    private UserId requesterId;
    private UserId agentId;

    @BeforeEach
    void setUp() {
        requesterId = UserId.generate();
        agentId = UserId.generate();

        insertUser(
                requesterId,
                "Requester User"
        );

        insertUser(
                agentId,
                "Support Agent"
        );

        jdbcTemplate.update(
                """
                INSERT INTO user_roles (
                    user_id,
                    role
                )
                VALUES (?, ?)
                """,
                agentId.value(),
                "AGENT"
        );
    }

    @Test
    @DisplayName(
            "Ticket aggregate should be saved and restored from PostgreSQL"
    )
    void shouldSaveAndRestoreTicketAggregate() {
        Ticket ticket = new Ticket(
                TicketId.generate(),
                requesterId,
                "Production database failure",
                "Backend cannot connect to PostgreSQL",
                TicketPriority.URGENT,
                CREATED_AT
        );

        Ticket savedTicket =
                ticketRepository.save(ticket);

        savedTicket.assignTo(
                agentId,
                CREATED_AT.plusSeconds(60)
        );

        savedTicket.addComment(
                requesterId,
                "The problem still continues",
                CREATED_AT.plusSeconds(120)
        );

        savedTicket.transitionTo(
                TicketStatus.IN_PROGRESS,
                CREATED_AT.plusSeconds(180)
        );

        ticketRepository.save(savedTicket);

        entityManager.flush();
        entityManager.clear();

        Ticket restoredTicket = ticketRepository
                .findById(savedTicket.getId())
                .orElseThrow();

        assertEquals(
                savedTicket.getId(),
                restoredTicket.getId()
        );

        assertEquals(
                requesterId,
                restoredTicket.getRequesterId()
        );

        assertEquals(
                agentId,
                restoredTicket.getAssignedAgentId()
        );

        assertEquals(
                TicketPriority.URGENT,
                restoredTicket.getPriority()
        );

        assertEquals(
                TicketStatus.IN_PROGRESS,
                restoredTicket.getStatus()
        );

        assertEquals(
                1,
                restoredTicket.getComments().size()
        );

        assertEquals(
                requesterId,
                restoredTicket
                        .getComments()
                        .getFirst()
                        .getAuthorId()
        );

        assertEquals(
                "The problem still continues",
                restoredTicket
                        .getComments()
                        .getFirst()
                        .getContent()
        );

        assertNotNull(
                restoredTicket.getUpdatedAt()
        );

        assertTrue(
                restoredTicket.getUpdatedAt()
                        .isAfter(
                                restoredTicket.getCreatedAt()
                        )
        );
    }

    private void insertUser(
            UserId userId,
            String fullName
    ) {
        String email =
                userId.value()
                        + "@supportdesk-test.local";

        OffsetDateTime createdAt =
                OffsetDateTime.ofInstant(
                        CREATED_AT,
                        ZoneOffset.UTC
                );

        jdbcTemplate.update(
                """
                INSERT INTO users (
                    id,
                    email,
                    password_hash,
                    full_name,
                    created_at
                )
                VALUES (?, ?, ?, ?, ?)
                """,
                userId.value(),
                email,
                "test-password-hash",
                fullName,
                createdAt
        );
    }
}