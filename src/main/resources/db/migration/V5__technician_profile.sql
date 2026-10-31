CREATE TABLE technician_profile (
    technician_id     uuid NOT NULL,
    version           bigint DEFAULT 0 NOT NULL,
    bio               varchar(500),
    years_experience  smallint,
    service_radius_km smallint,
    zip_code          varchar(8),
    street            varchar(160),
    number            varchar(20),
    complement        varchar(120),
    neighborhood      varchar(120),
    city              varchar(120),
    state             varchar(2),
    completed_at      timestamptz,
    created_at        timestamptz DEFAULT now() NOT NULL,
    updated_at        timestamptz DEFAULT now() NOT NULL,
    CONSTRAINT pk_technician_profile PRIMARY KEY (technician_id),
    CONSTRAINT ck_technician_profile_experience CHECK ((years_experience IS NULL OR years_experience BETWEEN 0 AND 60)),
    CONSTRAINT ck_technician_profile_radius CHECK ((service_radius_km IS NULL OR service_radius_km BETWEEN 1 AND 500)),
    CONSTRAINT ck_technician_profile_zip CHECK ((zip_code IS NULL OR zip_code ~ '^[0-9]{8}$'))
);

CREATE TABLE technician_specialty (
    technician_id uuid NOT NULL,
    specialty     varchar(24) NOT NULL,
    CONSTRAINT pk_technician_specialty PRIMARY KEY (technician_id, specialty),
    CONSTRAINT ck_technician_specialty CHECK ((specialty IN ('INSTALLATION', 'MAINTENANCE', 'CLEANING', 'INSPECTION', 'PROJECT', 'OTHER')))
);

ALTER TABLE technician_profile ADD CONSTRAINT fk_technician_profile_technician FOREIGN KEY (technician_id) REFERENCES technician(id);
ALTER TABLE technician_specialty ADD CONSTRAINT fk_technician_specialty_technician FOREIGN KEY (technician_id) REFERENCES technician(id);
