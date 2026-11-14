# TalentHub Features

TalentHub is a multi-sport athlete talent management and scouting platform. This backend is a Spring Boot 4.1.2 / Spring Modulith 2.1.1 modular monolith on Java with MySQL 8.4. This document lists what the platform does, who can do it, and which API endpoints expose it.

All endpoints are under `/api/v1`. Everything except `/api/v1/auth/**`, `/actuator/health`, `/actuator/info`, Swagger UI and `/v3/api-docs/**` requires a JWT bearer token.

Contents

1. [Platform overview](#1-platform-overview)
2. [User roles](#2-user-roles)
3. [Feature modules](#3-feature-modules)
4. [Role-to-feature matrix](#4-role-to-feature-matrix)
5. [Subscription plans and entitlements](#5-subscription-plans-and-entitlements)
6. [Cross-cutting capabilities](#6-cross-cutting-capabilities)
7. [Infrastructure and operations](#7-infrastructure-and-operations)
8. [Dev seed data](#8-dev-seed-data)
9. [Known gaps and notes](#9-known-gaps-and-notes)

---

## 1. Platform overview

| Area | What it gives you |
|---|---|
| Identity | Registration, email verification, login, token refresh, logout, password reset, MFA verification endpoint |
| Profiles | Base user profile for every role, plus athlete, scout and organization profiles |
| Athlete development | Sport-specific activity logging, performance measurements, milestones, verification |
| Scouting | Discovery search, favourites, watchlists, evaluations, saved searches, follows |
| Organizations | Clubs, schools, academies, federations and agencies with scouting workspaces and seats |
| Commercial | Subscription plans, entitlements, add-ons, invoices, payments through Paystack |
| Safeguarding | Consent management, guardian links for minors, reports, data subject requests, access audits |
| Media | Presigned uploads to MinIO, processing jobs, variants, playback, access audits |
| Notifications | In-app notifications, preferences and an email outbox with retry |
| Administration | Audit logs, admin-only configuration of activity types, templates and schemas |

---

## 2. User roles

Roles are defined by the `UserRole` enum.

| Role | Who it is | Typical purpose |
|---|---|---|
| `ATHLETE` | A player or competitor | Maintain a profile, log activities and measurements, control who sees their data |
| `GUARDIAN` | Parent or legal guardian of a minor | Manage a minor athlete's profile, consent and access |
| `SCOUT` | Talent scout, independent or agency | Search athletes, keep watchlists, write evaluations |
| `ORG_ADMIN` | Administrator of an organization | Run an organization, its athletes and its scouting workspace |
| `ADMIN` | Platform administrator | Verify scouts, review safeguarding reports, configure activity schemas |
| `SUPER_ADMIN` | Platform owner | Everything `ADMIN` can do |

Account statuses: `PENDING_VERIFICATION`, `ACTIVE`, `SUSPENDED`, `DEACTIVATED`.

Note: `VerificationController` also allows a `COACH` role, and the user guides describe Coach, Agent and Parent roles. `COACH` is not in the `UserRole` enum yet, so that check currently only passes for `ADMIN` and `SUPER_ADMIN`. See [Known gaps](#9-known-gaps-and-notes).

---

## 3. Feature modules

### 3.1 Identity and authentication (`identity`)

- Self-registration with email and password. Passwords are stored as BCrypt hashes (cost 12).
- Email verification through a time-limited token.
- Login returns a JWT access token (HMAC-SHA256, default 60 minutes) and an opaque refresh token. Refresh tokens are stored hashed (default expiry 30 days).
- Refresh token rotation and revocation, including logout of all sessions.
- Failed login attempt counting and temporary account lockout.
- Password reset by emailed token.
- MFA verification endpoint for admin accounts.

| Method | Path | Access |
|---|---|---|
| POST | `/auth/register` | Public |
| POST | `/auth/login` | Public |
| POST | `/auth/refresh` | Public |
| POST | `/auth/logout` | Public path, acts on the supplied token |
| GET | `/auth/verify-email` | Public |
| POST | `/auth/password-reset` | Public |
| POST | `/auth/password-reset/confirm` | Public |
| POST | `/auth/mfa/verify` | Public |

### 3.2 User profiles (`profile`)

- Every user has a base profile: first and last name, display name, date of birth, gender, phone, country, city, bio, avatar, role and a profile-complete flag.

| Method | Path | Access |
|---|---|---|
| GET | `/profiles/me` | Authenticated |
| PATCH | `/profiles/me` | Authenticated |
| GET | `/profiles/{userId}` | Authenticated |

### 3.3 Sports (`sport`)

- Ten seeded sports: Football (`FB`), Basketball (`BK`), Athletics (`AT`), Swimming (`SW`), Tennis (`TN`), Rugby (`RG`), Hockey (`HK`), Volleyball (`VB`), Boxing (`BX`), Golf (`GF`).
- Lookup by id or code.

| Method | Path | Access |
|---|---|---|
| GET | `/sports` | Authenticated |
| GET | `/sports/{id}` | Authenticated |
| GET | `/sports/code/{code}` | Authenticated |

### 3.4 Athlete profiles (`athlete`)

- One athlete profile per athlete user: primary sport, position, height, weight, dominant foot, nationality, status (`ACTIVE`, `INACTIVE`, `SUSPENDED`, `RETIRED`) and optional organization.
- Unique athlete numbers, generated under a database lock so concurrent signups never collide:
  - Unaffiliated athletes: `TH-00001`
  - Organization athletes: `ORGCODE-SPORTCODE-00001`, for example `LCFC-FB-00001`
- Lookup by user id or by athlete number.

| Method | Path | Access |
|---|---|---|
| POST | `/athletes/me` | Athlete |
| GET | `/athletes/me` | Athlete |
| PATCH | `/athletes/me` | Athlete |
| GET | `/athletes/user/{userId}` | Authenticated, subject to access grants |
| GET | `/athletes/number/{athleteNumber}` | Authenticated, subject to access grants |

### 3.5 Guardians (`guardian`)

- Guardian-to-athlete links with a relationship type and status (`PENDING`, `ACTIVE`, `REVOKED`, `REJECTED`).
- Consent flag and timestamp on each link, and the id of the admin who verified it.
- An active link lets the guardian manage a minor athlete's data.

### 3.6 Organizations (`organization`)

- Nine organization types: `CLUB`, `SCHOOL`, `ACADEMY`, `FEDERATION`, `NATIONAL_TEAM`, `UNIVERSITY`, `COLLEGE`, `PROFESSIONAL_TEAM`, `SCOUTING_AGENCY`.
- Each organization has a short unique code used in athlete numbers, an admin user, a location, a logo and a status (`ACTIVE`, `SUSPENDED`, `PENDING_VERIFICATION`, `DEACTIVATED`).

### 3.7 Athlete access control (`athleteaccess`)

- Fine-grained grants of access to an athlete's data.
- Permissions: `VIEW_PROFILE`, `VIEW_STATS`, `VIEW_DOCUMENTS`, `CONTACT`, `MANAGE`.
- Grantee types: `USER`, `ORGANIZATION`, `SCOUT`.
- Grants can expire and can be deactivated. An athlete always has access to their own data.

### 3.8 Activity configuration (`activityconfiguration`)

A configurable schema system that defines what data each kind of activity captures.

- Eleven activity types: `MATCH`, `TRAINING`, `TRIAL`, `PERSONAL_TRAINING`, `COMPETITION`, `FITNESS_ASSESSMENT`, `SKILLS_ASSESSMENT`, `TRAINING_CAMP`, `SHOWCASE`, `COMBINE`, `OTHER`.
- Templates per activity type and optional sport, for example `FOOTBALL_MATCH`, `AMFOOTBALL_COMBINE`, `PERSONAL_TRAINING_SESSION`.
- Versioned schemas with status `DRAFT`, `PUBLISHED` or `DEPRECATED`.
- Field definitions with 12 field types: `TEXT`, `LONG_TEXT`, `INTEGER`, `DECIMAL`, `DATE`, `DATETIME`, `DURATION`, `BOOLEAN`, `SINGLE_SELECT`, `MULTI_SELECT`, `MEASUREMENT`, `ENTITY_REFERENCE`.
- Field options for select fields and validation rules (`MIN`, `MAX`, `REGEX`, `REQUIRED_IF`, `CUSTOM`).

| Method | Path | Access |
|---|---|---|
| GET | `/activity-types/{id}/schema` | Authenticated |
| GET | `/sports/{id}/activity-types` | Authenticated |
| POST | `/admin/activity-types` | Admin, Super Admin |
| POST | `/admin/activity-templates` | Admin, Super Admin |
| POST | `/admin/activity-templates/{id}/fields` | Admin, Super Admin |
| POST | `/admin/activity-schema-versions/{id}/publish` | Admin, Super Admin |

### 3.9 Athlete activities (`activity`)

- Athletes log activities against a published schema version. Submitted values are validated against the schema's field definitions.
- Each activity holds: title, description, date, location, optional organization, field values, performances, media, participants and a revision history.
- Lifecycle status: `DRAFT`, `SUBMITTED`, `PUBLISHED`, `UNDER_REVIEW`, `REJECTED`, `ARCHIVED`.
- Visibility: `PUBLIC`, `ORGANIZATION`, `MANAGERS`, `PRIVATE`.
- Verification status: `UNVERIFIED`, `PENDING`, `VERIFIED`, `REJECTED`.
- Participants: `COACH`, `TRAINER`, `OPPONENT`, `TEAMMATE`.
- Domain events are published when an activity is submitted or published, so discovery and notifications can react.
- A legacy `activity_sessions` log is kept for backward compatibility.

| Method | Path | Access |
|---|---|---|
| POST | `/athletes/{id}/activities` | Athlete, or a user with access |
| GET | `/athletes/{id}/activities` | Authenticated, subject to access grants |
| GET | `/activities/{id}` | Authenticated, subject to visibility |
| PATCH | `/activities/{id}` | Owner |
| POST | `/activities/{id}/submit` | Owner |
| POST | `/activities/{id}/publish` | Owner |

### 3.10 Verification (`verification`)

- A verifier confirms an activity, which records who verified it, when, and optional notes.

| Method | Path | Access |
|---|---|---|
| POST | `/activities/{id}/verify` | Admin, Super Admin (and `COACH` once that role exists) |

### 3.11 Development tracking (`development`)

- Performance measurements with metric code, value, unit, observation date and context.
- Measurement source: `SELF_REPORTED`, `COACH_VERIFIED`, `SYSTEM`.
- Verification status and a verification trail (who, when, method, notes).
- Corrections reference the measurement they replace, so history is preserved.
- Development milestones: `FIRST_APPEARANCE`, `PERSONAL_BEST`, `ACHIEVEMENT`, `LEVEL_UP`, `OTHER`, optionally linked to evidence activities.

| Method | Path | Access |
|---|---|---|
| GET | `/athletes/{id}/development` | Authenticated, subject to access grants |
| GET | `/athletes/{id}/performance` | Authenticated, subject to access grants |
| GET | `/activities/{id}/measurements` | Authenticated |
| POST | `/athletes/{id}/measurements` | Athlete, or a user with access |

### 3.12 Media (`media`)

- Upload flow: request a presigned URL, upload straight to MinIO, then mark the upload complete.
- Asset types: `PROFILE_IMAGE`, `ACTIVITY_VIDEO`, `VIDEO_THUMBNAIL`, `ORG_LOGO`, `VERIFICATION_DOCUMENT`.
- Owner types: `ATHLETE`, `ORGANIZATION`, `USER`.
- Processing states: `INITIATED`, `UPLOADED`, `VALIDATING`, `PROCESSING`, `READY`, `FAILED`, `REJECTED`, `DELETED`.
- Background jobs: `VIDEO_TRANSCODE`, `THUMBNAIL_GENERATE`, `IMAGE_OPTIMIZE`.
- Variants: `ORIGINAL`, `HLS`, `MP4_720P`, `THUMBNAIL`.
- Every access to an asset is written to an access audit.
- Three buckets: athlete media, organization media and private documents. Maximum video size is configurable (500 MB in dev).
- Media can be attached to activities.

| Method | Path | Access |
|---|---|---|
| POST | `/media/uploads` | Authenticated |
| POST | `/media/uploads/{id}/complete` | Authenticated |
| GET | `/media/{id}` | Authenticated, subject to access |
| GET | `/media/{id}/playback` | Authenticated, subject to access |
| DELETE | `/media/{id}` | Owner |
| POST | `/activities/{id}/media` | Activity owner |

### 3.13 Scout profiles (`scout`)

- Scout profile with title, biography, years of experience, optional organization and verification status (`UNVERIFIED`, `PENDING`, `VERIFIED`, `REJECTED`).
- Specializations per sport with a level (`YOUTH`, `AMATEUR`, `SEMI_PRO`, `PROFESSIONAL`, `ELITE`) and preferred regions.
- Verification requests with a supporting document, reviewed by an admin.

| Method | Path | Access |
|---|---|---|
| POST | `/scouts` | Scout |
| GET | `/scouts/{id}` | Authenticated |
| POST | `/scouts/verification-requests` | Scout |
| POST | `/admin/scouts/{id}/verify` | Admin, Super Admin |
| POST | `/admin/scouts/{id}/reject` | Admin, Super Admin |

### 3.14 Athlete discovery (`discovery`)

- Searchable projection of athletes, kept in sync by activity events.
- Filters include sport, country, state or region, age category (`YOUTH`, `JUNIOR`, `SENIOR`, `VETERAN`), verification status and last activity date.
- Respects each athlete's visibility level.
- Scouts can save searches and reuse them.

| Method | Path | Access |
|---|---|---|
| GET | `/discovery/athletes` | Scout |
| GET | `/discovery/athletes/{id}` | Scout |
| POST | `/discovery/saved-searches` | Scout |
| GET | `/discovery/saved-searches` | Scout |

### 3.15 Scouting (`scouting`)

- Favourites with notes, limited by plan (`MAX_FAVOURITES`).
- Watchlists owned by a scout or an organization, optionally shared, limited by plan (`MAX_WATCHLISTS`).
- Athlete follows with optional notifications.
- Evaluations: per-sport criteria (Football and Basketball are seeded), a score for each criterion out of 10, an overall rating and a status of `DRAFT` or `COMPLETED`. Evaluations need an active plan.
- Private scout notes about athletes.

| Method | Path | Access |
|---|---|---|
| POST | `/scouting/favourites` | Scout |
| DELETE | `/scouting/favourites` | Scout |
| GET | `/scouting/favourites` | Scout |
| POST | `/scouting/watchlists` | Scout |
| GET | `/scouting/watchlists/{id}` | Scout |
| POST | `/scouting/watchlists/{id}/athletes` | Scout |
| DELETE | `/scouting/watchlists/{id}/athletes/{athleteId}` | Scout |
| POST | `/scouting/evaluations` | Scout |
| POST | `/scouting/evaluations/{id}/complete` | Scout |
| GET | `/scouting/evaluations` | Scout |

### 3.16 Organization scouting workspace (`scouting`)

- An organization on a qualifying plan can activate a shared scouting workspace.
- Scout seats can be assigned to scouts, limited by plan (`MAX_SCOUT_SEATS`).
- Shared watchlists and evaluations are visible to the organization's scouts.

| Method | Path | Access |
|---|---|---|
| POST | `/organizations/{orgId}/scouting/workspace/activate` | Org Admin, Admin, Super Admin |
| POST | `/organizations/{orgId}/scouting/seats` | Org Admin, Admin, Super Admin |
| GET | `/organizations/{orgId}/scouting/watchlists` | Org Admin, Scout, Admin, Super Admin |
| GET | `/organizations/{orgId}/scouting/evaluations` | Org Admin, Scout, Admin, Super Admin |

### 3.17 Subscriptions (`subscription`)

- Eleven plans plus the legacy free tier, each with entitlements, trial days and prices in USD and NGN (see [section 5](#5-subscription-plans-and-entitlements)).
- Subscription statuses: `ACTIVE`, `EXPIRED`, `CANCELLED`, `PENDING`, `SUSPENDED`. Billing periods: `MONTHLY`, `ANNUAL`.
- Grace period, cancellation reason and renewal date are tracked.
- Add-ons with quantities, and seats for organization scouting.
- A scheduled job (daily at 01:00) handles subscription lifecycle changes.
- `EntitlementService` answers `getLimit`, `isWithinLimit`, `assertWithinLimit` and `hasActivePlan`. A subscriber with no active plan falls back to free-tier limits.

| Method | Path | Access |
|---|---|---|
| GET | `/subscription-plans` | Authenticated |
| POST | `/subscriptions` | Authenticated |
| GET | `/subscriptions/me` | Authenticated |
| POST | `/subscriptions/{id}/cancel` | Subscriber |
| GET | `/subscriptions/{id}/addons` | Subscriber |

### 3.18 Billing and payments (`billing`)

- Payment gateway abstraction with Paystack as the first provider (`payment.provider=paystack`).
- Payment transactions (`PENDING`, `COMPLETED`, `FAILED`, `REFUNDED`), attempts and reconciliations.
- Webhook ingestion with idempotency on provider and event id.
- Invoices with numbering by year sequence, for example `INV-2026-0001`.

| Method | Path | Access |
|---|---|---|
| POST | `/payments/webhooks/{provider}` | Provider callback |

### 3.19 Notifications (`notification`)

- In-app notifications with read state and expiry.
- Types: `EMAIL_VERIFICATION`, `PASSWORD_RESET`, `RELATIONSHIP_REQUEST`, `RELATIONSHIP_APPROVED`, `RELATIONSHIP_REVOKED`, `ORG_INVITATION`, `BILLING_EVENT`, `DEVELOPMENT_UPDATE`, `VERIFICATION_STATUS_CHANGE`.
- Per-user, per-type preferences for email and in-app delivery.
- An email outbox with retry runs every minute and records delivery attempts. Local mail goes to MailHog.

| Method | Path | Access |
|---|---|---|
| GET | `/notifications` | Authenticated |
| PATCH | `/notifications/{id}/read` | Recipient |
| GET | `/notifications/preferences` | Authenticated |
| PUT | `/notifications/preferences` | Authenticated |

### 3.20 Safeguarding (`safeguarding`)

- Consent records per athlete and purpose: `PROFILE_DISCOVERY`, `PERFORMANCE_SHARING`, `MEDIA_SHARING`, `RESEARCH`. Consent can be revoked, and the record keeps who gave or revoked it and whether it came from the athlete or a guardian.
- Per-field-group privacy preferences.
- Safeguarding reports, reviewed by admins.
- Data subject requests such as export and deletion.
- Audit trail for access to sensitive data.

| Method | Path | Access |
|---|---|---|
| POST | `/athletes/{id}/consent` | Athlete or guardian |
| DELETE | `/athletes/{id}/consent/{purpose}` | Athlete or guardian |
| POST | `/safeguarding/reports` | Authenticated |
| GET | `/safeguarding/reports` | Authenticated |
| POST | `/admin/safeguarding/reports/{id}/review` | Admin, Super Admin |

### 3.21 Administration (`administration`)

- Audit log of significant actions with actor, role, entity, before and after state, IP and user agent. Indexed for admin reporting.
- Admin-only configuration endpoints are listed under activity configuration, scout verification and safeguarding review.

---

## 4. Role-to-feature matrix

`Y` means the role can use the feature. `Own` means only for their own data. `Granted` means only where an access grant allows it. `-` means no access.

| Feature | Athlete | Guardian | Scout | Org Admin | Admin | Super Admin |
|---|---|---|---|---|---|---|
| Register, log in, reset password | Y | Y | Y | Y | Y | Y |
| Edit own profile | Y | Y | Y | Y | Y | Y |
| Browse sports and activity schemas | Y | Y | Y | Y | Y | Y |
| Athlete profile | Own | Granted | Granted | Granted | Y | Y |
| Log activities and measurements | Own | Granted | - | Granted | Y | Y |
| Upload media | Y | Y | Y | Y | Y | Y |
| Verify activities | - | - | - | - | Y | Y |
| Manage consent | Own | Granted | - | - | Y | Y |
| File safeguarding report | Y | Y | Y | Y | Y | Y |
| Review safeguarding reports | - | - | - | - | Y | Y |
| Scout profile and verification request | - | - | Y | - | - | - |
| Verify or reject scouts | - | - | - | - | Y | Y |
| Discovery search, favourites, watchlists, evaluations | - | - | Y | - | - | - |
| Org scouting workspace and seats | - | - | - | Y | Y | Y |
| View org watchlists and evaluations | - | - | Y | Y | Y | Y |
| Subscribe, cancel, view add-ons | Y | Y | Y | Y | Y | Y |
| Configure activity types, templates, schemas | - | - | - | - | Y | Y |

---

## 5. Subscription plans and entitlements

Prices are monthly. NGN prices are seeded for the paid public plans.

| Plan code | Target | USD | Trial | Key entitlements |
|---|---|---|---|---|
| `FREE_TIER` | Platform default | 0.00 | - | 5 athletes, 0 scouts, 3 video uploads, 10 org members, 120 s video |
| `ATHLETE_FREE` | Athlete | 0.00 | - | 3 video uploads |
| `PARENT_GUARDIAN` | Guardian | 4.99 | 14 days | 5 athletes |
| `SCOUT_INDIVIDUAL` | Scout | 9.99 | 14 days | 50 favourites, 5 watchlists |
| `SCOUT_PRO` | Scout | 19.99 | 14 days | 200 favourites, 20 watchlists, evaluations, advanced search |
| `COACH_AGENT` | Coach | 14.99 | 14 days | 50 athletes |
| `SCHOOL` | Organization | 29.99 | 30 days | 100 athletes, 20 members |
| `ACADEMY` | Organization | 49.99 | 30 days | 300 athletes, 50 members, shared scouting workspace |
| `CLUB` | Organization | 79.99 | 30 days | 500 athletes, 100 members, shared workspace, 5 scout seats |
| `SCOUTING_AGENCY` | Organization | 99.99 | 30 days | 100 watchlists, evaluations, advanced search, 20 scout seats |
| `FEDERATION` | Organization | 199.99 | 30 days | 2000 athletes, 500 members, shared workspace (not public) |
| `ENTERPRISE` | Organization | custom | - | Effectively unlimited (not public) |

Entitlement keys enforced in code today: `MAX_FAVOURITES`, `MAX_WATCHLISTS`, `MAX_SCOUT_SEATS`, plus the active-plan checks for evaluations and the org scouting workspace.

---

## 6. Cross-cutting capabilities

- **Security:** stateless JWT authentication, role checks with `@PreAuthorize`, and security headers (`X-Content-Type-Options`, `X-Frame-Options: DENY`, `Content-Security-Policy`). Unauthenticated calls get 401 and forbidden calls get 403.
- **Audit:** `AuditableEntity` base class stores `created_at`, `updated_at`, `created_by` and `updated_by`. A separate audit log records business actions.
- **Events:** modules communicate through Spring Modulith application events, for example `AthleteActivitySubmittedEvent` and `AthleteActivityPublishedEvent`. Public interfaces such as `AthleteApi` and `OrganizationApi` are the only cross-module entry points.
- **Identifiers:** UUID primary keys stored as `CHAR(36)`.
- **Errors:** shared exceptions for duplicates, missing entities, invalid state transitions, entitlement limits and safeguarding violations.
- **Pagination:** list endpoints return a shared `PageResponse` wrapper.
- **API documentation:** OpenAPI specs live in `docs/` (`openapi-v1-phase1.json` to `phase3.json` and `openapi-v1-final.json`). Swagger UI is served when enabled.
- **Role guides:** per-role guides with curl examples are in `docs/user-guides/`.

---

## 7. Infrastructure and operations

| Concern | Choice |
|---|---|
| Runtime | Spring Boot 4.1.2, Spring Modulith 2.1.1, Jackson 3 (`tools.jackson`) |
| Database | MySQL 8.4, schema managed by Flyway (`V1` to `V24`) |
| Object storage | MinIO (S3 compatible), three buckets |
| Email | SMTP (MailHog in local Docker setups) |
| Payments | Paystack behind a `PaymentGatewayProvider` abstraction |
| Observability | Spring Boot Actuator, health and info are public |
| Scheduled jobs | Email outbox every 60 s, subscription lifecycle daily at 01:00 |
| Local setup | `docker-compose.dev.yml` (MySQL, MinIO, MailHog) |
| Production setup | `docker-compose.prod.yml` with a `.env` file (see `.env.example`) |
| Profiles | `dev` (default) and `prod` |

Key settings: `jwt.secret`, `jwt.access-token.expiry-minutes`, `jwt.refresh-token.expiry-days`, `minio.*`, `media.max-video-size-bytes`, `payment.provider`.

---

## 8. Dev seed data

With the `dev` profile, Flyway also loads `db/dev-seed/V1000__dev_seed_data.sql`. It is never loaded under `prod`. All seeded accounts use the password `Password123!`.

| Email | Role | Notes |
|---|---|---|
| `superadmin@talenthub.dev` | `SUPER_ADMIN` | |
| `admin@talenthub.dev` | `ADMIN` | |
| `orgadmin.club@talenthub.dev` | `ORG_ADMIN` | Lagos City FC (`CLUB`) |
| `orgadmin.academy@talenthub.dev` | `ORG_ADMIN` | Elite Hoops Academy (`ACADEMY`) |
| `orgadmin.agency@talenthub.dev` | `ORG_ADMIN` | Prime Scouts Agency (`SCOUTING_AGENCY`) |
| `scout.verified@talenthub.dev` | `SCOUT` | Verified, Scout Pro, agency seat |
| `scout.pending@talenthub.dev` | `SCOUT` | Verification pending |
| `guardian1@talenthub.dev` | `GUARDIAN` | Linked to Tunde |
| `guardian2@talenthub.dev` | `GUARDIAN` | Linked to Emeka, pending link to Tunde, failed payment |
| `athlete.chinedu@talenthub.dev` | `ATHLETE` | Adult, unaffiliated, football |
| `athlete.amara@talenthub.dev` | `ATHLETE` | Adult, Lagos City FC, football |
| `athlete.tunde@talenthub.dev` | `ATHLETE` | Minor, Lagos City FC |
| `athlete.zainab@talenthub.dev` | `ATHLETE` | Adult, Elite Hoops Academy, basketball |
| `athlete.emeka@talenthub.dev` | `ATHLETE` | Minor, unaffiliated, athletics |
| `athlete.ngozi@talenthub.dev` | `ATHLETE` | Adult, unaffiliated, swimming |
| `suspended@talenthub.dev` | `ATHLETE` | Status `SUSPENDED` |
| `unverified@talenthub.dev` | `ATHLETE` | Status `PENDING_VERIFICATION` |

The seed also fills activities in every lifecycle state, measurements, milestones, search projections, favourites, watchlists, evaluations, notifications, consent records, safeguarding reports, transactions and invoices.

---

## 9. Known gaps and notes

- **`COACH` role:** referenced by `VerificationController` and the user guides, but not in `UserRole`. Adding it, or removing the reference, is an open decision. The same applies to Agent and Parent roles named in the guides.
- **Subscriber keys:** entitlement checks use different keys in different places. Scouting checks use the scout profile id, and organization scouting uses the organization id. The dev seed subscribes both ids for scouts and the organization id for organizations.
- **Planned and partial items:** the implementation plan lists MFA for admins using TOTP, custom Micrometer counters, and Phase 3 and Phase 4 acceptance tests as still pending when this was written. Check `docs/IMPLEMENTATION_PLAN.md` for current status.
- **Dev schema mode:** `application-dev.properties` currently sets `spring.jpa.hibernate.ddl-auto=update`. Flyway is the source of truth, so `validate` is the safer setting once the schema settles.
- **Secrets:** `application-dev.properties` contains a mail password default. Move it to an environment variable before sharing the repository.
- **Sources:** this document was built from the controllers, enums, Flyway migrations and `SecurityConfig` in the repository. Per-endpoint access for the "Authenticated, subject to access grants" entries depends on service-layer checks that were not individually reviewed.
