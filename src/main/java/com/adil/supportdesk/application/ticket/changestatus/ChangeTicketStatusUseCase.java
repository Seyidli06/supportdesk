package com.adil.supportdesk.application.ticket.changestatus;

import com.adil.supportdesk.application.security.UserContext;
import com.adil.supportdesk.application.ticket.get.TicketResult;

public interface ChangeTicketStatusUseCase {
    TicketResult changeStatus(ChangeTicketStatusCommand command, UserContext userContext);
}