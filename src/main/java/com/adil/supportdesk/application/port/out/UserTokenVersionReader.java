package com.adil.supportdesk.application.port.out;

import com.adil.supportdesk.domain.user.valueobject.UserId;

import java.util.OptionalLong;

public interface UserTokenVersionReader {

    OptionalLong findTokenVersionById(
            UserId userId
    );
}