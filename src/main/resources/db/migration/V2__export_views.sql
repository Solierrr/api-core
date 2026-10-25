CREATE SCHEMA export;

CREATE VIEW export.address AS
    SELECT id, state, city, neighborhood, zip_code, street, number, owner_company_id, created_at, updated_at
    FROM public.address;

CREATE VIEW export.brand AS
    SELECT id, owner_company_id, name, normalized_name, visibility, is_archived, is_legacy, logo_url, reference_url, version, created_at, updated_at
    FROM public.brand;

CREATE VIEW export.brand_request AS
    SELECT id, brand_id, requester_company_id, requester_user_id, kind, status, proposed_name, proposed_logo_url, proposed_reference_url, result_brand_id, decision_reason, decided_by_user_id, decided_at, created_at, version, updated_at
    FROM public.brand_request;

CREATE VIEW export.business_contact AS
    SELECT id, company_email, website, created_at, updated_at
    FROM public.business_contact;

CREATE VIEW export.cart AS
    SELECT id, user_id, created_at, updated_at
    FROM public.cart;

CREATE VIEW export.cart_item AS
    SELECT id, cart_id, offer_id, quantity, added_at, created_at, updated_at
    FROM public.cart_item;

CREATE VIEW export.certification AS
    SELECT id, technician_id, type, information, description, issuer, name, valid_until_at, created_at, updated_at
    FROM public.certification;

CREATE VIEW export.certification_record AS
    SELECT id, professional_registration_id, certification_id, created_at, updated_at
    FROM public.certification_record;

CREATE VIEW export.charge AS
    SELECT id, subscription_id, amount, payment_method, status, due_date, paid_at, created_at, updated_at
    FROM public.charge;

CREATE VIEW export.company AS
    SELECT id, status, type, address_id, business_contact_id, cnpj, trade_name, corporate_name, slug, created_at, updated_at
    FROM public.company;

CREATE VIEW export.company_photo AS
    SELECT id, company_id, type, url, public_id, crop_x, crop_y, crop_width, crop_height, created_at, updated_at
    FROM public.company_photo;

CREATE VIEW export.company_plan AS
    SELECT id, name, price, cycle, created_at, updated_at
    FROM public.company_plan;

CREATE VIEW export.company_position AS
    SELECT id, company_id, job_position_id, created_at, updated_at
    FROM public.company_position;

CREATE VIEW export.contact AS
    SELECT id, created_at, updated_at
    FROM public.contact;

CREATE VIEW export.energy_bill AS
    SELECT id, local_unit_id, consumption, amount, created_at, updated_at
    FROM public.energy_bill;

CREATE VIEW export.flux_log AS
    SELECT id, user_id, action, created_at, updated_at
    FROM public.flux_log;

CREATE VIEW export.geolocation AS
    SELECT id, address_id, latitude, longitude, created_at, updated_at
    FROM public.geolocation;

CREATE VIEW export.inventory AS
    SELECT id, supplier_id, model_id, quantity, created_at, updated_at
    FROM public.inventory;

CREATE VIEW export.job_position AS
    SELECT id, name, accesses, created_at, updated_at
    FROM public.job_position;

CREATE VIEW export.local_unit AS
    SELECT id, requester_id, address_id, complement, location_type, created_at, updated_at
    FROM public.local_unit;

CREATE VIEW export.local_unit_photo AS
    SELECT id, local_unit_id, type, url, public_id, created_at, is_primary, updated_at
    FROM public.local_unit_photo;

CREATE VIEW export.model AS
    SELECT id, brand_id, model_name, power_wp, efficiency, dimension, type, width, length, weight, status, product_type, infrastructure_type, material, creator_company_id, created_at, updated_at
    FROM public.model;

CREATE VIEW export.model_photo AS
    SELECT id, model_id, url, public_id, is_primary, created_at, updated_at
    FROM public.model_photo;

CREATE VIEW export.offer AS
    SELECT id, supplier_id, model_id, unit_price, availability, expires_at, slug, discount_percentage, source_locale, translation_status, commercial_status, created_at, updated_at
    FROM public.offer;

CREATE VIEW export.offer_service_region AS
    SELECT offer_id, region, id, created_at, updated_at
    FROM public.offer_service_region;

CREATE VIEW export.offer_status_history AS
    SELECT id, offer_id, previous_status, new_status, actor_user_id, created_at, updated_at
    FROM public.offer_status_history;

CREATE VIEW export.offer_translation AS
    SELECT id, offer_id, locale, title, description, details, created_at, updated_at
    FROM public.offer_translation;

CREATE VIEW export.offer_translation_job AS
    SELECT id, offer_id, source_offer_translation_id, status, attempts, available_at, created_at, error_code, updated_at
    FROM public.offer_translation_job;

CREATE VIEW export.order_item AS
    SELECT id, purchase_order_id, offer_id, supplier_id, offer_slug, model_name, unit_price, discount_percentage, net_unit_price, quantity, line_total_amount, created_at, updated_at
    FROM public.order_item;

CREATE VIEW export.permission AS
    SELECT id, permission_name, name, description, created_at, updated_at
    FROM public.permission;

CREATE VIEW export.person AS
    SELECT id, user_id, contact_id, name, created_at, updated_at
    FROM public.person;

CREATE VIEW export.platform_admin AS
    SELECT id, user_id, created_at, updated_at
    FROM public.platform_admin;

CREATE VIEW export.position_permission AS
    SELECT id, job_position_id, permission_id, created_at, updated_at
    FROM public.position_permission;

CREATE VIEW export.profession AS
    SELECT id, name, is_emergency_call_accepted, is_registration_required, is_nr10_required, is_nr35_required, created_at, updated_at
    FROM public.profession;

CREATE VIEW export.professional_registration AS
    SELECT id, technician_id, profession_id, council, number, expires_at, created_at, updated_at
    FROM public.professional_registration;

CREATE VIEW export.professional_review AS
    SELECT id, technician_id, reviewer_user_id, technical_service_id, rating, comment, is_active, created_at, updated_at
    FROM public.professional_review;

CREATE VIEW export.professional_team AS
    SELECT id, company_id, name, responsible_technician_id, is_active, created_at, updated_at
    FROM public.professional_team;

CREATE VIEW export.professional_team_member AS
    SELECT id, professional_team_id, technician_id, created_at, updated_at
    FROM public.professional_team_member;

CREATE VIEW export.project_activity AS
    SELECT id, version, technical_project_id, title, description, type, status, starts_at, ends_at, is_onsite, assignee_user_ids, technical_service_id, created_at, updated_at
    FROM public.project_activity;

CREATE VIEW export.project_history AS
    SELECT id, version, technical_project_id, actor_user_id, action, detail, created_at, updated_at
    FROM public.project_history;

CREATE VIEW export.project_invitation AS
    SELECT id, version, technical_project_id, recipient_auth_id, role, function_name, status, expires_at, created_at, created_by_user_id, locale, email_state, email_attempts, next_email_attempt_at, responded_at, updated_at
    FROM public.project_invitation;

CREATE VIEW export.project_member AS
    SELECT id, version, technical_project_id, user_id, role, function_name, is_active, joined_at, created_at, updated_at
    FROM public.project_member;

CREATE VIEW export.project_photo AS
    SELECT id, version, technical_project_id, name, mime, is_primary, uploaded_by_user_id, created_at, updated_at
    FROM public.project_photo;

CREATE VIEW export.proposal AS
    SELECT id, requester_id, supplier_id, status, notes, total_amount, created_at, updated_at
    FROM public.proposal;

CREATE VIEW export.proposal_item AS
    SELECT id, proposal_id, offer_id, quantity, negotiated_unit_price, discount, created_at, updated_at
    FROM public.proposal_item;

CREATE VIEW export.proposal_unit AS
    SELECT id, proposal_item_id, local_unit_id, quantity, note, created_at, updated_at
    FROM public.proposal_unit;

CREATE VIEW export.purchase_order AS
    SELECT id, buyer_user_id, status, subtotal_amount, fee_percentage, fee_amount, total_amount, created_at, canceled_at, canceled_by_user_id, updated_at
    FROM public.purchase_order;

CREATE VIEW export.requester AS
    SELECT id, company_id, business_type, created_at, updated_at
    FROM public.requester;

CREATE VIEW export.service_contract AS
    SELECT id, technical_service_id, warranty, delivery_deadline_date, has_insurance, has_utility_approval, created_at, updated_at
    FROM public.service_contract;

CREATE VIEW export.service_executor AS
    SELECT id, technical_service_id, technician_affiliation_id, function, created_at, updated_at
    FROM public.service_executor;

CREATE VIEW export.shift AS
    SELECT id, technician_id, day_week, starts_at, ends_at, created_at, updated_at
    FROM public.shift;

CREATE VIEW export.subscription AS
    SELECT id, supplier_id, company_plan_id, status, has_auto_renewal, starts_at, ends_at, created_at, updated_at
    FROM public.subscription;

CREATE VIEW export.supplier AS
    SELECT id, company_id, status, business_type, created_at, updated_at
    FROM public.supplier;

CREATE VIEW export.technical_company AS
    SELECT id, company_id, created_at, updated_at
    FROM public.technical_company;

CREATE VIEW export.technical_course AS
    SELECT id, company_id, title, information, link, created_at, updated_at
    FROM public.technical_course;

CREATE VIEW export.technical_project AS
    SELECT id, requester_id, local_unit_id, status, starts_at, ends_at, name, description, owner_company_id, owner_user_id, created_by_user_id, is_archived, created_at, version, location_state, location_city, location_neighborhood, location_zip_code, location_street, location_number, location_complement, location_latitude, location_longitude, location_place_id, location_geocoded_at, updated_at
    FROM public.technical_project;

CREATE VIEW export.technical_service AS
    SELECT id, technical_project_id, purpose, status, scheduled_at, created_at, accepted_by_user_id, accepted_at, ends_at, updated_at
    FROM public.technical_service;

CREATE VIEW export.technician AS
    SELECT id, person_id, crea, slug, status, created_at, updated_at
    FROM public.technician;

CREATE VIEW export.technician_affiliation AS
    SELECT id, company_id, technician_id, affiliation_type, is_active, created_at, updated_at
    FROM public.technician_affiliation;

CREATE VIEW export.unit_specification AS
    SELECT id, local_unit_id, specifications, location_photos, date, created_at, updated_at
    FROM public.unit_specification;

CREATE VIEW export.user_account AS
    SELECT id, auth_id, username, is_active, managed_by_company_id, created_at, updated_at
    FROM public.user_account;

CREATE VIEW export.user_company AS
    SELECT id, company_id, user_id, job_position_id, created_at, updated_at
    FROM public.user_company;

CREATE VIEW export.user_photo AS
    SELECT id, user_id, type, crop_x, crop_y, crop_width, crop_height, created_at, updated_at
    FROM public.user_photo;
