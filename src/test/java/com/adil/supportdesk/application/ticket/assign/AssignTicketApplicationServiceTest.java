package com.adil.supportdesk.application.ticket.assign;

import com.adil.supportdesk.application.port.out.TicketMutationRepository;
import com.adil.supportdesk.application.port.out.TicketRepository;
import com.adil.supportdesk.application.port.out.UserDirectory;
import com.adil.supportdesk.application.security.UnauthorizedAccessException;
import com.adil.supportdesk.application.security.UserContext;
import com.adil.supportdesk.application.security.UserRole;
import com.adil.supportdesk.application.ticket.get.TicketResult;
import com.adil.supportdesk.application.user.UserSummary;
import com.adil.supportdesk.domain.ticket.model.Ticket;
import com.adil.supportdesk.domain.ticket.model.TicketEvent;
import com.adil.supportdesk.domain.ticket.model.TicketEventType;
import com.adil.supportdesk.domain.ticket.model.TicketPriority;
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
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AssignTicketApplicationServiceTest {

    private static final Instant NOW =
            Instant.parse(
                    "2026-08-02T15:00:00Z"
            );

    @Mock
    private TicketRepository ticketRepository;

    @Mock
    private TicketMutationRepository
            ticketMutationRepository;

    @Mock
    private UserDirectory userDirectory;

    private AssignTicketApplicationService service;

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

        service = new AssignTicketApplicationService(
                ticketRepository,
                ticketMutationRepository,
                userDirectory,
                clock
        );

        ticketId = TicketId.generate();
        requesterId = UserId.generate();
        agentId = UserId.generate();

        ticket = new Ticket(
                ticketId,
                requesterId,
                "Production database error",
                "Application cannot connect to database",
                TicketPriority.URGENT,
                NOW.minusSeconds(300)
        );
    }

    @Test
    @DisplayName(
            "Admin should assign ticket to an agent"
    )
    void adminShouldAssignTicketToAgent() {
        UserId adminId = UserId.generate();

        UserContext adminContext =
                new UserContext(
                        adminId.toString(),
                        UserRole.ADMIN
                );

        AssignTicketCommand command =
                new AssignTicketCommand(
                        ticketId
                                .getValue()
                                .toString(),
                        agentId.toString()
                );

        prepareAgentUser();
        prepareTicketLookup();
        prepareMutationSave();

        TicketResult result =
                service.assignTicket(
                        command,
                        adminContext
                );

        assertEquals(
                agentId.toString(),
                result.assignedAgentId()
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

        TicketEvent capturedEvent =
                eventCaptor.getValue();

        assertEquals(
                TicketEventType.ASSIGNMENT_CHANGED,
                capturedEvent.type()
        );

        assertEquals(
                ticketId,
                capturedEvent.ticketId()
        );

        assertEquals(
                adminId,
                capturedEvent.actorId()
        );

        assertEquals(
                null,
                capturedEvent.previousValue()
        );

        assertEquals(
                agentId.toString(),
                capturedEvent.newValue()
        );

        assertEquals(
                NOW,
                capturedEvent.createdAt()
        );
    }

    @Test
    @DisplayName(
            "Agent should assign ticket to themselves"
    )
    void agentShouldAssignTicketToSelf() {
        UserContext agentContext =
                new UserContext(
                        agentId.toString(),
                        UserRole.AGENT
                );

        AssignTicketCommand command =
                new AssignTicketCommand(
                        ticketId
                                .getValue()
                                .toString(),
                        agentId.toString()
                );

        prepareAgentUser();
        prepareTicketLookup();
        prepareMutationSave();

        TicketResult result =
                service.assignTicket(
                        command,
                        agentContext
                );

        assertEquals(
                agentId.toString(),
                result.assignedAgentId()
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

        TicketEvent capturedEvent =
                eventCaptor.getValue();

        assertEquals(
                TicketEventType.ASSIGNMENT_CHANGED,
                capturedEvent.type()
        );

        assertEquals(
                agentId,
                capturedEvent.actorId()
        );

        assertEquals(
                null,
                capturedEvent.previousValue()
        );

        assertEquals(
                agentId.toString(),
                capturedEvent.newValue()
        );
    }

    @Test
    @DisplayName(
            "Agent should not assign ticket to another agent"
    )
    void agentShouldNotAssignTicketToAnotherAgent() {
        UserId anotherAgentId =
                UserId.generate();

        UserContext agentContext =
                new UserContext(
                        agentId.toString(),
                        UserRole.AGENT
                );

        AssignTicketCommand command =
                new AssignTicketCommand(
                        ticketId
                                .getValue()
                                .toString(),
                        anotherAgentId.toString()
                );

        assertThrows(
                UnauthorizedAccessException.class,
                () -> service.assignTicket(
                        command,
                        agentContext
                )
        );

        verify(
                ticketMutationRepository,
                never()
        ).saveWithEvent(
                any(Ticket.class),
                any(TicketEvent.class)
        );
    }

    @Test
    @DisplayName(
            "Regular user should not assign tickets"
    )
    void regularUserShouldNotAssignTicket() {
        UserContext userContext =
                new UserContext(
                        requesterId.toString(),
                        UserRole.USER
                );

        AssignTicketCommand command =
                new AssignTicketCommand(
                        ticketId
                                .getValue()
                                .toString(),
                        agentId.toString()
                );

        assertThrows(
                UnauthorizedAccessException.class,
                () -> service.assignTicket(
                        command,
                        userContext
                )
        );

        verify(
                ticketMutationRepository,
                never()
        ).saveWithEvent(
                any(Ticket.class),
                any(TicketEvent.class)
        );
    }

    @Test
    @DisplayName(
            "Ticket should not be assigned to non-agent user"
    )
    void ticketShouldNotBeAssignedToNonAgent() {
        UserContext adminContext =
                new UserContext(
                        UserId.generate().toString(),
                        UserRole.ADMIN
                );

        AssignTicketCommand command =
                new AssignTicketCommand(
                        ticketId
                                .getValue()
                                .toString(),
                        agentId.toString()
                );

        when(
                userDirectory.findById(agentId)
        ).thenReturn(
                Optional.of(
                        new UserSummary(
                                agentId,
                                Set.of(UserRole.USER)
                        )
                )
        );

        assertThrows(
                InvalidAssigneeException.class,
                () -> service.assignTicket(
                        command,
                        adminContext
                )
        );

        verify(
                ticketMutationRepository,
                never()
        ).saveWithEvent(
                any(Ticket.class),
                any(TicketEvent.class)
        );
    }

    @Test
    @DisplayName(
            "Agent should not take over ticket "
                    + "assigned to another agent"
    )
    void agentShouldNotTakeOverTicketAssignedToAnotherAgent() {
        UserId existingAgentId =
                UserId.generate();

        ticket.assignTo(
                existingAgentId,
                NOW.minusSeconds(60)
        );

        UserContext agentContext =
                new UserContext(
                        agentId.toString(),
                        UserRole.AGENT
                );

        AssignTicketCommand command =
                new AssignTicketCommand(
                        ticketId
                                .getValue()
                                .toString(),
                        agentId.toString()
                );

        prepareAgentUser();
        prepareTicketLookup();

        UnauthorizedAccessException exception =
                assertThrows(
                        UnauthorizedAccessException.class,
                        () -> service.assignTicket(
                                command,
                                agentContext
                        )
                );

        assertEquals(
                "Agents cannot take over tickets "
                        + "assigned to another agent",
                exception.getMessage()
        );

        assertEquals(
                existingAgentId,
                ticket.getAssignedAgentId()
        );

        verify(
                ticketMutationRepository,
                never()
        ).saveWithEvent(
                any(Ticket.class),
                any(TicketEvent.class)
        );
    }

    @Test
    @DisplayName(
            "Admin should reassign ticket from another agent"
    )
    void adminShouldReassignTicketFromAnotherAgent() {
        UserId existingAgentId =
                UserId.generate();

        ticket.assignTo(
                existingAgentId,
                NOW.minusSeconds(60)
        );

        UserId adminId =
                UserId.generate();

        UserContext adminContext =
                new UserContext(
                        adminId.toString(),
                        UserRole.ADMIN
                );

        AssignTicketCommand command =
                new AssignTicketCommand(
                        ticketId
                                .getValue()
                                .toString(),
                        agentId.toString()
                );

        prepareAgentUser();
        prepareTicketLookup();
        prepareMutationSave();

        TicketResult result =
                service.assignTicket(
                        command,
                        adminContext
                );

        assertEquals(
                agentId.toString(),
                result.assignedAgentId()
        );

        assertEquals(
                agentId,
                ticket.getAssignedAgentId()
        );

        assertEquals(
                NOW,
                ticket.getUpdatedAt()
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

        TicketEvent capturedEvent =
                eventCaptor.getValue();

        assertEquals(
                TicketEventType.ASSIGNMENT_CHANGED,
                capturedEvent.type()
        );

        assertEquals(
                ticketId,
                capturedEvent.ticketId()
        );

        assertEquals(
                adminId,
                capturedEvent.actorId()
        );

        assertEquals(
                existingAgentId.toString(),
                capturedEvent.previousValue()
        );

        assertEquals(
                agentId.toString(),
                capturedEvent.newValue()
        );

        assertEquals(
                NOW,
                capturedEvent.createdAt()
        );
    }

    @Test
    @DisplayName(
            "Assigning ticket to the same agent "
                    + "should not create audit event"
    )
    void assigningSameAgentShouldNotCreateEvent() {
        ticket.assignTo(
                agentId,
                NOW.minusSeconds(60)
        );

        Instant previousUpdatedAt =
                ticket.getUpdatedAt();

        UserContext agentContext =
                new UserContext(
                        agentId.toString(),
                        UserRole.AGENT
                );

        AssignTicketCommand command =
                new AssignTicketCommand(
                        ticketId
                                .getValue()
                                .toString(),
                        agentId.toString()
                );

        prepareAgentUser();
        prepareTicketLookup();

        TicketResult result =
                service.assignTicket(
                        command,
                        agentContext
                );

        assertEquals(
                agentId.toString(),
                result.assignedAgentId()
        );

        assertEquals(
                previousUpdatedAt,
                result.updatedAt()
        );

        verify(
                ticketMutationRepository,
                never()
        ).saveWithEvent(
                any(Ticket.class),
                any(TicketEvent.class)
        );
    }

    private void prepareAgentUser() {
        when(
                userDirectory.findById(agentId)
        ).thenReturn(
                Optional.of(
                        new UserSummary(
                                agentId,
                                Set.of(UserRole.AGENT)
                        )
                )
        );
    }

    private void prepareTicketLookup() {
        when(
                ticketRepository.findById(ticketId)
        ).thenReturn(
                Optional.of(ticket)
        );
    }

    private void prepareMutationSave() {
        when(
                ticketMutationRepository.saveWithEvent(
                        any(Ticket.class),
                        any(TicketEvent.class)
                )
        ).thenAnswer(invocation ->
                invocation.getArgument(0)
        );
    }
}