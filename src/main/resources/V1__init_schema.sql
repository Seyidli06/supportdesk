-- Users Table
CREATE TABLE users (
                       id UUID PRIMARY KEY,
                       email VARCHAR(255) NOT NULL UNIQUE,
                       password_hash VARCHAR(255) NOT NULL,
                       full_name VARCHAR(100) NOT NULL,
                       created_at TIMESTAMP WITH TIME ZONE NOT NULL
);

-- User Roles Table
CREATE TABLE user_roles (
                            user_id UUID NOT NULL,
                            role VARCHAR(30) NOT NULL,
                            PRIMARY KEY (user_id, role),
                            CONSTRAINT fk_user_roles_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
);

-- Tickets Table
CREATE TABLE tickets (
                         id UUID PRIMARY KEY,
                         title VARCHAR(255) NOT NULL,
                         description TEXT NOT NULL,
                         status VARCHAR(30) NOT NULL,
                         priority VARCHAR(30) NOT NULL,
                         requester_id UUID NOT NULL,
                         assigned_agent_id UUID,
                         version BIGINT NOT NULL DEFAULT 0,
                         created_at TIMESTAMP WITH TIME ZONE NOT NULL,
                         updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
                         resolved_at TIMESTAMP WITH TIME ZONE,
                         closed_at TIMESTAMP WITH TIME ZONE,
                         sla_due_at TIMESTAMP WITH TIME ZONE,
                         CONSTRAINT fk_tickets_requester FOREIGN KEY (requester_id) REFERENCES users (id),
                         CONSTRAINT fk_tickets_agent FOREIGN KEY (assigned_agent_id) REFERENCES users (id)
);

-- Ticket Comments Table
CREATE TABLE ticket_comments (
                                 id UUID PRIMARY KEY,
                                 ticket_id UUID NOT NULL,
                                 author_id UUID NOT NULL,
                                 content TEXT NOT NULL,
                                 created_at TIMESTAMP WITH TIME ZONE NOT NULL,
                                 CONSTRAINT fk_comments_ticket FOREIGN KEY (ticket_id) REFERENCES tickets (id) ON DELETE CASCADE,
                                 CONSTRAINT fk_comments_author FOREIGN KEY (author_id) REFERENCES users (id)
);

-- Indexes for performance
CREATE INDEX idx_tickets_requester ON tickets(requester_id);
CREATE INDEX idx_tickets_agent ON tickets(assigned_agent_id);
CREATE INDEX idx_tickets_status ON tickets(status);
CREATE INDEX idx_comments_ticket ON ticket_comments(ticket_id);