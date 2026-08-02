package com.adil.supportdesk.application.ticket.query;

import com.adil.supportdesk.domain.ticket.model.TicketPriority;
import com.adil.supportdesk.domain.ticket.model.TicketStatus;

public record ListTicketsQuery(
        TicketStatus status,
        TicketPriority priority,
        int page,
        int size
) {

    private static final int MAX_PAGE_SIZE = 100;

    public ListTicketsQuery {
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