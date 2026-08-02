package com.adil.supportdesk.domain.ticket.model;

import com.adil.supportdesk.domain.ticket.valueobject.CommentId;

import java.time.Instant;
import java.util.Objects;

public class Comment {
    private final CommentId id;
    private final String author;
    private final String content;
    private final Instant createdAt;

    public Comment(CommentId id, String author, String content, Instant createdAt) {
        if (author == null || author.isBlank()) {
            throw new IllegalArgumentException("Comment author cannot be empty");
        }
        if (content == null || content.isBlank()) {
            throw new IllegalArgumentException("Comment content cannot be empty");
        }
        this.id = Objects.requireNonNull(id, "CommentId cannot be null");
        this.author = author;
        this.content = content;
        this.createdAt = Objects.requireNonNull(createdAt, "CreatedAt cannot be null");
    }

    public CommentId getId() { return id; }
    public String getAuthor() { return author; }
    public String getContent() { return content; }
    public Instant getCreatedAt() { return createdAt; }
}