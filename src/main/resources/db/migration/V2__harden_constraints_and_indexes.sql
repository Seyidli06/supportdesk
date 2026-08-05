
-- ============================================================
-- V2: Harden database constraints and query indexes
-- ============================================================

-- ------------------------------------------------------------
-- Preflight validation
-- ------------------------------------------------------------

DO $$
BEGIN
    IF EXISTS (
        SELECT 1
        FROM users
        WHERE email IS NULL
           OR BTRIM(email) = ''
    ) THEN
        RAISE EXCEPTION
            'Cannot apply V2: users contains an empty email';
    END IF;

    IF EXISTS (
        SELECT LOWER(BTRIM(email))
        FROM users
        GROUP BY LOWER(BTRIM(email))
        HAVING COUNT(*) > 1
    ) THEN
        RAISE EXCEPTION
            'Cannot apply V2: case-insensitive duplicate emails exist';
    END IF;

    IF EXISTS (
        SELECT 1
        FROM users
        WHERE full_name IS NULL
           OR CHAR_LENGTH(BTRIM(full_name)) < 2
           OR CHAR_LENGTH(BTRIM(full_name)) > 100
    ) THEN
        RAISE EXCEPTION
            'Cannot apply V2: invalid user full name exists';
    END IF;

    IF EXISTS (
        SELECT 1
        FROM user_roles
        WHERE role NOT IN ('USER', 'AGENT', 'ADMIN')
    ) THEN
        RAISE EXCEPTION
            'Cannot apply V2: invalid user role exists';
    END IF;

    IF EXISTS (
        SELECT 1
        FROM tickets
        WHERE title IS NULL
           OR BTRIM(title) = ''
           OR CHAR_LENGTH(BTRIM(title)) > 100
    ) THEN
        RAISE EXCEPTION
            'Cannot apply V2: invalid ticket title exists';
    END IF;

    IF EXISTS (
        SELECT 1
        FROM tickets
        WHERE description IS NULL
           OR BTRIM(description) = ''
    ) THEN
        RAISE EXCEPTION
            'Cannot apply V2: invalid ticket description exists';
    END IF;

    IF EXISTS (
        SELECT 1
        FROM tickets
        WHERE status NOT IN (
            'OPEN',
            'IN_PROGRESS',
            'WAITING_CUSTOMER',
            'RESOLVED',
            'CLOSED'
        )
    ) THEN
        RAISE EXCEPTION
            'Cannot apply V2: invalid ticket status exists';
    END IF;

    IF EXISTS (
        SELECT 1
        FROM tickets
        WHERE priority NOT IN (
            'LOW',
            'MEDIUM',
            'HIGH',
            'URGENT'
        )
    ) THEN
        RAISE EXCEPTION
            'Cannot apply V2: invalid ticket priority exists';
    END IF;

    IF EXISTS (
        SELECT 1
        FROM ticket_comments
        WHERE content IS NULL
           OR BTRIM(content) = ''
    ) THEN
        RAISE EXCEPTION
            'Cannot apply V2: empty ticket comment exists';
    END IF;
END
$$;

-- ------------------------------------------------------------
-- Normalize existing account data
-- ------------------------------------------------------------

UPDATE users
SET email = LOWER(BTRIM(email)),
    full_name = BTRIM(full_name);

-- ------------------------------------------------------------
-- Users
-- ------------------------------------------------------------

ALTER TABLE users
    DROP CONSTRAINT IF EXISTS users_email_key;

CREATE UNIQUE INDEX uq_users_email_normalized
    ON users (LOWER(email));

ALTER TABLE users
    ADD CONSTRAINT ck_users_email_normalized
        CHECK (
            email = LOWER(BTRIM(email))
            AND BTRIM(email) <> ''
        ),

    ADD CONSTRAINT ck_users_password_hash_not_blank
        CHECK (
            BTRIM(password_hash) <> ''
        ),

    ADD CONSTRAINT ck_users_full_name_length
        CHECK (
            CHAR_LENGTH(BTRIM(full_name))
                BETWEEN 2 AND 100
        );

-- ------------------------------------------------------------
-- User roles
-- ------------------------------------------------------------

ALTER TABLE user_roles
    ADD CONSTRAINT ck_user_roles_role
        CHECK (
            role IN (
                'USER',
                'AGENT',
                'ADMIN'
            )
        );

-- ------------------------------------------------------------
-- Tickets
-- ------------------------------------------------------------

ALTER TABLE tickets
    ADD CONSTRAINT ck_tickets_title
        CHECK (
            BTRIM(title) <> ''
            AND CHAR_LENGTH(BTRIM(title)) <= 100
        ),

    ADD CONSTRAINT ck_tickets_description
        CHECK (
            BTRIM(description) <> ''
        ),

    ADD CONSTRAINT ck_tickets_status
        CHECK (
            status IN (
                'OPEN',
                'IN_PROGRESS',
                'WAITING_CUSTOMER',
                'RESOLVED',
                'CLOSED'
            )
        ),

    ADD CONSTRAINT ck_tickets_priority
        CHECK (
            priority IN (
                'LOW',
                'MEDIUM',
                'HIGH',
                'URGENT'
            )
        ),

    ADD CONSTRAINT ck_tickets_updated_at
        CHECK (
            updated_at >= created_at
        ),

    ADD CONSTRAINT ck_tickets_resolved_at
        CHECK (
            resolved_at IS NULL
            OR resolved_at >= created_at
        ),

    ADD CONSTRAINT ck_tickets_closed_at
        CHECK (
            closed_at IS NULL
            OR closed_at >= created_at
        ),

    ADD CONSTRAINT ck_tickets_sla_due_at
        CHECK (
            sla_due_at IS NULL
            OR sla_due_at > created_at
        );

-- ------------------------------------------------------------
-- Ticket comments
-- ------------------------------------------------------------

ALTER TABLE ticket_comments
    ADD CONSTRAINT ck_ticket_comments_content
        CHECK (
            BTRIM(content) <> ''
        );

-- ------------------------------------------------------------
-- Replace basic indexes with query-oriented indexes
-- ------------------------------------------------------------

DROP INDEX IF EXISTS idx_tickets_requester;
DROP INDEX IF EXISTS idx_tickets_agent;
DROP INDEX IF EXISTS idx_tickets_status;
DROP INDEX IF EXISTS idx_comments_ticket;

CREATE INDEX idx_users_created_at_id
    ON users (
        created_at DESC,
        id ASC
    );

CREATE INDEX idx_user_roles_role_user_id
    ON user_roles (
        role,
        user_id
    );

CREATE INDEX idx_tickets_requester_created_at_id
    ON tickets (
        requester_id,
        created_at DESC,
        id DESC
    );

CREATE INDEX idx_tickets_agent_created_at_id
    ON tickets (
        assigned_agent_id,
        created_at DESC,
        id DESC
    )
    WHERE assigned_agent_id IS NOT NULL;

CREATE INDEX idx_tickets_status_created_at_id
    ON tickets (
        status,
        created_at DESC,
        id DESC
    );

CREATE INDEX idx_tickets_priority_created_at_id
    ON tickets (
        priority,
        created_at DESC,
        id DESC
    );

CREATE INDEX idx_ticket_comments_ticket_created_at_id
    ON ticket_comments (
        ticket_id,
        created_at ASC,
        id ASC
    );


