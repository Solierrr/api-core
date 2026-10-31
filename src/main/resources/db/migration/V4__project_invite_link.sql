CREATE TABLE project_invite_link (
    id                   uuid NOT NULL,
    version              bigint DEFAULT 0 NOT NULL,
    technical_project_id uuid NOT NULL,
    token_hash           varchar(64) NOT NULL,
    role                 varchar(16) NOT NULL,
    function_name        varchar(120),
    max_uses             integer,
    use_count            integer DEFAULT 0 NOT NULL,
    status               varchar(16) DEFAULT 'ACTIVE' NOT NULL,
    expires_at           timestamptz NOT NULL,
    created_at           timestamptz DEFAULT now() NOT NULL,
    created_by_user_id   uuid NOT NULL,
    revoked_at           timestamptz,
    updated_at           timestamptz DEFAULT now() NOT NULL,
    CONSTRAINT pk_project_invite_link PRIMARY KEY (id),
    CONSTRAINT uq_project_invite_link_token_hash UNIQUE (token_hash),
    CONSTRAINT ck_project_invite_link_role CHECK ((role IN ('MEMBER', 'VIEWER'))),
    CONSTRAINT ck_project_invite_link_status CHECK ((status IN ('ACTIVE', 'REVOKED'))),
    CONSTRAINT ck_project_invite_link_uses CHECK ((use_count >= 0 AND (max_uses IS NULL OR max_uses > 0)))
);

ALTER TABLE project_invite_link ADD CONSTRAINT fk_project_invite_link_technical_project FOREIGN KEY (technical_project_id) REFERENCES technical_project(id);
ALTER TABLE project_invite_link ADD CONSTRAINT fk_project_invite_link_created_by_user FOREIGN KEY (created_by_user_id) REFERENCES user_account(id);

CREATE INDEX idx_project_invite_link_project ON project_invite_link (technical_project_id, status);
