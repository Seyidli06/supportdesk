package com.adil.supportdesk.application.port.out;

import com.adil.supportdesk.application.user.UserSummary;
import com.adil.supportdesk.domain.user.valueobject.UserId;

import java.util.Optional;

public interface UserDirectory {

    Optional<UserSummary> findById(UserId userId);
}