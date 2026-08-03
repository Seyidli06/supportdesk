package com.adil.supportdesk.application.port.out;

import com.adil.supportdesk.application.auth.AuthUser;

import java.util.Optional;

public interface UserAccountRepository {

    boolean existsByEmail(String email);

    Optional<AuthUser> findByEmail(String email);

    AuthUser save(AuthUser user);
}