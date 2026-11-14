# Admin User Guide

## 1. Role Overview

Delegated administrators (ADMIN role) handle platform operations: verifying organizations and scouts, moderating flagged media, reviewing safeguarding reports, querying audit logs, and managing user accounts. Admins cannot manage subscription plans, sports, roles or administrator accounts, and cannot publish activity schemas. Those need the SUPER_ADMIN role and return `403` to an admin.

---

## 2. Getting Started

Admin accounts are created by a SUPER_ADMIN (`POST /admin/users`), or an existing user is promoted with `PUT /admin/users/{id}/role`. You cannot register as `ADMIN` yourself: `POST /auth/register` rejects it with `400`.

### Login (first time)

```bash
curl -X POST '{{BASE_URL}}/auth/login' \
  -H 'Content-Type: application/json' \
  -d '{
    "email": "admin.seun@talenthub.io",
    "password": "T3mpAdm!n#2024"
  }'
```

**Response (200 OK):**
```json
{
  "access_token": "eyJhbGciOiJIUzI1NiJ9...",
  "refresh_token": "f7a8b9c0-d1e2-3456-f012-3456789abcde",
  "user_id": "ad1b2c3d-0000-4000-8000-000000000001",
  "email": "admin.seun@talenthub.io",
  "role": "ADMIN",
  "expires_in": 3600
}
```

### Token Refresh

```bash
curl -X POST '{{BASE_URL}}/auth/refresh' \
  -H 'Content-Type: application/json' \
  -d '{ "refresh_token": "f7a8b9c0-d1e2-3456-f012-3456789abcde" }'
```

### Logout

```bash
curl -X POST '{{BASE_URL}}/auth/logout' \
  -H 'Authorization: Bearer {{ACCESS_TOKEN}}'
```

**Response:** `204 No Content`. Revokes all your refresh tokens.

---

## 3. Authentication

Use `Authorization: Bearer {{ACCESS_TOKEN}}` on every request. All `/admin/**` routes need the `ADMIN` or `SUPER_ADMIN` role; anyone else gets `403`. Some routes are `SUPER_ADMIN` only and return `403` to an `ADMIN` (see SUPER_ADMIN_USER_GUIDE.md).

---

## 4. Core Workflows

### Workflow A — View the platform dashboard

```bash
curl -X GET '{{BASE_URL}}/admin/reports/platform' \
  -H 'Authorization: Bearer {{ACCESS_TOKEN}}'
```

**Response (200 OK):**
```json
{
  "totalAthletes": 4821,
  "verifiedAthletes": 1290,
  "registeredScouts": 312,
  "verifiedScouts": 188,
  "registeredOrgs": 74,
  "activeSubscriptions": 905,
  "publishedActivities": 22410,
  "uploadedVideos": 6034,
  "pendingVerifications": 17,
  "pendingModerationReports": 6
}
```

`verifiedAthletes` counts athletes with at least one verified activity. `pendingVerifications` adds pending scouts, organizations and legal guardian requests. `pendingModerationReports` adds pending safeguarding reports and flagged media.

### Workflow B — Verify an organization

#### Step 1 — List pending organizations

```bash
curl -X GET '{{BASE_URL}}/admin/verifications/organizations?status=PENDING_VERIFICATION&page=0&size=20' \
  -H 'Authorization: Bearer {{ACCESS_TOKEN}}'
```

**Response (200 OK):**
```json
{
  "content": [
    {
      "id": "0f6c2a7e-0000-4000-8000-000000000001",
      "code": "FLA",
      "name": "FC Lagos Academy",
      "orgType": "ACADEMY",
      "countryCode": "NG",
      "city": "Lagos",
      "adminUserId": "e1o2w3n4-0000-4000-8000-000000000001",
      "logoUrl": null,
      "status": "PENDING_VERIFICATION",
      "description": "Premier youth development academy based in Lagos",
      "verifiedAt": null,
      "rejectionReason": null
    }
  ],
  "page": 0,
  "size": 20,
  "totalElements": 1,
  "totalPages": 1,
  "first": true,
  "last": true
}
```

`status` accepts any organization status (`PENDING_VERIFICATION`, `ACTIVE`, `SUSPENDED`, `DEACTIVATED`).

#### Step 2 — Approve the organization

```bash
curl -X POST '{{BASE_URL}}/admin/organizations/0f6c2a7e-0000-4000-8000-000000000001/verify' \
  -H 'Authorization: Bearer {{ACCESS_TOKEN}}'
```

**Response (200 OK):** the organization with `status: "ACTIVE"` and `verifiedAt` set. The owner can now invite members and request athlete affiliations.

#### Or reject with a reason

```bash
curl -X POST '{{BASE_URL}}/admin/organizations/0f6c2a7e-0000-4000-8000-000000000001/reject' \
  -H 'Authorization: Bearer {{ACCESS_TOKEN}}' \
  -H 'Content-Type: application/json' \
  -d '{ "reason": "Registration document could not be verified." }'
```

**Response (200 OK):** the organization with `status: "DEACTIVATED"` and `rejectionReason` set. The body is optional. Deciding an organization that is not `PENDING_VERIFICATION` returns `409`.

### Workflow C — Verify a scout

```bash
curl -X GET '{{BASE_URL}}/admin/scouts?status=PENDING&page=0&size=20' \
  -H 'Authorization: Bearer {{ACCESS_TOKEN}}'
```

`status` is `PENDING` (default), `UNVERIFIED`, `VERIFIED`, `REJECTED` or `ALL`.

**Response (200 OK):**
```json
{
  "content": [
    {
      "id": "sc1a2b3c-0000-4000-8000-000000000001",
      "userAccountId": "u5c0u7aa-0000-4000-8000-000000000001",
      "professionalTitle": "Senior Scout",
      "biography": "Ten years scouting West African youth football.",
      "yearsExperience": 10,
      "currentOrganizationId": null,
      "verificationStatus": "PENDING"
    }
  ],
  "page": 0,
  "size": 20,
  "totalElements": 1,
  "totalPages": 1,
  "first": true,
  "last": true
}
```

Approve or reject:

```bash
curl -X POST '{{BASE_URL}}/admin/scouts/sc1a2b3c-0000-4000-8000-000000000001/verify' \
  -H 'Authorization: Bearer {{ACCESS_TOKEN}}'

curl -X POST '{{BASE_URL}}/admin/scouts/sc1a2b3c-0000-4000-8000-000000000001/reject?notes=Credentials%20not%20verifiable' \
  -H 'Authorization: Bearer {{ACCESS_TOKEN}}'
```

Both return the scout profile with the new `verificationStatus` (`VERIFIED` or `REJECTED`).

### Workflow D — Review legal guardian requests

```bash
curl -X GET '{{BASE_URL}}/admin/guardian-relationships?status=PENDING_ADMIN_VERIFICATION&page=0&size=20' \
  -H 'Authorization: Bearer {{ACCESS_TOKEN}}'
```

Each item is a guardian relationship with its `verificationDocumentRef` (a media asset id you can open with `GET /media/{id}`). Decide it:

```bash
curl -X POST '{{BASE_URL}}/guardian-relationships/gr-0002/approve' \
  -H 'Authorization: Bearer {{ACCESS_TOKEN}}'

curl -X POST '{{BASE_URL}}/guardian-relationships/gr-0002/reject' \
  -H 'Authorization: Bearer {{ACCESS_TOKEN}}' \
  -H 'Content-Type: application/json' \
  -d '{ "reason": "Court order does not name this guardian." }'
```

Approval makes the link `ACTIVE` for 365 days. Only `PENDING_ADMIN_VERIFICATION` requests can be decided by an admin; `PENDING` ones belong to the athlete (`403`).

### Workflow E — Moderate flagged media

Media is flagged when someone submits a safeguarding report with `entityType: "MEDIA"` and the asset id as `reportedEntityId`.

```bash
curl -X GET '{{BASE_URL}}/admin/media/flagged?page=0&size=20' \
  -H 'Authorization: Bearer {{ACCESS_TOKEN}}'
```

`status` defaults to `FLAGGED`; use `RESTRICTED` to see hidden assets.

**Response (200 OK):**
```json
{
  "content": [
    {
      "id": "media-001",
      "ownerId": "b2c3d4e5-f6a7-8901-bcde-f12345678901",
      "ownerType": "ATHLETE",
      "assetType": "ACTIVITY_VIDEO",
      "originalFilename": "semi-final.mp4",
      "mimeType": "video/mp4",
      "processingStatus": "READY",
      "uploadedBy": "b2c3d4e5-f6a7-8901-bcde-f12345678901",
      "moderationStatus": "FLAGGED",
      "moderationNote": "Reported: INAPPROPRIATE_CONTENT",
      "moderatedBy": null,
      "moderatedAt": null
    }
  ],
  "page": 0,
  "size": 20,
  "totalElements": 1,
  "totalPages": 1,
  "first": true,
  "last": true
}
```

Media items carry more fields (storage keys and timestamps); the main ones are shown.

```bash
# Keep it visible
curl -X POST '{{BASE_URL}}/admin/media/media-001/approve' \
  -H 'Authorization: Bearer {{ACCESS_TOKEN}}'

# Hide it from everyone except the uploader, the owner's managers and admins
curl -X POST '{{BASE_URL}}/admin/media/media-001/restrict' \
  -H 'Authorization: Bearer {{ACCESS_TOKEN}}' \
  -H 'Content-Type: application/json' \
  -d '{ "reason": "Shows a minor without consent." }'
```

Both return the asset with the new `moderationStatus` (`CLEAN` or `RESTRICTED`). The body is optional. Repeating the current status returns `409`. Other users get `403` with `This media asset is not accessible.` on a restricted asset.

### Workflow F — Review safeguarding reports

```bash
curl -X GET '{{BASE_URL}}/admin/safeguarding/reports?status=PENDING' \
  -H 'Authorization: Bearer {{ACCESS_TOKEN}}'
```

**Response (200 OK):** an array, not paginated. Omit `status` for every report.
```json
[
  {
    "id": "sr-0001",
    "reporterId": "u9a8b7c6-0000-4000-8000-000000000001",
    "reportedEntityId": "media-001",
    "reportedEntityType": "MEDIA",
    "reportType": "INAPPROPRIATE_CONTENT",
    "description": "Video shows another child without consent.",
    "status": "PENDING",
    "submittedAt": "2024-06-15T09:30:00Z",
    "resolvedAt": null,
    "resolvedBy": null
  }
]
```

Record your decision (`status` is a free-form value such as `RESOLVED` or `DISMISSED`):

```bash
curl -X POST '{{BASE_URL}}/admin/safeguarding/reports/sr-0001/review?status=RESOLVED' \
  -H 'Authorization: Bearer {{ACCESS_TOKEN}}'
```

**Response (200 OK):** the report with `status`, `resolvedAt` and `resolvedBy` set. If the report is about media, restrict the asset separately (Workflow E).

### Workflow G — Manage user accounts

```bash
curl -X GET '{{BASE_URL}}/admin/users?role=SCOUT&status=ACTIVE&page=0&size=20' \
  -H 'Authorization: Bearer {{ACCESS_TOKEN}}'
```

`role` and `status` are optional. `role` matches primary and additional roles. `status` is `PENDING_VERIFICATION`, `ACTIVE`, `SUSPENDED` or `DEACTIVATED`.

**Response (200 OK):**
```json
{
  "content": [
    {
      "userId": "u5c0u7aa-0000-4000-8000-000000000001",
      "email": "kwame.mensah@example.com",
      "role": "SCOUT",
      "roles": ["SCOUT"],
      "status": "ACTIVE",
      "emailVerified": true,
      "createdAt": "2024-05-02T08:00:00Z",
      "lastLoginAt": "2024-06-15T07:45:00Z"
    }
  ],
  "page": 0,
  "size": 20,
  "totalElements": 1,
  "totalPages": 1,
  "first": true,
  "last": true
}
```

Get one user with `GET /admin/users/{id}`. Suspend or reactivate:

```bash
curl -X POST '{{BASE_URL}}/admin/users/u5c0u7aa-0000-4000-8000-000000000001/suspend' \
  -H 'Authorization: Bearer {{ACCESS_TOKEN}}'

curl -X POST '{{BASE_URL}}/admin/users/u5c0u7aa-0000-4000-8000-000000000001/reactivate' \
  -H 'Authorization: Bearer {{ACCESS_TOKEN}}'
```

Both return the user with the new `status`. Suspending revokes the user's refresh tokens and blocks login and refresh. An access token they already hold keeps working until it expires (at most 60 minutes).

Rules:
- Accounts that hold `ADMIN` or `SUPER_ADMIN` can only be suspended or reactivated by a `SUPER_ADMIN` (`403` for you).
- You cannot suspend yourself (`400`).
- Suspending a suspended account, or reactivating an active one, returns `409`.

### Workflow H — Query audit logs

```bash
curl -X GET '{{BASE_URL}}/admin/audit-logs?action=USER_SUSPENDED&from=2024-06-01T00:00:00Z&page=0&size=20' \
  -H 'Authorization: Bearer {{ACCESS_TOKEN}}'
```

Filters, all optional: `action`, `entityType`, `entityId`, `from` and `to` (ISO-8601 instants, `to` exclusive). As an `ADMIN` you only see your own entries; the `actor` filter is ignored for you. A `SUPER_ADMIN` sees everyone's.

**Response (200 OK):**
```json
{
  "content": [
    {
      "id": "al-0001",
      "createdAt": "2024-06-15T12:00:00Z",
      "actorId": "ad1b2c3d-0000-4000-8000-000000000001",
      "actorRole": "ADMIN",
      "action": "USER_SUSPENDED",
      "entityType": "User",
      "entityId": "u5c0u7aa-0000-4000-8000-000000000001",
      "details": null
    }
  ],
  "page": 0,
  "size": 20,
  "totalElements": 1,
  "totalPages": 1,
  "first": true,
  "last": true
}
```

Actions recorded for admins include `USER_SUSPENDED`, `USER_REACTIVATED`, `ORGANIZATION_VERIFIED`, `ORGANIZATION_REJECTED`, `SCOUT_VERIFIED`, `SCOUT_REJECTED`, `GUARDIAN_LINK_APPROVED`, `GUARDIAN_LINK_REJECTED`, `MEDIA_APPROVED`, `MEDIA_RESTRICTED` and `SAFEGUARDING_REPORT_REVIEWED`.

### Workflow I — Activity configuration

Admins can create activity types and templates and add fields. Publishing a schema is `SUPER_ADMIN` only; see SUPER_ADMIN_USER_GUIDE.md for the full flow.

---

## 5. Available Endpoints

| Method | Path | Description |
|---|---|---|
| GET | `/admin/reports/platform` | Platform dashboard |
| GET | `/admin/users` | List users (`role`, `status`, paginated) |
| GET | `/admin/users/{id}` | Get one user |
| POST | `/admin/users/{id}/suspend` | Suspend a user (admins: SUPER_ADMIN only) |
| POST | `/admin/users/{id}/reactivate` | Reactivate a user (admins: SUPER_ADMIN only) |
| GET | `/admin/verifications/organizations` | Organizations by status (default `PENDING_VERIFICATION`) |
| POST | `/admin/organizations/{id}/verify` | Approve organization |
| POST | `/admin/organizations/{id}/reject` | Reject organization (`{ reason? }`) |
| GET | `/admin/scouts` | Scouts by verification status (default `PENDING`) |
| POST | `/admin/scouts/{id}/verify` | Approve scout |
| POST | `/admin/scouts/{id}/reject` | Reject scout (`notes` query) |
| GET | `/admin/guardian-relationships` | Guardian relationships by status (default `PENDING_ADMIN_VERIFICATION`) |
| POST | `/guardian-relationships/{id}/approve` | Approve a legal guardian request |
| POST | `/guardian-relationships/{id}/reject` | Reject a legal guardian request (`{ reason? }`) |
| GET | `/admin/media/flagged` | Media by moderation status (default `FLAGGED`) |
| POST | `/admin/media/{id}/approve` | Mark media `CLEAN` |
| POST | `/admin/media/{id}/restrict` | Mark media `RESTRICTED` |
| GET | `/admin/safeguarding/reports` | Safeguarding reports (`status` optional) |
| POST | `/admin/safeguarding/reports/{id}/review` | Record a decision (`status` query) |
| GET | `/admin/audit-logs` | Your own audit entries |
| POST | `/admin/activity-types` | Create activity type |
| POST | `/admin/activity-templates` | Create activity template (creates draft schema v1) |
| GET | `/admin/activity-templates/{id}/schema-versions` | List schema versions |
| POST | `/admin/activity-templates/{id}/fields` | Add a field to the draft schema |

---

## 6. Entitlements and Subscription

Admin and super admin accounts are not limited by subscription plans: entitlement checks apply to athletes, scouts and organizations, not to `/admin/**` routes. Plan management is described in SUPER_ADMIN_USER_GUIDE.md.

---

## 7. Privacy and Consent Controls

Admins can access user data for the purpose of moderation, verification, and safeguarding. All admin actions are logged in the audit log with the admin's user ID, action type, target entity, and timestamp.

Admins must not access or share athlete data beyond what is needed for the specific moderation task (data minimisation principle).

---

## 8. Safeguarding Notes

**Admin responsibilities for safeguarding:**

- All OPEN safeguarding reports must be reviewed within 48 hours.
- Reports involving minors must be escalated to the designated safeguarding lead.
- Outcome options: NO_ACTION, WARNING_ISSUED, ACCOUNT_SUSPENDED, ESCALATED.
- Account suspensions triggered by safeguarding reports are permanent until reviewed by a SUPER_ADMIN.
- If a report indicates imminent risk to a child, immediately suspend the involved account and escalate to `safeguarding@talenthub.io` and relevant authorities.
- All safeguarding review actions are permanently retained in the audit log and cannot be deleted.

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
| 403 | Forbidden | Your role cannot call this endpoint | `{ "status": 403, "error": "Forbidden", "message": "Access denied" }` |
| 403 | Forbidden | Acting on an administrator account | `{ "status": 403, "error": "Forbidden", "message": "Only a SUPER_ADMIN can suspend an administrator account" }` |
| 400 | Bad Request | Acting on your own account | `{ "status": 400, "error": "Bad Request", "message": "You cannot suspend your own account" }` |
| 404 | Not Found | Resource not found | `{ "status": 404, "error": "Not Found", "message": "Organization not found with id: org-999" }` |
| 409 | Conflict | Already decided | `{ "status": 409, "error": "Conflict", "message": "Cannot transition Organization from 'ACTIVE' to 'ACTIVE'" }` |
| 400 | Bad Request | Request body failed validation | `{ "status": 400, "error": "Bad Request", "message": "Validation failed", "fieldErrors": [{ "field": "email", "rejectedValue": null, "message": "must be a well-formed email address" }] }` |
