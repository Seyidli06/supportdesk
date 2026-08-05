package com.adil.supportdesk.application.ticket.comment;

import com.adil.supportdesk.application.port.out.TicketMutationRepository;
import com.adil.supportdesk.application.port.out.TicketRepository;
import com.adil.supportdesk.application.security.UnauthorizedAccessException;
import com.adil.supportdesk.application.security.UserContext;
import com.adil.supportdesk.application.security.UserRole;
import com.adil.supportdesk.application.ticket.get.TicketResult;
import com.adil.supportdesk.domain.ticket.exception.TicketNotFoundException;
import com.adil.supportdesk.domain.ticket.model.Comment;
import com.adil.supportdesk.domain.ticket.model.Ticket;
import com.adil.supportdesk.domain.ticket.model.TicketEvent;
import com.adil.supportdesk.domain.ticket.model.TicketEventType;
import com.adil.supportdesk.domain.ticket.valueobject.TicketId;
import com.adil.supportdesk.domain.user.valueobject.UserId;

import java.time.Clock;
import java.time.Instant;
import java.util.Objects;

public class AddCommentApplicationService
        implements AddCommentUseCase {

    private final TicketRepository ticketRepository;

    private final TicketMutationRepository
            ticketMutationRepository;

    private final Clock clock;

    public AddCommentApplicationService(
            TicketRepository ticketRepository,
            TicketMutationRepository
                    ticketMutationRepository,
            Clock clock
    ) {
        this.ticketRepository = Objects.requireNonNull(
                ticketRepository,
                "TicketRepository cannot be null"
        );

        this.ticketMutationRepository =
                Objects.requireNonNull(
                        ticketMutationRepository,
                        "TicketMutationRepository cannot be null"
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

        Instant now = Instant.now(clock);

        ticket.addComment(
                authorId,
                command.content(),
                now
        );

        Comment addedComment =
                ticket.getComments()
                        .getLast();

        TicketEvent commentEvent =
                TicketEvent.create(
                        ticket.getId(),
                        authorId,
                        TicketEventType.COMMENT_ADDED,
                        null,
                        addedComment
                                .getId()
                                .getValue()
                                .toString(),
                        now
                );

        Ticket savedTicket =
                ticketMutationRepository
                        .saveWithEvent(
                                ticket,
                                commentEvent
                        );

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