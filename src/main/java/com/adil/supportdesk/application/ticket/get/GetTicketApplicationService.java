package com.adil.supportdesk.application.ticket.get;

import com.adil.supportdesk.application.port.out.TicketQueryRepository;
import com.adil.supportdesk.application.security.UnauthorizedAccessException;
import com.adil.supportdesk.application.security.UserContext;
import com.adil.supportdesk.application.security.UserRole;
import com.adil.supportdesk.domain.ticket.exception.TicketNotFoundException;
import com.adil.supportdesk.domain.ticket.valueobject.TicketId;
import com.adil.supportdesk.domain.user.valueobject.UserId;

import java.util.Objects;

public class GetTicketApplicationService
        implements GetTicketUseCase {

    private final TicketQueryRepository ticketQueryRepository;

    public GetTicketApplicationService(
            TicketQueryRepository ticketQueryRepository
    ) {
        this.ticketQueryRepository = Objects.requireNonNull(
                ticketQueryRepository,
                "TicketQueryRepository cannot be null"
        );
    }

    @Override
    public TicketResult getTicket(
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

        TicketResult ticket = ticketQueryRepository
                .findDetailsById(ticketId)
                .orElseThrow(() ->
                        new TicketNotFoundException(
                                ticketIdValue
                        )
                );

        validateAccess(
                ticket,
                userContext
        );

        return ticket;
    }

    private void validateAccess(
            TicketResult ticket,
            UserContext userContext
    ) {
        if (userContext.role() == UserRole.ADMIN) {
            return;
        }

        String actorId = UserId.of(
                userContext.userId()
        ).toString();

        boolean hasAccess =
                switch (userContext.role()) {
                    case USER ->
                            actorId.equals(
                                    ticket.requesterId()
                            );

                    case AGENT ->
                            actorId.equals(
                                    ticket.assignedAgentId()
                            );

                    case ADMIN -> true;
                };

        if (!hasAccess) {
            throw new UnauthorizedAccessException(
                    "You do not have access to this ticket"
            );
        }
    }
}