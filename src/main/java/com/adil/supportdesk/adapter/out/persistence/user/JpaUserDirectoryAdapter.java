package com.adil.supportdesk.adapter.out.persistence.user;

import com.adil.supportdesk.application.port.out.UserDirectory;
import com.adil.supportdesk.application.user.UserSummary;
import com.adil.supportdesk.domain.user.valueobject.UserId;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
@Transactional(readOnly = true)
public class JpaUserDirectoryAdapter
        implements UserDirectory {

    private final SpringDataUserJpaRepository repository;

    public JpaUserDirectoryAdapter(
            SpringDataUserJpaRepository repository
    ) {
        this.repository = repository;
    }

    @Override
    public Optional<UserSummary> findById(UserId userId) {
        return repository
                .findById(userId.value())
                .map(entity ->
                        new UserSummary(
                                UserId.of(entity.getId()),
                                entity.getRoles()
                        )
                );
    }
}