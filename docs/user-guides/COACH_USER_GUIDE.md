# Coach User Guide

## 1. Role Overview

Coaches use TalentHub within an organization context. They are invited to an organization by the owner or admin, and are assigned athletes to manage. Coaches can submit and verify activities on behalf of athletes, upload training media, and view development timelines.

---

## 2. Getting Started

### Step 1 — Register an account

```bash
curl -X POST '{{BASE_URL}}/auth/register' \
  -H 'Content-Type: application/json' \
  -d '{
    "email": "tunde.adeyemi@fclagos.ng",
    "password": "C0achPass#2024!",
    "role": "COACH"
  }'
```

**Response (201 Created):**
```json
{
  "userId": "c1a2b3c4-d5e6-7890-abcd-ef0987654321",
  "message": "Registration successful. Please verify your email."
}
```

### Step 2 — Verify your email and confirm your role

Open the verification link from your email (`GET {{BASE_URL}}/auth/verify-email?token=<TOKEN>`). The `role` you sent at registration is your primary role, so no separate assignment is needed. Check your account:

```bash
curl -X GET '{{BASE_URL}}/users/me' \
  -H 'Authorization: Bearer {{ACCESS_TOKEN}}'
```

**Response (200 OK):**
```json
{
  "userId": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
  "email": "user@example.com",
  "role": "COACH",
  "roles": ["COACH"],
  "status": "ACTIVE",
  "emailVerified": true
}
```

To hold an additional role, call `POST {{BASE_URL}}/users/me/roles` with `{ "role": "COACH" }`. Allowed roles are `ATHLETE`, `COACH`, `PARENT`, `GUARDIAN`, `AGENT` and `SCOUT`. `ADMIN`, `SUPER_ADMIN` and `ORG_ADMIN` are rejected with `400`, and adding a role you already hold returns `409`. New roles appear in tokens issued after your next login or refresh.

### Step 3 — Accept organization invitation

An organization `OWNER` or `ADMIN` invites you by email and gives you the `invitationToken` from their invitation response. Sign in with the invited email address and accept:

```bash
curl -X POST '{{BASE_URL}}/organizations/org-001/invitations/accept' \
  -H 'Authorization: Bearer {{ACCESS_TOKEN}}' \
  -H 'Content-Type: application/json' \
  -d '{ "invitationToken": "inv-tok-abc123" }'
```

**Response (200 OK):**
```json
{
  "id": "mem-0002",
  "organizationId": "org-001",
  "userId": "c1a2b3c4-d5e6-7890-abcd-ef0987654321",
  "memberRole": "COACH",
  "status": "ACTIVE",
  "joinedAt": "2024-06-15T10:00:00Z"
}
```

The invitation must be sent to your account's email (`403` otherwise), and it expires after 7 days (`409`). Your coach access to an athlete comes from the organization's approved affiliation with that athlete (see ORGANIZATION_USER_GUIDE.md).

---

## 3. Authentication

### Login

```bash
curl -X POST '{{BASE_URL}}/auth/login' \
  -H 'Content-Type: application/json' \
  -d '{
    "email": "tunde.adeyemi@fclagos.ng",
    "password": "C0achPass#2024!"
  }'
```

**Response (200 OK):**
```json
{
  "access_token": "eyJhbGciOiJIUzI1NiJ9...",
  "refresh_token": "c4d5e6f7-a8b9-0123-cdef-01234567890c",
  "user_id": "b2c3d4e5-f6a7-8901-bcde-f12345678901",
  "email": "user@example.com",
  "role": "COACH",
  "expires_in": 3600
}
```

### Token Refresh

```bash
curl -X POST '{{BASE_URL}}/auth/refresh' \
  -H 'Content-Type: application/json' \
  -d '{ "refresh_token": "c4d5e6f7-a8b9-0123-cdef-01234567890c" }\'
```

### Logout

```bash
curl -X POST '{{BASE_URL}}/auth/logout' \
  -H 'Authorization: Bearer {{ACCESS_TOKEN}}'
```

---

## 4. Core Workflows

### Workflow A — View organization members and assigned athletes

```bash
# View all members in your organization
curl -X GET '{{BASE_URL}}/organizations/org-001/members' \
  -H 'Authorization: Bearer {{ACCESS_TOKEN}}'
```

**Response (200 OK):**
```json
{
  "content": [
    {
      "userId": "c1a2b3c4-d5e6-7890-abcd-ef0987654321",
      "fullName": "Tunde Adeyemi",
      "memberRole": "COACH",
      "joinedAt": "2024-06-15T10:00:00Z"
    }
  ]
}
```

```bash
# View athletes affiliated with your organization
curl -X GET '{{BASE_URL}}/organizations/org-001/athletes' \
  -H 'Authorization: Bearer {{ACCESS_TOKEN}}'
```

**Response (200 OK):**
```json
{
  "content": [
    {
      "athleteId": "b2c3d4e5-f6a7-8901-bcde-f12345678901",
      "athleteNumber": "FCB-FB-00042",
      "displayName": "Jordan O.",
      "primarySportCode": "FOOTBALL",
      "assignedCoachId": "c1a2b3c4-d5e6-7890-abcd-ef0987654321"
    }
  ]
}
```

### Workflow B — Submit an activity on behalf of an athlete

```bash
curl -X POST '{{BASE_URL}}/athletes/b2c3d4e5-f6a7-8901-bcde-f12345678901/activities' \
  -H 'Authorization: Bearer {{ACCESS_TOKEN}}' \
  -H 'Content-Type: application/json' \
  -d '{
    "templateId": "tmpl-fb-training-v1",
    "activityTypeId": "at-training",
    "sportId": "sp-football",
    "title": "Tactical session — pressing from front",
    "activityDate": "2024-06-12",
    "location": "FC Lagos Training Ground",
    "organizationId": "org-001",
    "visibilityLevel": "ORGANIZATION",
    "fieldValues": {
      "drillType": "Pressing",
      "durationMinutes": "90",
      "intensityLevel": "HIGH"
    }
  }'
```

**Response (201 Created):**
```json
{
  "id": "act-0200",
  "athleteId": "b2c3d4e5-f6a7-8901-bcde-f12345678901",
  "status": "DRAFT",
  "submittedBy": "c1a2b3c4-d5e6-7890-abcd-ef0987654321"
}
```

### Workflow C — Submit and verify an activity

```bash
# Submit
curl -X POST '{{BASE_URL}}/activities/act-0200/submit' \
  -H 'Authorization: Bearer {{ACCESS_TOKEN}}'

# Publish (coach verification step)
curl -X POST '{{BASE_URL}}/activities/act-0200/publish' \
  -H 'Authorization: Bearer {{ACCESS_TOKEN}}'
```

**Response after publish (200 OK):**
```json
{
  "id": "act-0200",
  "status": "PUBLISHED",
  "verificationStatus": "COACH_VERIFIED"
}
```

### Workflow D — Upload training media

```bash
curl -X POST '{{BASE_URL}}/media/uploads' \
  -H 'Authorization: Bearer {{ACCESS_TOKEN}}' \
  -H 'Content-Type: application/json' \
  -d '{
    "filename": "pressing-drill-2024-06-12.mp4",
    "mimeType": "video/mp4",
    "fileSizeBytes": 78643200
  }'
```

Follow the standard three-step upload (initiate → PUT file → complete).

### Workflow E — View athlete development timeline

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
      "type": "ACTIVITY",
      "label": "Tactical session — pressing from front",
      "status": "PUBLISHED",
      "recordedAt": "2024-06-12"
    }
  ]
}
```

---

## 5. Available Endpoints

> **Access:** Coach access to an athlete's data comes from an access grant: the athlete (or their guardian) grants it, or an organization affiliation creates one. Viewing needs `VIEW_STATS` or higher, and creating or editing activities needs `MANAGE`. Without a grant, `/athletes/{id}/...`, activity and media calls return `403`. Verifying an activity (`POST /activities/{id}/verify`) is allowed for any `COACH`.

| Method | Path | Description |
|---|---|---|
| POST | `/auth/register` | Register account |
| POST | `/auth/login` | Log in |
| POST | `/auth/refresh` | Refresh tokens |
| POST | `/auth/logout` | Log out |
| GET | `/auth/verify-email` | Verify email |
| POST | `/users/me/roles` | Add an extra non-privileged role (`COACH` etc.) |
| GET | `/users/me` | Get account info |
| GET | `/organizations/{id}/members` | List organization members |
| GET | `/organizations/{id}/athletes` | List the organization's athlete affiliations (members only) |
| POST | `/organizations/{id}/invitations/accept` | Accept an invitation (`{ "invitationToken" }`) |
| GET | `/organizations/mine` | Organizations you belong to |
| POST | `/athletes/{id}/activities` | Submit activity for athlete |
| GET | `/athletes/{id}/activities` | List athlete activities |
| PATCH | `/activities/{id}` | Update draft activity |
| POST | `/activities/{id}/submit` | Submit activity |
| POST | `/activities/{id}/publish` | Publish / verify activity |
| GET | `/athletes/{id}/development` | Development timeline |
| GET | `/athletes/{id}/performance` | Performance chart data |
| POST | `/media/uploads` | Initiate media upload |
| POST | `/media/uploads/{id}/complete` | Complete upload |
| GET | `/media/{id}/playback` | Get playback URL |
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

Coaches can access athlete data only for athletes explicitly assigned to them within the organization. Accessing data outside this assignment returns `403 FORBIDDEN`.

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
| 403 | Forbidden | No affiliation or grant for this athlete | `{ "status": 403, "error": "Forbidden", "message": "You do not have VIEW_STATS permission for this athlete." }` |
| 403 | Forbidden | Invitation sent to another email | `{ "status": 403, "error": "Forbidden", "message": "This invitation was sent to a different email address" }` |
| 404 | Not Found | Resource not found | `{ "status": 404, "error": "Not Found", "message": "AthleteActivity not found with id: act-999" }` |
| 409 | Conflict | Activity not in the right state | `{ "status": 409, "error": "Conflict", "message": "Cannot transition AthleteActivity from 'DRAFT' to 'PUBLISHED'" }` |
| 400 | Bad Request | Request body failed validation | `{ "status": 400, "error": "Bad Request", "message": "Validation failed", "fieldErrors": [{ "field": "title", "rejectedValue": null, "message": "must not be blank" }] }` |
