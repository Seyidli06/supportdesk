package com.adil.supportdesk.adapter.out.persistence.ticket;

import com.adil.supportdesk.application.port.out.TicketEventRepository;
import com.adil.supportdesk.domain.ticket.model.TicketEvent;
import com.adil.supportdesk.domain.ticket.valueobject.TicketId;
import com.adil.supportdesk.domain.user.valueobject.UserId;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Repository
public class JpaTicketEventRepositoryAdapter
        implements TicketEventRepository {

    private final SpringDataTicketEventJpaRepository
            repository;

    public JpaTicketEventRepositoryAdapter(
            SpringDataTicketEventJpaRepository repository
    ) {
        this.repository = Objects.requireNonNull(
                repository,
                "Ticket event repository cannot be null"
        );
    }

    @Override
    @Transactional
    public TicketEvent save(
            TicketEvent event
    ) {
        Objects.requireNonNull(
                event,
                "Ticket event cannot be null"
        );

        TicketEventJpaEntity entity =
                toEntity(event);

        TicketEventJpaEntity savedEntity =
                repository.saveAndFlush(entity);

        return toDomain(savedEntity);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TicketEvent> findByTicketId(
            TicketId ticketId
    ) {
        Objects.requireNonNull(
                ticketId,
                "Ticket id cannot be null"
        );

        return repository
                .findAllByTicketIdOrderByCreatedAtAscIdAsc(
                        ticketId.getValue()
                )
                .stream()
                .map(this::toDomain)
                .toList();
    }

    private TicketEventJpaEntity toEntity(
            TicketEvent event
    ) {
        return new TicketEventJpaEntity(
                event.id(),
                event.ticketId().getValue(),
                event.actorId().value(),
                event.type(),
                event.previousValue(),
                event.newValue(),
                event.createdAt()
        );
    }

    private TicketEvent toDomain(
            TicketEventJpaEntity entity
    ) {
        return new TicketEvent(
                entity.getId(),
                new TicketId(
                        entity.getTicketId()
                ),
                UserId.of(
                        entity.getActorId()
                ),
                entity.getEventType(),
                entity.getPreviousValue(),
                entity.getNewValue(),
                entity.getCreatedAt()
        );
    }
}