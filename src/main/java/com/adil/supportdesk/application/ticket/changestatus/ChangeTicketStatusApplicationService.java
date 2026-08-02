package com.adil.supportdesk.application.ticket.changestatus;

import com.adil.supportdesk.application.port.out.TicketRepository;
import com.adil.supportdesk.application.security.UnauthorizedAccessException;
import com.adil.supportdesk.application.security.UserContext;
import com.adil.supportdesk.application.security.UserRole;
import com.adil.supportdesk.application.ticket.get.TicketResult;
import com.adil.supportdesk.domain.ticket.exception.TicketNotFoundException;
import com.adil.supportdesk.domain.ticket.model.Ticket;
import com.adil.supportdesk.domain.ticket.valueobject.TicketId;
import com.adil.supportdesk.domain.user.valueobject.UserId;

import java.time.Clock;
import java.time.Instant;
import java.util.Objects;

public class ChangeTicketStatusApplicationService
        implements ChangeTicketStatusUseCase {

    private final TicketRepository ticketRepository;
    private final Clock clock;

    public ChangeTicketStatusApplicationService(
            TicketRepository ticketRepository,
            Clock clock
    ) {
        this.ticketRepository = Objects.requireNonNull(
                ticketRepository,
                "TicketRepository cannot be null"
        );

        this.clock = Objects.requireNonNull(
                clock,
                "Clock cannot be null"
        );
    }

    @Override
    public TicketResult changeStatus(
            ChangeTicketStatusCommand command,
            UserContext userContext
    ) {
        Objects.requireNonNull(
                command,
                "Command cannot be null"
        );

        Objects.requireNonNull(
                userContext,
                "UserContext cannot be null"
        );

        validateActorRole(userContext);

        TicketId ticketId = TicketId.of(
                command.ticketId()
        );

        Ticket ticket = ticketRepository
                .findById(ticketId)
                .orElseThrow(() ->
                        new TicketNotFoundException(
                                command.ticketId()
                        )
                );

        validateTicketAccess(
                ticket,
                userContext
        );

        ticket.transitionTo(
                command.newStatus(),
                Instant.now(clock)
        );

        Ticket savedTicket =
                ticketRepository.save(ticket);

        return TicketResult.from(savedTicket);
    }

    private void validateActorRole(
            UserContext userContext
    ) {
        if (!userContext.isAgentOrAdmin()) {
            throw new UnauthorizedAccessException(
                    "Only assigned agents or admins "
                            + "can change ticket status"
            );
        }
    }

    private void validateTicketAccess(
            Ticket ticket,
            UserContext userContext
    ) {
        if (userContext.role() == UserRole.ADMIN) {
            return;
        }

        UserId actorId = UserId.of(
                userContext.userId()
        );

        if (!ticket.isAssignedTo(actorId)) {
            throw new UnauthorizedAccessException(
                    "Agent can only change the status "
                            + "of tickets assigned to them"
            );
        }
    }
}