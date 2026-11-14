# Agent User Guide

## 1. Role Overview

Sports agents use TalentHub to represent athletes they manage. With an approved AGENT management relationship, an agent can view the athlete's full profile and development history, edit the profile and consent settings (if the relationship was approved with `MANAGE` permission), and receive notifications when the athlete's activities are published.

---

## 2. Getting Started

### Step 1 — Register an account

```bash
curl -X POST '{{BASE_URL}}/auth/register' \
  -H 'Content-Type: application/json' \
  -d '{
    "email": "fatima.nwosu@eliteagency.io",
    "password": "Ag3ntPr0#2024!",
    "role": "AGENT"
  }'
```

**Response (201 Created):**
```json
{
  "userId": "ag1b2c3d-e5f6-7890-abcd-ef1234560000",
  "message": "Registration successful. Please verify your email."
}
```

### Step 2 — Confirm your role

The `role` you sent at registration is your primary role, so no separate assignment is needed. Check your account:

```bash
curl -X GET '{{BASE_URL}}/users/me' \
  -H 'Authorization: Bearer {{ACCESS_TOKEN}}'
```

**Response (200 OK):**
```json
{
  "userId": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
  "email": "user@example.com",
  "role": "AGENT",
  "roles": ["AGENT"],
  "status": "ACTIVE",
  "emailVerified": true
}
```

To hold an additional role, call `POST {{BASE_URL}}/users/me/roles` with `{ "role": "COACH" }`. Allowed roles are `ATHLETE`, `COACH`, `PARENT`, `GUARDIAN`, `AGENT` and `SCOUT`. `ADMIN`, `SUPER_ADMIN` and `ORG_ADMIN` are rejected with `400`, and adding a role you already hold returns `409`. New roles appear in tokens issued after your next login or refresh.

### Step 3 — Request a management relationship with an athlete

```bash
curl -X POST '{{BASE_URL}}/guardian-relationships' \
  -H 'Authorization: Bearer {{ACCESS_TOKEN}}' \
  -H 'Content-Type: application/json' \
  -d '{
    "athleteId": "b2c3d4e5-f6a7-8901-bcde-f12345678901",
    "relationshipType": "AGENT",
    "permission": "MANAGE"
  }'
```

**Response (201 Created):**
```json
{
  "id": "gr-0010",
  "guardianUserId": "ag1b2c3d-e5f6-7890-abcd-ef1234560000",
  "athleteUserId": "b2c3d4e5-f6a7-8901-bcde-f12345678901",
  "relationshipType": "AGENT",
  "relationship": "AGENT",
  "permission": "MANAGE",
  "status": "PENDING",
  "consentGiven": false,
  "verificationDocumentRef": null,
  "createdAt": "2024-06-15T10:05:00Z",
  "approvedAt": null,
  "expiresAt": null,
  "revokedAt": null
}
```

`permission` is the access level you are asking for (`VIEW_PROFILE`, `VIEW_STATS`, `VIEW_DOCUMENTS`, `CONTACT` or `MANAGE`, default `VIEW_STATS`). The athlete approves it as requested. Once `ACTIVE`, you hold an access grant of that level on the athlete, and revoking the relationship removes the grant. Writes need `MANAGE`.

### Step 4 — Athlete approves the relationship

```bash
# Called by the athlete
curl -X POST '{{BASE_URL}}/guardian-relationships/gr-0010/approve' \
  -H 'Authorization: Bearer {{ATHLETE_ACCESS_TOKEN}}'
```

**Response (200 OK):**
```json
{
  "id": "gr-0010",
  "guardianUserId": "ag1b2c3d-e5f6-7890-abcd-ef1234560000",
  "athleteUserId": "b2c3d4e5-f6a7-8901-bcde-f12345678901",
  "relationshipType": "AGENT",
  "relationship": "AGENT",
  "permission": "MANAGE",
  "status": "ACTIVE",
  "consentGiven": true,
  "verificationDocumentRef": null,
  "createdAt": "2024-06-15T10:05:00Z",
  "approvedAt": "2024-06-15T11:00:00Z",
  "expiresAt": null,
  "revokedAt": null
}
```

---

## 3. Authentication

### Login

```bash
curl -X POST '{{BASE_URL}}/auth/login' \
  -H 'Content-Type: application/json' \
  -d '{
    "email": "fatima.nwosu@eliteagency.io",
    "password": "Ag3ntPr0#2024!"
  }'
```

**Response (200 OK):**
```json
{
  "access_token": "eyJhbGciOiJIUzI1NiJ9...",
  "refresh_token": "d5e6f7a8-b9c0-1234-def0-12345678901a",
  "user_id": "b2c3d4e5-f6a7-8901-bcde-f12345678901",
  "email": "user@example.com",
  "role": "AGENT",
  "expires_in": 3600
}
```

### Token Refresh

```bash
curl -X POST '{{BASE_URL}}/auth/refresh' \
  -H 'Content-Type: application/json' \
  -d '{ "refresh_token": "d5e6f7a8-b9c0-1234-def0-12345678901a" }\'
```

### Logout

```bash
curl -X POST '{{BASE_URL}}/auth/logout' \
  -H 'Authorization: Bearer {{ACCESS_TOKEN}}'
```

---

## 4. Core Workflows

### Workflow A — View athlete profile (via approved management relationship)

```bash
curl -X GET '{{BASE_URL}}/athletes/b2c3d4e5-f6a7-8901-bcde-f12345678901' \
  -H 'Authorization: Bearer {{ACCESS_TOKEN}}'
```

**Response (200 OK):**
```json
{
  "id": "ap-0001",
  "userId": "b2c3d4e5-f6a7-8901-bcde-f12345678901",
  "athleteNumber": "TH-00001",
  "primarySportId": "sp-football",
  "primarySportCode": "FB",
  "position": "Attacking Midfielder",
  "heightCm": 175,
  "weightKg": 70,
  "dominantFoot": "RIGHT",
  "status": "ACTIVE",
  "organizationId": null,
  "nationality": "NG"
}
```

`{id}` is the athlete's user id. This needs at least `VIEW_PROFILE` on your agent relationship; without an active relationship you get `403`.

### Workflow B — View athlete development history

```bash
curl -X GET '{{BASE_URL}}/athletes/b2c3d4e5-f6a7-8901-bcde-f12345678901/development' \
  -H 'Authorization: Bearer {{ACCESS_TOKEN}}'
```

**Response (200 OK):**
```json
{
  "athleteId": "b2c3d4e5-f6a7-8901-bcde-f12345678901",
  "entries": [
    {
      "type": "MEASUREMENT",
      "label": "Sprint 40m",
      "value": "4.82s",
      "recordedAt": "2024-05-20"
    },
    {
      "type": "MILESTONE",
      "label": "Best Young Player — Lagos Regional League U-17",
      "recordedAt": "2023-11-30"
    }
  ]
}
```

### Workflow C — View athlete activity list

```bash
curl -X GET '{{BASE_URL}}/athletes/b2c3d4e5-f6a7-8901-bcde-f12345678901/activities?page=0&size=10' \
  -H 'Authorization: Bearer {{ACCESS_TOKEN}}'
```

**Response (200 OK):**
```json
{
  "content": [
    {
      "id": "act-0200",
      "title": "Quarter-final vs Kano Stars FC",
      "status": "PUBLISHED",
      "activityDate": "2024-06-10"
    }
  ],
  "totalElements": 1
}
```

### Workflow D — Update athlete profile (needs `MANAGE`)

Editing needs an `AGENT` relationship approved with `permission: "MANAGE"`. Then:

```bash
curl -X PATCH '{{BASE_URL}}/athletes/b2c3d4e5-f6a7-8901-bcde-f12345678901' \
  -H 'Authorization: Bearer {{ACCESS_TOKEN}}' \
  -H 'Content-Type: application/json' \
  -d '{
    "position": "Central Midfielder",
    "heightCm": 176
  }'
```

**Response (200 OK):** updated athlete profile.

**403 without `MANAGE`:**
```json
{
  "timestamp": "2024-06-15T11:30:00.000Z",
  "status": 403,
  "error": "Forbidden",
  "message": "You do not have MANAGE permission for this athlete.",
  "path": "/api/v1/athletes/b2c3d4e5-f6a7-8901-bcde-f12345678901"
}
```

### Workflow E — Receive and read notifications

```bash
curl -X GET '{{BASE_URL}}/notifications?unreadOnly=true' \
  -H 'Authorization: Bearer {{ACCESS_TOKEN}}'
```

**Response (200 OK):**
```json
{
  "content": [
    {
      "id": "notif-agent-001",
      "type": "DEVELOPMENT_UPDATE",
      "message": "Jordan Okafor published a new activity: 'Quarter-final vs Kano Stars FC'.",
      "relatedEntityId": "act-0200",
      "read": false,
      "createdAt": "2024-06-11T09:00:00Z"
    }
  ],
  "totalElements": 1
}
```

```bash
curl -X PATCH '{{BASE_URL}}/notifications/notif-agent-001/read' \
  -H 'Authorization: Bearer {{ACCESS_TOKEN}}'
```

### Workflow F — Revoke management relationship

```bash
curl -X POST '{{BASE_URL}}/guardian-relationships/gr-0010/revoke' \
  -H 'Authorization: Bearer {{ACCESS_TOKEN}}'
```

**Response (200 OK):**
```json
{
  "id": "gr-0010",
  "guardianUserId": "ag1b2c3d-e5f6-7890-abcd-ef1234560000",
  "athleteUserId": "b2c3d4e5-f6a7-8901-bcde-f12345678901",
  "relationshipType": "AGENT",
  "relationship": "AGENT",
  "permission": "MANAGE",
  "status": "REVOKED",
  "consentGiven": true,
  "verificationDocumentRef": null,
  "createdAt": "2024-06-15T10:05:00Z",
  "approvedAt": "2024-06-15T11:00:00Z",
  "expiresAt": null,
  "revokedAt": "2024-09-01T08:00:00Z"
}
```

---

## 5. Available Endpoints

> **Access:** Your access to an athlete is the `permission` on your approved `AGENT` relationship. Viewing activities, development and performance needs `VIEW_STATS` or higher. Creating or editing activities, uploading media, recording measurements and changing consent need `MANAGE`. Calls beyond your level return `403`. Revoking the relationship removes access straight away.

| Method | Path | Description |
|---|---|---|
| POST | `/auth/register` | Register account |
| POST | `/auth/login` | Log in |
| POST | `/auth/refresh` | Refresh tokens |
| POST | `/auth/logout` | Log out |
| GET | `/auth/verify-email` | Verify email |
| POST | `/users/me/roles` | Add an extra non-privileged role (`AGENT` etc.) |
| GET | `/users/me` | Get account info |
| POST | `/guardian-relationships` | Request AGENT relationship |
| GET | `/guardian-relationships` | List relationships |
| POST | `/guardian-relationships/{id}/revoke` | Revoke relationship |
| GET | `/athletes/{id}` | View athlete profile (requires relationship) |
| GET | `/athletes/{id}/development` | Development timeline |
| GET | `/athletes/{id}/activities` | List athlete activities |
| GET | `/athletes/{id}/performance` | Performance data |
| PATCH | `/athletes/{id}` | Update profile (needs `MANAGE`) |
| GET | `/subscriptions/me` | View subscription |
| GET | `/notifications` | List notifications |
| PATCH | `/notifications/{id}/read` | Mark notification read |

---

## 6. Entitlements and Subscription

Limits come from your active subscription plan. Each plan sets a limit per entitlement key (for example `MAX_FAVOURITES`, `MAX_WATCHLISTS`, `MAX_SCOUT_SEATS`). A key the plan doesn't list is unlimited. Without an active subscription, the free-tier defaults apply:

| Key | Free-tier limit |
|---|---|
| `max_athletes` | 5 |
| `max_scouts` | 0 |
| `max_video_uploads` | 3 |
| `max_org_members` | 10 |
| `video_max_duration_s` | 120 |

Going over a limit returns `402 Payment Required` with the standard error envelope:

```json
{
  "timestamp": "2024-06-15T15:00:00.000Z",
  "status": 402,
  "error": "Payment Required",
  "message": "Entitlement limit exceeded for 'MAX_WATCHLISTS': current=3, limit=3",
  "path": "/api/v1/scouting/watchlists"
}
```

#### View plans and subscribe

```bash
curl -X GET '{{BASE_URL}}/subscription-plans' \
  -H 'Authorization: Bearer {{ACCESS_TOKEN}}'

curl -X POST '{{BASE_URL}}/subscriptions' \
  -H 'Authorization: Bearer {{ACCESS_TOKEN}}' \
  -H 'Content-Type: application/json' \
  -d '{ "planId": "plan-0003" }'
```

A free plan starts straight away (`status: "ACTIVE"`). A paid plan starts as `PENDING`. To pay, start checkout and send the user to `authorizationUrl`:

```bash
curl -X POST '{{BASE_URL}}/subscriptions/sub-0001/checkout' \
  -H 'Authorization: Bearer {{ACCESS_TOKEN}}'
```

**Response (201 Created):**
```json
{
  "transactionId": "tx-0001",
  "reference": "ps_ref_7h2k9",
  "authorizationUrl": "https://checkout.paystack.com/7h2k9",
  "accessCode": "7h2k9"
}
```

When the payment provider confirms the payment, the subscription becomes `ACTIVE`.

#### View your subscription

```bash
curl -X GET '{{BASE_URL}}/subscriptions/me' \
  -H 'Authorization: Bearer {{ACCESS_TOKEN}}'
```

**Response (200 OK):**
```json
{
  "id": "sub-0001",
  "subscriberId": "b2c3d4e5-f6a7-8901-bcde-f12345678901",
  "plan": {
    "id": "plan-0003",
    "code": "SCOUT_PRO",
    "name": "Scout Pro",
    "targetRole": "SCOUT",
    "pricePerMonth": 29.00,
    "currency": "USD",
    "entitlementsJson": "{\"MAX_FAVOURITES\":200,\"MAX_WATCHLISTS\":20}",
    "active": true
  },
  "status": "ACTIVE",
  "startsAt": "2024-06-15T10:00:00Z",
  "expiresAt": null,
  "graceEndsAt": null,
  "cancelledAt": null,
  "cancelledBy": null,
  "cancellationReason": null,
  "renewsAt": null
}
```

`404` with an empty body means you have no active subscription. A `PENDING` one does not count.

- **Cancel:** `POST /subscriptions/{id}/cancel?immediate=false&reason=...`.
- **List add-ons:** `GET /subscriptions/{id}/addons`.

Both only work for your own subscription, your organization's, or as an admin (`403` otherwise).

---

## 7. Privacy and Consent Controls

Agent relationships grant read access to the athlete's data as configured by the athlete. If the athlete withdraws discovery consent, you can still view their profile (you have an approved relationship) but their profile disappears from scout discovery.

To end your access entirely, revoke the management relationship (see Workflow F).

---

## 8. Response Shapes, Pagination and Errors

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
| 403 | Forbidden | No active agent relationship, or a lower permission than needed | `{ "status": 403, "error": "Forbidden", "message": "You do not have MANAGE permission for this athlete." }` |
| 403 | Forbidden | Missing AGENT role | `{ "status": 403, "error": "Forbidden", "message": "The AGENT role is required to request a AGENT relationship" }` |
| 404 | Not Found | Athlete not found | `{ "status": 404, "error": "Not Found", "message": "Athlete not found with id: b2c3d4e5-f6a7-8901-bcde-f12345678901" }` |
| 409 | Conflict | Open relationship already exists | `{ "status": 409, "error": "Conflict", "message": "A AGENT relationship with this athlete already exists" }` |
| 400 | Bad Request | Unknown permission | `{ "status": 400, "error": "Bad Request", "message": "Unknown permission: SUPERUSER" }` |
| 400 | Bad Request | Request body failed validation | `{ "status": 400, "error": "Bad Request", "message": "Validation failed", "fieldErrors": [{ "field": "athleteId", "rejectedValue": null, "message": "must not be blank" }] }` |
