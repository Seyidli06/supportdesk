package com.adil.supportdesk.application.ticket.event;

import com.adil.supportdesk.application.port.out.TicketEventRepository;
import com.adil.supportdesk.application.security.UserContext;
import com.adil.supportdesk.application.ticket.get.GetTicketUseCase;
import com.adil.supportdesk.domain.ticket.valueobject.TicketId;

import java.util.List;
import java.util.Objects;

public class GetTicketEventsApplicationService
        implements GetTicketEventsUseCase {

    private final GetTicketUseCase
            getTicketUseCase;

    private final TicketEventRepository
            ticketEventRepository;

    public GetTicketEventsApplicationService(
            GetTicketUseCase getTicketUseCase,
            TicketEventRepository
                    ticketEventRepository
    ) {
        this.getTicketUseCase =
                Objects.requireNonNull(
                        getTicketUseCase,
                        "GetTicketUseCase cannot be null"
                );

        this.ticketEventRepository =
                Objects.requireNonNull(
                        ticketEventRepository,
                        "TicketEventRepository cannot be null"
                );
    }

    @Override
    public List<TicketEventResult> getEvents(
            String ticketIdValue,
            UserContext userContext
    ) {
        Objects.requireNonNull(
                userContext,
                "UserContext cannot be null"
        );

        TicketId ticketId = TicketId.of(
                ticketIdValue
        );

        getTicketUseCase.getTicket(
                ticketIdValue,
                userContext
        );

        return ticketEventRepository
                .findByTicketId(ticketId)
                .stream()
                .map(TicketEventResult::from)
                .toList();
    }
}