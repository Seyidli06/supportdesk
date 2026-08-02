package com.adil.supportdesk.domain.ticket;

import com.adil.supportdesk.domain.ticket.exception.InvalidStatusTransitionException;
import com.adil.supportdesk.domain.ticket.exception.TicketClosedException;
import com.adil.supportdesk.domain.ticket.model.Ticket;
import com.adil.supportdesk.domain.ticket.model.TicketStatus;
import com.adil.supportdesk.domain.ticket.valueobject.TicketId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TicketTest {

    private Ticket ticket;
    private Instant now;

    @BeforeEach
    void setUp() {
        now = Instant.parse("2026-08-02T12:00:00Z");

        ticket = new Ticket(
                TicketId.generate(),
                "System Crash",
                "Application crashes on launch",
                now
        );
    }

    @Test
    @DisplayName("New ticket should start with OPEN status")
    void newTicketShouldStartWithOpenStatus() {
        assertEquals(TicketStatus.OPEN, ticket.getStatus());
        assertTrue(ticket.getComments().isEmpty());
    }

    @Test
    @DisplayName("Ticket should follow valid status transition flow")
    void shouldFollowValidStatusTransitionFlow() {
        ticket.transitionTo(
                TicketStatus.IN_PROGRESS,
                now.plusSeconds(60)
        );

        assertEquals(
                TicketStatus.IN_PROGRESS,
                ticket.getStatus()
        );

        ticket.transitionTo(
                TicketStatus.WAITING_CUSTOMER,
                now.plusSeconds(120)
        );

        assertEquals(
                TicketStatus.WAITING_CUSTOMER,
                ticket.getStatus()
        );

        ticket.transitionTo(
                TicketStatus.IN_PROGRESS,
                now.plusSeconds(180)
        );

        assertEquals(
                TicketStatus.IN_PROGRESS,
                ticket.getStatus()
        );

        ticket.transitionTo(
                TicketStatus.RESOLVED,
                now.plusSeconds(240)
        );

        assertEquals(
                TicketStatus.RESOLVED,
                ticket.getStatus()
        );

        ticket.transitionTo(
                TicketStatus.CLOSED,
                now.plusSeconds(300)
        );

        assertEquals(
                TicketStatus.CLOSED,
                ticket.getStatus()
        );
    }

    @Test
    @DisplayName("Resolved ticket should be reopenable")
    void resolvedTicketShouldBeReopenable() {
        ticket.transitionTo(
                TicketStatus.IN_PROGRESS,
                now.plusSeconds(60)
        );

        ticket.transitionTo(
                TicketStatus.RESOLVED,
                now.plusSeconds(120)
        );

        ticket.transitionTo(
                TicketStatus.IN_PROGRESS,
                now.plusSeconds(180)
        );

        assertEquals(
                TicketStatus.IN_PROGRESS,
                ticket.getStatus()
        );
    }

    @Test
    @DisplayName("Ticket should not transition directly from OPEN to RESOLVED")
    void shouldRejectOpenToResolvedTransition() {
        assertThrows(
                InvalidStatusTransitionException.class,
                () -> ticket.transitionTo(
                        TicketStatus.RESOLVED,
                        now.plusSeconds(60)
                )
        );
    }

    @Test
    @DisplayName("Ticket should not transition directly from OPEN to CLOSED")
    void shouldRejectOpenToClosedTransition() {
        assertThrows(
                InvalidStatusTransitionException.class,
                () -> ticket.transitionTo(
                        TicketStatus.CLOSED,
                        now.plusSeconds(60)
                )
        );
    }

    @Test
    @DisplayName("Ticket should not transition directly from OPEN to WAITING_CUSTOMER")
    void shouldRejectOpenToWaitingCustomerTransition() {
        assertThrows(
                InvalidStatusTransitionException.class,
                () -> ticket.transitionTo(
                        TicketStatus.WAITING_CUSTOMER,
                        now.plusSeconds(60)
                )
        );
    }

    @Test
    @DisplayName("Comment should be added to an active ticket")
    void shouldAddCommentToActiveTicket() {
        ticket.addComment(
                "Alice",
                "Investigating application logs",
                now.plusSeconds(60)
        );

        assertEquals(1, ticket.getComments().size());
        assertEquals(
                "Alice",
                ticket.getComments().getFirst().getAuthor()
        );
    }

    @Test
    @DisplayName("Comment should not be added to a closed ticket")
    void shouldRejectCommentOnClosedTicket() {
        closeTicket();

        assertThrows(
                TicketClosedException.class,
                () -> ticket.addComment(
                        "Bob",
                        "Late comment",
                        now.plusSeconds(300)
                )
        );
    }

    @Test
    @DisplayName("Closed ticket should not allow another status transition")
    void closedTicketShouldRejectAnotherTransition() {
        closeTicket();

        assertThrows(
                TicketClosedException.class,
                () -> ticket.transitionTo(
                        TicketStatus.IN_PROGRESS,
                        now.plusSeconds(300)
                )
        );
    }

    private void closeTicket() {
        ticket.transitionTo(
                TicketStatus.IN_PROGRESS,
                now.plusSeconds(60)
        );

        ticket.transitionTo(
                TicketStatus.RESOLVED,
                now.plusSeconds(120)
        );

        ticket.transitionTo(
                TicketStatus.CLOSED,
                now.plusSeconds(180)
        );
    }
}