package com.adil.supportdesk.adapter.in.web.ticket.dto;

import com.adil.supportdesk.application.ticket.query.TicketPageResult;

import java.util.List;

public record TicketPageResponse(
        List<TicketSummaryResponse> content,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean first,
        boolean last
) {

    public static TicketPageResponse fromResult(
            TicketPageResult result
    ) {
        List<TicketSummaryResponse> content =
                result.content()
                        .stream()
                        .map(
                                TicketSummaryResponse::fromResult
                        )
                        .toList();

        return new TicketPageResponse(
                content,
                result.page(),
                result.size(),
                result.totalElements(),
                result.totalPages(),
                result.first(),
                result.last()
        );
    }
}