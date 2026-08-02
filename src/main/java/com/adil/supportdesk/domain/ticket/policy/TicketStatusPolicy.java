package com.adil.supportdesk.domain.ticket.policy;

import com.adil.supportdesk.domain.ticket.model.TicketStatus;

public class TicketStatusPolicy {

    public static boolean canTransition(TicketStatus current, TicketStatus target) {
        if (current == target) return true;

        return switch (current) {
            case OPEN -> target == TicketStatus.IN_PROGRESS || target == TicketStatus.CLOSED;
            case IN_PROGRESS -> target == TicketStatus.RESOLVED || target == TicketStatus.CLOSED;
            case RESOLVED -> target == TicketStatus.CLOSED || target == TicketStatus.IN_PROGRESS;
            case CLOSED -> false;
        };
    }
}