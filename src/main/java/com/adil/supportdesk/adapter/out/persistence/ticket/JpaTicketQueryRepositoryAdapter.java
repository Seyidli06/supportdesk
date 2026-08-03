package com.adil.supportdesk.adapter.out.persistence.ticket;

import com.adil.supportdesk.application.port.out.TicketQueryRepository;
import com.adil.supportdesk.application.ticket.get.TicketResult;
import com.adil.supportdesk.application.ticket.query.TicketPageResult;
import com.adil.supportdesk.application.ticket.query.TicketSearchCriteria;
import com.adil.supportdesk.application.ticket.query.TicketSummaryResult;
import com.adil.supportdesk.domain.ticket.valueobject.TicketId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
@Transactional(readOnly = true)
public class JpaTicketQueryRepositoryAdapter
        implements TicketQueryRepository {

    private final SpringDataTicketJpaRepository repository;

    public JpaTicketQueryRepositoryAdapter(
            SpringDataTicketJpaRepository repository
    ) {
        this.repository = repository;
    }

    @Override
    public Optional<TicketResult> findDetailsById(
            TicketId ticketId
    ) {
        return repository
                .findDetailedById(
                        ticketId.getValue()
                )
                .map(this::toDetailsResult);
    }

    @Override
    public TicketPageResult findPage(
            TicketSearchCriteria criteria
    ) {
        Specification<TicketJpaEntity> specification =
                createSpecification(criteria);

        PageRequest pageRequest = PageRequest.of(
                criteria.page(),
                criteria.size(),
                Sort.by(
                        Sort.Order.desc("createdAt"),
                        Sort.Order.desc("id")
                )
        );

        Page<TicketJpaEntity> result =
                repository.findAll(
                        specification,
                        pageRequest
                );

        List<TicketSummaryResult> content =
                result.getContent()
                        .stream()
                        .map(this::toSummaryResult)
                        .toList();

        return new TicketPageResult(
                content,
                result.getNumber(),
                result.getSize(),
                result.getTotalElements(),
                result.getTotalPages(),
                result.isFirst(),
                result.isLast()
        );
    }

    private Specification<TicketJpaEntity>
    createSpecification(
            TicketSearchCriteria criteria
    ) {
        Specification<TicketJpaEntity> specification =
                (root, query, criteriaBuilder) ->
                        criteriaBuilder.conjunction();

        if (criteria.requesterId() != null) {
            specification = specification.and(
                    (root, query, criteriaBuilder) ->
                            criteriaBuilder.equal(
                                    root.get("requesterId"),
                                    criteria.requesterId()
                                            .value()
                            )
            );
        }

        if (criteria.assignedAgentId() != null) {
            specification = specification.and(
                    (root, query, criteriaBuilder) ->
                            criteriaBuilder.equal(
                                    root.get(
                                            "assignedAgentId"
                                    ),
                                    criteria.assignedAgentId()
                                            .value()
                            )
            );
        }

        if (criteria.status() != null) {
            specification = specification.and(
                    (root, query, criteriaBuilder) ->
                            criteriaBuilder.equal(
                                    root.get("status"),
                                    criteria.status()
                            )
            );
        }

        if (criteria.priority() != null) {
            specification = specification.and(
                    (root, query, criteriaBuilder) ->
                            criteriaBuilder.equal(
                                    root.get("priority"),
                                    criteria.priority()
                            )
            );
        }

        return specification;
    }

    private TicketSummaryResult toSummaryResult(
            TicketJpaEntity entity
    ) {
        return new TicketSummaryResult(
                entity.getId().toString(),
                entity.getTitle(),
                entity.getPriority(),
                entity.getStatus(),
                entity.getRequesterId().toString(),
                entity.getAssignedAgentId() == null
                        ? null
                        : entity.getAssignedAgentId()
                        .toString(),
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                entity.getSlaDueAt()
        );
    }

    private TicketResult toDetailsResult(
            TicketJpaEntity entity
    ) {
        List<TicketResult.CommentResult> comments =
                entity.getComments()
                        .stream()
                        .map(comment ->
                                new TicketResult.CommentResult(
                                        comment.getId()
                                                .toString(),
                                        comment.getAuthorId()
                                                .toString(),
                                        comment.getContent(),
                                        comment.getCreatedAt()
                                )
                        )
                        .toList();

        return new TicketResult(
                entity.getId().toString(),
                entity.getTitle(),
                entity.getDescription(),
                entity.getPriority(),
                entity.getStatus(),
                entity.getRequesterId().toString(),
                entity.getAssignedAgentId() == null
                        ? null
                        : entity.getAssignedAgentId()
                        .toString(),
                comments,
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                entity.getResolvedAt(),
                entity.getClosedAt(),
                entity.getSlaDueAt()
        );
    }
}