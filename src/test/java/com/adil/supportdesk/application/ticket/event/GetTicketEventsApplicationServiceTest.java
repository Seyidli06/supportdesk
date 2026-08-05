package com.adil.supportdesk.application.ticket.event;

import com.adil.supportdesk.application.port.out.TicketEventRepository;
import com.adil.supportdesk.application.security.UnauthorizedAccessException;
import com.adil.supportdesk.application.security.UserContext;
import com.adil.supportdesk.application.security.UserRole;
import com.adil.supportdesk.application.ticket.get.GetTicketUseCase;
import com.adil.supportdesk.domain.ticket.model.TicketEvent;
import com.adil.supportdesk.domain.ticket.model.TicketEventType;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetTicketEventsApplicationServiceTest {

    @Mock
    private GetTicketUseCase getTicketUseCase;

    @Mock
    private TicketEventRepository
            ticketEventRepository;

    private GetTicketEventsApplicationService
            service;

    @BeforeEach
    void setUp() {
        service =
                new GetTicketEventsApplicationService(
                        getTicketUseCase,
                        ticketEventRepository
                );
    }

    @Test
    @DisplayName(
            "Authorized user should receive "
                    + "ticket events"
    )
    void authorizedUserShouldReceiveEvents() {
        TicketId ticketId =
                TicketId.generate();

        UserId actorId =
                UserId.generate();

        UserContext context =
                new UserContext(
                        actorId.toString(),
                        UserRole.ADMIN
                );

        Instant firstTime =
                Instant.parse(
                        "2026-08-05T10:00:00Z"
                );

        Instant secondTime =
                firstTime.plusSeconds(60);

        TicketEvent createdEvent =
                TicketEvent.create(
                        ticketId,
                        actorId,
                        TicketEventType.TICKET_CREATED,
                        null,
                        "OPEN",
                        firstTime
                );

        TicketEvent statusEvent =
                TicketEvent.create(
                        ticketId,
                        actorId,
                        TicketEventType.STATUS_CHANGED,
                        "OPEN",
                        "IN_PROGRESS",
                        secondTime
                );

        when(
                ticketEventRepository.findByTicketId(
                        ticketId
                )
        ).thenReturn(
                List.of(
                        createdEvent,
                        statusEvent
                )
        );

        List<TicketEventResult> results =
                service.getEvents(
                        ticketId
                                .getValue()
                                .toString(),
                        context
                );

        assertEquals(
                2,
                results.size()
        );

        assertEquals(
                TicketEventType.TICKET_CREATED,
                results.get(0).type()
        );

        assertEquals(
                TicketEventType.STATUS_CHANGED,
                results.get(1).type()
        );

        assertEquals(
                "OPEN",
                results.get(1).previousValue()
        );

        assertEquals(
                "IN_PROGRESS",
                results.get(1).newValue()
        );

        verify(
                getTicketUseCase
        ).getTicket(
                ticketId
                        .getValue()
                        .toString(),
                context
        );
    }

    @Test
    @DisplayName(
            "Unauthorized user should not "
                    + "read ticket events"
    )
    void unauthorizedUserShouldBeDenied() {
        TicketId ticketId =
                TicketId.generate();

        UserContext context =
                new UserContext(
                        UserId.generate().toString(),
                        UserRole.USER
                );

        String ticketIdValue =
                ticketId.getValue().toString();

        doThrow(
                new UnauthorizedAccessException(
                        "You do not have access "
                                + "to this ticket"
                )
        ).when(
                getTicketUseCase
        ).getTicket(
                ticketIdValue,
                context
        );

        assertThrows(
                UnauthorizedAccessException.class,
                () -> service.getEvents(
                        ticketIdValue,
                        context
                )
        );

        verifyNoInteractions(
                ticketEventRepository
        );
    }
}