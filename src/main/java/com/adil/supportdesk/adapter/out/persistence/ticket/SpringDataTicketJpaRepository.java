package com.adil.supportdesk.adapter.out.persistence.ticket;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface SpringDataTicketJpaRepository
        extends JpaRepository<TicketJpaEntity, UUID>,
        JpaSpecificationExecutor<TicketJpaEntity> {

    @EntityGraph(attributePaths = "comments")
    @Query("""
            SELECT ticket
            FROM TicketJpaEntity ticket
            WHERE ticket.id = :ticketId
            """)
    Optional<TicketJpaEntity> findDetailedById(
            @Param("ticketId") UUID ticketId
    );
}