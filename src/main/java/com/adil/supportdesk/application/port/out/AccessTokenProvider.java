package com.adil.supportdesk.application.port.out;

import com.adil.supportdesk.application.auth.AccessToken;
import com.adil.supportdesk.application.auth.AuthUser;

public interface AccessTokenProvider {

    AccessToken issueToken(AuthUser user);
}