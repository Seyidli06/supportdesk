package com.adil.supportdesk.application.ticket.changepriority;

import com.adil.supportdesk.application.port.out.TicketMutationRepository;
import com.adil.supportdesk.application.port.out.TicketRepository;
import com.adil.supportdesk.application.security.UnauthorizedAccessException;
import com.adil.supportdesk.application.security.UserContext;
import com.adil.supportdesk.application.security.UserRole;
import com.adil.supportdesk.application.ticket.get.TicketResult;
import com.adil.supportdesk.domain.ticket.exception.TicketClosedException;
import com.adil.supportdesk.domain.ticket.exception.TicketNotFoundException;
import com.adil.supportdesk.domain.ticket.model.Ticket;
import com.adil.supportdesk.domain.ticket.model.TicketEvent;
import com.adil.supportdesk.domain.ticket.model.TicketEventType;
import com.adil.supportdesk.domain.ticket.model.TicketPriority;
import com.adil.supportdesk.domain.ticket.model.TicketStatus;
import com.adil.supportdesk.domain.ticket.valueobject.TicketId;
import com.adil.supportdesk.domain.user.valueobject.UserId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
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
class ChangeTicketPriorityApplicationServiceTest {

    private static final Instant NOW =
            Instant.parse(
                    "2026-08-05T12:00:00Z"
            );

    @Mock
    private TicketRepository ticketRepository;

    @Mock
    private TicketMutationRepository
            ticketMutationRepository;

    private ChangeTicketPriorityApplicationService
            service;

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

        service =
                new ChangeTicketPriorityApplicationService(
                        ticketRepository,
                        ticketMutationRepository,
                        clock
                );

        ticketId = TicketId.generate();
        requesterId = UserId.generate();
        agentId = UserId.generate();

        ticket = new Ticket(
                ticketId,
                requesterId,
                "Production service unavailable",
                "The production API is not responding",
                TicketPriority.MEDIUM,
                NOW.minusSeconds(600)
        );
    }

    @Test
    @DisplayName(
            "Assigned agent should change priority "
                    + "and create audit event"
    )
    void assignedAgentShouldChangePriority() {
        ticket.assignTo(
                agentId,
                NOW.minusSeconds(300)
        );

        prepareRepository();

        UserContext agentContext =
                new UserContext(
                        agentId.toString(),
                        UserRole.AGENT
                );

        TicketResult result =
                service.changePriority(
                        command(
                                TicketPriority.HIGH
                        ),
                        agentContext
                );

        assertEquals(
                TicketPriority.HIGH,
                result.priority()
        );

        assertEquals(
                NOW,
                result.updatedAt()
        );

        ArgumentCaptor<TicketEvent> eventCaptor =
                ArgumentCaptor.forClass(
                        TicketEvent.class
                );

        verify(
                ticketMutationRepository
        ).saveWithEvent(
                any(Ticket.class),
                eventCaptor.capture()
        );

        TicketEvent event =
                eventCaptor.getValue();

        assertEquals(
                TicketEventType.PRIORITY_CHANGED,
                event.type()
        );

        assertEquals(
                ticketId,
                event.ticketId()
        );

        assertEquals(
                agentId,
                event.actorId()
        );

        assertEquals(
                "MEDIUM",
                event.previousValue()
        );

        assertEquals(
                "HIGH",
                event.newValue()
        );

        assertEquals(
                NOW,
                event.createdAt()
        );
    }

    @Test
    @DisplayName(
            "Admin should change priority "
                    + "of any ticket"
    )
    void adminShouldChangeAnyTicketPriority() {
        prepareRepository();

        UserId adminId = UserId.generate();

        UserContext adminContext =
                new UserContext(
                        adminId.toString(),
                        UserRole.ADMIN
                );

        TicketResult result =
                service.changePriority(
                        command(
                                TicketPriority.URGENT
                        ),
                        adminContext
                );

        assertEquals(
                TicketPriority.URGENT,
                result.priority()
        );

        ArgumentCaptor<TicketEvent> eventCaptor =
                ArgumentCaptor.forClass(
                        TicketEvent.class
                );

        verify(
                ticketMutationRepository
        ).saveWithEvent(
                any(Ticket.class),
                eventCaptor.capture()
        );

        TicketEvent event =
                eventCaptor.getValue();

        assertEquals(
                adminId,
                event.actorId()
        );

        assertEquals(
                "MEDIUM",
                event.previousValue()
        );

        assertEquals(
                "URGENT",
                event.newValue()
        );
    }

    @Test
    @DisplayName(
            "Unassigned agent should not "
                    + "change ticket priority"
    )
    void unassignedAgentShouldBeDenied() {
        when(
                ticketRepository.findById(ticketId)
        ).thenReturn(
                Optional.of(ticket)
        );

        UserContext agentContext =
                new UserContext(
                        agentId.toString(),
                        UserRole.AGENT
                );

        assertThrows(
                UnauthorizedAccessException.class,
                () -> service.changePriority(
                        command(
                                TicketPriority.HIGH
                        ),
                        agentContext
                )
        );

        verifyNoMutation();
    }

    @Test
    @DisplayName(
            "Regular user should not "
                    + "change ticket priority"
    )
    void regularUserShouldBeDenied() {
        UserContext userContext =
                new UserContext(
                        requesterId.toString(),
                        UserRole.USER
                );

        assertThrows(
                UnauthorizedAccessException.class,
                () -> service.changePriority(
                        command(
                                TicketPriority.HIGH
                        ),
                        userContext
                )
        );

        verifyNoMutation();
    }

    @Test
    @DisplayName(
            "Missing ticket should throw "
                    + "TicketNotFoundException"
    )
    void missingTicketShouldThrowException() {
        when(
                ticketRepository.findById(ticketId)
        ).thenReturn(
                Optional.empty()
        );

        UserContext adminContext =
                new UserContext(
                        UserId.generate().toString(),
                        UserRole.ADMIN
                );

        assertThrows(
                TicketNotFoundException.class,
                () -> service.changePriority(
                        command(
                                TicketPriority.HIGH
                        ),
                        adminContext
                )
        );

        verifyNoMutation();
    }

    @Test
    @DisplayName(
            "Changing to same priority should not "
                    + "create audit event"
    )
    void samePriorityShouldNotCreateEvent() {
        Instant previousUpdatedAt =
                ticket.getUpdatedAt();

        when(
                ticketRepository.findById(ticketId)
        ).thenReturn(
                Optional.of(ticket)
        );

        UserContext adminContext =
                new UserContext(
                        UserId.generate().toString(),
                        UserRole.ADMIN
                );

        TicketResult result =
                service.changePriority(
                        command(
                                TicketPriority.MEDIUM
                        ),
                        adminContext
                );

        assertEquals(
                TicketPriority.MEDIUM,
                result.priority()
        );

        assertEquals(
                previousUpdatedAt,
                result.updatedAt()
        );

        verifyNoMutation();
    }

    @Test
    @DisplayName(
            "Closed ticket should reject "
                    + "priority changes"
    )
    void closedTicketShouldRejectPriorityChange() {
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

        when(
                ticketRepository.findById(ticketId)
        ).thenReturn(
                Optional.of(ticket)
        );

        UserContext adminContext =
                new UserContext(
                        UserId.generate().toString(),
                        UserRole.ADMIN
                );

        assertThrows(
                TicketClosedException.class,
                () -> service.changePriority(
                        command(
                                TicketPriority.URGENT
                        ),
                        adminContext
                )
        );

        verifyNoMutation();
    }

    private ChangeTicketPriorityCommand command(
            TicketPriority priority
    ) {
        return new ChangeTicketPriorityCommand(
                ticketId
                        .getValue()
                        .toString(),
                priority
        );
    }

    private void prepareRepository() {
        when(
                ticketRepository.findById(ticketId)
        ).thenReturn(
                Optional.of(ticket)
        );

        when(
                ticketMutationRepository.saveWithEvent(
                        any(Ticket.class),
                        any(TicketEvent.class)
                )
        ).thenAnswer(invocation ->
                invocation.getArgument(0)
        );
    }

    private void verifyNoMutation() {
        verify(
                ticketMutationRepository,
                never()
        ).saveWithEvent(
                any(Ticket.class),
                any(TicketEvent.class)
        );
    }
}