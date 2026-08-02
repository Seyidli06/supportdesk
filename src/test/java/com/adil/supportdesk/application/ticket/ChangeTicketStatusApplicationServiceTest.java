package com.adil.supportdesk.application.ticket;

import com.adil.supportdesk.application.port.out.TicketRepository;
import com.adil.supportdesk.application.security.UnauthorizedAccessException;
import com.adil.supportdesk.application.security.UserContext;
import com.adil.supportdesk.application.security.UserRole;
import com.adil.supportdesk.application.ticket.changestatus.ChangeTicketStatusApplicationService;
import com.adil.supportdesk.application.ticket.changestatus.ChangeTicketStatusCommand;
import com.adil.supportdesk.application.ticket.get.TicketResult;
import com.adil.supportdesk.domain.ticket.model.Ticket;
import com.adil.supportdesk.domain.ticket.model.TicketPriority;
import com.adil.supportdesk.domain.ticket.model.TicketStatus;
import com.adil.supportdesk.domain.ticket.valueobject.TicketId;
import com.adil.supportdesk.domain.user.valueobject.UserId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ChangeTicketStatusApplicationServiceTest {

    @Mock
    private TicketRepository ticketRepository;

    @InjectMocks
    private ChangeTicketStatusApplicationService service;

    private Ticket existingTicket;
    private TicketId ticketId;

    @BeforeEach
    void setUp() {
        ticketId = TicketId.generate();

        existingTicket = new Ticket(
                ticketId,
                UserId.generate(),
                "Database Down",
                "Production DB error",
                TicketPriority.URGENT,
                Instant.now()
        );
    }

    @Test
    @DisplayName(
            "Agent should successfully change ticket status to IN_PROGRESS"
    )
    void agentCanChangeStatus() {
        UserContext agentContext = new UserContext(
                UUID.randomUUID().toString(),
                UserRole.AGENT
        );

        ChangeTicketStatusCommand command =
                new ChangeTicketStatusCommand(
                        ticketId.getValue().toString(),
                        TicketStatus.IN_PROGRESS
                );

        when(ticketRepository.findById(ticketId))
                .thenReturn(Optional.of(existingTicket));

        when(ticketRepository.save(any(Ticket.class)))
                .thenAnswer(invocation ->
                        invocation.getArgument(0)
                );

        TicketResult result = service.changeStatus(
                command,
                agentContext
        );

        assertEquals(
                TicketStatus.IN_PROGRESS,
                result.status()
        );

        verify(ticketRepository).save(existingTicket);
    }

    @Test
    @DisplayName(
            "Regular user should be denied status change access"
    )
    void regularUserDenied() {
        UserContext userContext = new UserContext(
                UUID.randomUUID().toString(),
                UserRole.USER
        );

        ChangeTicketStatusCommand command =
                new ChangeTicketStatusCommand(
                        ticketId.getValue().toString(),
                        TicketStatus.IN_PROGRESS
                );

        assertThrows(
                UnauthorizedAccessException.class,
                () -> service.changeStatus(
                        command,
                        userContext
                )
        );

        verify(
                ticketRepository,
                never()
        ).save(any());
    }
}