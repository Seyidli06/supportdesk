package com.adil.supportdesk.application.ticket.changestatus;

import com.adil.supportdesk.application.port.out.TicketRepository;
import com.adil.supportdesk.application.security.UnauthorizedAccessException;
import com.adil.supportdesk.application.security.UserContext;
import com.adil.supportdesk.application.ticket.get.TicketResult;
import com.adil.supportdesk.domain.ticket.model.Ticket;
import com.adil.supportdesk.domain.ticket.valueobject.TicketId;

import java.time.Instant;

public class ChangeTicketStatusApplicationService implements ChangeTicketStatusUseCase {

    private final TicketRepository ticketRepository;

    public ChangeTicketStatusApplicationService(TicketRepository ticketRepository) {
        this.ticketRepository = ticketRepository;
    }

    @Override
    public TicketResult changeStatus(ChangeTicketStatusCommand command, UserContext userContext) {
        if (!userContext.isAgentOrAdmin()) {
            throw new UnauthorizedAccessException("Only agents or admins can change ticket status");
        }

        TicketId ticketId = TicketId.of(command.ticketId());
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new IllegalArgumentException("Ticket not found: " + command.ticketId()));

        ticket.transitionTo(command.newStatus(), Instant.now());
        Ticket updatedTicket = ticketRepository.save(ticket);

        return TicketResult.from(updatedTicket);
    }
}