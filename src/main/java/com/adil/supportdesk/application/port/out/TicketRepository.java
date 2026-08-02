package com.adil.supportdesk.application.port.out;

import com.adil.supportdesk.domain.ticket.model.Ticket;
import com.adil.supportdesk.domain.ticket.valueobject.TicketId;

import java.util.Optional;

public interface TicketRepository {
    Ticket save(Ticket ticket);
    Optional<Ticket> findById(TicketId id);
}