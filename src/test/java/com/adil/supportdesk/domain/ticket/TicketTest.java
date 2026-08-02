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

import static org.junit.jupiter.api.Assertions.*;

class TicketTest {

    private Ticket ticket;
    private final Instant now = Instant.now();

    @BeforeEach
    void setUp() {
        ticket = new Ticket(TicketId.generate(), "System Crash", "App crashes on launch", now);
    }

    @Test
    @DisplayName("New ticket should start in OPEN status")
    void newTicketShouldBeOpen() {
        assertEquals(TicketStatus.OPEN, ticket.getStatus());
        assertTrue(ticket.getComments().isEmpty());
    }

    @Test
    @DisplayName("Should successfully transition status from OPEN -> IN_PROGRESS -> RESOLVED -> CLOSED")
    void validStatusTransitions() {
        ticket.transitionTo(TicketStatus.IN_PROGRESS, now);
        assertEquals(TicketStatus.IN_PROGRESS, ticket.getStatus());

        ticket.transitionTo(TicketStatus.RESOLVED, now);
        assertEquals(TicketStatus.RESOLVED, ticket.getStatus());

        ticket.transitionTo(TicketStatus.CLOSED, now);
        assertEquals(TicketStatus.CLOSED, ticket.getStatus());
    }

    @Test
    @DisplayName("Should throw exception when resolving directly from OPEN status")
    void invalidDirectResolution() {
        assertThrows(InvalidStatusTransitionException.class,
                () -> ticket.transitionTo(TicketStatus.RESOLVED, now));
    }

    @Test
    @DisplayName("Should allow adding comments when ticket is open or in progress")
    void addCommentSuccessfully() {
        ticket.addComment("Alice", "Investigating logs...", now);
        assertEquals(1, ticket.getComments().size());
        assertEquals("Alice", ticket.getComments().get(0).getAuthor());
    }

    @Test
    @DisplayName("Should throw exception when adding comment to a CLOSED ticket")
    void cannotCommentOnClosedTicket() {
        ticket.transitionTo(TicketStatus.CLOSED, now);
        assertThrows(TicketClosedException.class,
                () -> ticket.addComment("Bob", "Late comment", now));
    }
}