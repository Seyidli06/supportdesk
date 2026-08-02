package com.adil.supportdesk.application.ticket.create;

import com.adil.supportdesk.application.security.UserContext;
import com.adil.supportdesk.application.ticket.get.TicketResult;

public interface CreateTicketUseCase {

    TicketResult createTicket(
            CreateTicketCommand command,
            UserContext userContext
    );
}