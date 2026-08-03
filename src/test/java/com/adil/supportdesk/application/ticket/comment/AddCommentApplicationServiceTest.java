package com.adil.supportdesk.application.ticket.comment;

import com.adil.supportdesk.application.port.out.TicketRepository;
import com.adil.supportdesk.application.security.UnauthorizedAccessException;
import com.adil.supportdesk.application.security.UserContext;
import com.adil.supportdesk.application.security.UserRole;
import com.adil.supportdesk.application.ticket.comment.AddCommentApplicationService;
import com.adil.supportdesk.application.ticket.comment.AddCommentCommand;
import com.adil.supportdesk.application.ticket.get.TicketResult;
import com.adil.supportdesk.domain.ticket.exception.TicketClosedException;
import com.adil.supportdesk.domain.ticket.model.Ticket;
import com.adil.supportdesk.domain.ticket.model.TicketPriority;
import com.adil.supportdesk.domain.ticket.model.TicketStatus;
import com.adil.supportdesk.domain.ticket.valueobject.TicketId;
import com.adil.supportdesk.domain.user.valueobject.UserId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AddCommentApplicationServiceTest {

    private static final Instant NOW =
            Instant.parse("2026-08-02T16:00:00Z");

    @Mock
    private TicketRepository ticketRepository;

    private AddCommentApplicationService service;

    private TicketId ticketId;
    private UserId requesterId;
    private UserId agentId;
    private Ticket ticket;

    @BeforeEach
    void setUp() {
        Clock clock = Clock.fixed(
                NOW,
                ZoneOffset.UTC
        );

        service = new AddCommentApplicationService(
                ticketRepository,
                clock
        );

        ticketId = TicketId.generate();
        requesterId = UserId.generate();
        agentId = UserId.generate();

        ticket = new Ticket(
                ticketId,
                requesterId,
                "Production system error",
                "Application returns internal server error",
                TicketPriority.HIGH,
                NOW.minusSeconds(600)
        );
    }

    @Test
    @DisplayName(
            "Requester should add comment to own ticket"
    )
    void requesterShouldAddComment() {
        prepareRepository();

        UserContext context = new UserContext(
                requesterId.toString(),
                UserRole.USER
        );

        TicketResult result = service.addComment(
                command(),
                context
        );

        assertEquals(1, result.comments().size());
        assertEquals(
                requesterId.toString(),
                result.comments().getFirst().authorId()
        );

        verify(ticketRepository).save(ticket);
    }

    @Test
    @DisplayName(
            "Assigned agent should add comment"
    )
    void assignedAgentShouldAddComment() {
        ticket.assignTo(
                agentId,
                NOW.minusSeconds(300)
        );

        prepareRepository();

        UserContext context = new UserContext(
                agentId.toString(),
                UserRole.AGENT
        );

        TicketResult result = service.addComment(
                command(),
                context
        );

        assertEquals(1, result.comments().size());
        assertEquals(
                agentId.toString(),
                result.comments().getFirst().authorId()
        );
    }

    @Test
    @DisplayName(
            "Unassigned agent should not add comment"
    )
    void unassignedAgentShouldBeDenied() {
        when(ticketRepository.findById(ticketId))
                .thenReturn(Optional.of(ticket));

        UserContext context = new UserContext(
                agentId.toString(),
                UserRole.AGENT
        );

        assertThrows(
                UnauthorizedAccessException.class,
                () -> service.addComment(
                        command(),
                        context
                )
        );

        verify(
                ticketRepository,
                never()
        ).save(any());
    }

    @Test
    @DisplayName(
            "Another user should not add comment"
    )
    void anotherUserShouldBeDenied() {
        when(ticketRepository.findById(ticketId))
                .thenReturn(Optional.of(ticket));

        UserContext context = new UserContext(
                UserId.generate().toString(),
                UserRole.USER
        );

        assertThrows(
                UnauthorizedAccessException.class,
                () -> service.addComment(
                        command(),
                        context
                )
        );

        verify(
                ticketRepository,
                never()
        ).save(any());
    }

    @Test
    @DisplayName(
            "Admin should add comment to any ticket"
    )
    void adminShouldAddComment() {
        prepareRepository();

        UserId adminId = UserId.generate();

        UserContext context = new UserContext(
                adminId.toString(),
                UserRole.ADMIN
        );

        TicketResult result = service.addComment(
                command(),
                context
        );

        assertEquals(
                adminId.toString(),
                result.comments().getFirst().authorId()
        );
    }

    @Test
    @DisplayName(
            "Closed ticket should reject comments"
    )
    void closedTicketShouldRejectComment() {
        ticket.transitionTo(
                TicketStatus.IN_PROGRESS,
                NOW.minusSeconds(400)
        );

        ticket.transitionTo(
                TicketStatus.RESOLVED,
                NOW.minusSeconds(300)
        );

        ticket.transitionTo(
                TicketStatus.CLOSED,
                NOW.minusSeconds(200)
        );

        when(ticketRepository.findById(ticketId))
                .thenReturn(Optional.of(ticket));

        UserContext context = new UserContext(
                requesterId.toString(),
                UserRole.USER
        );

        assertThrows(
                TicketClosedException.class,
                () -> service.addComment(
                        command(),
                        context
                )
        );

        verify(
                ticketRepository,
                never()
        ).save(any());
    }

    private AddCommentCommand command() {
        return new AddCommentCommand(
                ticketId.getValue().toString(),
                "The problem still continues"
        );
    }

    private void prepareRepository() {
        when(ticketRepository.findById(ticketId))
                .thenReturn(Optional.of(ticket));

        when(ticketRepository.save(any(Ticket.class)))
                .thenAnswer(invocation ->
                        invocation.getArgument(0)
                );
    }
}