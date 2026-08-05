package com.adil.supportdesk.adapter.out.persistence.ticket;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface SpringDataTicketEventJpaRepository
        extends JpaRepository<
        TicketEventJpaEntity,
        UUID
        > {

    List<TicketEventJpaEntity>
    findAllByTicketIdOrderByCreatedAtAscIdAsc(
            UUID ticketId
    );
}