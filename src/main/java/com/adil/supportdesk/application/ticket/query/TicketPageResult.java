package com.adil.supportdesk.application.ticket.query;

import java.util.List;
import java.util.Objects;

public record TicketPageResult(
        List<TicketSummaryResult> content,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean first,
        boolean last
) {

    public TicketPageResult {
        Objects.requireNonNull(
                content,
                "Content cannot be null"
        );

        content = List.copyOf(content);
    }
}