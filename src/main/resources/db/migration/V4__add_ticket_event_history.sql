-- ============================================================
-- V4: Add immutable ticket event history
-- ============================================================

CREATE TABLE ticket_events (
                               id UUID PRIMARY KEY,

                               ticket_id UUID NOT NULL,

                               actor_id UUID NOT NULL,

                               event_type VARCHAR(40) NOT NULL,

                               previous_value VARCHAR(255),

                               new_value VARCHAR(255),

                               created_at TIMESTAMP WITH TIME ZONE NOT NULL,

                               CONSTRAINT fk_ticket_events_ticket
                                   FOREIGN KEY (ticket_id)
                                       REFERENCES tickets (id)
                                       ON DELETE CASCADE,

                               CONSTRAINT fk_ticket_events_actor
                                   FOREIGN KEY (actor_id)
                                       REFERENCES users (id),

                               CONSTRAINT ck_ticket_events_type
                                   CHECK (
                                       event_type IN (
                                                      'TICKET_CREATED',
                                                      'ASSIGNMENT_CHANGED',
                                                      'STATUS_CHANGED',
                                                      'PRIORITY_CHANGED',
                                                      'COMMENT_ADDED'
                                           )
                                       ),

                               CONSTRAINT ck_ticket_events_previous_value
                                   CHECK (
                                       previous_value IS NULL
                                           OR BTRIM(previous_value) <> ''
                                       ),

                               CONSTRAINT ck_ticket_events_new_value
                                   CHECK (
                                       new_value IS NULL
                                           OR BTRIM(new_value) <> ''
                                       )
);

CREATE INDEX idx_ticket_events_ticket_created_at_id
    ON ticket_events (
                      ticket_id,
                      created_at ASC,
                      id ASC
        );

CREATE INDEX idx_ticket_events_actor_created_at_id
    ON ticket_events (
                      actor_id,
                      created_at DESC,
                      id DESC
        );