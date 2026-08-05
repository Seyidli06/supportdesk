package com.adil.supportdesk.adapter.out.persistence.ticket;

import com.adil.supportdesk.application.port.out.TicketEventRepository;
import com.adil.supportdesk.application.port.out.TicketMutationRepository;
import com.adil.supportdesk.application.port.out.TicketRepository;
import com.adil.supportdesk.domain.ticket.model.Ticket;
import com.adil.supportdesk.domain.ticket.model.TicketEvent;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@Repository
public class JpaTicketMutationRepositoryAdapter
        implements TicketMutationRepository {

    private final TicketRepository ticketRepository;

    private final TicketEventRepository
            ticketEventRepository;

    public JpaTicketMutationRepositoryAdapter(
            TicketRepository ticketRepository,
            TicketEventRepository ticketEventRepository
    ) {
        this.ticketRepository =
                Objects.requireNonNull(
                        ticketRepository,
                        "TicketRepository cannot be null"
                );

        this.ticketEventRepository =
                Objects.requireNonNull(
                        ticketEventRepository,
                        "TicketEventRepository cannot be null"
                );
    }

    @Override
    @Transactional
    public Ticket saveWithEvent(
            Ticket ticket,
            TicketEvent event
    ) {
        Objects.requireNonNull(
                ticket,
                "Ticket cannot be null"
        );

        Objects.requireNonNull(
                event,
                "Ticket event cannot be null"
        );

        Ticket savedTicket =
                ticketRepository.save(ticket);

        ticketEventRepository.save(event);

        return savedTicket;
    }
}