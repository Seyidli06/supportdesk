package com.adil.supportdesk.application.ticket.comment;

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

public class AddCommentApplicationService
        implements AddCommentUseCase {

    private final TicketRepository ticketRepository;
    private final Clock clock;

    public AddCommentApplicationService(
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
    public TicketResult addComment(
            AddCommentCommand command,
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

        UserId authorId = UserId.of(
                userContext.userId()
        );

        validateCommentAccess(
                ticket,
                authorId,
                userContext.role()
        );

        ticket.addComment(
                authorId,
                command.content(),
                Instant.now(clock)
        );

        Ticket savedTicket =
                ticketRepository.save(ticket);

        return TicketResult.from(savedTicket);
    }

    private void validateCommentAccess(
            Ticket ticket,
            UserId userId,
            UserRole role
    ) {
        if (role == UserRole.ADMIN) {
            return;
        }

        if (ticket.isRequestedBy(userId)) {
            return;
        }

        if (
                role == UserRole.AGENT
                        && ticket.isAssignedTo(userId)
        ) {
            return;
        }

        throw new UnauthorizedAccessException(
                "Only the requester, assigned agent or admin "
                        + "can comment on this ticket"
        );
    }
}