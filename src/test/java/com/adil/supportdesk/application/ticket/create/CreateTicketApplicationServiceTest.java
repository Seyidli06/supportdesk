package com.adil.supportdesk.application.ticket.create;

import com.adil.supportdesk.application.port.out.TicketMutationRepository;
import com.adil.supportdesk.application.security.UserContext;
import com.adil.supportdesk.application.security.UserRole;
import com.adil.supportdesk.application.ticket.get.TicketResult;
import com.adil.supportdesk.domain.ticket.model.Ticket;
import com.adil.supportdesk.domain.ticket.model.TicketEvent;
import com.adil.supportdesk.domain.ticket.model.TicketEventType;
import com.adil.supportdesk.domain.ticket.model.TicketPriority;
import com.adil.supportdesk.domain.ticket.model.TicketStatus;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CreateTicketApplicationServiceTest {

    private static final Instant NOW =
            Instant.parse(
                    "2026-08-05T08:00:00Z"
            );

    @Mock
    private TicketMutationRepository
            ticketMutationRepository;

    private CreateTicketApplicationService service;

    @BeforeEach
    void setUp() {
        Clock clock = Clock.fixed(
                NOW,
                ZoneOffset.UTC
        );

        service = new CreateTicketApplicationService(
                ticketMutationRepository,
                clock
        );
    }

    @Test
    @DisplayName(
            "Creating ticket should store "
                    + "TICKET_CREATED audit event"
    )
    void creatingTicketShouldStoreAuditEvent() {
        UserId requesterId = UserId.generate();

        UserContext userContext = new UserContext(
                requesterId.toString(),
                UserRole.USER
        );

        CreateTicketCommand command =
                new CreateTicketCommand(
                        "Production database error",
                        "Application cannot connect to database",
                        TicketPriority.HIGH
                );

        when(
                ticketMutationRepository.saveWithEvent(
                        any(Ticket.class),
                        any(TicketEvent.class)
                )
        ).thenAnswer(invocation ->
                invocation.getArgument(0)
        );

        TicketResult result =
                service.createTicket(
                        command,
                        userContext
                );

        ArgumentCaptor<Ticket> ticketCaptor =
                ArgumentCaptor.forClass(
                        Ticket.class
                );

        ArgumentCaptor<TicketEvent> eventCaptor =
                ArgumentCaptor.forClass(
                        TicketEvent.class
                );

        verify(
                ticketMutationRepository
        ).saveWithEvent(
                ticketCaptor.capture(),
                eventCaptor.capture()
        );

        Ticket capturedTicket =
                ticketCaptor.getValue();

        TicketEvent capturedEvent =
                eventCaptor.getValue();

        assertEquals(
                TicketStatus.OPEN,
                result.status()
        );

        assertEquals(
                requesterId,
                capturedTicket.getRequesterId()
        );

        assertEquals(
                NOW,
                capturedTicket.getCreatedAt()
        );

        assertEquals(
                capturedTicket.getId(),
                capturedEvent.ticketId()
        );

        assertEquals(
                requesterId,
                capturedEvent.actorId()
        );

        assertEquals(
                TicketEventType.TICKET_CREATED,
                capturedEvent.type()
        );

        assertNull(
                capturedEvent.previousValue()
        );

        assertEquals(
                "OPEN",
                capturedEvent.newValue()
        );

        assertEquals(
                NOW,
                capturedEvent.createdAt()
        );
    }
}