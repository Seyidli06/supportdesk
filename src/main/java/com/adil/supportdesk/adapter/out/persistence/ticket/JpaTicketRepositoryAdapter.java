package com.adil.supportdesk.adapter.out.persistence.ticket;

import com.adil.supportdesk.application.port.out.TicketRepository;
import com.adil.supportdesk.domain.ticket.model.Ticket;
import com.adil.supportdesk.domain.ticket.valueobject.TicketId;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@Repository
public class JpaTicketRepositoryAdapter
        implements TicketRepository {

    private final SpringDataTicketJpaRepository repository;
    private final TicketPersistenceMapper mapper;

    public JpaTicketRepositoryAdapter(
            SpringDataTicketJpaRepository repository,
            TicketPersistenceMapper mapper
    ) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    @Transactional
    public Ticket save(Ticket ticket) {
        Objects.requireNonNull(
                ticket,
                "Ticket cannot be null"
        );

        UUID ticketId =
                ticket.getId().getValue();

        TicketJpaEntity entity = repository
                .findById(ticketId)
                .orElseGet(() ->
                        new TicketJpaEntity(ticketId)
                );

        mapper.updateEntity(ticket, entity);

        TicketJpaEntity savedEntity =
                repository.saveAndFlush(entity);

        return mapper.toDomain(savedEntity);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Ticket> findById(TicketId id) {
        Objects.requireNonNull(
                id,
                "TicketId cannot be null"
        );

        return repository
                .findById(id.getValue())
                .map(mapper::toDomain);
    }
}