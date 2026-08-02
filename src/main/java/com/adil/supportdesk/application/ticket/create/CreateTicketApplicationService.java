package com.adil.supportdesk.application.ticket.create;

import com.adil.supportdesk.application.port.out.TicketRepository;
import com.adil.supportdesk.application.ticket.get.TicketResult;
import com.adil.supportdesk.domain.ticket.model.Ticket;
import com.adil.supportdesk.domain.ticket.valueobject.TicketId;

import java.time.Instant;

public class CreateTicketApplicationService implements CreateTicketUseCase {

    private final TicketRepository ticketRepository;

    public CreateTicketApplicationService(TicketRepository ticketRepository) {
        this.ticketRepository = ticketRepository;
    }

    @Override
    public TicketResult createTicket(CreateTicketCommand command) {
        Ticket ticket = new Ticket(
                TicketId.generate(),
                command.title(),
                command.description(),
                Instant.now()
        );

        Ticket savedTicket = ticketRepository.save(ticket);
        return TicketResult.from(savedTicket);
    }
}