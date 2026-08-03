package com.adil.supportdesk.application.ticket.get;

import com.adil.supportdesk.application.port.out.TicketQueryRepository;
import com.adil.supportdesk.application.security.UnauthorizedAccessException;
import com.adil.supportdesk.application.security.UserContext;
import com.adil.supportdesk.application.security.UserRole;
import com.adil.supportdesk.application.ticket.get.GetTicketApplicationService;
import com.adil.supportdesk.application.ticket.get.TicketResult;
import com.adil.supportdesk.domain.ticket.exception.TicketNotFoundException;
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

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetTicketApplicationServiceTest {

    private static final Instant NOW =
            Instant.parse("2026-08-02T18:00:00Z");

    @Mock
    private TicketQueryRepository ticketQueryRepository;

    private GetTicketApplicationService service;

    private TicketId ticketId;
    private UserId requesterId;
    private UserId agentId;

    @BeforeEach
    void setUp() {
        service = new GetTicketApplicationService(
                ticketQueryRepository
        );

        ticketId = TicketId.generate();
        requesterId = UserId.generate();
        agentId = UserId.generate();
    }

    @Test
    @DisplayName(
            "Requester should access own ticket"
    )
    void requesterShouldAccessOwnTicket() {
        TicketResult ticket = ticketResult(
                agentId.toString()
        );

        when(
                ticketQueryRepository.findDetailsById(
                        ticketId
                )
        ).thenReturn(Optional.of(ticket));

        TicketResult result = service.getTicket(
                ticketId.getValue().toString(),
                new UserContext(
                        requesterId.toString(),
                        UserRole.USER
                )
        );

        assertEquals(ticket.id(), result.id());
    }

    @Test
    @DisplayName(
            "Assigned agent should access ticket"
    )
    void assignedAgentShouldAccessTicket() {
        TicketResult ticket = ticketResult(
                agentId.toString()
        );

        when(
                ticketQueryRepository.findDetailsById(
                        ticketId
                )
        ).thenReturn(Optional.of(ticket));

        TicketResult result = service.getTicket(
                ticketId.getValue().toString(),
                new UserContext(
                        agentId.toString(),
                        UserRole.AGENT
                )
        );

        assertEquals(ticket.id(), result.id());
    }

    @Test
    @DisplayName(
            "Admin should access any ticket"
    )
    void adminShouldAccessAnyTicket() {
        TicketResult ticket = ticketResult(null);

        when(
                ticketQueryRepository.findDetailsById(
                        ticketId
                )
        ).thenReturn(Optional.of(ticket));

        TicketResult result = service.getTicket(
                ticketId.getValue().toString(),
                new UserContext(
                        UserId.generate().toString(),
                        UserRole.ADMIN
                )
        );

        assertEquals(ticket.id(), result.id());
    }

    @Test
    @DisplayName(
            "Another user should not access ticket"
    )
    void anotherUserShouldBeDenied() {
        when(
                ticketQueryRepository.findDetailsById(
                        ticketId
                )
        ).thenReturn(
                Optional.of(
                        ticketResult(
                                agentId.toString()
                        )
                )
        );

        UserContext context = new UserContext(
                UserId.generate().toString(),
                UserRole.USER
        );

        assertThrows(
                UnauthorizedAccessException.class,
                () -> service.getTicket(
                        ticketId.getValue().toString(),
                        context
                )
        );
    }

    @Test
    @DisplayName(
            "Missing ticket should throw not-found"
    )
    void missingTicketShouldThrowNotFound() {
        when(
                ticketQueryRepository.findDetailsById(
                        ticketId
                )
        ).thenReturn(Optional.empty());

        assertThrows(
                TicketNotFoundException.class,
                () -> service.getTicket(
                        ticketId.getValue().toString(),
                        new UserContext(
                                requesterId.toString(),
                                UserRole.USER
                        )
                )
        );
    }

    private TicketResult ticketResult(
            String assignedAgentId
    ) {
        return new TicketResult(
                ticketId.getValue().toString(),
                "Production error",
                "Application is unavailable",
                TicketPriority.HIGH,
                TicketStatus.OPEN,
                requesterId.toString(),
                assignedAgentId,
                List.of(),
                NOW,
                NOW,
                null,
                null,
                null
        );
    }
}