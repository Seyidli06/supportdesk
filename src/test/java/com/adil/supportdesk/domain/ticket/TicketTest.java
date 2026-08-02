package com.adil.supportdesk.domain.ticket;

import com.adil.supportdesk.domain.ticket.exception.InvalidStatusTransitionException;
import com.adil.supportdesk.domain.ticket.exception.TicketClosedException;
import com.adil.supportdesk.domain.ticket.model.Ticket;
import com.adil.supportdesk.domain.ticket.model.TicketPriority;
import com.adil.supportdesk.domain.ticket.model.TicketStatus;
import com.adil.supportdesk.domain.ticket.valueobject.TicketId;
import com.adil.supportdesk.domain.user.valueobject.UserId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class TicketTest {

    private Ticket ticket;
    private UserId requesterId;
    private Instant createdAt;

    @BeforeEach
    void setUp() {
        requesterId = UserId.generate();
        createdAt = Instant.parse(
                "2026-08-02T12:00:00Z"
        );

        ticket = new Ticket(
                TicketId.generate(),
                requesterId,
                "System Crash",
                "Application crashes on launch",
                TicketPriority.HIGH,
                createdAt
        );
    }

    @Test
    @DisplayName(
            "New ticket should contain requester, priority and OPEN status"
    )
    void newTicketShouldContainInitialValues() {
        assertEquals(
                requesterId,
                ticket.getRequesterId()
        );
        assertEquals(
                TicketPriority.HIGH,
                ticket.getPriority()
        );
        assertEquals(
                TicketStatus.OPEN,
                ticket.getStatus()
        );
        assertNull(ticket.getAssignedAgentId());
        assertNull(ticket.getResolvedAt());
        assertNull(ticket.getClosedAt());
    }

    @Test
    @DisplayName("Ticket should be assigned to an agent")
    void ticketShouldBeAssignedToAgent() {
        UserId agentId = UserId.generate();
        Instant assignedAt = createdAt.plusSeconds(60);

        ticket.assignTo(agentId, assignedAt);

        assertEquals(
                agentId,
                ticket.getAssignedAgentId()
        );
        assertTrue(ticket.isAssigned());
        assertTrue(ticket.isAssignedTo(agentId));
        assertEquals(
                assignedAt,
                ticket.getUpdatedAt()
        );
    }

    @Test
    @DisplayName(
            "Ticket should follow complete status flow"
    )
    void ticketShouldFollowCompleteStatusFlow() {
        ticket.transitionTo(
                TicketStatus.IN_PROGRESS,
                createdAt.plusSeconds(60)
        );
        ticket.transitionTo(
                TicketStatus.WAITING_CUSTOMER,
                createdAt.plusSeconds(120)
        );
        ticket.transitionTo(
                TicketStatus.IN_PROGRESS,
                createdAt.plusSeconds(180)
        );
        ticket.transitionTo(
                TicketStatus.RESOLVED,
                createdAt.plusSeconds(240)
        );
        ticket.transitionTo(
                TicketStatus.CLOSED,
                createdAt.plusSeconds(300)
        );

        assertEquals(
                TicketStatus.CLOSED,
                ticket.getStatus()
        );
        assertEquals(
                createdAt.plusSeconds(240),
                ticket.getResolvedAt()
        );
        assertEquals(
                createdAt.plusSeconds(300),
                ticket.getClosedAt()
        );
    }

    @Test
    @DisplayName(
            "Reopening a resolved ticket should clear resolvedAt"
    )
    void reopeningShouldClearResolvedAt() {
        ticket.transitionTo(
                TicketStatus.IN_PROGRESS,
                createdAt.plusSeconds(60)
        );
        ticket.transitionTo(
                TicketStatus.RESOLVED,
                createdAt.plusSeconds(120)
        );

        assertNotNull(ticket.getResolvedAt());

        ticket.transitionTo(
                TicketStatus.IN_PROGRESS,
                createdAt.plusSeconds(180)
        );

        assertNull(ticket.getResolvedAt());
    }

    @Test
    @DisplayName(
            "Ticket should reject invalid transition"
    )
    void ticketShouldRejectInvalidTransition() {
        assertThrows(
                InvalidStatusTransitionException.class,
                () -> ticket.transitionTo(
                        TicketStatus.RESOLVED,
                        createdAt.plusSeconds(60)
                )
        );
    }

    @Test
    @DisplayName(
            "Comment should contain author UserId"
    )
    void commentShouldContainAuthorId() {
        UserId authorId = UserId.generate();

        ticket.addComment(
                authorId,
                "Investigating application logs",
                createdAt.plusSeconds(60)
        );

        assertEquals(1, ticket.getComments().size());
        assertEquals(
                authorId,
                ticket.getComments()
                        .getFirst()
                        .getAuthorId()
        );
    }

    @Test
    @DisplayName(
            "Closed ticket should reject assignment"
    )
    void closedTicketShouldRejectAssignment() {
        closeTicket();

        assertThrows(
                TicketClosedException.class,
                () -> ticket.assignTo(
                        UserId.generate(),
                        createdAt.plusSeconds(300)
                )
        );
    }

    @Test
    @DisplayName(
            "SLA due date should be after creation"
    )
    void slaDueDateShouldBeAfterCreation() {
        assertThrows(
                IllegalArgumentException.class,
                () -> ticket.defineSlaDueAt(
                        createdAt.minusSeconds(1)
                )
        );

        Instant validDueDate =
                createdAt.plusSeconds(3600);

        ticket.defineSlaDueAt(validDueDate);

        assertEquals(
                validDueDate,
                ticket.getSlaDueAt()
        );
    }

    private void closeTicket() {
        ticket.transitionTo(
                TicketStatus.IN_PROGRESS,
                createdAt.plusSeconds(60)
        );
        ticket.transitionTo(
                TicketStatus.RESOLVED,
                createdAt.plusSeconds(120)
        );
        ticket.transitionTo(
                TicketStatus.CLOSED,
                createdAt.plusSeconds(180)
        );
    }
}