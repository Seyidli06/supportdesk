package com.adil.supportdesk.adapter.out.persistence.ticket;

import com.adil.supportdesk.application.port.out.TicketRepository;
import com.adil.supportdesk.domain.ticket.model.Ticket;
import com.adil.supportdesk.domain.ticket.valueobject.TicketId;
import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class InMemoryTicketRepository implements TicketRepository {

    private final Map<TicketId, Ticket> storage = new ConcurrentHashMap<>();

    @Override
    public Ticket save(Ticket ticket) {
        storage.put(ticket.getId(), ticket);
        return ticket;
    }

    @Override
    public Optional<Ticket> findById(TicketId id) {
        return Optional.ofNullable(storage.get(id));
    }
}