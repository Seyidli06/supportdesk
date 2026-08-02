package com.adil.supportdesk.domain.ticket.valueobject;


// domain/ticket/valueobject/CommentId.java

import java.util.Objects;
import java.util.UUID;

public final class CommentId {
    private final UUID value;

    public CommentId(UUID value) {
        if (value == null) {
            throw new IllegalArgumentException("CommentId cannot be null");
        }
        this.value = value;
    }

    public static CommentId generate() {
        return new CommentId(UUID.randomUUID());
    }

    public static CommentId of(String uuid) {
        return new CommentId(UUID.fromString(uuid));
    }

    public UUID getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CommentId commentId = (CommentId) o;
        return Objects.equals(value, commentId.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }
}