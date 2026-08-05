package com.adil.supportdesk.application.ticket.changepriority;

import com.adil.supportdesk.application.port.out.TicketMutationRepository;
import com.adil.supportdesk.application.port.out.TicketRepository;
import com.adil.supportdesk.application.security.UnauthorizedAccessException;
import com.adil.supportdesk.application.security.UserContext;
import com.adil.supportdesk.application.security.UserRole;
import com.adil.supportdesk.application.ticket.get.TicketResult;
import com.adil.supportdesk.domain.ticket.exception.TicketNotFoundException;
import com.adil.supportdesk.domain.ticket.model.Ticket;
import com.adil.supportdesk.domain.ticket.model.TicketEvent;
import com.adil.supportdesk.domain.ticket.model.TicketEventType;
import com.adil.supportdesk.domain.ticket.model.TicketPriority;
import com.adil.supportdesk.domain.ticket.valueobject.TicketId;
import com.adil.supportdesk.domain.user.valueobject.UserId;

import java.time.Clock;
import java.time.Instant;
import java.util.Objects;

public class ChangeTicketPriorityApplicationService
        implements ChangeTicketPriorityUseCase {

    private final TicketRepository ticketRepository;

    private final TicketMutationRepository
            ticketMutationRepository;

    private final Clock clock;

    public ChangeTicketPriorityApplicationService(
            TicketRepository ticketRepository,
            TicketMutationRepository
                    ticketMutationRepository,
            Clock clock
    ) {
        this.ticketRepository =
                Objects.requireNonNull(
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
    public TicketResult changePriority(
            ChangeTicketPriorityCommand command,
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

        UserId actorId = UserId.of(
                userContext.userId()
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
                userContext,
                actorId
        );

        TicketPriority previousPriority =
                ticket.getPriority();

        Instant now = Instant.now(clock);

        ticket.changePriority(
                command.newPriority(),
                now
        );

        boolean priorityUnchanged =
                previousPriority
                        == ticket.getPriority();

        if (priorityUnchanged) {
            return TicketResult.from(ticket);
        }

        TicketEvent priorityEvent =
                TicketEvent.create(
                        ticket.getId(),
                        actorId,
                        TicketEventType.PRIORITY_CHANGED,
                        previousPriority.name(),
                        ticket.getPriority().name(),
                        now
                );

        Ticket savedTicket =
                ticketMutationRepository
                        .saveWithEvent(
                                ticket,
                                priorityEvent
                        );

        return TicketResult.from(savedTicket);
    }

    private void validateActorRole(
            UserContext userContext
    ) {
        if (!userContext.isAgentOrAdmin()) {
            throw new UnauthorizedAccessException(
                    "Only assigned agents or admins "
                            + "can change ticket priority"
            );
        }
    }

    private void validateTicketAccess(
            Ticket ticket,
            UserContext userContext,
            UserId actorId
    ) {
        if (userContext.role() == UserRole.ADMIN) {
            return;
        }

        if (!ticket.isAssignedTo(actorId)) {
            throw new UnauthorizedAccessException(
                    "Agent can only change the priority "
                            + "of tickets assigned to them"
            );
        }
    }
}