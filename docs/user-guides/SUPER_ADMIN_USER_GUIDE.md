# Super Admin User Guide

## 1. Role Overview

Super administrators (SUPER_ADMIN role) have unrestricted access to all platform functions. In addition to everything an ADMIN can do, super admins can:

- Suspend or reactivate other admin accounts
- Create ADMIN accounts and change any user's role
- Access audit logs across all users without scope restriction
- Manage subscription plans, pricing and entitlement limits
- Configure sports, disciplines, and performance metrics
- Publish and deprecate activity schema versions

SUPER_ADMIN accounts should be kept to a minimum. Every super admin action is recorded in the audit log.

---

## 2. Getting Started

Super admin accounts are created during platform setup (seed data) or by another SUPER_ADMIN promoting a user with `PUT /admin/users/{id}/role`. Nobody can register as `SUPER_ADMIN`; `POST /auth/register` rejects it with `400`.

### Step 1 — Login

```bash
curl -X POST '{{BASE_URL}}/auth/login' \
  -H 'Content-Type: application/json' \
  -d '{
    "email": "superadmin@talenthub.io",
    "password": "Sup3rAdm!n#2024!"
  }'
```

**Response (200 OK):**
```json
{
  "access_token": "eyJhbGciOiJIUzI1NiJ9...",
  "refresh_token": "a9b0c1d2-e3f4-5678-9012-34567890abcd",
  "user_id": "5u9e7a0d-0000-4000-8000-000000000001",
  "email": "superadmin@talenthub.io",
  "role": "SUPER_ADMIN",
  "expires_in": 3600
}
```

### Step 2 — Verify a TOTP code (optional)

`POST /auth/mfa/verify` checks a TOTP code against a base64 secret. It needs a valid token.

```bash
curl -X POST '{{BASE_URL}}/auth/mfa/verify' \
  -H 'Authorization: Bearer {{ACCESS_TOKEN}}' \
  -H 'Content-Type: application/json' \
  -d '{
    "secret": "<base64-encoded-totp-secret>",
    "code": "847291"
  }'
```

**Response (200 OK):**
```json
{
  "verified": true,
  "message": "MFA verification successful."
}
```

A wrong or expired code returns `401` with the standard error envelope and the message `The TOTP code is invalid or has expired.`

> **Note:** MFA is not enforced yet. Logging in does not require this step, and no endpoint checks that it was done. The client also has to supply the secret, because the server does not store one per account.

### Token Refresh

```bash
curl -X POST '{{BASE_URL}}/auth/refresh' \
  -H 'Content-Type: application/json' \
  -d '{ "refresh_token": "a9b0c1d2-e3f4-5678-9012-34567890abcd" }'
```

### Logout

```bash
curl -X POST '{{BASE_URL}}/auth/logout' \
  -H 'Authorization: Bearer {{ACCESS_TOKEN}}'
```

---

## 3. Authentication

Super admins use the same JWT authentication as other roles. Every route in this guide checks for the `SUPER_ADMIN` role and returns `403` to an `ADMIN`. All ADMIN routes (ADMIN_USER_GUIDE.md) are open to you as well. Every action below is recorded in the audit log.

---

## 4. Core Workflows

### All ADMIN workflows are available — see ADMIN_USER_GUIDE.md

The differences for you:
- You can suspend and reactivate accounts that hold `ADMIN` or `SUPER_ADMIN`.
- `GET /admin/audit-logs` shows every actor, and you can filter by `actor`.

### Workflow A — Manage subscription plans

#### List every plan, including inactive ones

```bash
curl -X GET '{{BASE_URL}}/admin/subscription-plans' \
  -H 'Authorization: Bearer {{ACCESS_TOKEN}}'
```

**Response (200 OK):** an array of plans, as below. `GET /admin/subscription-plans/{id}` returns one.

#### Create a new plan

```bash
curl -X POST '{{BASE_URL}}/admin/subscription-plans' \
  -H 'Authorization: Bearer {{ACCESS_TOKEN}}' \
  -H 'Content-Type: application/json' \
  -d '{
    "code": "SCOUT_ELITE",
    "name": "Scout Elite",
    "targetRole": "SCOUT",
    "pricePerMonth": 49.00,
    "currency": "USD",
    "entitlements": {
      "MAX_FAVOURITES": 500,
      "MAX_WATCHLISTS": 50
    },
    "prices": [
      { "currency": "USD", "amount": 49.00, "billingPeriod": "MONTHLY" },
      { "currency": "USD", "amount": 490.00, "billingPeriod": "ANNUAL" }
    ]
  }'
```

- `code` must be upper case letters, digits and underscores. An existing code returns `409`.
- `targetRole` is `ATHLETE`, `GUARDIAN`, `SCOUT`, `ORGANIZATION` or `PLATFORM`.
- `entitlements` maps an entitlement key to its limit. Keys you leave out are unlimited for this plan.
- `prices` and `entitlements` are optional.

**Response (201 Created):**
```json
{
  "id": "plan-0009",
  "code": "SCOUT_ELITE",
  "name": "Scout Elite",
  "targetRole": "SCOUT",
  "pricePerMonth": 49.00,
  "currency": "USD",
  "active": true,
  "entitlements": { "MAX_FAVOURITES": 500, "MAX_WATCHLISTS": 50 },
  "prices": [
    { "id": "pp-0001", "currency": "USD", "amount": 49.00, "billingPeriod": "MONTHLY", "active": true },
    { "id": "pp-0002", "currency": "USD", "amount": 490.00, "billingPeriod": "ANNUAL", "active": true }
  ]
}
```

#### Update an existing plan

```bash
curl -X PATCH '{{BASE_URL}}/admin/subscription-plans/plan-0009' \
  -H 'Authorization: Bearer {{ACCESS_TOKEN}}' \
  -H 'Content-Type: application/json' \
  -d '{
    "pricePerMonth": 45.00,
    "entitlements": { "MAX_FAVOURITES": 1000, "MAX_WATCHLISTS": 100 }
  }'
```

All fields are optional: `name`, `pricePerMonth`, `currency`, `entitlements`, `prices`, `active`. If you send `entitlements` or `prices`, they replace the old values. **Response (200 OK):** the updated plan.

#### Deactivate a plan

```bash
curl -X POST '{{BASE_URL}}/admin/subscription-plans/plan-0009/deactivate' \
  -H 'Authorization: Bearer {{ACCESS_TOKEN}}'
```

**Response (200 OK):** the plan with `active: false`. The plan disappears from `GET /subscription-plans`, but existing subscriptions keep running. To reactivate it, send `PATCH` with `{ "active": true }`.

### Workflow B — Configure sports

#### Create a new sport

```bash
curl -X POST '{{BASE_URL}}/admin/sports' \
  -H 'Authorization: Bearer {{ACCESS_TOKEN}}' \
  -H 'Content-Type: application/json' \
  -d '{
    "code": "RG7",
    "name": "Rugby Sevens",
    "description": "Seven-a-side rugby union"
  }'
```

`code` is upper case, at most 10 characters. An existing code returns `409`.

**Response (201 Created):**
```json
{
  "id": "sp-0011",
  "code": "RG7",
  "name": "Rugby Sevens",
  "description": "Seven-a-side rugby union",
  "active": true
}
```

#### Add discipline to a sport

```bash
curl -X POST '{{BASE_URL}}/admin/sports/sp-0011/disciplines' \
  -H 'Authorization: Bearer {{ACCESS_TOKEN}}' \
  -H 'Content-Type: application/json' \
  -d '{ "code": "WOMENS_7S", "name": "Women'"'"'s Sevens" }'
```

**Response (201 Created):**
```json
{ "id": "sd-0001", "sportId": "sp-0011", "code": "WOMENS_7S", "name": "Women's Sevens", "active": true }
```

#### Add performance metric to a sport

```bash
curl -X POST '{{BASE_URL}}/admin/sports/sp-0011/metrics' \
  -H 'Authorization: Bearer {{ACCESS_TOKEN}}' \
  -H 'Content-Type: application/json' \
  -d '{ "code": "SPRINT_40M", "name": "40m sprint", "unit": "s", "higherIsBetter": false }'
```

**Response (201 Created):**
```json
{ "id": "sm-0001", "sportId": "sp-0011", "code": "SPRINT_40M", "name": "40m sprint", "unit": "s", "higherIsBetter": false, "active": true }
```

A duplicate code within the same sport returns `409`. Anyone signed in can read them with `GET /sports/{id}/disciplines` and `GET /sports/{id}/metrics`.

### Workflow C — Manage activity schemas

Admins can do the first three steps. Publishing and deprecating are yours only.

#### Create an activity type

```bash
curl -X POST '{{BASE_URL}}/admin/activity-types' \
  -H 'Authorization: Bearer {{ACCESS_TOKEN}}' \
  -H 'Content-Type: application/json' \
  -d '{
    "code": "SHOWCASE",
    "name": "Showcase",
    "description": "Invitation-only showcase event"
  }'
```

`code` is required and must be one of `MATCH`, `TRAINING`, `TRIAL`, `PERSONAL_TRAINING`, `COMPETITION`, `FITNESS_ASSESSMENT`, `SKILLS_ASSESSMENT`, `TRAINING_CAMP`, `SHOWCASE`, `COMBINE` or `OTHER`. An existing code returns `409`.

**Response (201 Created):**
```json
{
  "id": "at-0011",
  "code": "SHOWCASE",
  "name": "Showcase",
  "description": "Invitation-only showcase event",
  "active": true
}
```

#### Create an activity template

```bash
curl -X POST '{{BASE_URL}}/admin/activity-templates' \
  -H 'Authorization: Bearer {{ACCESS_TOKEN}}' \
  -H 'Content-Type: application/json' \
  -d '{
    "activityTypeId": "at-0011",
    "sportId": "sp-0011",
    "name": "Rugby 7s Showcase",
    "description": "Template for sevens showcase events"
  }'
```

**Response (201 Created):** the template (`id`, `activityTypeId`, `sportId`, `code`, `name`, `description`, `active`). Schema version 1 is created in `DRAFT` at the same time.

#### Add fields

```bash
curl -X POST '{{BASE_URL}}/admin/activity-templates/tmpl-0021/fields' \
  -H 'Authorization: Bearer {{ACCESS_TOKEN}}' \
  -H 'Content-Type: application/json' \
  -d '{ "fieldCode": "triesScored", "fieldLabel": "Tries Scored", "fieldType": "INTEGER", "required": false, "displayOrder": 1 }'
```

Each call adds one field to the template's current `DRAFT` version. If the latest version is already published, a new draft is created first, with its fields copied. `fieldType` values include `TEXT`, `LONG_TEXT`, `INTEGER`, `DECIMAL`, `DATE`, `DATETIME`, `DURATION`, `BOOLEAN`, `SINGLE_SELECT` and `MULTI_SELECT`.

#### Publish the schema version

Find the draft's id:

```bash
curl -X GET '{{BASE_URL}}/admin/activity-templates/tmpl-0021/schema-versions' \
  -H 'Authorization: Bearer {{ACCESS_TOKEN}}'
```

Then publish it:

```bash
curl -X POST '{{BASE_URL}}/admin/activity-schema-versions/sv-0031/publish' \
  -H 'Authorization: Bearer {{ACCESS_TOKEN}}'
```

**Response (200 OK):** the version with `status: "PUBLISHED"`, `publishedAt` and `publishedBy` (your user id). Publishing a version that is not `DRAFT` returns `409`. Retire a published version with `POST /admin/activity-schema-versions/{id}/deprecate`.

### Workflow D — Create an admin account

```bash
curl -X POST '{{BASE_URL}}/admin/users' \
  -H 'Authorization: Bearer {{ACCESS_TOKEN}}' \
  -H 'Content-Type: application/json' \
  -d '{ "email": "admin.seun@talenthub.io", "password": "T3mpAdm!n#2024" }'
```

**Response (201 Created):** the new account with `role: "ADMIN"`, `status: "ACTIVE"` and `emailVerified: true`. An existing email returns `409`. Share the password securely and ask the admin to change it with the password reset flow.

### Workflow E — Change a user's role

```bash
curl -X PUT '{{BASE_URL}}/admin/users/u5c0u7aa-0000-4000-8000-000000000001/role' \
  -H 'Authorization: Bearer {{ACCESS_TOKEN}}' \
  -H 'Content-Type: application/json' \
  -d '{ "role": "ADMIN" }'
```

**Response (200 OK):**
```json
{
  "userId": "u5c0u7aa-0000-4000-8000-000000000001",
  "email": "kwame.mensah@example.com",
  "role": "ADMIN",
  "roles": ["ADMIN", "SCOUT"],
  "status": "ACTIVE",
  "emailVerified": true,
  "createdAt": "2024-05-02T08:00:00Z",
  "lastLoginAt": "2024-06-15T07:45:00Z"
}
```

- The call replaces the primary role and keeps the user's other roles.
- Demoting an `ADMIN` or `SUPER_ADMIN` also removes those roles from the additional roles.
- The user's refresh tokens are revoked, so the change takes effect at their next login.
- You cannot change your own role (`400`).
- The audit log records the change as `USER_ROLE_CHANGED`, with `details` such as `SCOUT -> ADMIN`.

### Workflow F — Suspend another admin

```bash
curl -X POST '{{BASE_URL}}/admin/users/ad1b2c3d-0000-4000-8000-000000000001/suspend' \
  -H 'Authorization: Bearer {{ACCESS_TOKEN}}'
```

Same as for any user (ADMIN_USER_GUIDE.md, Workflow G), but only you can do it for accounts that hold `ADMIN` or `SUPER_ADMIN`. You cannot suspend yourself.

### Workflow G — Unrestricted audit log access

```bash
curl -X GET '{{BASE_URL}}/admin/audit-logs?actor=ad1b2c3d-0000-4000-8000-000000000001&page=0&size=50' \
  -H 'Authorization: Bearer {{ACCESS_TOKEN}}'
```

Without `actor` you see every entry. The other filters are `action`, `entityType`, `entityId`, `from` and `to`. These are the super admin actions:

| Area | Actions |
|---|---|
| Users | `ADMIN_CREATED`, `USER_ROLE_CHANGED` |
| Plans | `PLAN_CREATED`, `PLAN_UPDATED`, `PLAN_DEACTIVATED` |
| Sports | `SPORT_CREATED`, `SPORT_DISCIPLINE_ADDED`, `SPORT_METRIC_ADDED` |
| Activity schemas | `SCHEMA_PUBLISHED`, `SCHEMA_DEPRECATED` |

---

## 5. Available Endpoints

### All ADMIN endpoints (see ADMIN_USER_GUIDE.md), plus:

| Method | Path | Description |
|---|---|---|
| POST | `/admin/users` | Create an ADMIN account |
| PUT | `/admin/users/{id}/role` | Change a user's primary role |
| GET | `/admin/subscription-plans` | List all plans |
| GET | `/admin/subscription-plans/{id}` | Get one plan |
| POST | `/admin/subscription-plans` | Create subscription plan |
| PATCH | `/admin/subscription-plans/{id}` | Update subscription plan |
| POST | `/admin/subscription-plans/{id}/deactivate` | Deactivate a plan |
| POST | `/admin/sports` | Create a sport |
| POST | `/admin/sports/{id}/disciplines` | Add discipline |
| POST | `/admin/sports/{id}/metrics` | Add performance metric |
| POST | `/admin/activity-schema-versions/{id}/publish` | Publish schema version |
| POST | `/admin/activity-schema-versions/{id}/deprecate` | Deprecate a published version |
| GET | `/admin/audit-logs` | Every actor's audit entries |
| POST | `/admin/users/{id}/suspend` | Suspend any user, including admins |
| POST | `/admin/users/{id}/reactivate` | Reactivate any user, including admins |
| POST | `/auth/mfa/verify` | Verify a TOTP code (not enforced) |

---

## 6. Entitlements and Subscription

Admin and super admin accounts are not limited by subscription plans: entitlement checks apply to athletes, scouts and organizations, not to `/admin/**` routes. Plan management is described in SUPER_ADMIN_USER_GUIDE.md.

---

## 7. Privacy and Consent Controls

Super admins have broad data access by design. Every access to user data must be justified by a legitimate operational need and is recorded in the immutable audit log.

Data subject access requests (DSAR) and right-to-erasure requests must be processed through the designated data protection officer, not directly through admin tools.

---

## 8. Safeguarding Notes

Super admins bear the highest responsibility for platform safeguarding:

- All safeguarding reports are accessible without filter.
- SUPER_ADMIN accounts must have MFA enabled — this is enforced at login.
- Emergency safeguarding actions (immediate account suspension) should be performed within 30 minutes of receiving an escalation.
- SUPER_ADMIN actions related to minor athletes are subject to enhanced audit logging and must be documented in the safeguarding register.
- Annual safeguarding training is mandatory for all SUPER_ADMIN account holders.

---

## 9. Response Shapes, Pagination and Errors

### Response shapes

Successful responses return the resource directly as JSON, with no outer wrapper. A single resource is a JSON object, a list endpoint returns a page object (see below), and actions with nothing to return respond `204 No Content` with an empty body. Create endpoints respond `201 Created`. Timestamps are ISO-8601 UTC strings (for example `2024-06-01T10:15:30Z`). Fields with no value may be omitted from error bodies.

```json
{
  "id": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
  "status": "ACTIVE",
  "createdAt": "2024-06-01T10:15:30Z"
}
```

### Pagination format

List endpoints accept standard query parameters and return a page object.

| Parameter | Default | Description |
|---|---|---|
| `page` | `0` | Zero-based page index |
| `size` | `20` | Items per page |
| `sort` | endpoint default | `field,asc` or `field,desc`. Repeat the parameter to sort by several fields |

```bash
curl -X GET '{{BASE_URL}}/notifications?page=0&size=10&sort=createdAt,desc' \
  -H 'Authorization: Bearer {{ACCESS_TOKEN}}'
```

**Response (200 OK):**
```json
{
  "content": [ { "id": "n-1" }, { "id": "n-2" } ],
  "page": 0,
  "size": 10,
  "totalElements": 42,
  "totalPages": 5,
  "first": true,
  "last": false
}
```

| Field | Type | Description |
|---|---|---|
| `content` | array | Items on this page. Empty array when there are no results |
| `page` | integer | Zero-based index of this page |
| `size` | integer | Requested page size |
| `totalElements` | integer | Total items across all pages |
| `totalPages` | integer | Total number of pages |
| `first` | boolean | `true` on the first page |
| `last` | boolean | `true` on the last page. Stop paging when this is `true` |

Some examples in this guide show only part of the page object (for example `content` and `totalElements`) to keep them short. Real responses always include every field above.

### Error envelope

Errors raised by the API use one JSON structure for every endpoint:

```json
{
  "timestamp": "2024-06-01T10:15:30.123Z",
  "status": 404,
  "error": "Not Found",
  "message": "AthleteProfile not found for id: xyz",
  "path": "/api/v1/athletes/xyz"
}
```

| Field | Type | Description |
|---|---|---|
| `timestamp` | string | When the error was produced (UTC) |
| `status` | integer | HTTP status code, same as the response status |
| `error` | string | HTTP reason phrase, such as `Not Found` or `Conflict` |
| `message` | string | Human-readable explanation. Safe to show to users |
| `path` | string | Request path that failed |
| `fieldErrors` | array | Only on `400` validation failures. One entry per invalid field |

Validation failures (`400`) add `fieldErrors`:

```json
{
  "timestamp": "2024-06-01T10:15:30.123Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed",
  "path": "/api/v1/auth/register",
  "fieldErrors": [
    { "field": "email", "rejectedValue": "not-an-email", "message": "must be a valid email" }
  ]
}
```

Branch on `status`, not on the `message` text. Missing or invalid bearer tokens (`401`) and permission failures (`403`) use the same envelope, so every error response is JSON. Unexpected failures return `500` with a generic message and no internal detail.

### Error reference

The `error` column is the value of the `error` field. Examples show `status`, `error` and `message`; `timestamp` and `path` are always present too.

| HTTP Status | `error` | When it happens | Example |
|---|---|---|---|
| 401 | Unauthorized | Missing, invalid or expired token | `{ "status": 401, "error": "Unauthorized", "message": "Authentication is required to access this resource" }` |
| 401 | Unauthorized | Wrong or expired TOTP code | `{ "status": 401, "error": "Unauthorized", "message": "The TOTP code is invalid or has expired." }` |
| 403 | Forbidden | Route needs SUPER_ADMIN (for example, called as ADMIN) | `{ "status": 403, "error": "Forbidden", "message": "Access denied" }` |
| 404 | Not Found | Resource not found | `{ "status": 404, "error": "Not Found", "message": "SubscriptionPlan not found with id: plan-999" }` |
| 409 | Conflict | Plan code already exists | `{ "status": 409, "error": "Conflict", "message": "SubscriptionPlan already exists with code: SCOUT_ELITE" }` |
| 409 | Conflict | Schema version not DRAFT | `{ "status": 409, "error": "Conflict", "message": "Cannot transition ActivitySchemaVersion from 'PUBLISHED' to 'PUBLISHED'" }` |
| 400 | Bad Request | Changing your own role | `{ "status": 400, "error": "Bad Request", "message": "You cannot change your own role" }` |
| 400 | Bad Request | Request body failed validation | `{ "status": 400, "error": "Bad Request", "message": "Validation failed", "fieldErrors": [{ "field": "code", "rejectedValue": null, "message": "must match \"[A-Z0-9_]+\"" }] }` |
