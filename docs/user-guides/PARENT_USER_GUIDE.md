# Parent User Guide

## 1. Role Overview

Parents use TalentHub to manage their child's athlete account. With an approved guardian relationship, a parent can submit activities on behalf of the child, upload media, view the development timeline, and control visibility settings.

---

## 2. Getting Started

### Step 1 — Register an account

```bash
curl -X POST '{{BASE_URL}}/auth/register' \
  -H 'Content-Type: application/json' \
  -d '{
    "email": "amaka.okafor@example.com",
    "password": "Secur3Parent#2024",
    "role": "PARENT"
  }'
```

**Response (201 Created):**
```json
{
  "userId": "p1a2b3c4-d5e6-7890-abcd-ef9876543210",
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
  "role": "PARENT",
  "roles": ["PARENT"],
  "status": "ACTIVE",
  "emailVerified": true
}
```

To hold an additional role, call `POST {{BASE_URL}}/users/me/roles` with `{ "role": "COACH" }`. Allowed roles are `ATHLETE`, `COACH`, `PARENT`, `GUARDIAN`, `AGENT` and `SCOUT`. `ADMIN`, `SUPER_ADMIN` and `ORG_ADMIN` are rejected with `400`, and adding a role you already hold returns `409`. New roles appear in tokens issued after your next login or refresh.

### Step 3 — Request a relationship with your child

```bash
curl -X POST '{{BASE_URL}}/guardian-relationships' \
  -H 'Authorization: Bearer {{ACCESS_TOKEN}}' \
  -H 'Content-Type: application/json' \
  -d '{
    "athleteId": "b2c3d4e5-f6a7-8901-bcde-f12345678901",
    "relationshipType": "PARENT"
  }'
```

**Response (201 Created):**
```json
{
  "id": "gr-0001",
  "guardianUserId": "p1a2b3c4-d5e6-7890-abcd-ef9876543210",
  "athleteUserId": "b2c3d4e5-f6a7-8901-bcde-f12345678901",
  "relationshipType": "PARENT",
  "relationship": "PARENT",
  "permission": null,
  "status": "PENDING",
  "consentGiven": false,
  "verificationDocumentRef": null,
  "createdAt": "2024-06-15T10:05:00Z",
  "approvedAt": null,
  "expiresAt": null,
  "revokedAt": null
}
```

### Step 4 — Child athlete approves the relationship

The child athlete must log in and approve the request:

```bash
curl -X POST '{{BASE_URL}}/guardian-relationships/gr-0001/approve' \
  -H 'Authorization: Bearer {{ATHLETE_ACCESS_TOKEN}}'
```

**Response (200 OK):**
```json
{
  "id": "gr-0001",
  "guardianUserId": "p1a2b3c4-d5e6-7890-abcd-ef9876543210",
  "athleteUserId": "b2c3d4e5-f6a7-8901-bcde-f12345678901",
  "relationshipType": "PARENT",
  "relationship": "PARENT",
  "permission": null,
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
    "email": "amaka.okafor@example.com",
    "password": "Secur3Parent#2024"
  }'
```

**Response (200 OK):**
```json
{
  "access_token": "eyJhbGciOiJIUzI1NiJ9...",
  "refresh_token": "f1e2d3c4-b5a6-9870-dcba-098765432101",
  "user_id": "b2c3d4e5-f6a7-8901-bcde-f12345678901",
  "email": "user@example.com",
  "role": "PARENT",
  "expires_in": 3600
}
```

### Token Refresh

```bash
curl -X POST '{{BASE_URL}}/auth/refresh' \
  -H 'Content-Type: application/json' \
  -d '{ "refresh_token": "f1e2d3c4-b5a6-9870-dcba-098765432101" }\'
```

### Logout

```bash
curl -X POST '{{BASE_URL}}/auth/logout' \
  -H 'Authorization: Bearer {{ACCESS_TOKEN}}'
```

---

## 4. Core Workflows

### Workflow A — View your linked athletes

```bash
curl -X GET '{{BASE_URL}}/guardians/me/athletes' \
  -H 'Authorization: Bearer {{ACCESS_TOKEN}}'
```

**Response (200 OK):**
```json
[{
  "id": "gr-0001",
  "guardianUserId": "p1a2b3c4-d5e6-7890-abcd-ef9876543210",
  "athleteUserId": "b2c3d4e5-f6a7-8901-bcde-f12345678901",
  "relationshipType": "PARENT",
  "relationship": "PARENT",
  "permission": null,
  "status": "ACTIVE",
  "consentGiven": true,
  "verificationDocumentRef": null,
  "createdAt": "2024-06-15T10:05:00Z",
  "approvedAt": "2024-06-15T11:00:00Z",
  "expiresAt": null,
  "revokedAt": null
}]
```

### Workflow B — Submit an activity on behalf of your child

This uses the same activity endpoint as the athlete but is called with your parent access token. Your guardian relationship grants you MANAGE permission over the athlete's activities.

```bash
curl -X POST '{{BASE_URL}}/athletes/b2c3d4e5-f6a7-8901-bcde-f12345678901/activities' \
  -H 'Authorization: Bearer {{ACCESS_TOKEN}}' \
  -H 'Content-Type: application/json' \
  -d '{
    "templateId": "tmpl-fb-match-v1",
    "activityTypeId": "at-match",
    "sportId": "sp-football",
    "title": "U-15 League — Home vs Ibadan City FC",
    "activityDate": "2024-06-08",
    "location": "Academy Training Ground, Lagos",
    "visibilityLevel": "PUBLIC",
    "fieldValues": {
      "opponent": "Ibadan City FC",
      "goals": "1",
      "minutesPlayed": "70"
    }
  }'
```

**Response (201 Created):**
```json
{
  "id": "act-0099",
  "athleteId": "b2c3d4e5-f6a7-8901-bcde-f12345678901",
  "status": "DRAFT",
  "submittedBy": "p1a2b3c4-d5e6-7890-abcd-ef9876543210"
}
```

Submit and publish follow the same steps as the athlete workflow.

### Workflow C — Upload child's video

```bash
curl -X POST '{{BASE_URL}}/media/uploads' \
  -H 'Authorization: Bearer {{ACCESS_TOKEN}}' \
  -H 'Content-Type: application/json' \
  -d '{
    "filename": "jordan-u15-highlights-june.mp4",
    "mimeType": "video/mp4",
    "fileSizeBytes": 31457280
  }'
```

Follow the same three-step upload flow described in the Athlete guide (initiate → PUT file → complete).

### Workflow D — View child's development timeline

```bash
curl -X GET '{{BASE_URL}}/athletes/b2c3d4e5-f6a7-8901-bcde-f12345678901/development' \
  -H 'Authorization: Bearer {{ACCESS_TOKEN}}'
```

---

## 5. Available Endpoints

> **Access:** Every `/athletes/{id}/...` call, plus activity and media calls for your child, works only while your `PARENT` link is `ACTIVE`. Use the child's user id (`athleteUserId` from `GET /guardians/me/athletes`) as `{id}`. If the link is pending, rejected or revoked, these calls return `403`.

### Guardian Relationship Management

#### POST /guardian-relationships

Request a new guardian-athlete link.

```bash
curl -X POST '{{BASE_URL}}/guardian-relationships' \
  -H 'Authorization: Bearer {{ACCESS_TOKEN}}' \
  -H 'Content-Type: application/json' \
  -d '{
    "athleteId": "b2c3d4e5-f6a7-8901-bcde-f12345678901",
    "relationshipType": "PARENT"
  }'
```

**Response (201 Created):** relationship object with `status: "PENDING"`. Requires the `PARENT` role (403 otherwise).

**Common errors:**
- `409` — an open `PARENT` relationship already exists for this pair
- `404` — `athleteId` is not an athlete user id
- `400` — you tried to link yourself

#### GET /guardian-relationships

```bash
curl -X GET '{{BASE_URL}}/guardian-relationships' \
  -H 'Authorization: Bearer {{ACCESS_TOKEN}}'
```

**Response (200 OK):** every relationship you are part of, as requester or as athlete, newest first, in any status (`PENDING`, `PENDING_ADMIN_VERIFICATION`, `ACTIVE`, `REVOKED`, `REJECTED`). An `ACTIVE` link past its `expiresAt` shows as `EXPIRED`.

#### POST /guardian-relationships/{id}/approve

Approve a `PENDING` request. Only the athlete can call this (403 for anyone else, including the parent). 409 if the link is not `PENDING`.

#### POST /guardian-relationships/{id}/reject

The athlete declines a `PENDING` request. Optional body `{ "reason": "..." }`. The link becomes `REJECTED`.

#### POST /guardian-relationships/{id}/revoke

```bash
curl -X POST '{{BASE_URL}}/guardian-relationships/gr-0001/revoke' \
  -H 'Authorization: Bearer {{ACCESS_TOKEN}}'
```

**Response (200 OK):**
```json
{
  "id": "gr-0001",
  "guardianUserId": "p1a2b3c4-d5e6-7890-abcd-ef9876543210",
  "athleteUserId": "b2c3d4e5-f6a7-8901-bcde-f12345678901",
  "relationshipType": "PARENT",
  "relationship": "PARENT",
  "permission": null,
  "status": "REVOKED",
  "consentGiven": true,
  "verificationDocumentRef": null,
  "createdAt": "2024-06-15T10:05:00Z",
  "approvedAt": "2024-06-15T11:00:00Z",
  "expiresAt": null,
  "revokedAt": "2024-07-01T08:00:00Z"
}
```

### Athlete Data (with approved relationship)

| Method | Path | Description |
|---|---|---|
| GET | `/guardians/me/athletes` | List linked athletes |
| POST | `/athletes/{id}/activities` | Submit activity on behalf of child |
| GET | `/athletes/{id}/activities` | List child's activities |
| POST | `/activities/{id}/submit` | Submit activity |
| POST | `/activities/{id}/publish` | Publish activity |
| GET | `/athletes/{id}/development` | Development timeline |
| GET | `/athletes/{id}/performance` | Performance chart data |
| POST | `/media/uploads` | Initiate video/image upload |
| POST | `/media/uploads/{id}/complete` | Complete upload |
| GET | `/media/{id}/playback` | Get playback URL |

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

With an `ACTIVE` relationship you have `MANAGE` access, so you can read, grant and withdraw consent for the athlete. Use the athlete's user id as `{id}`.

```bash
# Current and past consent records
curl -X GET '{{BASE_URL}}/athletes/b2c3d4e5-f6a7-8901-bcde-f12345678901/consent' \
  -H 'Authorization: Bearer {{ACCESS_TOKEN}}'

# Grant consent for a purpose
curl -X POST '{{BASE_URL}}/athletes/b2c3d4e5-f6a7-8901-bcde-f12345678901/consent' \
  -H 'Authorization: Bearer {{ACCESS_TOKEN}}' \
  -H 'Content-Type: application/json' \
  -d '{ "purpose": "PROFILE_DISCOVERY", "consentVersion": "2024-06", "authorizationSource": "GUARDIAN" }'

# Withdraw it
curl -X DELETE '{{BASE_URL}}/athletes/b2c3d4e5-f6a7-8901-bcde-f12345678901/consent/PROFILE_DISCOVERY' \
  -H 'Authorization: Bearer {{ACCESS_TOKEN}}'
```

- `purpose` is `PROFILE_DISCOVERY`, `PERFORMANCE_SHARING`, `MEDIA_SHARING` or `RESEARCH`.
- Granting returns `201` with the consent record, and withdrawing returns `204`.
- Withdrawing a purpose that has no active consent returns `404`.
- Without an active relationship, all three calls return `403`.

---

## 8. Safeguarding Notes

**Minor athlete protections on TalentHub:**

- A parent relationship must be in `ACTIVE` status before you can act on the child's data.
- Either party (athlete or parent) can revoke the guardian relationship at any time. Revocation does not delete activities or media, but removes the parent's management permissions immediately.
- For a child under 18, discovery consent (`PROFILE_DISCOVERY`) can only be granted while your `PARENT` link is `ACTIVE`.
- Parents can contact `safeguarding@talenthub.io` to report a concern or escalate to the platform safeguarding team.

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
| 403 | Forbidden | No active PARENT link, or the wrong role for the request | `{ "status": 403, "error": "Forbidden", "message": "You do not have MANAGE permission for this athlete." }` |
| 403 | Forbidden | Only the athlete can approve a PARENT request | `{ "status": 403, "error": "Forbidden", "message": "Only the athlete can decide on this relationship" }` |
| 404 | Not Found | Relationship or athlete not found | `{ "status": 404, "error": "Not Found", "message": "GuardianLink not found with id: gr-9999" }` |
| 409 | Conflict | Open relationship already exists | `{ "status": 409, "error": "Conflict", "message": "A PARENT relationship with this athlete already exists" }` |
| 409 | Conflict | Relationship not in the right state | `{ "status": 409, "error": "Conflict", "message": "Cannot transition GuardianLink from 'REVOKED' to 'ACTIVE'" }` |
| 400 | Bad Request | Request body failed validation | `{ "status": 400, "error": "Bad Request", "message": "Validation failed", "fieldErrors": [{ "field": "athleteId", "rejectedValue": null, "message": "must not be blank" }] }` |
