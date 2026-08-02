package com.adil.supportdesk.application.ticket.assign;

import com.adil.supportdesk.application.security.UserContext;
import com.adil.supportdesk.application.ticket.get.TicketResult;

public interface AssignTicketUseCase {

    TicketResult assignTicket(
            AssignTicketCommand command,
            UserContext userContext
    );
}