package com.adil.supportdesk.domain.ticket.valueobject;

import java.util.Objects;
import java.util.UUID;

public final class TicketId {
    private final UUID value;

    public TicketId(UUID value) {
        if (value == null) {
            throw new IllegalArgumentException("TicketId cannot be null");
        }
        this.value = value;
    }

    public static TicketId generate() {
        return new TicketId(UUID.randomUUID());
    }

    public static TicketId of(String uuid) {
        return new TicketId(UUID.fromString(uuid));
    }

    public UUID getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TicketId ticketId = (TicketId) o;
        return Objects.equals(value, ticketId.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }
}