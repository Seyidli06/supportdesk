package com.adil.supportdesk.application.ticket.changepriority;

import com.adil.supportdesk.application.security.UserContext;
import com.adil.supportdesk.application.ticket.get.TicketResult;

public interface ChangeTicketPriorityUseCase {

    TicketResult changePriority(
            ChangeTicketPriorityCommand command,
            UserContext userContext
    );
}