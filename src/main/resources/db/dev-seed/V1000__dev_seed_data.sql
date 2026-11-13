-- V1000: DEV-ONLY seed data covering every entity and every user role.
-- Loaded only when the `dev` profile is active (see application-dev.properties -> spring.flyway.locations).
--
-- All seeded accounts share the password:  Password123!
--
--   superadmin@talenthub.dev   SUPER_ADMIN
--   admin@talenthub.dev        ADMIN
--   orgadmin.club@talenthub.dev     ORG_ADMIN  (Lagos City FC, CLUB)
--   orgadmin.academy@talenthub.dev  ORG_ADMIN  (Elite Hoops Academy, ACADEMY)
--   orgadmin.agency@talenthub.dev   ORG_ADMIN  (Prime Scouts Agency, SCOUTING_AGENCY)
--   scout.verified@talenthub.dev    SCOUT (VERIFIED, SCOUT_PRO)
--   scout.pending@talenthub.dev     SCOUT (verification PENDING, SCOUT_INDIVIDUAL)
--   guardian1@talenthub.dev / guardian2@talenthub.dev   GUARDIAN
--   athlete.chinedu@talenthub.dev   ATHLETE (adult, unaffiliated, football)
--   athlete.amara@talenthub.dev     ATHLETE (adult, Lagos City FC, football)
--   athlete.tunde@talenthub.dev     ATHLETE (minor, Lagos City FC, guardian1)
--   athlete.zainab@talenthub.dev    ATHLETE (adult, Elite Hoops Academy, basketball)
--   athlete.emeka@talenthub.dev     ATHLETE (minor, unaffiliated, athletics, guardian2)
--   athlete.ngozi@talenthub.dev     ATHLETE (adult, unaffiliated, swimming)
--   suspended@talenthub.dev         ATHLETE, SUSPENDED
--   unverified@talenthub.dev        ATHLETE, PENDING_VERIFICATION
--
-- IDs are fixed, syntactically valid UUIDs (hex only) so Hibernate can read them back.
-- Datetime literals use 'YYYY-MM-DD HH:MM:SS' (MySQL rejects ISO-8601 'T'/'Z').

-- BCrypt (cost 12) of 'Password123!'
SET @pw = '$2a$12$PgK09LqKaLozuspmj/gRSuCfevwS0u5Hjasdtg/nD5TkqImC7QlNe';

-- ---------------------------------------------------------------------------
-- ID variables
-- ---------------------------------------------------------------------------
-- users
SET @u_super    = 'a0000000-0000-4000-8000-000000000001';
SET @u_admin    = 'a0000000-0000-4000-8000-000000000002';
SET @u_org_club = 'a0000000-0000-4000-8000-000000000003';
SET @u_org_acad = 'a0000000-0000-4000-8000-000000000004';
SET @u_org_agcy = 'a0000000-0000-4000-8000-000000000005';
SET @u_scout1   = 'a0000000-0000-4000-8000-000000000006';
SET @u_scout2   = 'a0000000-0000-4000-8000-000000000007';
SET @u_guard1   = 'a0000000-0000-4000-8000-000000000008';
SET @u_guard2   = 'a0000000-0000-4000-8000-000000000009';
SET @u_chinedu  = 'a0000000-0000-4000-8000-000000000010';
SET @u_amara    = 'a0000000-0000-4000-8000-000000000011';
SET @u_tunde    = 'a0000000-0000-4000-8000-000000000012';
SET @u_zainab   = 'a0000000-0000-4000-8000-000000000013';
SET @u_emeka    = 'a0000000-0000-4000-8000-000000000014';
SET @u_ngozi    = 'a0000000-0000-4000-8000-000000000015';
SET @u_susp     = 'a0000000-0000-4000-8000-000000000016';
SET @u_unver    = 'a0000000-0000-4000-8000-000000000017';
-- organizations
SET @o_club = 'b0000000-0000-4000-8000-000000000001';
SET @o_acad = 'b0000000-0000-4000-8000-000000000002';
SET @o_agcy = 'b0000000-0000-4000-8000-000000000003';
-- scout profiles
SET @sp_scout1 = 'e0000000-0000-4000-8000-000000000001';
SET @sp_scout2 = 'e0000000-0000-4000-8000-000000000002';
-- sports / activity metadata (looked up, since V4/V10 generate random IDs)
SET @s_fb = (SELECT id FROM sports WHERE code = 'FB');
SET @s_bk = (SELECT id FROM sports WHERE code = 'BK');
SET @s_at = (SELECT id FROM sports WHERE code = 'AT');
SET @s_sw = (SELECT id FROM sports WHERE code = 'SW');
SET @at_match = (SELECT id FROM activity_types WHERE code = 'MATCH');
SET @at_pt    = (SELECT id FROM activity_types WHERE code = 'PERSONAL_TRAINING');
SET @sv_match = (SELECT sv.id FROM activity_schema_versions sv
                 JOIN activity_templates t ON t.id = sv.template_id
                 WHERE t.code = 'FOOTBALL_MATCH' AND sv.status = 'PUBLISHED' LIMIT 1);
SET @sv_pt    = (SELECT sv.id FROM activity_schema_versions sv
                 JOIN activity_templates t ON t.id = sv.template_id
                 WHERE t.code = 'PERSONAL_TRAINING_SESSION' AND sv.status = 'PUBLISHED' LIMIT 1);
-- activities
SET @a1 = 'f0000000-0000-4000-8000-000000000001';
SET @a2 = 'f0000000-0000-4000-8000-000000000002';
SET @a3 = 'f0000000-0000-4000-8000-000000000003';
SET @a4 = 'f0000000-0000-4000-8000-000000000004';
SET @a5 = 'f0000000-0000-4000-8000-000000000005';
-- media assets
SET @m1 = '90000000-0000-4000-8000-000000000001';
SET @m2 = '90000000-0000-4000-8000-000000000002';
SET @m3 = '90000000-0000-4000-8000-000000000003';
-- subscriptions
SET @sub_club  = 'c1000000-0000-4000-8000-000000000001';
SET @sub_agcy  = 'c1000000-0000-4000-8000-000000000002';
-- watchlists / evaluations
SET @wl1 = 'd1000000-0000-4000-8000-000000000001';
SET @wl2 = 'd1000000-0000-4000-8000-000000000002';
SET @ev1 = 'd2000000-0000-4000-8000-000000000001';
SET @ev2 = 'd2000000-0000-4000-8000-000000000002';
-- transactions / invoices
SET @tx1 = 'd3000000-0000-4000-8000-000000000001';
SET @tx2 = 'd3000000-0000-4000-8000-000000000002';
SET @tx3 = 'd3000000-0000-4000-8000-000000000003';
-- notifications
SET @n1 = 'd4000000-0000-4000-8000-000000000001';

-- ---------------------------------------------------------------------------
-- identity: users
-- ---------------------------------------------------------------------------
INSERT INTO users (id, created_at, email, password_hash, role, status, email_verified,
                   email_verification_token, email_verification_token_expires_at, last_login_at) VALUES
(@u_super,    NOW(6), 'superadmin@talenthub.dev',        @pw, 'SUPER_ADMIN', 'ACTIVE', 1, NULL, NULL, NOW(6)),
(@u_admin,    NOW(6), 'admin@talenthub.dev',             @pw, 'ADMIN',       'ACTIVE', 1, NULL, NULL, NOW(6)),
(@u_org_club, NOW(6), 'orgadmin.club@talenthub.dev',     @pw, 'ORG_ADMIN',   'ACTIVE', 1, NULL, NULL, NOW(6)),
(@u_org_acad, NOW(6), 'orgadmin.academy@talenthub.dev',  @pw, 'ORG_ADMIN',   'ACTIVE', 1, NULL, NULL, NOW(6)),
(@u_org_agcy, NOW(6), 'orgadmin.agency@talenthub.dev',   @pw, 'ORG_ADMIN',   'ACTIVE', 1, NULL, NULL, NOW(6)),
(@u_scout1,   NOW(6), 'scout.verified@talenthub.dev',    @pw, 'SCOUT',       'ACTIVE', 1, NULL, NULL, NOW(6)),
(@u_scout2,   NOW(6), 'scout.pending@talenthub.dev',     @pw, 'SCOUT',       'ACTIVE', 1, NULL, NULL, NULL),
(@u_guard1,   NOW(6), 'guardian1@talenthub.dev',         @pw, 'GUARDIAN',    'ACTIVE', 1, NULL, NULL, NOW(6)),
(@u_guard2,   NOW(6), 'guardian2@talenthub.dev',         @pw, 'GUARDIAN',    'ACTIVE', 1, NULL, NULL, NULL),
(@u_chinedu,  NOW(6), 'athlete.chinedu@talenthub.dev',   @pw, 'ATHLETE',     'ACTIVE', 1, NULL, NULL, NOW(6)),
(@u_amara,    NOW(6), 'athlete.amara@talenthub.dev',     @pw, 'ATHLETE',     'ACTIVE', 1, NULL, NULL, NOW(6)),
(@u_tunde,    NOW(6), 'athlete.tunde@talenthub.dev',     @pw, 'ATHLETE',     'ACTIVE', 1, NULL, NULL, NULL),
(@u_zainab,   NOW(6), 'athlete.zainab@talenthub.dev',    @pw, 'ATHLETE',     'ACTIVE', 1, NULL, NULL, NOW(6)),
(@u_emeka,    NOW(6), 'athlete.emeka@talenthub.dev',     @pw, 'ATHLETE',     'ACTIVE', 1, NULL, NULL, NULL),
(@u_ngozi,    NOW(6), 'athlete.ngozi@talenthub.dev',     @pw, 'ATHLETE',     'ACTIVE', 1, NULL, NULL, NOW(6)),
(@u_susp,     NOW(6), 'suspended@talenthub.dev',         @pw, 'ATHLETE',     'SUSPENDED', 1, NULL, NULL, NULL),
(@u_unver,    NOW(6), 'unverified@talenthub.dev',        @pw, 'ATHLETE',     'PENDING_VERIFICATION', 0,
                 'dev-verify-token-0001', DATE_ADD(NOW(6), INTERVAL 7 DAY), NULL);

INSERT INTO refresh_tokens (id, user_id, token_hash, expires_at, revoked, device_info, ip_address) VALUES
('a2000000-0000-4000-8000-000000000001', @u_chinedu, @pw, DATE_ADD(NOW(6), INTERVAL 30 DAY), 0, 'Dev Chrome / macOS', '127.0.0.1'),
('a2000000-0000-4000-8000-000000000002', @u_amara,   @pw, DATE_SUB(NOW(6), INTERVAL 1 DAY),  1, 'Dev Safari / iOS',   '127.0.0.1');

-- ---------------------------------------------------------------------------
-- profile: user_profiles
-- ---------------------------------------------------------------------------
INSERT INTO user_profiles (id, created_at, user_id, first_name, last_name, display_name, date_of_birth,
                           gender, phone_number, country_code, city, bio, role, profile_complete) VALUES
(UUID(), NOW(6), @u_super,    'Sade',    'Adeyemi',  'Sade Adeyemi',    '1985-03-14', 'FEMALE', '+2348010000001', 'NG', 'Lagos',  'Platform super administrator.', 'SUPER_ADMIN', 1),
(UUID(), NOW(6), @u_admin,    'Bola',    'Okafor',   'Bola Okafor',     '1988-07-02', 'MALE',   '+2348010000002', 'NG', 'Abuja',  'Platform administrator.',       'ADMIN',       1),
(UUID(), NOW(6), @u_org_club, 'Ifeanyi', 'Nwosu',    'Ifeanyi Nwosu',   '1978-11-21', 'MALE',   '+2348010000003', 'NG', 'Lagos',  'Director of Lagos City FC.',    'ORG_ADMIN',   1),
(UUID(), NOW(6), @u_org_acad, 'Funmi',   'Balogun',  'Funmi Balogun',   '1981-05-09', 'FEMALE', '+2348010000004', 'NG', 'Ibadan', 'Head of Elite Hoops Academy.', 'ORG_ADMIN',   1),
(UUID(), NOW(6), @u_org_agcy, 'Kunle',   'Adebayo',  'Kunle Adebayo',   '1976-01-30', 'MALE',   '+2348010000005', 'NG', 'Lagos',  'Founder of Prime Scouts Agency.', 'ORG_ADMIN', 1),
(UUID(), NOW(6), @u_scout1,   'Emeka',   'Obi',      'Emeka Obi',       '1983-09-17', 'MALE',   '+2348010000006', 'NG', 'Lagos',  'Football scout, 12 years experience.', 'SCOUT', 1),
(UUID(), NOW(6), @u_scout2,   'Halima',  'Bello',    'Halima Bello',    '1990-04-25', 'FEMALE', '+2348010000007', 'NG', 'Kano',   'Basketball and athletics scout.', 'SCOUT',     1),
(UUID(), NOW(6), @u_guard1,   'Chioma',  'Eze',      'Chioma Eze',      '1982-02-12', 'FEMALE', '+2348010000008', 'NG', 'Lagos',  'Parent of Tunde.',              'GUARDIAN',    1),
(UUID(), NOW(6), @u_guard2,   'Musa',    'Ibrahim',  'Musa Ibrahim',    '1979-08-08', 'MALE',   '+2348010000009', 'NG', 'Kaduna', 'Parent of Emeka.',              'GUARDIAN',    1),
(UUID(), NOW(6), @u_chinedu,  'Chinedu', 'Okeke',    'Chinedu Okeke',   '2002-06-15', 'MALE',   '+2348010000010', 'NG', 'Enugu',  'Striker looking for a trial.',  'ATHLETE',     1),
(UUID(), NOW(6), @u_amara,    'Amara',   'Nnamdi',   'Amara Nnamdi',    '2003-12-01', 'FEMALE', '+2348010000011', 'NG', 'Lagos',  'Midfielder at Lagos City FC.',  'ATHLETE',     1),
(UUID(), NOW(6), @u_tunde,    'Tunde',   'Eze',      'Tunde Eze',       '2011-03-22', 'MALE',   NULL,             'NG', 'Lagos',  'Youth winger at Lagos City FC.', 'ATHLETE',    1),
(UUID(), NOW(6), @u_zainab,   'Zainab',  'Abdullahi','Zainab Abdullahi','2004-10-05', 'FEMALE', '+2348010000013', 'NG', 'Ibadan', 'Point guard at Elite Hoops.',   'ATHLETE',     1),
(UUID(), NOW(6), @u_emeka,    'Emeka',   'Ibrahim',  'Emeka Ibrahim',   '2010-09-19', 'MALE',   NULL,             'NG', 'Kaduna', '400m sprinter.',                'ATHLETE',     1),
(UUID(), NOW(6), @u_ngozi,    'Ngozi',   'Uche',     'Ngozi Uche',      '2001-01-27', 'FEMALE', '+2348010000015', 'NG', 'Port Harcourt', 'Freestyle swimmer.',      'ATHLETE',     1),
(UUID(), NOW(6), @u_susp,     'Sam',     'Suspended','Sam Suspended',   '2000-05-05', 'MALE',   NULL,             'NG', 'Lagos',  NULL,                            'ATHLETE',     0),
(UUID(), NOW(6), @u_unver,    'Una',     'Unverified','Una Unverified', '2000-06-06', 'FEMALE', NULL,             'NG', 'Lagos',  NULL,                            'ATHLETE',     0);

-- ---------------------------------------------------------------------------
-- organization
-- ---------------------------------------------------------------------------
INSERT INTO organizations (id, created_at, code, name, org_type, country_code, city, admin_user_id, status) VALUES
(@o_club, NOW(6), 'LCFC',     'Lagos City FC',         'CLUB',            'NG', 'Lagos',         @u_org_club, 'ACTIVE'),
(@o_acad, NOW(6), 'ELITEAC',  'Elite Hoops Academy',   'ACADEMY',         'NG', 'Ibadan',        @u_org_acad, 'ACTIVE'),
(@o_agcy, NOW(6), 'PSA',      'Prime Scouts Agency',   'SCOUTING_AGENCY', 'NG', 'Lagos',         @u_org_agcy, 'ACTIVE');

-- ---------------------------------------------------------------------------
-- athlete: number sequences + profiles
-- ---------------------------------------------------------------------------
INSERT INTO athlete_number_sequences (id, prefix_key, last_seq) VALUES
(UUID(), 'TH',         3),
(UUID(), 'LCFC-FB',    2),
(UUID(), 'ELITEAC-BK', 1);

INSERT INTO athlete_profiles (id, created_at, user_id, athlete_number, primary_sport_id, primary_sport_code,
                              position, height_cm, weight_kg, dominant_foot, status, organization_id, nationality) VALUES
(UUID(), NOW(6), @u_chinedu, 'TH-00001',         @s_fb, 'FB', 'Striker',        182, 76, 'RIGHT', 'ACTIVE', NULL,    'NG'),
(UUID(), NOW(6), @u_amara,   'LCFC-FB-00001',    @s_fb, 'FB', 'Midfielder',     170, 62, 'LEFT',  'ACTIVE', @o_club, 'NG'),
(UUID(), NOW(6), @u_tunde,   'LCFC-FB-00002',    @s_fb, 'FB', 'Right Winger',   158, 48, 'RIGHT', 'ACTIVE', @o_club, 'NG'),
(UUID(), NOW(6), @u_zainab,  'ELITEAC-BK-00001', @s_bk, 'BK', 'Point Guard',    175, 66, NULL,    'ACTIVE', @o_acad, 'NG'),
(UUID(), NOW(6), @u_emeka,   'TH-00002',         @s_at, 'AT', '400m',           165, 52, NULL,    'ACTIVE', NULL,    'NG'),
(UUID(), NOW(6), @u_ngozi,   'TH-00003',         @s_sw, 'SW', 'Freestyle',      172, 60, NULL,    'ACTIVE', NULL,    'NG'),
(UUID(), NOW(6), @u_susp,    'TH-00004',         @s_fb, 'FB', 'Goalkeeper',     188, 82, 'RIGHT', 'SUSPENDED', NULL, 'NG');
UPDATE athlete_number_sequences SET last_seq = 4 WHERE prefix_key = 'TH';

-- ---------------------------------------------------------------------------
-- guardian links
-- ---------------------------------------------------------------------------
INSERT INTO guardian_links (id, created_at, guardian_user_id, athlete_user_id, relationship, status,
                            consent_given, consent_given_at, verified_by) VALUES
(UUID(), NOW(6), @u_guard1, @u_tunde, 'PARENT', 'ACTIVE',  1, DATE_SUB(NOW(6), INTERVAL 20 DAY), @u_admin),
(UUID(), NOW(6), @u_guard2, @u_emeka, 'PARENT', 'ACTIVE',  1, DATE_SUB(NOW(6), INTERVAL 15 DAY), @u_admin),
(UUID(), NOW(6), @u_guard2, @u_tunde, 'OTHER',  'PENDING', 0, NULL, NULL),
(UUID(), NOW(6), @u_guard1, @u_emeka, 'PARENT', 'REJECTED',0, NULL, @u_admin);

-- ---------------------------------------------------------------------------
-- athlete access grants
-- ---------------------------------------------------------------------------
INSERT INTO athlete_access_grants (id, created_at, athlete_user_id, grantee_id, grantee_type, permission, expires_at, active) VALUES
(UUID(), NOW(6), @u_tunde,   @u_guard1,   'USER',         'MANAGE',       NULL, 1),
(UUID(), NOW(6), @u_emeka,   @u_guard2,   'USER',         'MANAGE',       NULL, 1),
(UUID(), NOW(6), @u_amara,   @o_club,     'ORGANIZATION', 'MANAGE',       NULL, 1),
(UUID(), NOW(6), @u_tunde,   @o_club,     'ORGANIZATION', 'MANAGE',       NULL, 1),
(UUID(), NOW(6), @u_zainab,  @o_acad,     'ORGANIZATION', 'MANAGE',       NULL, 1),
(UUID(), NOW(6), @u_chinedu, @u_scout1,   'SCOUT',        'VIEW_PROFILE', DATE_ADD(NOW(6), INTERVAL 90 DAY), 1),
(UUID(), NOW(6), @u_chinedu, @u_scout1,   'SCOUT',        'VIEW_STATS',   DATE_ADD(NOW(6), INTERVAL 90 DAY), 1),
(UUID(), NOW(6), @u_amara,   @u_scout1,   'SCOUT',        'VIEW_PROFILE', NULL, 1),
(UUID(), NOW(6), @u_amara,   @u_scout1,   'SCOUT',        'CONTACT',      NULL, 1),
(UUID(), NOW(6), @u_zainab,  @u_scout2,   'SCOUT',        'VIEW_PROFILE', DATE_SUB(NOW(6), INTERVAL 5 DAY), 0);

-- ---------------------------------------------------------------------------
-- subscriptions (plans are seeded by V5/V22)
-- ---------------------------------------------------------------------------
INSERT INTO user_subscriptions (id, created_at, subscriber_id, plan_id, status, starts_at, expires_at, renews_at)
SELECT UUID(), NOW(6), x.subscriber_id, p.id, x.status, DATE_SUB(NOW(6), INTERVAL 10 DAY),
       DATE_ADD(NOW(6), INTERVAL 20 DAY), DATE_ADD(NOW(6), INTERVAL 20 DAY)
FROM (
    SELECT @u_chinedu AS subscriber_id, 'ATHLETE_FREE'    AS plan_code, 'ACTIVE'  AS status UNION ALL
    SELECT @u_amara,                    'ATHLETE_FREE',                 'ACTIVE'            UNION ALL
    SELECT @u_tunde,                    'ATHLETE_FREE',                 'ACTIVE'            UNION ALL
    SELECT @u_zainab,                   'ATHLETE_FREE',                 'ACTIVE'            UNION ALL
    SELECT @u_emeka,                    'ATHLETE_FREE',                 'ACTIVE'            UNION ALL
    SELECT @u_ngozi,                    'ATHLETE_FREE',                 'ACTIVE'            UNION ALL
    SELECT @u_guard1,                   'PARENT_GUARDIAN',              'ACTIVE'            UNION ALL
    SELECT @u_guard2,                   'PARENT_GUARDIAN',              'PENDING'           UNION ALL
    SELECT @u_scout1,                   'SCOUT_PRO',                    'ACTIVE'            UNION ALL
    SELECT @sp_scout1,                  'SCOUT_PRO',                    'ACTIVE'            UNION ALL
    SELECT @u_scout2,                   'SCOUT_INDIVIDUAL',             'ACTIVE'            UNION ALL
    SELECT @sp_scout2,                  'SCOUT_INDIVIDUAL',             'ACTIVE'
) x JOIN subscription_plans p ON p.code = x.plan_code;

INSERT INTO user_subscriptions (id, created_at, subscriber_id, plan_id, status, starts_at, expires_at, renews_at)
SELECT @sub_club, NOW(6), @u_org_club, id, 'ACTIVE', DATE_SUB(NOW(6), INTERVAL 10 DAY),
       DATE_ADD(NOW(6), INTERVAL 20 DAY), DATE_ADD(NOW(6), INTERVAL 20 DAY)
FROM subscription_plans WHERE code = 'CLUB';
INSERT INTO user_subscriptions (id, created_at, subscriber_id, plan_id, status, starts_at, expires_at, renews_at)
SELECT 'c1000000-0000-4000-8000-000000000003', NOW(6), @u_org_acad, id, 'ACTIVE', DATE_SUB(NOW(6), INTERVAL 10 DAY),
       DATE_ADD(NOW(6), INTERVAL 20 DAY), DATE_ADD(NOW(6), INTERVAL 20 DAY)
FROM subscription_plans WHERE code = 'ACADEMY';
INSERT INTO user_subscriptions (id, created_at, subscriber_id, plan_id, status, starts_at, expires_at, renews_at)
SELECT @sub_agcy, NOW(6), @u_org_agcy, id, 'ACTIVE', DATE_SUB(NOW(6), INTERVAL 10 DAY),
       DATE_ADD(NOW(6), INTERVAL 20 DAY), DATE_ADD(NOW(6), INTERVAL 20 DAY)
FROM subscription_plans WHERE code = 'SCOUTING_AGENCY';

INSERT INTO subscription_seats (id, subscription_id, seat_type, assigned_to_user_id, assigned_at, revoked_at) VALUES
(UUID(), @sub_agcy, 'SCOUT', @u_scout1, DATE_SUB(NOW(6), INTERVAL 9 DAY), NULL),
(UUID(), @sub_agcy, 'SCOUT', NULL,      NULL, NULL),
(UUID(), @sub_club, 'SCOUT', NULL,      NULL, NULL);

INSERT INTO subscription_addons (id, created_at, subscription_id, addon_code, quantity, active) VALUES
(UUID(), NOW(6), @sub_agcy, 'EXTRA_SCOUT_SEAT', 2, 1);

-- ---------------------------------------------------------------------------
-- scout
-- ---------------------------------------------------------------------------
INSERT INTO scout_profiles (id, created_at, user_account_id, professional_title, biography, years_experience,
                            current_organization_id, verification_status) VALUES
(@sp_scout1, NOW(6), @u_scout1, 'Senior Football Scout',
    'Scouting West African talent for professional clubs for over a decade.', 12, @o_agcy, 'VERIFIED'),
(@sp_scout2, NOW(6), @u_scout2, 'Regional Scout',
    'Independent scout covering basketball and athletics in Northern Nigeria.', 4, NULL, 'PENDING');

INSERT INTO scout_specializations (id, created_at, scout_profile_id, sport_id, level, preferred_regions_json) VALUES
(UUID(), NOW(6), @sp_scout1, @s_fb, 'PROFESSIONAL', '["Lagos","Enugu","Abuja"]'),
(UUID(), NOW(6), @sp_scout1, @s_bk, 'SEMI_PRO',     '["Lagos"]'),
(UUID(), NOW(6), @sp_scout2, @s_bk, 'AMATEUR',      '["Kano","Kaduna"]'),
(UUID(), NOW(6), @sp_scout2, @s_at, 'YOUTH',        '["Kano","Kaduna"]');

INSERT INTO scout_verifications (id, scout_profile_id, submitted_at, reviewed_by, reviewed_at, status, notes) VALUES
(UUID(), @sp_scout1, DATE_SUB(NOW(6), INTERVAL 30 DAY), @u_admin, DATE_SUB(NOW(6), INTERVAL 28 DAY), 'VERIFIED', 'Credentials confirmed.'),
(UUID(), @sp_scout2, DATE_SUB(NOW(6), INTERVAL 2 DAY),  NULL,     NULL,                              'PENDING',  NULL);

-- ---------------------------------------------------------------------------
-- media
-- ---------------------------------------------------------------------------
INSERT INTO media_assets (id, created_at, owner_id, owner_type, asset_type, original_filename, mime_type, size_bytes,
                          bucket_name, object_key, processing_status, uploaded_at, processed_at, content_hash, uploaded_by) VALUES
(@m1, NOW(6), @u_chinedu, 'ATHLETE', 'PROFILE_IMAGE',  'chinedu.jpg',  'image/jpeg', 245000,
    'athlete-media', CONCAT(@u_chinedu, '/profile/chinedu.jpg'), 'READY', NOW(6), NOW(6),
    'a3f1c9e0d4b25f7a8c6e1b0d9f2a4c6e8b1d3f5a7c9e0b2d4f6a8c0e2b4d6f80', @u_chinedu),
(@m2, NOW(6), @u_amara,   'ATHLETE', 'ACTIVITY_VIDEO', 'match-highlights.mp4', 'video/mp4', 48000000,
    'athlete-media', CONCAT(@u_amara, '/videos/match-highlights.mp4'), 'READY', NOW(6), NOW(6),
    'b4a2d0f1e5c36a8b9d7f2c1e0a3b5d7f9c1e3a5b7d9f0c2e4a6b8d0f2c4e6a81', @u_amara),
(@m3, NOW(6), @u_scout2,  'USER',    'VERIFICATION_DOCUMENT', 'license.pdf', 'application/pdf', 120000,
    'private-docs', CONCAT(@u_scout2, '/docs/license.pdf'), 'UPLOADED', NOW(6), NULL, NULL, @u_scout2);

INSERT INTO media_upload_sessions (id, media_asset_id, presigned_url, presigned_expiry, session_status, created_at, completed_at) VALUES
(UUID(), @m1, NULL, DATE_ADD(NOW(6), INTERVAL 15 MINUTE), 'COMPLETED', NOW(6), NOW(6)),
(UUID(), @m2, NULL, DATE_ADD(NOW(6), INTERVAL 15 MINUTE), 'COMPLETED', NOW(6), NOW(6));

INSERT INTO media_processing_jobs (id, media_asset_id, job_type, status, attempts, last_attempt_at, created_at, completed_at) VALUES
(UUID(), @m2, 'VIDEO_TRANSCODE',    'COMPLETED', 1, NOW(6), NOW(6), NOW(6)),
(UUID(), @m2, 'THUMBNAIL_GENERATE', 'COMPLETED', 1, NOW(6), NOW(6), NOW(6)),
(UUID(), @m3, 'IMAGE_OPTIMIZE',     'QUEUED',    0, NULL,   NOW(6), NULL);

INSERT INTO media_variants (id, media_asset_id, variant_type, object_key, width, height, size_bytes, created_at) VALUES
(UUID(), @m1, 'ORIGINAL',  CONCAT(@u_chinedu, '/profile/chinedu.jpg'),       1024, 1024, 245000, NOW(6)),
(UUID(), @m1, 'THUMBNAIL', CONCAT(@u_chinedu, '/profile/chinedu_thumb.jpg'),  150,  150,  8000, NOW(6)),
(UUID(), @m2, 'MP4_720P',  CONCAT(@u_amara, '/videos/match-highlights_720.mp4'), 1280, 720, 22000000, NOW(6));

INSERT INTO media_access_audits (id, media_asset_id, accessed_by, accessed_at, access_type) VALUES
(UUID(), @m2, @u_scout1, NOW(6), 'VIEW'),
(UUID(), @m1, @u_scout1, NOW(6), 'VIEW');

-- ---------------------------------------------------------------------------
-- activities (new model)
-- ---------------------------------------------------------------------------
INSERT INTO athlete_activities (id, created_at, athlete_id, sport_id, activity_type_id, schema_version_id, title,
                                description, activity_date, location, organization_id, submitted_by, submitted_at,
                                visibility_level, activity_status, verification_status) VALUES
(@a1, NOW(6), @u_chinedu, @s_fb, @at_match, @sv_match, 'Enugu Rangers U23 vs Kano Pillars U23',
    'Scored twice in a 3-1 victory.', DATE_SUB(CURDATE(), INTERVAL 14 DAY), 'Nnamdi Azikiwe Stadium', NULL,
    @u_chinedu, DATE_SUB(NOW(6), INTERVAL 13 DAY), 'PUBLIC', 'PUBLISHED', 'VERIFIED'),
(@a2, NOW(6), @u_amara,   @s_fb, @at_match, @sv_match, 'Lagos City FC vs Abuja Queens',
    'Controlled the midfield and provided one assist.', DATE_SUB(CURDATE(), INTERVAL 7 DAY), 'Teslim Balogun Stadium', @o_club,
    @u_amara, DATE_SUB(NOW(6), INTERVAL 6 DAY), 'PUBLIC', 'PUBLISHED', 'UNVERIFIED'),
(@a3, NOW(6), @u_amara,   @s_fb, @at_pt, @sv_pt, 'Finishing drills with coach',
    'Draft session notes.', DATE_SUB(CURDATE(), INTERVAL 2 DAY), 'LCFC Training Ground', @o_club,
    NULL, NULL, 'PRIVATE', 'DRAFT', 'UNVERIFIED'),
(@a4, NOW(6), @u_tunde,   @s_fb, @at_match, @sv_match, 'Lagos City U15 vs Ikeja Stars U15',
    'Awaiting coach and guardian review.', DATE_SUB(CURDATE(), INTERVAL 3 DAY), 'LCFC Academy Pitch', @o_club,
    @u_tunde, DATE_SUB(NOW(6), INTERVAL 2 DAY), 'ORGANIZATION', 'SUBMITTED', 'PENDING'),
(@a5, NOW(6), @u_zainab,  @s_bk, @at_pt, @sv_pt, 'Ball handling and shooting session',
    'Focused on pick-and-roll reads.', DATE_SUB(CURDATE(), INTERVAL 5 DAY), 'Elite Hoops Gym', @o_acad,
    @u_zainab, DATE_SUB(NOW(6), INTERVAL 4 DAY), 'PUBLIC', 'PUBLISHED', 'UNVERIFIED');

INSERT INTO athlete_activity_field_values (id, created_at, activity_id, field_definition_id, field_code,
                                           string_value, numeric_value, date_value)
SELECT UUID(), NOW(6), v.activity_id, fd.id, v.field_code, v.string_value, v.numeric_value, v.date_value
FROM (
    SELECT @a1 AS activity_id, @sv_match AS sv, 'opponent'        AS field_code, 'Kano Pillars U23' AS string_value, NULL AS numeric_value, NULL AS date_value UNION ALL
    SELECT @a1, @sv_match, 'matchDate',       NULL, NULL, DATE_SUB(CURDATE(), INTERVAL 14 DAY) UNION ALL
    SELECT @a1, @sv_match, 'minutesPlayed',   NULL, 90,   NULL UNION ALL
    SELECT @a1, @sv_match, 'goals',           NULL, 2,    NULL UNION ALL
    SELECT @a1, @sv_match, 'assists',         NULL, 0,    NULL UNION ALL
    SELECT @a2, @sv_match, 'opponent',        'Abuja Queens', NULL, NULL UNION ALL
    SELECT @a2, @sv_match, 'matchDate',       NULL, NULL, DATE_SUB(CURDATE(), INTERVAL 7 DAY) UNION ALL
    SELECT @a2, @sv_match, 'minutesPlayed',   NULL, 85,   NULL UNION ALL
    SELECT @a2, @sv_match, 'goals',           NULL, 0,    NULL UNION ALL
    SELECT @a2, @sv_match, 'assists',         NULL, 1,    NULL UNION ALL
    SELECT @a4, @sv_match, 'opponent',        'Ikeja Stars U15', NULL, NULL UNION ALL
    SELECT @a4, @sv_match, 'matchDate',       NULL, NULL, DATE_SUB(CURDATE(), INTERVAL 3 DAY) UNION ALL
    SELECT @a4, @sv_match, 'minutesPlayed',   NULL, 60,   NULL UNION ALL
    SELECT @a3, @sv_pt,    'trainingObjective','Improve finishing under pressure', NULL, NULL UNION ALL
    SELECT @a3, @sv_pt,    'duration',        NULL, 75,   NULL UNION ALL
    SELECT @a5, @sv_pt,    'trainingObjective','Pick-and-roll decision making', NULL, NULL UNION ALL
    SELECT @a5, @sv_pt,    'duration',        NULL, 90,   NULL
) v JOIN activity_field_definitions fd ON fd.schema_version_id = v.sv AND fd.field_code = v.field_code;

INSERT INTO athlete_activity_performances (id, created_at, activity_id, metric_code, measured_value, unit, measurement_context, source) VALUES
(UUID(), NOW(6), @a1, 'GOALS',          2,  'count',   'Full match', 'COACH_VERIFIED'),
(UUID(), NOW(6), @a1, 'MINUTES_PLAYED', 90, 'minutes', 'Full match', 'COACH_VERIFIED'),
(UUID(), NOW(6), @a2, 'ASSISTS',        1,  'count',   'Full match', 'SELF_REPORTED'),
(UUID(), NOW(6), @a5, 'FREE_THROW_PCT', 84, 'percent', 'Practice',   'SELF_REPORTED');

INSERT INTO athlete_activity_media (id, created_at, activity_id, media_asset_id, media_type, caption, ordinal) VALUES
(UUID(), NOW(6), @a2, @m2, 'VIDEO', 'Match highlights', 0);

INSERT INTO athlete_activity_participants (id, created_at, activity_id, participant_type, participant_id, participant_name) VALUES
(UUID(), NOW(6), @a2, 'COACH',    NULL, 'Coach Segun Taiwo'),
(UUID(), NOW(6), @a2, 'OPPONENT', NULL, 'Abuja Queens'),
(UUID(), NOW(6), @a5, 'TRAINER',  NULL, 'Coach Funmi Balogun');

INSERT INTO athlete_activity_revisions (id, activity_id, revision_number, changed_by, changed_at, change_summary, snapshot_json) VALUES
(UUID(), @a1, 1, @u_chinedu, DATE_SUB(NOW(6), INTERVAL 13 DAY), 'Initial submission', '{"goals":1}'),
(UUID(), @a1, 2, @u_chinedu, DATE_SUB(NOW(6), INTERVAL 12 DAY), 'Corrected goals count', '{"goals":2}');

-- legacy activity_sessions table
INSERT INTO activity_sessions (id, created_at, athlete_user_id, template_code, session_date, sport_code, data_json, coach_notes, logged_by) VALUES
(UUID(), NOW(6), @u_chinedu, 'FOOTBALL_MATCH', DATE_SUB(CURDATE(), INTERVAL 30 DAY), 'FB',
    '{"opponent":"Rivers United U23","goals":1,"minutesPlayed":75}', 'Good movement off the ball.', @u_chinedu);

-- ---------------------------------------------------------------------------
-- verification & development
-- ---------------------------------------------------------------------------
INSERT INTO activity_verifications (id, activity_id, verifier_id, verified_at, notes) VALUES
(UUID(), @a1, @u_org_club, DATE_SUB(NOW(6), INTERVAL 10 DAY), 'Confirmed against official match report.');

SET @pm1 = 'd5000000-0000-4000-8000-000000000001';
SET @pm2 = 'd5000000-0000-4000-8000-000000000002';
INSERT INTO performance_measurements (id, created_at, athlete_id, metric_code, measured_value, unit, observed_at,
                                      measurement_context, source_activity_id, source, verification_status,
                                      verified_by, verified_at, recorded_by, recorded_at) VALUES
(@pm1, NOW(6), @u_chinedu, 'SPRINT_30M', 4.1800, 'seconds', DATE_SUB(CURDATE(), INTERVAL 20 DAY), 'Pre-season fitness test',
    NULL, 'COACH_VERIFIED', 'VERIFIED', @u_org_club, DATE_SUB(NOW(6), INTERVAL 19 DAY), @u_chinedu, DATE_SUB(NOW(6), INTERVAL 20 DAY)),
(@pm2, NOW(6), @u_emeka,   'RUN_400M',   54.2000, 'seconds', DATE_SUB(CURDATE(), INTERVAL 10 DAY), 'School championship',
    NULL, 'SELF_REPORTED', 'UNVERIFIED', NULL, NULL, @u_guard2, DATE_SUB(NOW(6), INTERVAL 10 DAY)),
(UUID(), NOW(6), @u_zainab, 'VERTICAL_JUMP', 62.0000, 'cm', DATE_SUB(CURDATE(), INTERVAL 8 DAY), 'Academy combine',
    NULL, 'SYSTEM', 'PENDING', NULL, NULL, @u_zainab, DATE_SUB(NOW(6), INTERVAL 8 DAY));

INSERT INTO performance_verifications (id, measurement_id, verified_by, verified_at, method, notes) VALUES
(UUID(), @pm1, @u_org_club, DATE_SUB(NOW(6), INTERVAL 19 DAY), 'Timing gates', 'Witnessed by coaching staff.');

INSERT INTO development_milestones (id, created_at, created_by, athlete_id, sport_id, milestone_type, title, description, milestone_date, evidence_activity_id) VALUES
(UUID(), NOW(6), @u_chinedu, @u_chinedu, @s_fb, 'ACHIEVEMENT',      'First brace', 'Scored two goals in a single match.', DATE_SUB(CURDATE(), INTERVAL 14 DAY), @a1),
(UUID(), NOW(6), @u_amara, @u_amara,   @s_fb, 'FIRST_APPEARANCE', 'Senior debut', 'First appearance for Lagos City FC.', DATE_SUB(CURDATE(), INTERVAL 60 DAY), NULL),
(UUID(), NOW(6), @u_guard2, @u_emeka,   @s_at, 'PERSONAL_BEST',    '400m PB', 'New personal best of 54.20s.', DATE_SUB(CURDATE(), INTERVAL 10 DAY), NULL);

-- ---------------------------------------------------------------------------
-- discovery: search projections + saved searches
-- ---------------------------------------------------------------------------
INSERT INTO athlete_search_projections (id, athlete_id, athlete_number, display_name, sport_ids_json, country_of_residence,
                                        state_region, age_category, verification_status, profile_status,
                                        last_activity_date, top_metrics_json, visibility_level) VALUES
(UUID(), @u_chinedu, 'TH-00001',         'Chinedu Okeke',    CONCAT('["', @s_fb, '"]'), 'NG', 'Enugu',  'SENIOR', 'VERIFIED',   'ACTIVE', DATE_SUB(CURDATE(), INTERVAL 14 DAY), '{"GOALS":2,"SPRINT_30M":4.18}', 'PUBLIC'),
(UUID(), @u_amara,   'LCFC-FB-00001',    'Amara Nnamdi',     CONCAT('["', @s_fb, '"]'), 'NG', 'Lagos',  'SENIOR', 'UNVERIFIED', 'ACTIVE', DATE_SUB(CURDATE(), INTERVAL 7 DAY),  '{"ASSISTS":1}',                'PUBLIC'),
(UUID(), @u_tunde,   'LCFC-FB-00002',    'Tunde Eze',        CONCAT('["', @s_fb, '"]'), 'NG', 'Lagos',  'YOUTH',  'UNVERIFIED', 'ACTIVE', DATE_SUB(CURDATE(), INTERVAL 3 DAY),  '{}',                           'ORGANIZATION'),
(UUID(), @u_zainab,  'ELITEAC-BK-00001', 'Zainab Abdullahi', CONCAT('["', @s_bk, '"]'), 'NG', 'Oyo',    'SENIOR', 'UNVERIFIED', 'ACTIVE', DATE_SUB(CURDATE(), INTERVAL 5 DAY),  '{"VERTICAL_JUMP":62}',         'PUBLIC'),
(UUID(), @u_emeka,   'TH-00002',         'Emeka Ibrahim',    CONCAT('["', @s_at, '"]'), 'NG', 'Kaduna', 'YOUTH',  'UNVERIFIED', 'ACTIVE', DATE_SUB(CURDATE(), INTERVAL 10 DAY), '{"RUN_400M":54.2}',            'PRIVATE'),
(UUID(), @u_ngozi,   'TH-00003',         'Ngozi Uche',       CONCAT('["', @s_sw, '"]'), 'NG', 'Rivers', 'SENIOR', 'UNVERIFIED', 'ACTIVE', NULL,                                  '{}',                           'PUBLIC');

INSERT INTO saved_searches (id, scout_profile_id, name, query_json, created_at) VALUES
(UUID(), @sp_scout1, 'Nigerian senior strikers', '{"sport":"FB","ageCategory":"SENIOR","country":"NG","position":"Striker"}', NOW(6)),
(UUID(), @sp_scout2, 'Northern youth sprinters', '{"sport":"AT","ageCategory":"YOUTH","country":"NG"}', NOW(6));

-- ---------------------------------------------------------------------------
-- scouting: favourites, watchlists, follows, evaluations, notes
-- ---------------------------------------------------------------------------
INSERT INTO scout_favourites (id, scout_profile_id, athlete_id, added_at, notes) VALUES
(UUID(), @sp_scout1, @u_chinedu, NOW(6), 'Strong finisher, follow up.'),
(UUID(), @sp_scout1, @u_amara,   NOW(6), 'Excellent vision.');

INSERT INTO watchlists (id, created_at, owner_id, owner_type, name, description, is_shared) VALUES
(@wl1, NOW(6), @sp_scout1, 'SCOUT',        'Summer trial shortlist', 'Players to invite for summer trials.', 0),
(@wl2, NOW(6), @o_agcy,    'ORGANIZATION', 'Agency top prospects',   'Shared across all agency scouts.',     1);

INSERT INTO watchlist_athletes (id, watchlist_id, athlete_id, added_at, added_by, notes) VALUES
(UUID(), @wl1, @u_chinedu, NOW(6), @u_scout1, 'Priority 1'),
(UUID(), @wl1, @u_amara,   NOW(6), @u_scout1, 'Priority 2'),
(UUID(), @wl2, @u_zainab,  NOW(6), @u_scout1, 'Basketball prospect');

INSERT INTO athlete_follows (id, follower_id, follower_type, athlete_id, followed_at, notifications_enabled) VALUES
(UUID(), @sp_scout1, 'SCOUT',        @u_chinedu, NOW(6), 1),
(UUID(), @sp_scout2, 'SCOUT',        @u_zainab,  NOW(6), 1),
(UUID(), @o_agcy,    'ORGANIZATION', @u_amara,   NOW(6), 0);

INSERT INTO scout_evaluations (id, created_at, scout_profile_id, athlete_id, sport_id, evaluation_title,
                               overall_rating, status, evaluation_date) VALUES
(@ev1, NOW(6), @sp_scout1, @u_chinedu, @s_fb, 'Chinedu Okeke - Trial assessment', 8.2, 'COMPLETED', DATE_SUB(CURDATE(), INTERVAL 5 DAY)),
(@ev2, NOW(6), @sp_scout1, @u_amara,   @s_fb, 'Amara Nnamdi - Initial look',      NULL, 'DRAFT',     CURDATE());

INSERT INTO evaluation_scores (id, created_at, evaluation_id, criterion_id, score, max_score, notes)
SELECT UUID(), NOW(6), @ev1, c.id, s.score, 10.0, s.notes
FROM (
    SELECT 'FB_TECHNICAL' AS code, 8.5 AS score, 'Clean first touch.' AS notes UNION ALL
    SELECT 'FB_TACTICAL',  7.5, 'Good positioning.' UNION ALL
    SELECT 'FB_PHYSICAL',  8.0, 'Quick over short distances.' UNION ALL
    SELECT 'FB_MENTAL',    8.0, 'Composed in front of goal.' UNION ALL
    SELECT 'FB_POTENTIAL', 9.0, 'High ceiling.'
) s JOIN evaluation_criteria c ON c.criterion_code = s.code;

INSERT INTO scout_notes (id, created_at, scout_profile_id, athlete_id, note_text, note_date, is_private) VALUES
(UUID(), NOW(6), @sp_scout1, @u_chinedu, 'Great off-ball movement; check stamina late in games.', DATE_SUB(CURDATE(), INTERVAL 6 DAY), 1),
(UUID(), NOW(6), @sp_scout2, @u_zainab,  'Smart decision maker. Needs consistent outside shot.',   DATE_SUB(CURDATE(), INTERVAL 3 DAY), 1);

-- ---------------------------------------------------------------------------
-- notifications
-- ---------------------------------------------------------------------------
INSERT INTO notifications (id, recipient_id, notification_type, title, body_text, `read`, read_at, created_at, expires_at) VALUES
(@n1,        @u_guard1,   'RELATIONSHIP_APPROVED',       'Guardian link approved',  'You are now linked to Tunde Eze.',           1, NOW(6), DATE_SUB(NOW(6), INTERVAL 20 DAY), NULL),
(UUID(),     @u_guard2,   'RELATIONSHIP_REQUEST',        'Link request pending',    'Your request to link Tunde Eze is pending.', 0, NULL,   NOW(6), NULL),
(UUID(),     @u_scout2,   'VERIFICATION_STATUS_CHANGE',  'Verification submitted',  'Your scout verification is under review.',   0, NULL,   NOW(6), NULL),
(UUID(),     @u_chinedu,  'DEVELOPMENT_UPDATE',          'New measurement verified','Your 30m sprint has been verified.',         0, NULL,   NOW(6), NULL),
(UUID(),     @u_org_club, 'BILLING_EVENT',               'Payment received',        'Thank you, your CLUB subscription payment was received.', 1, NOW(6), DATE_SUB(NOW(6), INTERVAL 10 DAY), NULL),
(UUID(),     @u_org_agcy, 'ORG_INVITATION',              'Scout seat assigned',     'Emeka Obi was assigned a scout seat.',       0, NULL,   NOW(6), NULL);

INSERT INTO notification_delivery_attempts (id, notification_id, channel, attempted_at, success) VALUES
(UUID(), @n1, 'IN_APP', DATE_SUB(NOW(6), INTERVAL 20 DAY), 1);

INSERT INTO notification_preferences (id, user_account_id, notification_type, email_enabled, in_app_enabled)
SELECT UUID(), @u_chinedu, t.type, 1, 1 FROM (
    SELECT 'DEVELOPMENT_UPDATE' AS type UNION ALL SELECT 'VERIFICATION_STATUS_CHANGE' UNION ALL SELECT 'BILLING_EVENT'
) t;

INSERT INTO email_outbox (id, recipient_email, subject, body_html, status, attempts, last_attempt_at, created_at, sent_at) VALUES
(UUID(), 'unverified@talenthub.dev', 'Verify your TalentHub email', '<p>Click to verify your email.</p>', 'PENDING', 0, NULL, NOW(6), NULL),
(UUID(), 'guardian1@talenthub.dev',  'Guardian link approved',      '<p>You are linked to Tunde.</p>',   'SENT',    1, NOW(6), DATE_SUB(NOW(6), INTERVAL 20 DAY), DATE_SUB(NOW(6), INTERVAL 20 DAY));

-- ---------------------------------------------------------------------------
-- safeguarding
-- ---------------------------------------------------------------------------
INSERT INTO consent_records (id, athlete_id, purpose_code, consent_version, consented_by, consented_at, revoked_at, revoked_by, authorization_source) VALUES
(UUID(), @u_tunde,   'PROFILE_DISCOVERY',   'v1', @u_guard1,  DATE_SUB(NOW(6), INTERVAL 20 DAY), NULL, NULL, 'GUARDIAN'),
(UUID(), @u_tunde,   'MEDIA_SHARING',       'v1', @u_guard1,  DATE_SUB(NOW(6), INTERVAL 20 DAY), DATE_SUB(NOW(6), INTERVAL 2 DAY), @u_guard1, 'GUARDIAN'),
(UUID(), @u_emeka,   'PROFILE_DISCOVERY',   'v1', @u_guard2,  DATE_SUB(NOW(6), INTERVAL 15 DAY), NULL, NULL, 'GUARDIAN'),
(UUID(), @u_chinedu, 'PERFORMANCE_SHARING', 'v1', @u_chinedu, DATE_SUB(NOW(6), INTERVAL 40 DAY), NULL, NULL, 'SELF'),
(UUID(), @u_amara,   'PROFILE_DISCOVERY',   'v1', @u_amara,   DATE_SUB(NOW(6), INTERVAL 40 DAY), NULL, NULL, 'SELF');

INSERT INTO athlete_privacy_preferences (id, athlete_id, field_group_code, visibility_level) VALUES
(UUID(), @u_chinedu, 'CONTACT_INFO',  'PRIVATE'),
(UUID(), @u_chinedu, 'PERFORMANCE',   'PUBLIC'),
(UUID(), @u_tunde,   'CONTACT_INFO',  'PRIVATE'),
(UUID(), @u_tunde,   'PERFORMANCE',   'ORGANIZATION');

INSERT INTO safeguarding_reports (id, reporter_id, reported_entity_id, reported_entity_type, report_type, description,
                                  status, submitted_at, resolved_at, resolved_by) VALUES
(UUID(), @u_guard1, @u_scout2, 'USER', 'INAPPROPRIATE_CONTACT', 'Unsolicited message to a minor (dev sample).', 'PENDING', NOW(6), NULL, NULL),
(UUID(), @u_amara,  @m2,       'MEDIA', 'INAPPROPRIATE_CONTENT', 'Reported video (dev sample).', 'RESOLVED', DATE_SUB(NOW(6), INTERVAL 6 DAY), DATE_SUB(NOW(6), INTERVAL 5 DAY), @u_admin);

INSERT INTO data_subject_requests (id, requester_id, request_type, status, submitted_at, resolved_at) VALUES
(UUID(), @u_ngozi,  'EXPORT', 'PENDING',   NOW(6), NULL),
(UUID(), @u_chinedu,'DELETE', 'COMPLETED', DATE_SUB(NOW(6), INTERVAL 30 DAY), DATE_SUB(NOW(6), INTERVAL 29 DAY));

INSERT INTO sensitive_access_audits (id, accessor_id, resource_type, resource_id, access_type, accessed_at, justification) VALUES
(UUID(), @u_admin, 'ATHLETE_PROFILE', @u_tunde, 'VIEW_MINOR_PROFILE', NOW(6), 'Guardian link verification');

-- ---------------------------------------------------------------------------
-- billing
-- ---------------------------------------------------------------------------
INSERT INTO payment_transactions (id, created_at, subscription_id, user_id, provider_code, provider_reference, amount,
                                  currency, status, initiated_at, completed_at, raw_provider_response_json) VALUES
(@tx1, NOW(6), @sub_club, @u_org_club, 'PAYSTACK', 'dev_ref_club_0001',   119999.00, 'NGN', 'COMPLETED', DATE_SUB(NOW(6), INTERVAL 10 DAY), DATE_SUB(NOW(6), INTERVAL 10 DAY), '{"status":"success"}'),
(@tx2, NOW(6), @sub_agcy, @u_org_agcy, 'PAYSTACK', 'dev_ref_agency_0001',  99.99,     'USD', 'COMPLETED', DATE_SUB(NOW(6), INTERVAL 10 DAY), DATE_SUB(NOW(6), INTERVAL 10 DAY), '{"status":"success"}'),
(@tx3, NOW(6), NULL,      @u_guard2,   'PAYSTACK', 'dev_ref_guardian_0001', 7500.00,  'NGN', 'FAILED',    DATE_SUB(NOW(6), INTERVAL 1 DAY),  NULL, '{"status":"failed","message":"Insufficient funds"}');

INSERT INTO payment_provider_events (id, provider_code, event_type, provider_event_id, payload_json, received_at, processed_at, status) VALUES
(UUID(), 'PAYSTACK', 'charge.success', 'dev_evt_0001', '{"reference":"dev_ref_club_0001"}',   DATE_SUB(NOW(6), INTERVAL 10 DAY), DATE_SUB(NOW(6), INTERVAL 10 DAY), 'PROCESSED'),
(UUID(), 'PAYSTACK', 'charge.failed',  'dev_evt_0002', '{"reference":"dev_ref_guardian_0001"}', DATE_SUB(NOW(6), INTERVAL 1 DAY),  NULL,                              'RECEIVED');

INSERT INTO billing_invoices (id, created_at, subscription_id, user_id, line_items_json, total_amount, currency, status,
                              issued_at, paid_at, invoice_number) VALUES
(UUID(), NOW(6), @sub_club, @u_org_club, '[{"description":"CLUB plan - monthly","amount":119999.00}]', 119999.00, 'NGN', 'PAID',
    DATE_SUB(NOW(6), INTERVAL 10 DAY), DATE_SUB(NOW(6), INTERVAL 10 DAY), 'INV-2026-0001'),
(UUID(), NOW(6), @sub_agcy, @u_org_agcy, '[{"description":"Scouting Agency plan - monthly","amount":99.99}]', 99.99, 'USD', 'PAID',
    DATE_SUB(NOW(6), INTERVAL 10 DAY), DATE_SUB(NOW(6), INTERVAL 10 DAY), 'INV-2026-0002'),
(UUID(), NOW(6), NULL, @u_guard2, '[{"description":"Parent / Guardian plan - monthly","amount":7500.00}]', 7500.00, 'NGN', 'OPEN',
    DATE_SUB(NOW(6), INTERVAL 1 DAY), NULL, 'INV-2026-0003');

INSERT INTO payment_attempts (id, transaction_id, attempt_number, attempted_at, outcome, error_code) VALUES
(UUID(), @tx1, 1, DATE_SUB(NOW(6), INTERVAL 10 DAY), 'SUCCESS', NULL),
(UUID(), @tx3, 1, DATE_SUB(NOW(6), INTERVAL 1 DAY),  'FAILED',  'insufficient_funds');

INSERT INTO payment_reconciliations (id, transaction_id, provider_reference, reconciled_at, match_status) VALUES
(UUID(), @tx1, 'dev_ref_club_0001', NOW(6), 'MATCHED');

INSERT INTO invoice_sequences (year, next_seq) VALUES (YEAR(NOW()), 4)
ON DUPLICATE KEY UPDATE next_seq = GREATEST(next_seq, 4);

-- ---------------------------------------------------------------------------
-- audit log
-- ---------------------------------------------------------------------------
INSERT INTO audit_logs (id, created_at, actor_id, actor_role, action, entity_type, entity_id, ip_address, user_agent) VALUES
(UUID(), DATE_SUB(NOW(6), INTERVAL 28 DAY), @u_admin,    'ADMIN',     'SCOUT_VERIFIED',     'ScoutProfile', @sp_scout1, '127.0.0.1', 'dev-seed'),
(UUID(), DATE_SUB(NOW(6), INTERVAL 20 DAY), @u_admin,    'ADMIN',     'GUARDIAN_LINK_VERIFIED', 'GuardianLink', @u_tunde, '127.0.0.1', 'dev-seed'),
(UUID(), DATE_SUB(NOW(6), INTERVAL 10 DAY), @u_org_club, 'ORG_ADMIN', 'SUBSCRIPTION_PAID',  'UserSubscription', @sub_club, '127.0.0.1', 'dev-seed');
