CREATE TABLE project_report (
    id                   uuid NOT NULL,
    version              bigint DEFAULT 0 NOT NULL,
    technical_project_id uuid NOT NULL,
    author_user_id       uuid NOT NULL,
    title                varchar(160) NOT NULL,
    content              text NOT NULL,
    created_at           timestamptz DEFAULT now() NOT NULL,
    updated_at           timestamptz DEFAULT now() NOT NULL,
    CONSTRAINT pk_project_report PRIMARY KEY (id),
    CONSTRAINT ck_project_report_title CHECK ((length(btrim(title)) > 0)),
    CONSTRAINT ck_project_report_content CHECK ((length(btrim(content)) > 0 AND length(content) <= 4000))
);

ALTER TABLE project_report ADD CONSTRAINT fk_project_report_technical_project FOREIGN KEY (technical_project_id) REFERENCES technical_project(id);
ALTER TABLE project_report ADD CONSTRAINT fk_project_report_author_user FOREIGN KEY (author_user_id) REFERENCES user_account(id);

CREATE INDEX idx_project_report_project ON project_report (technical_project_id, created_at DESC);

-- As imagens do relatório são fotos do projeto, então aparecem junto das imagens do local; o histórico aponta para o relatório.
ALTER TABLE project_photo ADD COLUMN report_id uuid;
ALTER TABLE project_photo ADD CONSTRAINT fk_project_photo_report FOREIGN KEY (report_id) REFERENCES project_report(id);
CREATE INDEX idx_project_photo_report ON project_photo (report_id) WHERE report_id IS NOT NULL;

ALTER TABLE project_history ADD COLUMN report_id uuid;
ALTER TABLE project_history ADD CONSTRAINT fk_project_history_report FOREIGN KEY (report_id) REFERENCES project_report(id);
