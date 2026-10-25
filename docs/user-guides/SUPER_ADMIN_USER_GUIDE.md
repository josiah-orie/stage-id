# Super Admin User Guide

## 1. Role Overview

Super administrators (SUPER_ADMIN role) have unrestricted access to all platform functions. In addition to everything an ADMIN can do, super admins can:

- Suspend or reactivate other admin accounts
- Access audit logs across all users without scope restriction
- Manage subscription plans and pricing
- Configure sports, disciplines, and performance metrics
- Manage activity type schemas and publish new schema versions
- Enforce MFA for all admin accounts
- Access all safeguarding reports without filter

SUPER_ADMIN accounts should be kept to a minimum. Each login is logged and all actions are permanently retained.

---

## 2. Getting Started

Super admin accounts are created during initial platform setup or promoted by another SUPER_ADMIN. MFA is mandatory for all SUPER_ADMIN accounts.

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
  "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
  "refreshToken": "a9b0c1d2-e3f4-5678-9012-34567890abcd",
  "tokenType": "Bearer",
  "expiresIn": 3600,
  "mfaRequired": true
}
```

If `mfaRequired: true`, complete MFA verification before accessing protected endpoints.

### Step 2 — Complete MFA verification

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

**401 if code is wrong or expired:**
```json
{
  "error": "INVALID_MFA_CODE",
  "message": "The TOTP code is invalid or has expired."
}
```

### Token Refresh

```bash
curl -X POST '{{BASE_URL}}/auth/refresh' \
  -H 'Content-Type: application/json' \
  -d '{ "refreshToken": "a9b0c1d2-e3f4-5678-9012-34567890abcd" }'
```

### Logout

```bash
curl -X POST '{{BASE_URL}}/auth/logout' \
  -H 'Authorization: Bearer {{ACCESS_TOKEN}}'
```

---

## 3. Authentication

Super admins use the same JWT authentication as other roles. The `SUPER_ADMIN` role claim in the JWT grants access to all admin and super-admin endpoints. MFA verification is required on every new login session.

---

## 4. Core Workflows

### All ADMIN workflows are available — see ADMIN_USER_GUIDE.md

Super admins inherit all admin capabilities. The additional workflows below are SUPER_ADMIN exclusive.

### Workflow A — Manage subscription plans

#### Create a new plan

```bash
curl -X POST '{{BASE_URL}}/admin/subscription-plans' \
  -H 'Authorization: Bearer {{ACCESS_TOKEN}}' \
  -H 'Content-Type: application/json' \
  -d '{
    "code": "SCOUT_ELITE",
    "name": "Scout Elite",
    "priceUsd": 79.99,
    "billingCycleDays": 30,
    "entitlements": {
      "discoveryAllowed": true,
      "evaluationsAllowed": true,
      "maxEvaluationsPerMonth": -1,
      "maxWatchlists": -1,
      "apiAccess": true,
      "organizationScouting": true,
      "prioritySupport": true
    }
  }'
```

**Response (201 Created):**
```json
{
  "planId": "plan-elite-001",
  "code": "SCOUT_ELITE",
  "name": "Scout Elite",
  "priceUsd": 79.99,
  "status": "ACTIVE",
  "createdAt": "2024-06-15T12:00:00Z"
}
```

#### Update an existing plan

```bash
curl -X PATCH '{{BASE_URL}}/admin/subscription-plans/plan-elite-001' \
  -H 'Authorization: Bearer {{ACCESS_TOKEN}}' \
  -H 'Content-Type: application/json' \
  -d '{
    "priceUsd": 89.99,
    "entitlements": {
      "prioritySupport": true,
      "dedicatedAccountManager": true
    }
  }'
```

**Response (200 OK):** updated plan object.

### Workflow B — Configure sports

#### Create a new sport

```bash
curl -X POST '{{BASE_URL}}/admin/sports' \
  -H 'Authorization: Bearer {{ACCESS_TOKEN}}' \
  -H 'Content-Type: application/json' \
  -d '{
    "name": "Rugby Sevens",
    "code": "RUGBY7",
    "category": "TEAM_SPORT"
  }'
```

**Response (201 Created):**
```json
{
  "sportId": "sport-020",
  "name": "Rugby Sevens",
  "code": "RUGBY7",
  "category": "TEAM_SPORT"
}
```

#### Add discipline to a sport

```bash
curl -X POST '{{BASE_URL}}/admin/sports/sport-020/disciplines' \
  -H 'Authorization: Bearer {{ACCESS_TOKEN}}' \
  -H 'Content-Type: application/json' \
  -d '{
    "name": "Open",
    "code": "RUGBY7_OPEN"
  }'
```

#### Add performance metric to a sport

```bash
curl -X POST '{{BASE_URL}}/admin/sports/sport-020/metrics' \
  -H 'Authorization: Bearer {{ACCESS_TOKEN}}' \
  -H 'Content-Type: application/json' \
  -d '{
    "name": "Tries Scored",
    "unit": "count"
  }'
```

### Workflow C — Manage activity schemas

#### Create an activity type

```bash
curl -X POST '{{BASE_URL}}/admin/activity-types' \
  -H 'Authorization: Bearer {{ACCESS_TOKEN}}' \
  -H 'Content-Type: application/json' \
  -d '{
    "name": "Rugby Match",
    "code": "RUGBY7_MATCH",
    "sportCode": "RUGBY7"
  }'
```

#### Create an activity template

```bash
curl -X POST '{{BASE_URL}}/admin/activity-templates' \
  -H 'Authorization: Bearer {{ACCESS_TOKEN}}' \
  -H 'Content-Type: application/json' \
  -d '{
    "name": "Rugby 7s Match Template v1",
    "activityTypeCode": "RUGBY7_MATCH",
    "fields": [
      { "fieldCode": "opponent", "label": "Opponent", "type": "TEXT", "required": true },
      { "fieldCode": "triesScored", "label": "Tries Scored", "type": "INTEGER", "required": false },
      { "fieldCode": "minutesPlayed", "label": "Minutes Played", "type": "INTEGER", "required": true }
    ]
  }'
```

**Response (201 Created):**
```json
{
  "templateId": "tmpl-rugby7-match-v1",
  "name": "Rugby 7s Match Template v1",
  "status": "DRAFT"
}
```

#### Publish the schema version

```bash
curl -X POST '{{BASE_URL}}/admin/activity-schema-versions/tmpl-rugby7-match-v1/publish' \
  -H 'Authorization: Bearer {{ACCESS_TOKEN}}'
```

**Response (200 OK):**
```json
{
  "schemaVersionId": "sv-rugby7-001",
  "templateId": "tmpl-rugby7-match-v1",
  "status": "PUBLISHED",
  "publishedAt": "2024-06-15T13:00:00Z"
}
```

### Workflow D — Suspend another admin

```bash
curl -X POST '{{BASE_URL}}/admin/users/some-admin-user-id/suspend' \
  -H 'Authorization: Bearer {{ACCESS_TOKEN}}' \
  -H 'Content-Type: application/json' \
  -d '{ "reason": "Suspected unauthorized data access. Under investigation." }'
```

**Response (200 OK):**
```json
{
  "userId": "some-admin-user-id",
  "status": "SUSPENDED",
  "suspendedAt": "2024-06-15T14:00:00Z"
}
```

Note: Only SUPER_ADMIN can suspend other admin accounts.

### Workflow E — Unrestricted audit log access

SUPER_ADMIN can query audit logs for any actor:

```bash
curl -X GET '{{BASE_URL}}/admin/audit-logs?actorId=some-admin-user-id&from=2024-06-01T00:00:00Z' \
  -H 'Authorization: Bearer {{ACCESS_TOKEN}}'
```

ADMIN accounts can only view audit logs for their own actions. SUPER_ADMIN can view any actor's audit trail.

---

## 5. Available Endpoints

### All ADMIN endpoints (see ADMIN_USER_GUIDE.md), plus:

| Method | Path | Description |
|---|---|---|
| POST | `/admin/subscription-plans` | Create subscription plan |
| PATCH | `/admin/subscription-plans/{id}` | Update subscription plan |
| POST | `/admin/sports` | Create a sport |
| POST | `/admin/sports/{id}/disciplines` | Add discipline |
| POST | `/admin/sports/{id}/metrics` | Add performance metric |
| POST | `/admin/activity-types` | Create activity type |
| POST | `/admin/activity-templates` | Create activity template |
| POST | `/admin/activity-schema-versions/{id}/publish` | Publish schema version |
| GET | `/admin/audit-logs` | Unrestricted audit log access (any actor) |
| POST | `/admin/users/{id}/suspend` | Suspend any user including admins |
| POST | `/admin/users/{id}/reactivate` | Reactivate any user |
| POST | `/auth/mfa/verify` | Verify TOTP MFA code |

---

## 6. Entitlements and Subscription

Super admin accounts are not bound by subscription entitlements. All platform features are accessible by role authority.

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

## 9. Error Reference

| Error Code | HTTP Status | Meaning | Example JSON |
|---|---|---|---|
| `UNAUTHORIZED` | 401 | Missing or expired token | `{ "error": "UNAUTHORIZED", "message": "Full authentication is required." }` |
| `INVALID_MFA_CODE` | 401 | Wrong or expired TOTP code | `{ "error": "INVALID_MFA_CODE", "message": "The TOTP code is invalid or has expired." }` |
| `FORBIDDEN` | 403 | SUPER_ADMIN role required | `{ "error": "FORBIDDEN", "message": "SUPER_ADMIN role required for this operation." }` |
| `NOT_FOUND` | 404 | Resource not found | `{ "error": "NOT_FOUND", "message": "SubscriptionPlan not found for id: plan-999" }` |
| `DUPLICATE_RESOURCE` | 409 | Plan code already exists | `{ "error": "DUPLICATE_RESOURCE", "message": "SubscriptionPlan with code SCOUT_ELITE already exists." }` |
| `INVALID_STATE_TRANSITION` | 409 | Schema already published | `{ "error": "INVALID_STATE_TRANSITION", "message": "Schema version is already PUBLISHED." }` |
| `VALIDATION_ERROR` | 400 | Bad request | `{ "error": "VALIDATION_ERROR", "errors": [{ "field": "code", "message": "must not be blank" }] }` |
