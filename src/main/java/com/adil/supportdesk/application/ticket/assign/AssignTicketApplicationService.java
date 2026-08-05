package com.adil.supportdesk.application.ticket.assign;

import com.adil.supportdesk.application.port.out.TicketRepository;
import com.adil.supportdesk.application.port.out.UserDirectory;
import com.adil.supportdesk.application.security.UnauthorizedAccessException;
import com.adil.supportdesk.application.security.UserContext;
import com.adil.supportdesk.application.security.UserRole;
import com.adil.supportdesk.application.ticket.get.TicketResult;
import com.adil.supportdesk.application.user.UserNotFoundException;
import com.adil.supportdesk.application.user.UserSummary;
import com.adil.supportdesk.domain.ticket.exception.TicketNotFoundException;
import com.adil.supportdesk.domain.ticket.model.Ticket;
import com.adil.supportdesk.domain.ticket.valueobject.TicketId;
import com.adil.supportdesk.domain.user.valueobject.UserId;

import java.time.Clock;
import java.time.Instant;
import java.util.Objects;

public class AssignTicketApplicationService
        implements AssignTicketUseCase {

    private final TicketRepository ticketRepository;
    private final UserDirectory userDirectory;
    private final Clock clock;

    public AssignTicketApplicationService(
            TicketRepository ticketRepository,
            UserDirectory userDirectory,
            Clock clock
    ) {
        this.ticketRepository = Objects.requireNonNull(
                ticketRepository,
                "TicketRepository cannot be null"
        );

        this.userDirectory = Objects.requireNonNull(
                userDirectory,
                "UserDirectory cannot be null"
        );

        this.clock = Objects.requireNonNull(
                clock,
                "Clock cannot be null"
        );
    }

    @Override
    public TicketResult assignTicket(
            AssignTicketCommand command,
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

        UserId targetAgentId = UserId.of(
                command.agentId()
        );

        validateAgentSelfAssignment(
                userContext,
                actorId,
                targetAgentId
        );

        UserSummary targetUser = userDirectory
                .findById(targetAgentId)
                .orElseThrow(() ->
                        new UserNotFoundException(
                                targetAgentId.toString()
                        )
                );

        if (!targetUser.hasRole(UserRole.AGENT)) {
            throw new InvalidAssigneeException(
                    targetAgentId.toString()
            );
        }

        Ticket ticket = ticketRepository
                .findById(ticketId)
                .orElseThrow(() ->
                        new TicketNotFoundException(
                                command.ticketId()
                        )
                );

        validateAgentAssignmentOwnership(
                userContext,
                actorId,
                ticket
        );

        Instant now = Instant.now(clock);

        ticket.assignTo(
                targetAgentId,
                now
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
                    "Only AGENT or ADMIN can assign tickets"
            );
        }
    }

    private void validateAgentSelfAssignment(
            UserContext userContext,
            UserId actorId,
            UserId targetAgentId
    ) {
        if (
                userContext.role() == UserRole.AGENT
                        && !actorId.equals(targetAgentId)
        ) {
            throw new UnauthorizedAccessException(
                    "Agents can only assign tickets to themselves"
            );
        }
    }

    private void validateAgentAssignmentOwnership(
            UserContext userContext,
            UserId actorId,
            Ticket ticket
    ) {
        if (
                userContext.role() == UserRole.AGENT
                        && ticket.isAssigned()
                        && !ticket.isAssignedTo(actorId)
        ) {
            throw new UnauthorizedAccessException(
                    "Agents cannot take over tickets assigned to another agent"
            );
        }
    }

}