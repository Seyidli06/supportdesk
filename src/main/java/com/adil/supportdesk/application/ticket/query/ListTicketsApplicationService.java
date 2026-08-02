package com.adil.supportdesk.application.ticket.query;

import com.adil.supportdesk.application.port.out.TicketQueryRepository;
import com.adil.supportdesk.application.security.UserContext;
import com.adil.supportdesk.domain.user.valueobject.UserId;

import java.util.Objects;

public class ListTicketsApplicationService
        implements ListTicketsUseCase {

    private final TicketQueryRepository ticketQueryRepository;

    public ListTicketsApplicationService(
            TicketQueryRepository ticketQueryRepository
    ) {
        this.ticketQueryRepository = Objects.requireNonNull(
                ticketQueryRepository,
                "TicketQueryRepository cannot be null"
        );
    }

    @Override
    public TicketPageResult listTickets(
            ListTicketsQuery query,
            UserContext userContext
    ) {
        Objects.requireNonNull(
                query,
                "Query cannot be null"
        );

        Objects.requireNonNull(
                userContext,
                "UserContext cannot be null"
        );

        UserId actorId = UserId.of(
                userContext.userId()
        );

        TicketSearchCriteria criteria =
                switch (userContext.role()) {
                    case USER ->
                            new TicketSearchCriteria(
                                    actorId,
                                    null,
                                    query.status(),
                                    query.priority(),
                                    query.page(),
                                    query.size()
                            );

                    case AGENT ->
                            new TicketSearchCriteria(
                                    null,
                                    actorId,
                                    query.status(),
                                    query.priority(),
                                    query.page(),
                                    query.size()
                            );

                    case ADMIN ->
                            new TicketSearchCriteria(
                                    null,
                                    null,
                                    query.status(),
                                    query.priority(),
                                    query.page(),
                                    query.size()
                            );
                };

        return ticketQueryRepository.findPage(
                criteria
        );
    }
}