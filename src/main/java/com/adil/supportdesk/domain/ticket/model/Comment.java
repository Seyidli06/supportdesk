package com.adil.supportdesk.domain.ticket.model;

import com.adil.supportdesk.domain.ticket.valueobject.CommentId;
import com.adil.supportdesk.domain.user.valueobject.UserId;

import java.time.Instant;
import java.util.Objects;

public class Comment {

    private final CommentId id;
    private final UserId authorId;
    private final String content;
    private final Instant createdAt;

    public Comment(
            CommentId id,
            UserId authorId,
            String content,
            Instant createdAt
    ) {
        if (content == null || content.isBlank()) {
            throw new IllegalArgumentException(
                    "Comment content cannot be empty"
            );
        }

        this.id = Objects.requireNonNull(
                id,
                "CommentId cannot be null"
        );
        this.authorId = Objects.requireNonNull(
                authorId,
                "Comment authorId cannot be null"
        );
        this.content = content.trim();
        this.createdAt = Objects.requireNonNull(
                createdAt,
                "Comment createdAt cannot be null"
        );
    }

    public CommentId getId() {
        return id;
    }

    public UserId getAuthorId() {
        return authorId;
    }

    public String getContent() {
        return content;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}