package com.adil.supportdesk.application.ticket.changestatus;

import com.adil.supportdesk.application.port.out.TicketRepository;
import com.adil.supportdesk.application.security.UnauthorizedAccessException;
import com.adil.supportdesk.application.security.UserContext;
import com.adil.supportdesk.application.security.UserRole;
import com.adil.supportdesk.application.ticket.changestatus.ChangeTicketStatusApplicationService;
import com.adil.supportdesk.application.ticket.changestatus.ChangeTicketStatusCommand;
import com.adil.supportdesk.application.ticket.get.TicketResult;
import com.adil.supportdesk.domain.ticket.exception.TicketNotFoundException;
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
class ChangeTicketStatusApplicationServiceTest {

    private static final Instant NOW =
            Instant.parse("2026-08-02T17:00:00Z");

    @Mock
    private TicketRepository ticketRepository;

    private ChangeTicketStatusApplicationService service;

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
                new ChangeTicketStatusApplicationService(
                        ticketRepository,
                        clock
                );

        ticketId = TicketId.generate();
        requesterId = UserId.generate();
        agentId = UserId.generate();

        ticket = new Ticket(
                ticketId,
                requesterId,
                "Production database failure",
                "Application cannot connect to database",
                TicketPriority.URGENT,
                NOW.minusSeconds(600)
        );
    }

    @Test
    @DisplayName(
            "Assigned agent should change ticket status"
    )
    void assignedAgentShouldChangeStatus() {
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

        TicketResult result = service.changeStatus(
                command(TicketStatus.IN_PROGRESS),
                agentContext
        );

        assertEquals(
                TicketStatus.IN_PROGRESS,
                result.status()
        );

        assertEquals(
                NOW,
                result.updatedAt()
        );

        verify(ticketRepository).save(ticket);
    }

    @Test
    @DisplayName(
            "Unassigned agent should not change ticket status"
    )
    void unassignedAgentShouldBeDenied() {
        when(ticketRepository.findById(ticketId))
                .thenReturn(Optional.of(ticket));

        UserContext agentContext =
                new UserContext(
                        agentId.toString(),
                        UserRole.AGENT
                );

        assertThrows(
                UnauthorizedAccessException.class,
                () -> service.changeStatus(
                        command(TicketStatus.IN_PROGRESS),
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
            "Another agent should not change ticket status"
    )
    void anotherAgentShouldBeDenied() {
        ticket.assignTo(
                agentId,
                NOW.minusSeconds(300)
        );

        when(ticketRepository.findById(ticketId))
                .thenReturn(Optional.of(ticket));

        UserContext anotherAgent =
                new UserContext(
                        UserId.generate().toString(),
                        UserRole.AGENT
                );

        assertThrows(
                UnauthorizedAccessException.class,
                () -> service.changeStatus(
                        command(TicketStatus.IN_PROGRESS),
                        anotherAgent
                )
        );

        verify(
                ticketRepository,
                never()
        ).save(any());
    }

    @Test
    @DisplayName(
            "Admin should change status of any ticket"
    )
    void adminShouldChangeAnyTicketStatus() {
        prepareRepository();

        UserContext adminContext =
                new UserContext(
                        UserId.generate().toString(),
                        UserRole.ADMIN
                );

        TicketResult result = service.changeStatus(
                command(TicketStatus.IN_PROGRESS),
                adminContext
        );

        assertEquals(
                TicketStatus.IN_PROGRESS,
                result.status()
        );

        verify(ticketRepository).save(ticket);
    }

    @Test
    @DisplayName(
            "Regular user should not change ticket status"
    )
    void regularUserShouldBeDenied() {
        UserContext userContext =
                new UserContext(
                        requesterId.toString(),
                        UserRole.USER
                );

        assertThrows(
                UnauthorizedAccessException.class,
                () -> service.changeStatus(
                        command(TicketStatus.IN_PROGRESS),
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
            "Missing ticket should throw TicketNotFoundException"
    )
    void missingTicketShouldThrowException() {
        when(ticketRepository.findById(ticketId))
                .thenReturn(Optional.empty());

        UserContext adminContext =
                new UserContext(
                        UserId.generate().toString(),
                        UserRole.ADMIN
                );

        assertThrows(
                TicketNotFoundException.class,
                () -> service.changeStatus(
                        command(TicketStatus.IN_PROGRESS),
                        adminContext
                )
        );

        verify(
                ticketRepository,
                never()
        ).save(any());
    }

    private ChangeTicketStatusCommand command(
            TicketStatus status
    ) {
        return new ChangeTicketStatusCommand(
                ticketId.getValue().toString(),
                status
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