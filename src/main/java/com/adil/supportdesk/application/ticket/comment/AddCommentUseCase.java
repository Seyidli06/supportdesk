package com.adil.supportdesk.application.ticket.comment;

import com.adil.supportdesk.application.security.UserContext;
import com.adil.supportdesk.application.ticket.get.TicketResult;

public interface AddCommentUseCase {

    TicketResult addComment(
            AddCommentCommand command,
            UserContext userContext
    );
}