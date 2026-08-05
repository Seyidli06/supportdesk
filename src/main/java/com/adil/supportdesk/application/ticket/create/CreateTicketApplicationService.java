package com.adil.supportdesk.application.ticket.create;

import com.adil.supportdesk.application.port.out.TicketMutationRepository;
import com.adil.supportdesk.application.security.UserContext;
import com.adil.supportdesk.application.ticket.get.TicketResult;
import com.adil.supportdesk.domain.ticket.model.Ticket;
import com.adil.supportdesk.domain.ticket.model.TicketEvent;
import com.adil.supportdesk.domain.ticket.model.TicketEventType;
import com.adil.supportdesk.domain.ticket.valueobject.TicketId;
import com.adil.supportdesk.domain.user.valueobject.UserId;

import java.time.Clock;
import java.time.Instant;
import java.util.Objects;

public class CreateTicketApplicationService
        implements CreateTicketUseCase {

    private final TicketMutationRepository
            ticketMutationRepository;

    private final Clock clock;

    public CreateTicketApplicationService(
            TicketMutationRepository
                    ticketMutationRepository,
            Clock clock
    ) {
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
    public TicketResult createTicket(
            CreateTicketCommand command,
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

        UserId requesterId = UserId.of(
                userContext.userId()
        );

        Instant now = Instant.now(clock);

        Ticket ticket = new Ticket(
                TicketId.generate(),
                requesterId,
                command.title(),
                command.description(),
                command.priority(),
                now
        );

        TicketEvent creationEvent =
                TicketEvent.create(
                        ticket.getId(),
                        requesterId,
                        TicketEventType.TICKET_CREATED,
                        null,
                        ticket.getStatus().name(),
                        now
                );

        Ticket savedTicket =
                ticketMutationRepository
                        .saveWithEvent(
                                ticket,
                                creationEvent
                        );

        return TicketResult.from(savedTicket);
    }
}