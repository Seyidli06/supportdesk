package com.adil.supportdesk.adapter.out.persistence.user;

import com.adil.supportdesk.application.security.UserRole;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import org.hibernate.annotations.BatchSize;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "users")
public class UserJpaEntity {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(
            name = "email",
            nullable = false,
            unique = true,
            length = 255
    )
    private String email;

    @Column(
            name = "password_hash",
            nullable = false,
            length = 255
    )
    private String passwordHash;

    @Column(
            name = "full_name",
            nullable = false,
            length = 100
    )
    private String fullName;

    @Column(
            name = "created_at",
            nullable = false
    )
    private Instant createdAt;

    @Column(
            name = "token_version",
            nullable = false
    )
    private long tokenVersion;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(
            name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id")
    )
    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    @BatchSize(size = 100)
    private Set<UserRole> roles = new HashSet<>();

    protected UserJpaEntity() {
    }

    UserJpaEntity(
            UUID id,
            String email,
            String passwordHash,
            String fullName,
            Instant createdAt,
            Set<UserRole> roles
    ) {
        this(
                id,
                email,
                passwordHash,
                fullName,
                createdAt,
                roles,
                0L
        );
    }

    UserJpaEntity(
            UUID id,
            String email,
            String passwordHash,
            String fullName,
            Instant createdAt,
            Set<UserRole> roles,
            long tokenVersion
    ) {
        this.id = id;
        this.email = email;
        this.passwordHash = passwordHash;
        this.fullName = fullName;
        this.createdAt = createdAt;
        this.roles = new HashSet<>(roles);
        this.tokenVersion = tokenVersion;
    }

    public UUID getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public String getFullName() {
        return fullName;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Set<UserRole> getRoles() {
        return Set.copyOf(roles);
    }

    public long getTokenVersion() {
        return tokenVersion;
    }
}