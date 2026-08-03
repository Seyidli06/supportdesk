package com.adil.supportdesk.adapter.out.persistence.user;

import com.adil.supportdesk.application.auth.AuthUser;
import com.adil.supportdesk.application.port.out.UserAccountPage;
import com.adil.supportdesk.application.port.out.UserAccountRepository;
import com.adil.supportdesk.application.port.out.UserAdministrationRepository;
import com.adil.supportdesk.application.security.UserRole;
import com.adil.supportdesk.domain.user.valueobject.UserId;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Repository
public class JpaUserAccountRepositoryAdapter
        implements UserAccountRepository,
        UserAdministrationRepository {

    private final SpringDataUserJpaRepository repository;

    public JpaUserAccountRepositoryAdapter(
            SpringDataUserJpaRepository repository
    ) {
        this.repository = repository;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByEmail(String email) {
        return repository.existsByEmailIgnoreCase(email);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<AuthUser> findByEmail(String email) {
        return repository
                .findByEmailIgnoreCase(email)
                .map(this::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<AuthUser> findById(
            UserId userId
    ) {
        return repository
                .findById(userId.value())
                .map(this::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public UserAccountPage findAll(
            UserRole role,
            String email,
            int page,
            int size
    ) {
        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by(
                                Sort.Direction.DESC,
                                "createdAt"
                        )
                        .and(
                                Sort.by(
                                        Sort.Direction.ASC,
                                        "id"
                                )
                        )
        );

        Specification<UserJpaEntity> specification =
                createSpecification(
                        role,
                        email
                );

        Page<UserJpaEntity> result =
                repository.findAll(
                        specification,
                        pageable
                );

        List<AuthUser> content = result
                .getContent()
                .stream()
                .map(this::toDomain)
                .toList();

        return new UserAccountPage(
                content,
                result.getNumber(),
                result.getSize(),
                result.getTotalElements(),
                result.getTotalPages(),
                result.isFirst(),
                result.isLast()
        );
    }

    @Override
    @Transactional
    public AuthUser save(AuthUser user) {
        UserJpaEntity entity = new UserJpaEntity(
                user.id().value(),
                user.email(),
                user.passwordHash(),
                user.fullName(),
                user.createdAt(),
                user.roles()
        );

        UserJpaEntity savedEntity =
                repository.saveAndFlush(entity);

        return toDomain(savedEntity);
    }

    private Specification<UserJpaEntity>
    createSpecification(
            UserRole role,
            String email
    ) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates =
                    new ArrayList<>();

            if (role != null) {
                predicates.add(
                        criteriaBuilder.equal(
                                root.join(
                                        "roles",
                                        JoinType.INNER
                                ),
                                role
                        )
                );

                query.distinct(true);
            }

            if (email != null && !email.isBlank()) {
                String emailPattern =
                        "%"
                                + email.trim()
                                .toLowerCase(Locale.ROOT)
                                + "%";

                predicates.add(
                        criteriaBuilder.like(
                                criteriaBuilder.lower(
                                        root.get("email")
                                ),
                                emailPattern
                        )
                );
            }

            return criteriaBuilder.and(
                    predicates.toArray(
                            Predicate[]::new
                    )
            );
        };
    }

    private AuthUser toDomain(
            UserJpaEntity entity
    ) {
        return new AuthUser(
                UserId.of(entity.getId()),
                entity.getEmail(),
                entity.getPasswordHash(),
                entity.getFullName(),
                entity.getRoles(),
                entity.getCreatedAt()
        );
    }
}