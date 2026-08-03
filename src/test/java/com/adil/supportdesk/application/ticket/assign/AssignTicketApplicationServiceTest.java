package com.adil.supportdesk.application.ticket.assign;

import com.adil.supportdesk.application.port.out.TicketRepository;
import com.adil.supportdesk.application.port.out.UserDirectory;
import com.adil.supportdesk.application.security.UnauthorizedAccessException;
import com.adil.supportdesk.application.security.UserContext;
import com.adil.supportdesk.application.security.UserRole;
import com.adil.supportdesk.application.ticket.assign.AssignTicketApplicationService;
import com.adil.supportdesk.application.ticket.assign.AssignTicketCommand;
import com.adil.supportdesk.application.ticket.assign.InvalidAssigneeException;
import com.adil.supportdesk.application.ticket.get.TicketResult;
import com.adil.supportdesk.application.user.UserSummary;
import com.adil.supportdesk.domain.ticket.model.Ticket;
import com.adil.supportdesk.domain.ticket.model.TicketPriority;
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
            Instant.parse("2026-08-02T15:00:00Z");

    @Mock
    private TicketRepository ticketRepository;

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
        UserContext adminContext =
                new UserContext(
                        UserId.generate().toString(),
                        UserRole.ADMIN
                );

        AssignTicketCommand command =
                new AssignTicketCommand(
                        ticketId.getValue().toString(),
                        agentId.toString()
                );

        when(userDirectory.findById(agentId))
                .thenReturn(
                        Optional.of(
                                new UserSummary(
                                        agentId,
                                        Set.of(UserRole.AGENT)
                                )
                        )
                );

        when(ticketRepository.findById(ticketId))
                .thenReturn(Optional.of(ticket));

        when(ticketRepository.save(any(Ticket.class)))
                .thenAnswer(invocation ->
                        invocation.getArgument(0)
                );

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

        verify(ticketRepository).save(ticket);
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
                        ticketId.getValue().toString(),
                        agentId.toString()
                );

        when(userDirectory.findById(agentId))
                .thenReturn(
                        Optional.of(
                                new UserSummary(
                                        agentId,
                                        Set.of(UserRole.AGENT)
                                )
                        )
                );

        when(ticketRepository.findById(ticketId))
                .thenReturn(Optional.of(ticket));

        when(ticketRepository.save(any(Ticket.class)))
                .thenAnswer(invocation ->
                        invocation.getArgument(0)
                );

        TicketResult result =
                service.assignTicket(
                        command,
                        agentContext
                );

        assertEquals(
                agentId.toString(),
                result.assignedAgentId()
        );
    }

    @Test
    @DisplayName(
            "Agent should not assign ticket to another agent"
    )
    void agentShouldNotAssignTicketToAnotherAgent() {
        UserId anotherAgentId = UserId.generate();

        UserContext agentContext =
                new UserContext(
                        agentId.toString(),
                        UserRole.AGENT
                );

        AssignTicketCommand command =
                new AssignTicketCommand(
                        ticketId.getValue().toString(),
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
                ticketRepository,
                never()
        ).save(any());
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
                        ticketId.getValue().toString(),
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
                ticketRepository,
                never()
        ).save(any());
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
                        ticketId.getValue().toString(),
                        agentId.toString()
                );

        when(userDirectory.findById(agentId))
                .thenReturn(
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
                ticketRepository,
                never()
        ).save(any());
    }
}