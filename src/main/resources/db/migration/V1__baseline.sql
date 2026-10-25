CREATE TABLE access_code (
    id              uuid DEFAULT gen_random_uuid() NOT NULL,
    company_id      uuid NOT NULL,
    job_position_id uuid NOT NULL,
    code            varchar(12) NOT NULL,
    status          varchar(16) DEFAULT 'ACTIVE' NOT NULL,
    expires_at      timestamptz NOT NULL,
    created_at      timestamptz DEFAULT now() NOT NULL,
    updated_at      timestamptz DEFAULT now() NOT NULL,
    CONSTRAINT pk_access_code PRIMARY KEY (id),
    CONSTRAINT uq_access_code_code UNIQUE (code),
    CONSTRAINT ck_access_code_status CHECK ((status IN ('ACTIVE', 'USED', 'REVOKED')))
);

CREATE TABLE address (
    id               uuid DEFAULT gen_random_uuid() NOT NULL,
    state            varchar(2) NOT NULL,
    city             varchar(255) NOT NULL,
    neighborhood     varchar(255),
    zip_code         varchar(8) NOT NULL,
    street           varchar(255) NOT NULL,
    number           varchar(10),
    owner_company_id uuid,
    created_at       timestamptz DEFAULT now() NOT NULL,
    updated_at       timestamptz DEFAULT now() NOT NULL,
    CONSTRAINT pk_address PRIMARY KEY (id)
);

CREATE TABLE brand (
    id               uuid DEFAULT gen_random_uuid() NOT NULL,
    owner_company_id uuid,
    name             varchar(255) NOT NULL,
    normalized_name  varchar(255) NOT NULL,
    visibility       varchar(16) DEFAULT 'PRIVATE' NOT NULL,
    is_archived      boolean DEFAULT false NOT NULL,
    is_legacy        boolean DEFAULT false NOT NULL,
    logo_url         varchar(2048),
    reference_url    varchar(2048),
    version          bigint DEFAULT 0 NOT NULL,
    created_at       timestamptz DEFAULT now() NOT NULL,
    updated_at       timestamptz DEFAULT now() NOT NULL,
    CONSTRAINT pk_brand PRIMARY KEY (id),
    CONSTRAINT ck_brand_owner CHECK (((owner_company_id IS NOT NULL) OR is_legacy OR (visibility = 'PUBLIC'))),
    CONSTRAINT ck_brand_public_active CHECK (((visibility <> 'PUBLIC') OR (NOT is_archived))),
    CONSTRAINT ck_brand_visibility CHECK ((visibility IN ('PRIVATE', 'PUBLIC')))
);

CREATE TABLE brand_request (
    id                     uuid DEFAULT gen_random_uuid() NOT NULL,
    brand_id               uuid NOT NULL,
    requester_company_id   uuid,
    requester_user_id      uuid,
    kind                   varchar(16) NOT NULL,
    status                 varchar(16) DEFAULT 'PENDING' NOT NULL,
    proposed_name          varchar(255) NOT NULL,
    proposed_logo_url      varchar(2048),
    proposed_reference_url varchar(2048),
    result_brand_id        uuid,
    decision_reason        varchar(2000),
    decided_by_user_id     uuid,
    decided_at             timestamptz,
    created_at             timestamptz DEFAULT now() NOT NULL,
    version                bigint DEFAULT 0 NOT NULL,
    updated_at             timestamptz DEFAULT now() NOT NULL,
    CONSTRAINT pk_brand_request PRIMARY KEY (id),
    CONSTRAINT ck_brand_request_kind CHECK ((kind IN ('PUBLICATION', 'CORRECTION'))),
    CONSTRAINT ck_brand_request_rejection_reason CHECK (((status <> 'REJECTED') OR ((length(TRIM(BOTH FROM decision_reason)) > 0) AND (decision_reason IS NOT NULL)))),
    CONSTRAINT ck_brand_request_status CHECK ((status IN ('PENDING', 'APPROVED', 'REJECTED', 'CANCELLED')))
);

CREATE TABLE business_contact (
    id            uuid DEFAULT gen_random_uuid() NOT NULL,
    company_email varchar(100) NOT NULL,
    phone         varchar(12),
    website       varchar(255),
    created_at    timestamptz DEFAULT now() NOT NULL,
    updated_at    timestamptz DEFAULT now() NOT NULL,
    CONSTRAINT pk_business_contact PRIMARY KEY (id)
);

CREATE TABLE cart (
    id         uuid DEFAULT gen_random_uuid() NOT NULL,
    user_id    uuid NOT NULL,
    created_at timestamptz DEFAULT now() NOT NULL,
    updated_at timestamptz DEFAULT now() NOT NULL,
    CONSTRAINT pk_cart PRIMARY KEY (id),
    CONSTRAINT uq_cart_user UNIQUE (user_id)
);

CREATE TABLE cart_item (
    id         uuid DEFAULT gen_random_uuid() NOT NULL,
    cart_id    uuid NOT NULL,
    offer_id   uuid NOT NULL,
    quantity   integer NOT NULL,
    added_at   timestamptz DEFAULT now() NOT NULL,
    created_at timestamptz DEFAULT now() NOT NULL,
    updated_at timestamptz DEFAULT now() NOT NULL,
    CONSTRAINT pk_cart_item PRIMARY KEY (id),
    CONSTRAINT uq_cart_item_cart_offer UNIQUE (cart_id, offer_id),
    CONSTRAINT ck_cart_item_quantity CHECK ((quantity > 0))
);

CREATE TABLE certification (
    id             uuid DEFAULT gen_random_uuid() NOT NULL,
    technician_id  uuid,
    type           varchar(255),
    information    text,
    image          text,
    description    text,
    issuer         varchar(100),
    name           varchar(100),
    valid_until_at timestamptz,
    created_at     timestamptz DEFAULT now() NOT NULL,
    updated_at     timestamptz DEFAULT now() NOT NULL,
    CONSTRAINT pk_certification PRIMARY KEY (id)
);

CREATE TABLE certification_record (
    id                           uuid DEFAULT gen_random_uuid() NOT NULL,
    professional_registration_id uuid,
    certification_id             uuid,
    created_at                   timestamptz DEFAULT now() NOT NULL,
    updated_at                   timestamptz DEFAULT now() NOT NULL,
    CONSTRAINT pk_certification_record PRIMARY KEY (id)
);

CREATE TABLE charge (
    id              uuid DEFAULT gen_random_uuid() NOT NULL,
    subscription_id uuid NOT NULL,
    amount          numeric NOT NULL,
    payment_method  varchar(16) NOT NULL,
    status          varchar(16) DEFAULT 'PENDING' NOT NULL,
    due_date        date NOT NULL,
    paid_at         timestamptz,
    created_at      timestamptz DEFAULT now() NOT NULL,
    updated_at      timestamptz DEFAULT now() NOT NULL,
    CONSTRAINT pk_charge PRIMARY KEY (id),
    CONSTRAINT ck_charge_payment_method CHECK ((payment_method IN ('PIX', 'BOLETO', 'CREDIT_CARD', 'TRANSFER'))),
    CONSTRAINT ck_charge_status CHECK ((status IN ('PENDING', 'PAID', 'CANCELED', 'REFUNDED')))
);

CREATE TABLE company (
    id                  uuid DEFAULT gen_random_uuid() NOT NULL,
    status              varchar(16) DEFAULT 'UNDER_ANALYSIS' NOT NULL,
    type                varchar(16) NOT NULL,
    address_id          uuid,
    business_contact_id uuid,
    cnpj                varchar(14) NOT NULL,
    trade_name          varchar(120) NOT NULL,
    corporate_name      varchar(120) NOT NULL,
    slug                varchar(160) NOT NULL,
    created_at          timestamptz DEFAULT now() NOT NULL,
    updated_at          timestamptz DEFAULT now() NOT NULL,
    CONSTRAINT pk_company PRIMARY KEY (id),
    CONSTRAINT uq_company_slug UNIQUE (slug),
    CONSTRAINT ck_company_status CHECK ((status IN ('UNDER_ANALYSIS', 'APPROVED', 'REJECTED'))),
    CONSTRAINT ck_company_type CHECK ((type IN ('SUPPLIER', 'REQUESTER')))
);

CREATE TABLE company_photo (
    id          uuid DEFAULT gen_random_uuid() NOT NULL,
    company_id  uuid NOT NULL,
    type        varchar(255) NOT NULL,
    url         varchar(255) NOT NULL,
    public_id   varchar(255) NOT NULL,
    crop_x      integer,
    crop_y      integer,
    crop_width  integer,
    crop_height integer,
    created_at  timestamptz DEFAULT now() NOT NULL,
    updated_at  timestamptz DEFAULT now() NOT NULL,
    CONSTRAINT pk_company_photo PRIMARY KEY (id),
    CONSTRAINT ck_company_photo_crop CHECK ((((crop_x IS NULL) AND (crop_y IS NULL) AND (crop_width IS NULL) AND (crop_height IS NULL)) OR ((crop_x IS NOT NULL) AND (crop_y IS NOT NULL) AND (crop_width IS NOT NULL) AND (crop_height IS NOT NULL) AND (crop_x >= 0) AND (crop_y >= 0) AND (crop_width > 0) AND (crop_height > 0)))),
    CONSTRAINT ck_company_photo_type CHECK ((type IN ('PROFILE', 'BANNER')))
);

CREATE TABLE company_plan (
    id         uuid DEFAULT gen_random_uuid() NOT NULL,
    name       varchar(255) NOT NULL,
    price      numeric NOT NULL,
    cycle      varchar(16) NOT NULL,
    created_at timestamptz DEFAULT now() NOT NULL,
    updated_at timestamptz DEFAULT now() NOT NULL,
    CONSTRAINT pk_company_plan PRIMARY KEY (id),
    CONSTRAINT ck_company_plan_cycle CHECK ((cycle IN ('MONTHLY', 'QUARTERLY', 'YEARLY')))
);

CREATE TABLE company_position (
    id              uuid DEFAULT gen_random_uuid() NOT NULL,
    company_id      uuid NOT NULL,
    job_position_id uuid NOT NULL,
    created_at      timestamptz DEFAULT now() NOT NULL,
    updated_at      timestamptz DEFAULT now() NOT NULL,
    CONSTRAINT pk_company_position PRIMARY KEY (id)
);

CREATE TABLE contact (
    id         uuid DEFAULT gen_random_uuid() NOT NULL,
    email      varchar(100),
    phone      varchar(12),
    created_at timestamptz DEFAULT now() NOT NULL,
    updated_at timestamptz DEFAULT now() NOT NULL,
    CONSTRAINT pk_contact PRIMARY KEY (id)
);

CREATE TABLE energy_bill (
    id              uuid DEFAULT gen_random_uuid() NOT NULL,
    local_unit_id   uuid NOT NULL,
    consumption     numeric NOT NULL,
    amount          numeric NOT NULL,
    photo_url       varchar(255),
    photo_public_id varchar(255),
    created_at      timestamptz DEFAULT now() NOT NULL,
    updated_at      timestamptz DEFAULT now() NOT NULL,
    CONSTRAINT pk_energy_bill PRIMARY KEY (id)
);

CREATE TABLE flux_log (
    id         uuid DEFAULT gen_random_uuid() NOT NULL,
    user_id    uuid NOT NULL,
    action     varchar(255) NOT NULL,
    created_at timestamptz DEFAULT now() NOT NULL,
    updated_at timestamptz DEFAULT now() NOT NULL,
    CONSTRAINT pk_flux_log PRIMARY KEY (id)
);

CREATE TABLE geolocation (
    id         uuid DEFAULT gen_random_uuid() NOT NULL,
    address_id uuid,
    latitude   numeric(10,7) NOT NULL,
    longitude  numeric(10,7) NOT NULL,
    created_at timestamptz DEFAULT now() NOT NULL,
    updated_at timestamptz DEFAULT now() NOT NULL,
    CONSTRAINT pk_geolocation PRIMARY KEY (id)
);

CREATE TABLE inventory (
    id          uuid DEFAULT gen_random_uuid() NOT NULL,
    supplier_id uuid NOT NULL,
    model_id    uuid NOT NULL,
    quantity    integer NOT NULL,
    created_at  timestamptz DEFAULT now() NOT NULL,
    updated_at  timestamptz DEFAULT now() NOT NULL,
    CONSTRAINT pk_inventory PRIMARY KEY (id),
    CONSTRAINT uq_inventory_supplier_model UNIQUE (supplier_id, model_id),
    CONSTRAINT ck_inventory_quantity CHECK ((quantity >= 0))
);

CREATE TABLE job_position (
    id         uuid DEFAULT gen_random_uuid() NOT NULL,
    name       varchar(12) NOT NULL,
    accesses   varchar(255),
    created_at timestamptz DEFAULT now() NOT NULL,
    updated_at timestamptz DEFAULT now() NOT NULL,
    CONSTRAINT pk_job_position PRIMARY KEY (id),
    CONSTRAINT uq_job_position_name UNIQUE (name)
);

CREATE TABLE local_unit (
    id            uuid DEFAULT gen_random_uuid() NOT NULL,
    requester_id  uuid NOT NULL,
    address_id    uuid NOT NULL,
    complement    varchar(255),
    location_type varchar(16) NOT NULL,
    created_at    timestamptz DEFAULT now() NOT NULL,
    updated_at    timestamptz DEFAULT now() NOT NULL,
    CONSTRAINT pk_local_unit PRIMARY KEY (id),
    CONSTRAINT ck_local_unit_location_type CHECK ((location_type IN ('BUILDING', 'HOUSE', 'COMPLEX')))
);

CREATE TABLE local_unit_photo (
    id            uuid DEFAULT gen_random_uuid() NOT NULL,
    local_unit_id uuid NOT NULL,
    type          varchar(16) DEFAULT 'GALLERY' NOT NULL,
    url           varchar(255) NOT NULL,
    public_id     varchar(255) NOT NULL,
    created_at    timestamptz DEFAULT now() NOT NULL,
    is_primary    boolean DEFAULT false NOT NULL,
    updated_at    timestamptz DEFAULT now() NOT NULL,
    CONSTRAINT pk_local_unit_photo PRIMARY KEY (id),
    CONSTRAINT ck_local_unit_photo_type CHECK ((type IN ('BANNER', 'GALLERY')))
);

CREATE TABLE model (
    id                  uuid DEFAULT gen_random_uuid() NOT NULL,
    brand_id            uuid NOT NULL,
    model_name          varchar(255) NOT NULL,
    power_wp            numeric,
    efficiency          numeric,
    dimension           numeric,
    type                varchar(32),
    width               numeric,
    length              numeric,
    weight              numeric,
    status              varchar(16) DEFAULT 'UNDER_ANALYSIS' NOT NULL,
    product_type        varchar(16) DEFAULT 'PANEL' NOT NULL,
    infrastructure_type varchar(16),
    material            varchar(64),
    creator_company_id  uuid,
    created_at          timestamptz DEFAULT now() NOT NULL,
    updated_at          timestamptz DEFAULT now() NOT NULL,
    CONSTRAINT pk_model PRIMARY KEY (id),
    CONSTRAINT ck_model_infrastructure_fields CHECK (((product_type <> 'INFRASTRUCTURE') OR ((infrastructure_type IS NOT NULL) AND (power_wp IS NULL) AND (efficiency IS NULL) AND (type IS NULL)))),
    CONSTRAINT ck_model_infrastructure_type CHECK (((infrastructure_type IS NULL) OR (infrastructure_type IN ('RAIL', 'CLAMP', 'BRACKET', 'HOOK', 'KIT', 'OTHER')))),
    CONSTRAINT ck_model_panel_fields CHECK (((product_type <> 'PANEL') OR ((power_wp IS NOT NULL) AND (efficiency IS NOT NULL) AND (type IS NOT NULL) AND (width IS NOT NULL) AND (length IS NOT NULL) AND (weight IS NOT NULL) AND (infrastructure_type IS NULL)))),
    CONSTRAINT ck_model_product_type CHECK ((product_type IN ('PANEL', 'INFRASTRUCTURE'))),
    CONSTRAINT ck_model_status CHECK ((status IN ('APPROVED', 'REJECTED', 'UNDER_ANALYSIS'))),
    CONSTRAINT ck_model_type CHECK (((type IS NULL) OR (type IN ('MONOCRYSTALLINE', 'POLYCRYSTALLINE', 'THIN_FILM'))))
);

CREATE TABLE model_photo (
    id         uuid DEFAULT gen_random_uuid() NOT NULL,
    model_id   uuid NOT NULL,
    url        varchar(255) NOT NULL,
    public_id  varchar(255) NOT NULL,
    is_primary boolean DEFAULT false NOT NULL,
    created_at timestamptz DEFAULT now() NOT NULL,
    updated_at timestamptz DEFAULT now() NOT NULL,
    CONSTRAINT pk_model_photo PRIMARY KEY (id)
);

CREATE TABLE offer (
    id                  uuid DEFAULT gen_random_uuid() NOT NULL,
    supplier_id         uuid NOT NULL,
    model_id            uuid NOT NULL,
    unit_price          numeric NOT NULL,
    availability        integer NOT NULL,
    expires_at          timestamptz,
    slug                varchar(160) NOT NULL,
    discount_percentage numeric,
    source_locale       varchar(10),
    translation_status  varchar(32) DEFAULT 'PENDING' NOT NULL,
    commercial_status   varchar(16) DEFAULT 'ACTIVE' NOT NULL,
    created_at          timestamptz DEFAULT now() NOT NULL,
    updated_at          timestamptz DEFAULT now() NOT NULL,
    CONSTRAINT pk_offer PRIMARY KEY (id),
    CONSTRAINT uq_offer_slug UNIQUE (slug),
    CONSTRAINT ck_offer_availability CHECK ((availability >= 0)),
    CONSTRAINT ck_offer_commercial_status CHECK ((commercial_status IN ('ACTIVE', 'PAUSED', 'CLOSED'))),
    CONSTRAINT ck_offer_discount CHECK (((discount_percentage >= (0)::numeric) AND (discount_percentage <= (100)::numeric))),
    CONSTRAINT ck_offer_translation_status CHECK ((translation_status IN ('PENDING', 'COMPLETED', 'FAILED'))),
    CONSTRAINT ck_offer_unit_price CHECK ((unit_price > (0)::numeric))
);

CREATE TABLE offer_service_region (
    offer_id   uuid NOT NULL,
    region     varchar(120),
    id         uuid DEFAULT gen_random_uuid() NOT NULL,
    created_at timestamptz DEFAULT now() NOT NULL,
    updated_at timestamptz DEFAULT now() NOT NULL,
    CONSTRAINT pk_offer_service_region PRIMARY KEY (id)
);

CREATE TABLE offer_status_history (
    id              uuid NOT NULL,
    offer_id        uuid NOT NULL,
    previous_status varchar(16) NOT NULL,
    new_status      varchar(16) NOT NULL,
    actor_user_id   uuid NOT NULL,
    created_at      timestamptz DEFAULT now() NOT NULL,
    updated_at      timestamptz DEFAULT now() NOT NULL,
    CONSTRAINT pk_offer_status_history PRIMARY KEY (id),
    CONSTRAINT ck_offer_status_history_new_status CHECK ((new_status IN ('ACTIVE', 'PAUSED', 'CLOSED'))),
    CONSTRAINT ck_offer_status_history_previous_status CHECK ((previous_status IN ('ACTIVE', 'PAUSED', 'CLOSED'))),
    CONSTRAINT ck_offer_status_history_transition CHECK ((previous_status <> new_status))
);

CREATE TABLE offer_translation (
    id          uuid DEFAULT gen_random_uuid() NOT NULL,
    offer_id    uuid NOT NULL,
    locale      varchar(10) NOT NULL,
    title       varchar(160) NOT NULL,
    description text NOT NULL,
    details     text,
    created_at  timestamptz DEFAULT now() NOT NULL,
    updated_at  timestamptz DEFAULT now() NOT NULL,
    CONSTRAINT pk_offer_translation PRIMARY KEY (id),
    CONSTRAINT uq_offer_translation_offer_locale UNIQUE (offer_id, locale)
);

CREATE TABLE offer_translation_job (
    id                          uuid NOT NULL,
    offer_id                    uuid NOT NULL,
    source_offer_translation_id uuid NOT NULL,
    status                      varchar(20) DEFAULT 'QUEUED' NOT NULL,
    attempts                    integer DEFAULT 0 NOT NULL,
    available_at                timestamptz DEFAULT now() NOT NULL,
    created_at                  timestamptz DEFAULT now() NOT NULL,
    claim_token                 uuid,
    error_code                  varchar(40),
    updated_at                  timestamptz DEFAULT now() NOT NULL,
    CONSTRAINT pk_offer_translation_job PRIMARY KEY (id),
    CONSTRAINT uq_offer_translation_job_offer_source_offer_translation UNIQUE (offer_id, source_offer_translation_id),
    CONSTRAINT ck_offer_translation_job_attempts CHECK ((attempts >= 0)),
    CONSTRAINT ck_offer_translation_job_status CHECK ((status IN ('QUEUED', 'PROCESSING', 'RETRY_PENDING', 'COMPLETED', 'FAILED', 'CANCELLED')))
);

CREATE TABLE order_item (
    id                  uuid DEFAULT gen_random_uuid() NOT NULL,
    purchase_order_id   uuid NOT NULL,
    offer_id            uuid,
    supplier_id         uuid NOT NULL,
    offer_slug          varchar(160) NOT NULL,
    model_name          varchar(255) NOT NULL,
    unit_price          numeric(12,2) NOT NULL,
    discount_percentage numeric(5,2) DEFAULT 0 NOT NULL,
    net_unit_price      numeric(12,2) NOT NULL,
    quantity            integer NOT NULL,
    line_total_amount   numeric(12,2) NOT NULL,
    created_at          timestamptz DEFAULT now() NOT NULL,
    updated_at          timestamptz DEFAULT now() NOT NULL,
    CONSTRAINT pk_order_item PRIMARY KEY (id),
    CONSTRAINT ck_order_item_discount_percentage CHECK (((discount_percentage >= (0)::numeric) AND (discount_percentage <= (100)::numeric))),
    CONSTRAINT ck_order_item_line_total CHECK ((line_total_amount = (net_unit_price * (quantity)::numeric))),
    CONSTRAINT ck_order_item_net_price CHECK ((net_unit_price = round(((unit_price * ((100)::numeric - discount_percentage)) / (100)::numeric), 2))),
    CONSTRAINT ck_order_item_quantity CHECK ((quantity > 0)),
    CONSTRAINT ck_order_item_unit_price CHECK ((unit_price >= (0)::numeric))
);

CREATE TABLE order_payment (
    id                uuid DEFAULT gen_random_uuid() NOT NULL,
    purchase_order_id uuid NOT NULL,
    method            varchar(16) NOT NULL,
    amount            numeric(12,2) NOT NULL,
    status            varchar(16) DEFAULT 'PAID' NOT NULL,
    paid_at           timestamptz DEFAULT now() NOT NULL,
    refunded_at       timestamptz,
    created_at        timestamptz DEFAULT now() NOT NULL,
    updated_at        timestamptz DEFAULT now() NOT NULL,
    CONSTRAINT pk_order_payment PRIMARY KEY (id),
    CONSTRAINT uq_order_payment_purchase_order UNIQUE (purchase_order_id),
    CONSTRAINT ck_order_payment_amount CHECK ((amount >= (0)::numeric)),
    CONSTRAINT ck_order_payment_method CHECK ((method IN ('PIX', 'BOLETO', 'CREDIT_CARD', 'TRANSFER'))),
    CONSTRAINT ck_order_payment_refunded CHECK (((status = 'REFUNDED') = (refunded_at IS NOT NULL))),
    CONSTRAINT ck_order_payment_status CHECK ((status IN ('PAID', 'REFUNDED')))
);

CREATE TABLE permission (
    id              uuid DEFAULT gen_random_uuid() NOT NULL,
    permission_name varchar(100) NOT NULL,
    name            varchar(150) NOT NULL,
    description     varchar(300) NOT NULL,
    created_at      timestamptz DEFAULT now() NOT NULL,
    updated_at      timestamptz DEFAULT now() NOT NULL,
    CONSTRAINT pk_permission PRIMARY KEY (id),
    CONSTRAINT uq_permission_permission_name UNIQUE (permission_name)
);

CREATE TABLE person (
    id         uuid DEFAULT gen_random_uuid() NOT NULL,
    user_id    uuid,
    contact_id uuid,
    name       varchar(60) NOT NULL,
    cpf        varchar(11) NOT NULL,
    birth_date date NOT NULL,
    created_at timestamptz DEFAULT now() NOT NULL,
    updated_at timestamptz DEFAULT now() NOT NULL,
    CONSTRAINT pk_person PRIMARY KEY (id)
);

CREATE TABLE platform_admin (
    id         uuid DEFAULT gen_random_uuid() NOT NULL,
    user_id    uuid NOT NULL,
    created_at timestamptz DEFAULT now() NOT NULL,
    updated_at timestamptz DEFAULT now() NOT NULL,
    CONSTRAINT pk_platform_admin PRIMARY KEY (id),
    CONSTRAINT uq_platform_admin_user UNIQUE (user_id)
);

CREATE TABLE position_permission (
    id              uuid DEFAULT gen_random_uuid() NOT NULL,
    job_position_id uuid NOT NULL,
    permission_id   uuid NOT NULL,
    created_at      timestamptz DEFAULT now() NOT NULL,
    updated_at      timestamptz DEFAULT now() NOT NULL,
    CONSTRAINT pk_position_permission PRIMARY KEY (id)
);

CREATE TABLE profession (
    id                         uuid DEFAULT gen_random_uuid() NOT NULL,
    name                       varchar(100),
    is_emergency_call_accepted boolean DEFAULT false NOT NULL,
    is_registration_required   boolean,
    is_nr10_required           boolean,
    is_nr35_required           boolean,
    created_at                 timestamptz DEFAULT now() NOT NULL,
    updated_at                 timestamptz DEFAULT now() NOT NULL,
    CONSTRAINT pk_profession PRIMARY KEY (id)
);

CREATE TABLE professional_document (
    id                   uuid NOT NULL,
    user_id              uuid NOT NULL,
    kind                 varchar(20) NOT NULL,
    content_type         varchar(100) NOT NULL,
    sha256               varchar(64) NOT NULL,
    content              bytea,
    created_at           timestamptz DEFAULT now() NOT NULL,
    cloudinary_public_id varchar(255),
    cloudinary_format    varchar(20),
    original_file_name   varchar(255),
    updated_at           timestamptz DEFAULT now() NOT NULL,
    CONSTRAINT pk_professional_document PRIMARY KEY (id),
    CONSTRAINT ck_professional_document_storage CHECK ((((content IS NOT NULL) AND (octet_length(content) > 0) AND (cloudinary_public_id IS NULL) AND (cloudinary_format IS NULL)) OR ((content IS NULL) AND (cloudinary_public_id IS NOT NULL) AND (length(TRIM(BOTH FROM cloudinary_public_id)) > 0) AND (cloudinary_format IS NOT NULL) AND (length(TRIM(BOTH FROM cloudinary_format)) > 0))))
);

CREATE TABLE professional_onboarding (
    id               uuid NOT NULL,
    auth_id          uuid NOT NULL,
    user_id          uuid NOT NULL,
    technician_id    uuid,
    revision         bigint DEFAULT 0 NOT NULL,
    approved_version bigint DEFAULT 0 NOT NULL,
    draft_json       text DEFAULT '{}' NOT NULL,
    approved_json    text,
    is_suspended     boolean DEFAULT false NOT NULL,
    created_at       timestamptz DEFAULT now() NOT NULL,
    updated_at       timestamptz DEFAULT now() NOT NULL,
    CONSTRAINT pk_professional_onboarding PRIMARY KEY (id),
    CONSTRAINT uq_professional_onboarding_auth_id UNIQUE (auth_id)
);

CREATE TABLE professional_registration (
    id            uuid DEFAULT gen_random_uuid() NOT NULL,
    technician_id uuid,
    profession_id uuid,
    council       varchar(60),
    number        varchar(30),
    expires_at    timestamptz,
    created_at    timestamptz DEFAULT now() NOT NULL,
    updated_at    timestamptz DEFAULT now() NOT NULL,
    CONSTRAINT pk_professional_registration PRIMARY KEY (id)
);

CREATE TABLE professional_review (
    id                   uuid DEFAULT gen_random_uuid() NOT NULL,
    technician_id        uuid NOT NULL,
    reviewer_user_id     uuid NOT NULL,
    technical_service_id uuid NOT NULL,
    rating               numeric(2,1) NOT NULL,
    comment              varchar(255),
    is_active            boolean DEFAULT true NOT NULL,
    created_at           timestamptz DEFAULT now() NOT NULL,
    updated_at           timestamptz DEFAULT now() NOT NULL,
    CONSTRAINT pk_professional_review PRIMARY KEY (id),
    CONSTRAINT uq_professional_review_reviewer_technician_service UNIQUE (reviewer_user_id, technician_id, technical_service_id),
    CONSTRAINT ck_professional_review_rating CHECK (((rating >= (0)::numeric) AND (rating <= (5)::numeric)))
);

CREATE TABLE professional_submission (
    id                         uuid NOT NULL,
    professional_onboarding_id uuid NOT NULL,
    revision                   bigint NOT NULL,
    base_approved_version      bigint NOT NULL,
    snapshot_json              text NOT NULL,
    policy_json                text NOT NULL,
    status                     varchar(30) NOT NULL,
    result_json                text,
    attempts                   integer DEFAULT 0 NOT NULL,
    available_at               timestamptz NOT NULL,
    created_at                 timestamptz DEFAULT now() NOT NULL,
    decided_at                 timestamptz,
    decided_by_user_id         uuid,
    decision_origin            varchar(10),
    decision_reason            text,
    updated_at                 timestamptz DEFAULT now() NOT NULL,
    CONSTRAINT pk_professional_submission PRIMARY KEY (id),
    CONSTRAINT uq_professional_submission_professional_onboarding_revision UNIQUE (professional_onboarding_id, revision),
    CONSTRAINT ck_professional_submission_attempts CHECK ((attempts >= 0)),
    CONSTRAINT ck_professional_submission_status CHECK ((status IN ('QUEUED', 'VALIDATING', 'RETRY_PENDING', 'MANUAL_REVIEW', 'APPROVED', 'REJECTED', 'SUSPENDED', 'REINSTATED')))
);

CREATE TABLE professional_team (
    id                        uuid NOT NULL,
    company_id                uuid NOT NULL,
    name                      varchar(100) NOT NULL,
    responsible_technician_id uuid NOT NULL,
    is_active                 boolean DEFAULT true NOT NULL,
    created_at                timestamptz DEFAULT now() NOT NULL,
    updated_at                timestamptz DEFAULT now() NOT NULL,
    CONSTRAINT pk_professional_team PRIMARY KEY (id),
    CONSTRAINT ck_professional_team_name CHECK ((length(TRIM(BOTH FROM name)) > 0))
);

CREATE TABLE professional_team_member (
    id                   uuid NOT NULL,
    professional_team_id uuid NOT NULL,
    technician_id        uuid NOT NULL,
    created_at           timestamptz DEFAULT now() NOT NULL,
    updated_at           timestamptz DEFAULT now() NOT NULL,
    CONSTRAINT pk_professional_team_member PRIMARY KEY (id),
    CONSTRAINT uq_professional_team_member_professional_team_technician UNIQUE (professional_team_id, technician_id)
);

CREATE TABLE project_activity (
    id                   uuid NOT NULL,
    version              bigint DEFAULT 0 NOT NULL,
    technical_project_id uuid NOT NULL,
    title                varchar(160) NOT NULL,
    description          text,
    type                 varchar(16) NOT NULL,
    status               varchar(16) DEFAULT 'OPEN' NOT NULL,
    starts_at            timestamptz NOT NULL,
    ends_at              timestamptz,
    is_onsite            boolean DEFAULT false NOT NULL,
    assignee_user_ids    uuid[] DEFAULT ARRAY[]::uuid[] NOT NULL,
    technical_service_id uuid,
    created_at           timestamptz DEFAULT now() NOT NULL,
    updated_at           timestamptz DEFAULT now() NOT NULL,
    CONSTRAINT pk_project_activity PRIMARY KEY (id),
    CONSTRAINT ck_project_activity_period CHECK (((ends_at IS NULL) OR (ends_at > starts_at))),
    CONSTRAINT ck_project_activity_status CHECK ((status IN ('OPEN', 'IN_PROGRESS', 'COMPLETED', 'CANCELED'))),
    CONSTRAINT ck_project_activity_type CHECK ((type IN ('SERVICE', 'VISIT', 'MEETING', 'OTHER')))
);

CREATE TABLE project_history (
    id                   uuid NOT NULL,
    version              bigint DEFAULT 0 NOT NULL,
    technical_project_id uuid NOT NULL,
    actor_user_id        uuid NOT NULL,
    action               varchar(40) NOT NULL,
    detail               text NOT NULL,
    created_at           timestamptz DEFAULT now() NOT NULL,
    updated_at           timestamptz DEFAULT now() NOT NULL,
    CONSTRAINT pk_project_history PRIMARY KEY (id)
);

CREATE TABLE project_invitation (
    id                    uuid NOT NULL,
    version               bigint DEFAULT 0 NOT NULL,
    technical_project_id  uuid NOT NULL,
    recipient_auth_id     uuid,
    email                 varchar(254) NOT NULL,
    role                  varchar(16) NOT NULL,
    function_name         varchar(120),
    status                varchar(16) DEFAULT 'PENDING' NOT NULL,
    expires_at            timestamptz NOT NULL,
    created_at            timestamptz DEFAULT now() NOT NULL,
    created_by_user_id    uuid NOT NULL,
    locale                varchar(5) DEFAULT 'pt-BR' NOT NULL,
    email_state           varchar(16) DEFAULT 'PENDING' NOT NULL,
    email_attempts        integer DEFAULT 0 NOT NULL,
    next_email_attempt_at timestamptz,
    responded_at          timestamptz,
    updated_at            timestamptz DEFAULT now() NOT NULL,
    CONSTRAINT pk_project_invitation PRIMARY KEY (id),
    CONSTRAINT ck_project_invitation_role CHECK ((role IN ('MANAGER', 'MEMBER', 'VIEWER'))),
    CONSTRAINT ck_project_invitation_status CHECK ((status IN ('PENDING', 'ACCEPTED', 'DECLINED', 'REVOKED')))
);

CREATE TABLE project_member (
    id                   uuid NOT NULL,
    version              bigint DEFAULT 0 NOT NULL,
    technical_project_id uuid NOT NULL,
    user_id              uuid NOT NULL,
    role                 varchar(16) NOT NULL,
    function_name        varchar(120),
    is_active            boolean DEFAULT true NOT NULL,
    joined_at            timestamptz DEFAULT now() NOT NULL,
    created_at           timestamptz DEFAULT now() NOT NULL,
    updated_at           timestamptz DEFAULT now() NOT NULL,
    CONSTRAINT pk_project_member PRIMARY KEY (id),
    CONSTRAINT uq_project_member_technical_project_user UNIQUE (technical_project_id, user_id),
    CONSTRAINT ck_project_member_role CHECK ((role IN ('OWNER', 'MANAGER', 'MEMBER', 'VIEWER')))
);

CREATE TABLE project_photo (
    id                   uuid NOT NULL,
    version              bigint DEFAULT 0 NOT NULL,
    technical_project_id uuid NOT NULL,
    name                 varchar(160) NOT NULL,
    mime                 varchar(40) NOT NULL,
    content              bytea NOT NULL,
    is_primary           boolean DEFAULT false NOT NULL,
    uploaded_by_user_id  uuid NOT NULL,
    created_at           timestamptz DEFAULT now() NOT NULL,
    updated_at           timestamptz DEFAULT now() NOT NULL,
    CONSTRAINT pk_project_photo PRIMARY KEY (id),
    CONSTRAINT ck_project_photo_content CHECK ((octet_length(content) <= 8388608)),
    CONSTRAINT ck_project_photo_mime CHECK ((mime IN ('image/jpeg', 'image/png')))
);

CREATE TABLE proposal (
    id           uuid DEFAULT gen_random_uuid() NOT NULL,
    requester_id uuid NOT NULL,
    supplier_id  uuid,
    status       varchar(24) DEFAULT 'AWAITING_SUPPLIER' NOT NULL,
    notes        varchar(500),
    total_amount numeric,
    created_at   timestamptz DEFAULT now() NOT NULL,
    updated_at   timestamptz DEFAULT now() NOT NULL,
    CONSTRAINT pk_proposal PRIMARY KEY (id),
    CONSTRAINT ck_proposal_status CHECK ((status IN ('AWAITING_SUPPLIER', 'AWAITING_REQUESTER', 'ACCEPTED', 'REJECTED', 'CANCELED')))
);

CREATE TABLE proposal_item (
    id                    uuid DEFAULT gen_random_uuid() NOT NULL,
    proposal_id           uuid NOT NULL,
    offer_id              uuid NOT NULL,
    quantity              integer NOT NULL,
    negotiated_unit_price numeric,
    discount              numeric,
    created_at            timestamptz DEFAULT now() NOT NULL,
    updated_at            timestamptz DEFAULT now() NOT NULL,
    CONSTRAINT pk_proposal_item PRIMARY KEY (id),
    CONSTRAINT ck_proposal_item_quantity CHECK ((quantity > 0))
);

CREATE TABLE proposal_unit (
    id               uuid DEFAULT gen_random_uuid() NOT NULL,
    proposal_item_id uuid NOT NULL,
    local_unit_id    uuid NOT NULL,
    quantity         integer NOT NULL,
    note             varchar(255),
    created_at       timestamptz DEFAULT now() NOT NULL,
    updated_at       timestamptz DEFAULT now() NOT NULL,
    CONSTRAINT pk_proposal_unit PRIMARY KEY (id),
    CONSTRAINT ck_proposal_unit_quantity CHECK ((quantity > 0))
);

CREATE TABLE purchase_order (
    id                  uuid DEFAULT gen_random_uuid() NOT NULL,
    buyer_user_id       uuid NOT NULL,
    status              varchar(16) DEFAULT 'PAID' NOT NULL,
    subtotal_amount     numeric(12,2) NOT NULL,
    fee_percentage      numeric(6,5) NOT NULL,
    fee_amount          numeric(12,2) NOT NULL,
    total_amount        numeric(12,2) NOT NULL,
    created_at          timestamptz DEFAULT now() NOT NULL,
    canceled_at         timestamptz,
    canceled_by_user_id uuid,
    updated_at          timestamptz DEFAULT now() NOT NULL,
    CONSTRAINT pk_purchase_order PRIMARY KEY (id),
    CONSTRAINT ck_purchase_order_canceled CHECK (((status = 'CANCELED') = (canceled_at IS NOT NULL))),
    CONSTRAINT ck_purchase_order_fee_amount CHECK ((fee_amount >= (0)::numeric)),
    CONSTRAINT ck_purchase_order_fee_percentage CHECK (((fee_percentage >= (0)::numeric) AND (fee_percentage <= (1)::numeric))),
    CONSTRAINT ck_purchase_order_status CHECK ((status IN ('PAID', 'CANCELED'))),
    CONSTRAINT ck_purchase_order_subtotal_amount CHECK ((subtotal_amount >= (0)::numeric)),
    CONSTRAINT ck_purchase_order_total CHECK ((total_amount = (subtotal_amount + fee_amount)))
);

CREATE TABLE requester (
    id            uuid DEFAULT gen_random_uuid() NOT NULL,
    company_id    uuid NOT NULL,
    business_type varchar(40),
    created_at    timestamptz DEFAULT now() NOT NULL,
    updated_at    timestamptz DEFAULT now() NOT NULL,
    CONSTRAINT pk_requester PRIMARY KEY (id)
);

CREATE TABLE service_contract (
    id                     uuid DEFAULT gen_random_uuid() NOT NULL,
    technical_service_id   uuid NOT NULL,
    warranty               varchar(255),
    delivery_deadline_date date,
    has_insurance          boolean DEFAULT false NOT NULL,
    has_utility_approval   boolean DEFAULT false NOT NULL,
    created_at             timestamptz DEFAULT now() NOT NULL,
    updated_at             timestamptz DEFAULT now() NOT NULL,
    CONSTRAINT pk_service_contract PRIMARY KEY (id),
    CONSTRAINT uq_service_contract_technical_service UNIQUE (technical_service_id)
);

CREATE TABLE service_executor (
    id                        uuid DEFAULT gen_random_uuid() NOT NULL,
    technical_service_id      uuid NOT NULL,
    technician_affiliation_id uuid NOT NULL,
    function                  varchar(255) NOT NULL,
    created_at                timestamptz DEFAULT now() NOT NULL,
    updated_at                timestamptz DEFAULT now() NOT NULL,
    CONSTRAINT pk_service_executor PRIMARY KEY (id)
);

CREATE TABLE shift (
    id            uuid DEFAULT gen_random_uuid() NOT NULL,
    technician_id uuid NOT NULL,
    day_week      varchar(16) NOT NULL,
    starts_at     timestamptz NOT NULL,
    ends_at       timestamptz NOT NULL,
    created_at    timestamptz DEFAULT now() NOT NULL,
    updated_at    timestamptz DEFAULT now() NOT NULL,
    CONSTRAINT pk_shift PRIMARY KEY (id),
    CONSTRAINT ck_shift_day_week CHECK ((day_week IN ('MONDAY', 'TUESDAY', 'WEDNESDAY', 'THURSDAY', 'FRIDAY', 'SATURDAY', 'SUNDAY'))),
    CONSTRAINT ck_shift_period CHECK ((ends_at > starts_at))
);

CREATE TABLE subscription (
    id               uuid DEFAULT gen_random_uuid() NOT NULL,
    supplier_id      uuid NOT NULL,
    company_plan_id  uuid NOT NULL,
    status           varchar(16) DEFAULT 'PAID' NOT NULL,
    has_auto_renewal boolean DEFAULT true NOT NULL,
    starts_at        timestamptz NOT NULL,
    ends_at          timestamptz,
    created_at       timestamptz DEFAULT now() NOT NULL,
    updated_at       timestamptz DEFAULT now() NOT NULL,
    CONSTRAINT pk_subscription PRIMARY KEY (id),
    CONSTRAINT ck_subscription_status CHECK ((status IN ('PAID', 'IN_DEBT', 'SUSPENDED')))
);

CREATE TABLE supplier (
    id            uuid DEFAULT gen_random_uuid() NOT NULL,
    company_id    uuid NOT NULL,
    status        varchar(16) DEFAULT 'ACTIVE' NOT NULL,
    business_type varchar(40),
    created_at    timestamptz DEFAULT now() NOT NULL,
    updated_at    timestamptz DEFAULT now() NOT NULL,
    CONSTRAINT pk_supplier PRIMARY KEY (id),
    CONSTRAINT ck_supplier_status CHECK ((status IN ('ACTIVE', 'SUSPENDED', 'DEACTIVATED')))
);

CREATE TABLE technical_company (
    id         uuid DEFAULT gen_random_uuid() NOT NULL,
    company_id uuid NOT NULL,
    created_at timestamptz DEFAULT now() NOT NULL,
    updated_at timestamptz DEFAULT now() NOT NULL,
    CONSTRAINT pk_technical_company PRIMARY KEY (id)
);

CREATE TABLE technical_course (
    id          uuid DEFAULT gen_random_uuid() NOT NULL,
    company_id  uuid,
    title       varchar(30),
    information text,
    link        text,
    created_at  timestamptz DEFAULT now() NOT NULL,
    updated_at  timestamptz DEFAULT now() NOT NULL,
    CONSTRAINT pk_technical_course PRIMARY KEY (id)
);

CREATE TABLE technical_project (
    id                    uuid DEFAULT gen_random_uuid() NOT NULL,
    requester_id          uuid,
    local_unit_id         uuid,
    status                varchar(20) DEFAULT 'OPEN' NOT NULL,
    starts_at             timestamptz DEFAULT now(),
    ends_at               timestamptz,
    name                  varchar(160) DEFAULT 'Projeto técnico' NOT NULL,
    description           text,
    owner_company_id      uuid,
    owner_user_id         uuid,
    created_by_user_id    uuid,
    is_archived           boolean DEFAULT false NOT NULL,
    created_at            timestamptz DEFAULT now() NOT NULL,
    version               bigint DEFAULT 0 NOT NULL,
    location_state        varchar(2),
    location_city         varchar(120),
    location_neighborhood varchar(120),
    location_zip_code     varchar(8),
    location_street       varchar(180),
    location_number       varchar(30),
    location_complement   varchar(180),
    location_latitude     double precision,
    location_longitude    double precision,
    location_place_id     varchar(300),
    location_geocoded_at  timestamptz,
    updated_at            timestamptz DEFAULT now() NOT NULL,
    CONSTRAINT pk_technical_project PRIMARY KEY (id),
    CONSTRAINT ck_technical_project_coordinates CHECK ((((location_latitude IS NULL) AND (location_longitude IS NULL)) OR ((location_latitude IS NOT NULL) AND (location_longitude IS NOT NULL) AND ((location_latitude >= ('-90'::integer)::double precision) AND (location_latitude <= (90)::double precision)) AND ((location_longitude >= ('-180'::integer)::double precision) AND (location_longitude <= (180)::double precision))))),
    CONSTRAINT ck_technical_project_owner CHECK (((((owner_company_id IS NOT NULL))::integer + ((owner_user_id IS NOT NULL))::integer) = 1)),
    CONSTRAINT ck_technical_project_status CHECK ((status IN ('OPEN', 'IN_PROGRESS', 'COMPLETED', 'CANCELED')))
);

CREATE TABLE technical_service (
    id                   uuid DEFAULT gen_random_uuid() NOT NULL,
    technical_project_id uuid NOT NULL,
    purpose              varchar(255) NOT NULL,
    status               varchar(20) DEFAULT 'OPEN' NOT NULL,
    scheduled_at         timestamptz,
    created_at           timestamptz DEFAULT now() NOT NULL,
    accepted_by_user_id  uuid,
    accepted_at          timestamptz,
    ends_at              timestamptz,
    updated_at           timestamptz DEFAULT now() NOT NULL,
    CONSTRAINT pk_technical_service PRIMARY KEY (id),
    CONSTRAINT ck_technical_service_status CHECK ((status IN ('OPEN', 'IN_PROGRESS', 'COMPLETED', 'CANCELED')))
);

CREATE TABLE technician (
    id         uuid DEFAULT gen_random_uuid() NOT NULL,
    person_id  uuid NOT NULL,
    crea       varchar(255),
    slug       varchar(160) NOT NULL,
    status     varchar(24) DEFAULT 'UNDER_ANALYSIS' NOT NULL,
    created_at timestamptz DEFAULT now() NOT NULL,
    updated_at timestamptz DEFAULT now() NOT NULL,
    CONSTRAINT pk_technician PRIMARY KEY (id),
    CONSTRAINT uq_technician_slug UNIQUE (slug)
);

CREATE TABLE technician_affiliation (
    id               uuid DEFAULT gen_random_uuid() NOT NULL,
    company_id       uuid,
    technician_id    uuid NOT NULL,
    affiliation_type varchar(16) NOT NULL,
    is_active        boolean DEFAULT true NOT NULL,
    created_at       timestamptz DEFAULT now() NOT NULL,
    updated_at       timestamptz DEFAULT now() NOT NULL,
    CONSTRAINT pk_technician_affiliation PRIMARY KEY (id),
    CONSTRAINT ck_technician_affiliation_company CHECK (((company_id IS NOT NULL) OR (affiliation_type = 'INDEPENDENT'))),
    CONSTRAINT ck_technician_affiliation_type CHECK ((affiliation_type IN ('INDEPENDENT', 'AFFILIATED', 'PARTNER')))
);

CREATE TABLE unit_specification (
    id              uuid DEFAULT gen_random_uuid() NOT NULL,
    local_unit_id   uuid NOT NULL,
    specifications  varchar(255),
    location_photos varchar(255),
    date            timestamptz NOT NULL,
    created_at      timestamptz DEFAULT now() NOT NULL,
    updated_at      timestamptz DEFAULT now() NOT NULL,
    CONSTRAINT pk_unit_specification PRIMARY KEY (id)
);

CREATE TABLE user_account (
    id                    uuid DEFAULT gen_random_uuid() NOT NULL,
    auth_id               uuid NOT NULL,
    username              varchar(30) NOT NULL,
    avatar                varchar(255),
    banner                varchar(255),
    is_active             boolean DEFAULT true NOT NULL,
    connection_user_ids   uuid[] DEFAULT ARRAY[]::uuid[] NOT NULL,
    managed_by_company_id uuid,
    created_at            timestamptz DEFAULT now() NOT NULL,
    updated_at            timestamptz DEFAULT now() NOT NULL,
    CONSTRAINT pk_user_account PRIMARY KEY (id),
    CONSTRAINT uq_user_account_auth_id UNIQUE (auth_id),
    CONSTRAINT uq_user_account_username UNIQUE (username),
    CONSTRAINT ck_user_account_username_format CHECK ((username ~ '^[a-z0-9_]{3,30}$'))
);

CREATE TABLE user_company (
    id              uuid DEFAULT gen_random_uuid() NOT NULL,
    company_id      uuid NOT NULL,
    user_id         uuid NOT NULL,
    job_position_id uuid NOT NULL,
    created_at      timestamptz DEFAULT now() NOT NULL,
    updated_at      timestamptz DEFAULT now() NOT NULL,
    CONSTRAINT pk_user_company PRIMARY KEY (id),
    CONSTRAINT uq_user_company_company_user UNIQUE (company_id, user_id)
);

CREATE TABLE user_photo (
    id          uuid DEFAULT gen_random_uuid() NOT NULL,
    user_id     uuid NOT NULL,
    type        varchar(255) NOT NULL,
    url         varchar(255) NOT NULL,
    public_id   varchar(255) NOT NULL,
    crop_x      integer,
    crop_y      integer,
    crop_width  integer,
    crop_height integer,
    created_at  timestamptz DEFAULT now() NOT NULL,
    updated_at  timestamptz DEFAULT now() NOT NULL,
    CONSTRAINT pk_user_photo PRIMARY KEY (id),
    CONSTRAINT ck_user_photo_crop CHECK ((((crop_x IS NULL) AND (crop_y IS NULL) AND (crop_width IS NULL) AND (crop_height IS NULL)) OR ((crop_x IS NOT NULL) AND (crop_y IS NOT NULL) AND (crop_width IS NOT NULL) AND (crop_height IS NOT NULL) AND (crop_x >= 0) AND (crop_y >= 0) AND (crop_width > 0) AND (crop_height > 0)))),
    CONSTRAINT ck_user_photo_type CHECK ((type IN ('PROFILE', 'BANNER')))
);

ALTER TABLE access_code ADD CONSTRAINT fk_access_code_company FOREIGN KEY (company_id) REFERENCES company(id);
ALTER TABLE access_code ADD CONSTRAINT fk_access_code_job_position FOREIGN KEY (job_position_id) REFERENCES job_position(id);
ALTER TABLE address ADD CONSTRAINT fk_address_owner_company FOREIGN KEY (owner_company_id) REFERENCES company(id) DEFERRABLE INITIALLY DEFERRED;
ALTER TABLE brand ADD CONSTRAINT fk_brand_owner_company FOREIGN KEY (owner_company_id) REFERENCES company(id);
ALTER TABLE brand_request ADD CONSTRAINT fk_brand_request_brand FOREIGN KEY (brand_id) REFERENCES brand(id);
ALTER TABLE brand_request ADD CONSTRAINT fk_brand_request_decided_by_user FOREIGN KEY (decided_by_user_id) REFERENCES user_account(id);
ALTER TABLE brand_request ADD CONSTRAINT fk_brand_request_requester_company FOREIGN KEY (requester_company_id) REFERENCES company(id);
ALTER TABLE brand_request ADD CONSTRAINT fk_brand_request_requester_user FOREIGN KEY (requester_user_id) REFERENCES user_account(id);
ALTER TABLE brand_request ADD CONSTRAINT fk_brand_request_result_brand FOREIGN KEY (result_brand_id) REFERENCES brand(id);
ALTER TABLE cart ADD CONSTRAINT fk_cart_user FOREIGN KEY (user_id) REFERENCES user_account(id);
ALTER TABLE cart_item ADD CONSTRAINT fk_cart_item_cart FOREIGN KEY (cart_id) REFERENCES cart(id) ON DELETE CASCADE;
ALTER TABLE cart_item ADD CONSTRAINT fk_cart_item_offer FOREIGN KEY (offer_id) REFERENCES offer(id) ON DELETE CASCADE;
ALTER TABLE certification ADD CONSTRAINT fk_certification_technician FOREIGN KEY (technician_id) REFERENCES technician(id);
ALTER TABLE certification_record ADD CONSTRAINT fk_certification_record_certification FOREIGN KEY (certification_id) REFERENCES certification(id);
ALTER TABLE certification_record ADD CONSTRAINT fk_certification_record_professional_registration FOREIGN KEY (professional_registration_id) REFERENCES professional_registration(id);
ALTER TABLE charge ADD CONSTRAINT fk_charge_subscription FOREIGN KEY (subscription_id) REFERENCES subscription(id);
ALTER TABLE company ADD CONSTRAINT fk_company_address FOREIGN KEY (address_id) REFERENCES address(id);
ALTER TABLE company ADD CONSTRAINT fk_company_business_contact FOREIGN KEY (business_contact_id) REFERENCES business_contact(id);
ALTER TABLE company_photo ADD CONSTRAINT fk_company_photo_company FOREIGN KEY (company_id) REFERENCES company(id);
ALTER TABLE company_position ADD CONSTRAINT fk_company_position_company FOREIGN KEY (company_id) REFERENCES company(id);
ALTER TABLE company_position ADD CONSTRAINT fk_company_position_job_position FOREIGN KEY (job_position_id) REFERENCES job_position(id);
ALTER TABLE energy_bill ADD CONSTRAINT fk_energy_bill_local_unit FOREIGN KEY (local_unit_id) REFERENCES local_unit(id);
ALTER TABLE flux_log ADD CONSTRAINT fk_flux_log_user FOREIGN KEY (user_id) REFERENCES user_account(id);
ALTER TABLE geolocation ADD CONSTRAINT fk_geolocation_address FOREIGN KEY (address_id) REFERENCES address(id);
ALTER TABLE inventory ADD CONSTRAINT fk_inventory_model FOREIGN KEY (model_id) REFERENCES model(id);
ALTER TABLE inventory ADD CONSTRAINT fk_inventory_supplier FOREIGN KEY (supplier_id) REFERENCES supplier(id);
ALTER TABLE local_unit ADD CONSTRAINT fk_local_unit_address FOREIGN KEY (address_id) REFERENCES address(id);
ALTER TABLE local_unit ADD CONSTRAINT fk_local_unit_requester FOREIGN KEY (requester_id) REFERENCES requester(id);
ALTER TABLE local_unit_photo ADD CONSTRAINT fk_local_unit_photo_local_unit FOREIGN KEY (local_unit_id) REFERENCES local_unit(id);
ALTER TABLE model ADD CONSTRAINT fk_model_brand FOREIGN KEY (brand_id) REFERENCES brand(id);
ALTER TABLE model ADD CONSTRAINT fk_model_creator_company FOREIGN KEY (creator_company_id) REFERENCES company(id);
ALTER TABLE model_photo ADD CONSTRAINT fk_model_photo_model FOREIGN KEY (model_id) REFERENCES model(id);
ALTER TABLE offer ADD CONSTRAINT fk_offer_model FOREIGN KEY (model_id) REFERENCES model(id);
ALTER TABLE offer ADD CONSTRAINT fk_offer_supplier FOREIGN KEY (supplier_id) REFERENCES supplier(id);
ALTER TABLE offer_service_region ADD CONSTRAINT fk_offer_service_region_offer FOREIGN KEY (offer_id) REFERENCES offer(id);
ALTER TABLE offer_status_history ADD CONSTRAINT fk_offer_status_history_actor_user FOREIGN KEY (actor_user_id) REFERENCES user_account(id);
ALTER TABLE offer_status_history ADD CONSTRAINT fk_offer_status_history_offer FOREIGN KEY (offer_id) REFERENCES offer(id);
ALTER TABLE offer_translation ADD CONSTRAINT fk_offer_translation_offer FOREIGN KEY (offer_id) REFERENCES offer(id);
ALTER TABLE offer_translation_job ADD CONSTRAINT fk_offer_translation_job_offer FOREIGN KEY (offer_id) REFERENCES offer(id) ON DELETE CASCADE;
ALTER TABLE offer_translation_job ADD CONSTRAINT fk_offer_translation_job_source_offer_translation FOREIGN KEY (source_offer_translation_id) REFERENCES offer_translation(id);
ALTER TABLE order_item ADD CONSTRAINT fk_order_item_offer FOREIGN KEY (offer_id) REFERENCES offer(id) ON DELETE SET NULL;
ALTER TABLE order_item ADD CONSTRAINT fk_order_item_purchase_order FOREIGN KEY (purchase_order_id) REFERENCES purchase_order(id) ON DELETE CASCADE;
ALTER TABLE order_item ADD CONSTRAINT fk_order_item_supplier FOREIGN KEY (supplier_id) REFERENCES supplier(id);
ALTER TABLE order_payment ADD CONSTRAINT fk_order_payment_purchase_order FOREIGN KEY (purchase_order_id) REFERENCES purchase_order(id) ON DELETE CASCADE;
ALTER TABLE person ADD CONSTRAINT fk_person_contact FOREIGN KEY (contact_id) REFERENCES contact(id);
ALTER TABLE person ADD CONSTRAINT fk_person_user FOREIGN KEY (user_id) REFERENCES user_account(id);
ALTER TABLE platform_admin ADD CONSTRAINT fk_platform_admin_user FOREIGN KEY (user_id) REFERENCES user_account(id);
ALTER TABLE position_permission ADD CONSTRAINT fk_position_permission_job_position FOREIGN KEY (job_position_id) REFERENCES job_position(id);
ALTER TABLE position_permission ADD CONSTRAINT fk_position_permission_permission FOREIGN KEY (permission_id) REFERENCES permission(id);
ALTER TABLE professional_document ADD CONSTRAINT fk_professional_document_user FOREIGN KEY (user_id) REFERENCES user_account(id);
ALTER TABLE professional_onboarding ADD CONSTRAINT fk_professional_onboarding_technician FOREIGN KEY (technician_id) REFERENCES technician(id);
ALTER TABLE professional_onboarding ADD CONSTRAINT fk_professional_onboarding_user FOREIGN KEY (user_id) REFERENCES user_account(id);
ALTER TABLE professional_registration ADD CONSTRAINT fk_professional_registration_profession FOREIGN KEY (profession_id) REFERENCES profession(id);
ALTER TABLE professional_registration ADD CONSTRAINT fk_professional_registration_technician FOREIGN KEY (technician_id) REFERENCES technician(id);
ALTER TABLE professional_review ADD CONSTRAINT fk_professional_review_reviewer_user FOREIGN KEY (reviewer_user_id) REFERENCES user_account(id);
ALTER TABLE professional_review ADD CONSTRAINT fk_professional_review_technical_service FOREIGN KEY (technical_service_id) REFERENCES technical_service(id);
ALTER TABLE professional_review ADD CONSTRAINT fk_professional_review_technician FOREIGN KEY (technician_id) REFERENCES technician(id);
ALTER TABLE professional_submission ADD CONSTRAINT fk_professional_submission_decided_by_user FOREIGN KEY (decided_by_user_id) REFERENCES user_account(id);
ALTER TABLE professional_submission ADD CONSTRAINT fk_professional_submission_professional_onboarding FOREIGN KEY (professional_onboarding_id) REFERENCES professional_onboarding(id);
ALTER TABLE professional_team ADD CONSTRAINT fk_professional_team_company FOREIGN KEY (company_id) REFERENCES company(id);
ALTER TABLE professional_team ADD CONSTRAINT fk_professional_team_responsible_technician FOREIGN KEY (responsible_technician_id) REFERENCES technician(id);
ALTER TABLE professional_team_member ADD CONSTRAINT fk_professional_team_member_professional_team FOREIGN KEY (professional_team_id) REFERENCES professional_team(id) ON DELETE CASCADE;
ALTER TABLE professional_team_member ADD CONSTRAINT fk_professional_team_member_technician FOREIGN KEY (technician_id) REFERENCES technician(id);
ALTER TABLE project_activity ADD CONSTRAINT fk_project_activity_technical_project FOREIGN KEY (technical_project_id) REFERENCES technical_project(id);
ALTER TABLE project_activity ADD CONSTRAINT fk_project_activity_technical_service FOREIGN KEY (technical_service_id) REFERENCES technical_service(id);
ALTER TABLE project_history ADD CONSTRAINT fk_project_history_actor_user FOREIGN KEY (actor_user_id) REFERENCES user_account(id);
ALTER TABLE project_history ADD CONSTRAINT fk_project_history_technical_project FOREIGN KEY (technical_project_id) REFERENCES technical_project(id);
ALTER TABLE project_invitation ADD CONSTRAINT fk_project_invitation_created_by_user FOREIGN KEY (created_by_user_id) REFERENCES user_account(id);
ALTER TABLE project_invitation ADD CONSTRAINT fk_project_invitation_technical_project FOREIGN KEY (technical_project_id) REFERENCES technical_project(id);
ALTER TABLE project_member ADD CONSTRAINT fk_project_member_technical_project FOREIGN KEY (technical_project_id) REFERENCES technical_project(id);
ALTER TABLE project_member ADD CONSTRAINT fk_project_member_user FOREIGN KEY (user_id) REFERENCES user_account(id);
ALTER TABLE project_photo ADD CONSTRAINT fk_project_photo_technical_project FOREIGN KEY (technical_project_id) REFERENCES technical_project(id);
ALTER TABLE project_photo ADD CONSTRAINT fk_project_photo_uploaded_by_user FOREIGN KEY (uploaded_by_user_id) REFERENCES user_account(id);
ALTER TABLE proposal ADD CONSTRAINT fk_proposal_requester FOREIGN KEY (requester_id) REFERENCES requester(id);
ALTER TABLE proposal ADD CONSTRAINT fk_proposal_supplier FOREIGN KEY (supplier_id) REFERENCES supplier(id);
ALTER TABLE proposal_item ADD CONSTRAINT fk_proposal_item_offer FOREIGN KEY (offer_id) REFERENCES offer(id);
ALTER TABLE proposal_item ADD CONSTRAINT fk_proposal_item_proposal FOREIGN KEY (proposal_id) REFERENCES proposal(id);
ALTER TABLE proposal_unit ADD CONSTRAINT fk_proposal_unit_local_unit FOREIGN KEY (local_unit_id) REFERENCES local_unit(id);
ALTER TABLE proposal_unit ADD CONSTRAINT fk_proposal_unit_proposal_item FOREIGN KEY (proposal_item_id) REFERENCES proposal_item(id);
ALTER TABLE purchase_order ADD CONSTRAINT fk_purchase_order_buyer_user FOREIGN KEY (buyer_user_id) REFERENCES user_account(id);
ALTER TABLE purchase_order ADD CONSTRAINT fk_purchase_order_canceled_by_user FOREIGN KEY (canceled_by_user_id) REFERENCES user_account(id);
ALTER TABLE requester ADD CONSTRAINT fk_requester_company FOREIGN KEY (company_id) REFERENCES company(id);
ALTER TABLE service_contract ADD CONSTRAINT fk_service_contract_technical_service FOREIGN KEY (technical_service_id) REFERENCES technical_service(id);
ALTER TABLE service_executor ADD CONSTRAINT fk_service_executor_technical_service FOREIGN KEY (technical_service_id) REFERENCES technical_service(id);
ALTER TABLE service_executor ADD CONSTRAINT fk_service_executor_technician_affiliation FOREIGN KEY (technician_affiliation_id) REFERENCES technician_affiliation(id);
ALTER TABLE shift ADD CONSTRAINT fk_shift_technician FOREIGN KEY (technician_id) REFERENCES technician(id);
ALTER TABLE subscription ADD CONSTRAINT fk_subscription_company_plan FOREIGN KEY (company_plan_id) REFERENCES company_plan(id);
ALTER TABLE subscription ADD CONSTRAINT fk_subscription_supplier FOREIGN KEY (supplier_id) REFERENCES supplier(id);
ALTER TABLE supplier ADD CONSTRAINT fk_supplier_company FOREIGN KEY (company_id) REFERENCES company(id);
ALTER TABLE technical_company ADD CONSTRAINT fk_technical_company_company FOREIGN KEY (company_id) REFERENCES company(id);
ALTER TABLE technical_course ADD CONSTRAINT fk_technical_course_company FOREIGN KEY (company_id) REFERENCES company(id);
ALTER TABLE technical_project ADD CONSTRAINT fk_technical_project_created_by_user FOREIGN KEY (created_by_user_id) REFERENCES user_account(id);
ALTER TABLE technical_project ADD CONSTRAINT fk_technical_project_local_unit FOREIGN KEY (local_unit_id) REFERENCES local_unit(id);
ALTER TABLE technical_project ADD CONSTRAINT fk_technical_project_owner_company FOREIGN KEY (owner_company_id) REFERENCES company(id);
ALTER TABLE technical_project ADD CONSTRAINT fk_technical_project_owner_user FOREIGN KEY (owner_user_id) REFERENCES user_account(id);
ALTER TABLE technical_project ADD CONSTRAINT fk_technical_project_requester FOREIGN KEY (requester_id) REFERENCES requester(id);
ALTER TABLE technical_service ADD CONSTRAINT fk_technical_service_accepted_by_user FOREIGN KEY (accepted_by_user_id) REFERENCES user_account(id);
ALTER TABLE technical_service ADD CONSTRAINT fk_technical_service_technical_project FOREIGN KEY (technical_project_id) REFERENCES technical_project(id);
ALTER TABLE technician ADD CONSTRAINT fk_technician_person FOREIGN KEY (person_id) REFERENCES person(id);
ALTER TABLE technician_affiliation ADD CONSTRAINT fk_technician_affiliation_company FOREIGN KEY (company_id) REFERENCES company(id);
ALTER TABLE technician_affiliation ADD CONSTRAINT fk_technician_affiliation_technician FOREIGN KEY (technician_id) REFERENCES technician(id);
ALTER TABLE unit_specification ADD CONSTRAINT fk_unit_specification_local_unit FOREIGN KEY (local_unit_id) REFERENCES local_unit(id);
ALTER TABLE user_account ADD CONSTRAINT fk_user_account_managed_by_company FOREIGN KEY (managed_by_company_id) REFERENCES company(id);
ALTER TABLE user_company ADD CONSTRAINT fk_user_company_company FOREIGN KEY (company_id) REFERENCES company(id);
ALTER TABLE user_company ADD CONSTRAINT fk_user_company_job_position FOREIGN KEY (job_position_id) REFERENCES job_position(id);
ALTER TABLE user_company ADD CONSTRAINT fk_user_company_user FOREIGN KEY (user_id) REFERENCES user_account(id);
ALTER TABLE user_photo ADD CONSTRAINT fk_user_photo_user FOREIGN KEY (user_id) REFERENCES user_account(id);

CREATE INDEX idx_access_code_company ON access_code (company_id);
CREATE INDEX idx_address_owner_company ON address (owner_company_id);
CREATE UNIQUE INDEX uq_brand_private_owner_company_name ON brand (owner_company_id, normalized_name) WHERE ((visibility = 'PRIVATE') AND (NOT is_archived));
CREATE UNIQUE INDEX uq_brand_public_name ON brand (normalized_name) WHERE (visibility = 'PUBLIC');
CREATE INDEX idx_brand_request_requester_company_created ON brand_request (requester_company_id, created_at DESC);
CREATE UNIQUE INDEX uq_brand_request_pending_brand ON brand_request (brand_id) WHERE (status = 'PENDING');
CREATE INDEX idx_charge_subscription ON charge (subscription_id);
CREATE INDEX idx_flux_log_user_created ON flux_log (user_id, created_at DESC);
CREATE INDEX idx_local_unit_requester ON local_unit (requester_id);
CREATE UNIQUE INDEX uq_local_unit_photo_banner ON local_unit_photo (local_unit_id) WHERE (type = 'BANNER');
CREATE UNIQUE INDEX uq_local_unit_photo_primary ON local_unit_photo (local_unit_id) WHERE is_primary;
CREATE INDEX idx_model_product_type ON model (product_type);
CREATE INDEX idx_model_photo_model_created ON model_photo (model_id, created_at DESC);
CREATE UNIQUE INDEX uq_model_photo_primary ON model_photo (model_id) WHERE is_primary;
CREATE INDEX idx_offer_model ON offer (model_id);
CREATE INDEX idx_offer_supplier ON offer (supplier_id);
CREATE INDEX idx_offer_service_region_offer ON offer_service_region (offer_id);
CREATE INDEX idx_offer_status_history_offer_created ON offer_status_history (offer_id, created_at DESC);
CREATE INDEX idx_offer_translation_job_available ON offer_translation_job (available_at, id) WHERE (status IN ('QUEUED', 'PROCESSING', 'RETRY_PENDING'));
CREATE INDEX idx_order_item_offer ON order_item (offer_id) WHERE (offer_id IS NOT NULL);
CREATE INDEX idx_order_item_purchase_order ON order_item (purchase_order_id);
CREATE INDEX idx_order_item_supplier_purchase_order ON order_item (supplier_id, purchase_order_id);
CREATE INDEX idx_professional_document_user ON professional_document (user_id);
CREATE INDEX idx_professional_registration_technician ON professional_registration (technician_id);
CREATE INDEX idx_professional_review_technician_created ON professional_review (technician_id, created_at DESC);
CREATE INDEX idx_professional_submission_work ON professional_submission (status, available_at);
CREATE INDEX idx_professional_team_company ON professional_team (company_id);
CREATE INDEX idx_professional_team_member_technician ON professional_team_member (technician_id);
CREATE INDEX idx_project_activity_agenda ON project_activity (technical_project_id, starts_at);
CREATE UNIQUE INDEX uq_project_activity_technical_service ON project_activity (technical_service_id) WHERE (technical_service_id IS NOT NULL);
CREATE INDEX idx_project_history_timeline ON project_history (technical_project_id, created_at DESC);
CREATE INDEX idx_project_invitation_recipient ON project_invitation (lower(email), status, expires_at);
CREATE INDEX idx_project_member_access ON project_member (user_id, technical_project_id) WHERE is_active;
CREATE INDEX idx_project_photo_gallery ON project_photo (technical_project_id, created_at DESC);
CREATE UNIQUE INDEX uq_project_photo_primary ON project_photo (technical_project_id) WHERE is_primary;
CREATE INDEX idx_proposal_requester ON proposal (requester_id);
CREATE INDEX idx_proposal_supplier ON proposal (supplier_id);
CREATE INDEX idx_proposal_item_offer ON proposal_item (offer_id);
CREATE INDEX idx_proposal_item_proposal ON proposal_item (proposal_id);
CREATE INDEX idx_purchase_order_buyer_user_created ON purchase_order (buyer_user_id, created_at DESC);
CREATE INDEX idx_requester_company ON requester (company_id);
CREATE INDEX idx_service_executor_technical_service ON service_executor (technical_service_id);
CREATE INDEX idx_shift_technician ON shift (technician_id);
CREATE INDEX idx_subscription_supplier ON subscription (supplier_id);
CREATE INDEX idx_supplier_company ON supplier (company_id);
CREATE INDEX idx_technical_project_owner_company ON technical_project (owner_company_id, created_at DESC);
CREATE INDEX idx_technical_project_owner_user ON technical_project (owner_user_id, created_at DESC);
CREATE INDEX idx_technical_project_requester ON technical_project (requester_id);
CREATE INDEX idx_technical_service_technical_project ON technical_service (technical_project_id);
CREATE INDEX idx_technician_person ON technician (person_id);
CREATE INDEX idx_technician_affiliation_technician ON technician_affiliation (technician_id);
CREATE UNIQUE INDEX uq_technician_affiliation_active_independent ON technician_affiliation (technician_id) WHERE (is_active AND (company_id IS NULL) AND (affiliation_type = 'INDEPENDENT'));
CREATE INDEX idx_user_company_company ON user_company (company_id);
CREATE INDEX idx_user_company_user ON user_company (user_id);

CREATE FUNCTION enforce_user_company_managed_account() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
BEGIN
    IF EXISTS (SELECT 1 FROM user_account WHERE id = NEW.user_id AND managed_by_company_id IS NOT NULL AND managed_by_company_id <> NEW.company_id) THEN
        RAISE EXCEPTION 'Conta administrada pela empresa não pode ter vínculo com outra empresa' USING ERRCODE = '23514';
    END IF;
    RETURN NEW;
END;
$$;

CREATE TRIGGER trg_user_company_managed_account BEFORE INSERT OR UPDATE ON user_company
    FOR EACH ROW EXECUTE FUNCTION enforce_user_company_managed_account();

CREATE FUNCTION enforce_purchase_order_buyer_eligibility() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
BEGIN
    IF EXISTS (SELECT 1 FROM user_company uc JOIN supplier s ON s.company_id = uc.company_id WHERE uc.user_id = NEW.buyer_user_id)
       AND NOT EXISTS (SELECT 1 FROM person p JOIN technician t ON t.person_id = p.id WHERE p.user_id = NEW.buyer_user_id AND t.status = 'APPROVED') THEN
        RAISE EXCEPTION 'Usuário de empresa fornecedora não pode comprar' USING ERRCODE = '23514';
    END IF;
    RETURN NEW;
END;
$$;

CREATE TRIGGER trg_purchase_order_buyer_eligibility BEFORE INSERT ON purchase_order
    FOR EACH ROW EXECUTE FUNCTION enforce_purchase_order_buyer_eligibility();

CREATE FUNCTION enforce_order_item_not_own_supplier() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
BEGIN
    IF EXISTS (SELECT 1 FROM purchase_order o JOIN user_company uc ON uc.user_id = o.buyer_user_id JOIN supplier s ON s.company_id = uc.company_id
               WHERE o.id = NEW.purchase_order_id AND s.id = NEW.supplier_id) THEN
        RAISE EXCEPTION 'Não é possível comprar de uma empresa à qual o comprador pertence' USING ERRCODE = '23514';
    END IF;
    RETURN NEW;
END;
$$;

CREATE TRIGGER trg_order_item_not_own_supplier BEFORE INSERT ON order_item
    FOR EACH ROW EXECUTE FUNCTION enforce_order_item_not_own_supplier();

CREATE PROCEDURE generate_subscription_charge(p_subscription_id uuid, p_due_date date, p_payment_method varchar(16))
    LANGUAGE plpgsql
    AS $$
BEGIN
    INSERT INTO charge (subscription_id, amount, payment_method, status, due_date)
    SELECT s.id, p.price, p_payment_method, 'PENDING', p_due_date
    FROM subscription s
    JOIN company_plan p ON p.id = s.company_plan_id
    WHERE s.id = p_subscription_id;
    IF NOT FOUND THEN
        RAISE EXCEPTION 'Assinatura não encontrada: %', p_subscription_id USING ERRCODE = 'P0002';
    END IF;
END;
$$;
