package com.adil.supportdesk.application.ticket.create;

import com.adil.supportdesk.application.port.out.TicketRepository;
import com.adil.supportdesk.application.security.UserContext;
import com.adil.supportdesk.application.ticket.get.TicketResult;
import com.adil.supportdesk.domain.ticket.model.Ticket;
import com.adil.supportdesk.domain.ticket.valueobject.TicketId;
import com.adil.supportdesk.domain.user.valueobject.UserId;

import java.time.Clock;
import java.time.Instant;
import java.util.Objects;

public class CreateTicketApplicationService
        implements CreateTicketUseCase {

    private final TicketRepository ticketRepository;
    private final Clock clock;

    public CreateTicketApplicationService(
            TicketRepository ticketRepository,
            Clock clock
    ) {
        this.ticketRepository = Objects.requireNonNull(
                ticketRepository
        );
        this.clock = Objects.requireNonNull(clock);
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

        Ticket savedTicket = ticketRepository.save(ticket);

        return TicketResult.from(savedTicket);
    }
}