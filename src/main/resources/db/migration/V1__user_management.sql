-- ============================================================
-- V1: User Management Tables
-- ============================================================

CREATE TABLE IF NOT EXISTS users (
    id                   UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    name                 VARCHAR(100) NOT NULL,
    email                VARCHAR(150) NOT NULL UNIQUE,
    password             VARCHAR(255),
    role                 VARCHAR(50)  NOT NULL,
    phone                VARCHAR(15),
    designation          VARCHAR(100),
    department           VARCHAR(100),
    profile_picture      VARCHAR(500),
    is_active            BOOLEAN      NOT NULL DEFAULT true,
    is_email_verified    BOOLEAN      NOT NULL DEFAULT false,
    last_login_at        TIMESTAMP,
    password_changed_at  TIMESTAMP,
    password_reset_token VARCHAR(255),
    password_reset_expiry TIMESTAMP,
    created_at           TIMESTAMP    NOT NULL DEFAULT NOW(),
    created_by           UUID,
    updated_at           TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_by           UUID
);

CREATE INDEX IF NOT EXISTS idx_users_email  ON users(email);
CREATE INDEX IF NOT EXISTS idx_users_role   ON users(role);
CREATE INDEX IF NOT EXISTS idx_users_active ON users(is_active);

-- ----------------------------------------------------------------

CREATE TABLE IF NOT EXISTS user_sessions (
    id           UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id      UUID        NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    token_hash   VARCHAR(255) NOT NULL,
    ip_address   VARCHAR(45),
    user_agent   TEXT,
    created_at   TIMESTAMP   NOT NULL DEFAULT NOW(),
    expires_at   TIMESTAMP,
    is_active    BOOLEAN     NOT NULL DEFAULT true
);

CREATE INDEX IF NOT EXISTS idx_sessions_user ON user_sessions(user_id);

-- ----------------------------------------------------------------

CREATE TABLE IF NOT EXISTS audit_logs (
    id           UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    actor_id     UUID        REFERENCES users(id),
    actor_name   VARCHAR(100),
    action       VARCHAR(100) NOT NULL,
    entity_type  VARCHAR(50),
    entity_id    VARCHAR(100),
    old_value    TEXT,
    new_value    TEXT,
    ip_address   VARCHAR(45),
    performed_at TIMESTAMP   NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_audit_actor  ON audit_logs(actor_id);
CREATE INDEX IF NOT EXISTS idx_audit_entity ON audit_logs(entity_type, entity_id);

-- ----------------------------------------------------------------
-- Default seed users — BCrypt of Admin@123
-- ----------------------------------------------------------------

INSERT INTO users (id, name, email, password, role, is_active, is_email_verified, created_at, updated_at)
VALUES
  ('a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11',
   'Super Admin',
   'admin@nexcrm.com',
   '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMZJaaaSwm.4W0aWK0IaKGSyuS',
   'SUPER_ADMIN', true, true, NOW(), NOW()),

  ('b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a12',
   'Sales Manager',
   'manager@nexcrm.com',
   '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMZJaaaSwm.4W0aWK0IaKGSyuS',
   'SALES_MANAGER', true, true, NOW(), NOW()),

  ('c0eebc99-9c0b-4ef8-bb6d-6bb9bd380a13',
   'Sales Executive',
   'sales@nexcrm.com',
   '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMZJaaaSwm.4W0aWK0IaKGSyuS',
   'SALES_EXECUTIVE', true, true, NOW(), NOW())
ON CONFLICT (email) DO NOTHING;
