package com.adil.supportdesk.application.ticket.query;

import com.adil.supportdesk.domain.ticket.model.TicketPriority;
import com.adil.supportdesk.domain.ticket.model.TicketStatus;
import com.adil.supportdesk.domain.user.valueobject.UserId;

public record TicketSearchCriteria(
        UserId requesterId,
        UserId assignedAgentId,
        TicketStatus status,
        TicketPriority priority,
        int page,
        int size
) {

    private static final int MAX_PAGE_SIZE = 100;

    public TicketSearchCriteria {
        if (requesterId != null && assignedAgentId != null) {
            throw new IllegalArgumentException(
                    "Requester and assigned-agent filters "
                            + "cannot be used together"
            );
        }

        if (page < 0) {
            throw new IllegalArgumentException(
                    "Page cannot be negative"
            );
        }

        if (size < 1 || size > MAX_PAGE_SIZE) {
            throw new IllegalArgumentException(
                    "Page size must be between 1 and "
                            + MAX_PAGE_SIZE
            );
        }
    }
}