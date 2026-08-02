package com.adil.supportdesk.domain.ticket.policy;

import com.adil.supportdesk.domain.ticket.model.TicketStatus;

import java.util.Objects;

public final class TicketStatusPolicy {

    private TicketStatusPolicy() {
    }

    public static boolean canTransition(
            TicketStatus current,
            TicketStatus target
    ) {
        Objects.requireNonNull(current, "Current status cannot be null");
        Objects.requireNonNull(target, "Target status cannot be null");

        if (current == target) {
            return true;
        }

        return switch (current) {
            case OPEN ->
                    target == TicketStatus.IN_PROGRESS;

            case IN_PROGRESS ->
                    target == TicketStatus.WAITING_CUSTOMER
                            || target == TicketStatus.RESOLVED;

            case WAITING_CUSTOMER ->
                    target == TicketStatus.IN_PROGRESS
                            || target == TicketStatus.RESOLVED;

            case RESOLVED ->
                    target == TicketStatus.IN_PROGRESS
                            || target == TicketStatus.CLOSED;

            case CLOSED -> false;
        };
    }
}