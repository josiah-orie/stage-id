# Guardian User Guide

## 1. Role Overview

Legal guardians are adults with formal legal authority over a minor athlete — e.g., a court-appointed guardian, a school welfare officer, or an orphanage representative. The GUARDIAN role differs from PARENT in that the platform requires document verification before the relationship becomes active. Legal guardians have the same management capabilities as parents once approved, plus access to an extended safeguarding audit trail.

---

## 2. Getting Started

### Step 1 — Register an account

```bash
curl -X POST '{{BASE_URL}}/auth/register' \
  -H 'Content-Type: application/json' \
  -d '{
    "email": "chidinma.eze@example.com",
    "password": "L3galGuard#2024!",
    "role": "GUARDIAN"
  }'
```

**Response (201 Created):**
```json
{
  "userId": "g1b2c3d4-e5f6-7890-cdef-ab1234567890",
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
  "role": "GUARDIAN",
  "roles": ["GUARDIAN"],
  "status": "ACTIVE",
  "emailVerified": true
}
```

To hold an additional role, call `POST {{BASE_URL}}/users/me/roles` with `{ "role": "COACH" }`. Allowed roles are `ATHLETE`, `COACH`, `PARENT`, `GUARDIAN`, `AGENT` and `SCOUT`. `ADMIN`, `SUPER_ADMIN` and `ORG_ADMIN` are rejected with `400`, and adding a role you already hold returns `409`. New roles appear in tokens issued after your next login or refresh.

### Step 3 — Request a LEGAL_GUARDIAN relationship with the athlete

Legal guardian relationships require a verification document upload. Include the `verificationDocumentRef` once you have uploaded the document via the media upload endpoint.

```bash
curl -X POST '{{BASE_URL}}/guardian-relationships' \
  -H 'Authorization: Bearer {{ACCESS_TOKEN}}' \
  -H 'Content-Type: application/json' \
  -d '{
    "athleteId": "b2c3d4e5-f6a7-8901-bcde-f12345678901",
    "relationshipType": "LEGAL_GUARDIAN",
    "verificationDocumentRef": "doc-media-id-0001"
  }'
```

**Response (201 Created):**
```json
{
  "id": "gr-0002",
  "guardianUserId": "g1b2c3d4-e5f6-7890-cdef-ab1234567890",
  "athleteUserId": "b2c3d4e5-f6a7-8901-bcde-f12345678901",
  "relationshipType": "LEGAL_GUARDIAN",
  "relationship": "LEGAL_GUARDIAN",
  "permission": null,
  "status": "PENDING_ADMIN_VERIFICATION",
  "consentGiven": false,
  "verificationDocumentRef": "doc-media-id-0001",
  "createdAt": "2024-06-15T10:05:00Z",
  "approvedAt": null,
  "expiresAt": null,
  "revokedAt": null
}
```

Unlike a PARENT relationship, `LEGAL_GUARDIAN` requires admin review before activation. The athlete does not need to independently approve it.

### Step 4 — Admin reviews and approves

An admin will review the uploaded verification document and either approve or reject the request. You will receive an in-app notification and email when the review is complete.

```bash
# Check relationship status
curl -X GET '{{BASE_URL}}/guardian-relationships' \
  -H 'Authorization: Bearer {{ACCESS_TOKEN}}'
```

**Response once approved:**
```json
[{
  "id": "gr-0002",
  "guardianUserId": "g1b2c3d4-e5f6-7890-cdef-ab1234567890",
  "athleteUserId": "b2c3d4e5-f6a7-8901-bcde-f12345678901",
  "relationshipType": "LEGAL_GUARDIAN",
  "relationship": "LEGAL_GUARDIAN",
  "permission": null,
  "status": "ACTIVE",
  "consentGiven": true,
  "verificationDocumentRef": "doc-media-id-0001",
  "createdAt": "2024-06-15T10:05:00Z",
  "approvedAt": "2024-06-16T09:00:00Z",
  "expiresAt": "2025-06-16T09:00:00Z",
  "revokedAt": null
}]
```

`expiresAt` is one year after approval. When it passes, the status shows as `EXPIRED` and your access stops. To renew, submit a new `LEGAL_GUARDIAN` request with a current document. An admin decides it with `POST /guardian-relationships/{id}/approve` or `/reject`.

---

## 3. Authentication

### Login

```bash
curl -X POST '{{BASE_URL}}/auth/login' \
  -H 'Content-Type: application/json' \
  -d '{
    "email": "chidinma.eze@example.com",
    "password": "L3galGuard#2024!"
  }'
```

**Response (200 OK):**
```json
{
  "access_token": "eyJhbGciOiJIUzI1NiJ9...",
  "refresh_token": "a2b3c4d5-e6f7-8901-bcde-f01234567890",
  "user_id": "b2c3d4e5-f6a7-8901-bcde-f12345678901",
  "email": "user@example.com",
  "role": "GUARDIAN",
  "expires_in": 3600
}
```

### Token Refresh

```bash
curl -X POST '{{BASE_URL}}/auth/refresh' \
  -H 'Content-Type: application/json' \
  -d '{ "refresh_token": "a2b3c4d5-e6f7-8901-bcde-f01234567890" }\'
```

### Logout

```bash
curl -X POST '{{BASE_URL}}/auth/logout' \
  -H 'Authorization: Bearer {{ACCESS_TOKEN}}'
```

---

## 4. Core Workflows

### Workflow A — Upload a verification document

Before requesting a LEGAL_GUARDIAN relationship, upload the court order or guardianship certificate.

```bash
curl -X POST '{{BASE_URL}}/media/uploads' \
  -H 'Authorization: Bearer {{ACCESS_TOKEN}}' \
  -H 'Content-Type: application/json' \
  -d '{
    "filename": "court-order-guardianship-2024.pdf",
    "mimeType": "application/pdf",
    "fileSizeBytes": 204800
  }'
```

**Response (201 Created):**
```json
{
  "uploadId": "doc-media-id-0001",
  "presignedUrl": "https://minio.talenthub.io/...",
  "expiresAt": "2024-06-15T11:00:00Z"
}
```

PUT the file to the presigned URL, then call `/media/uploads/doc-media-id-0001/complete`. Use the returned `mediaAssetId` as `verificationDocumentRef`.

### Workflow B — View linked athletes (post-approval)

```bash
curl -X GET '{{BASE_URL}}/guardians/me/athletes' \
  -H 'Authorization: Bearer {{ACCESS_TOKEN}}'
```

**Response (200 OK):**
```json
[{
  "id": "gr-0002",
  "guardianUserId": "g1b2c3d4-e5f6-7890-cdef-ab1234567890",
  "athleteUserId": "b2c3d4e5-f6a7-8901-bcde-f12345678901",
  "relationshipType": "LEGAL_GUARDIAN",
  "relationship": "LEGAL_GUARDIAN",
  "permission": null,
  "status": "ACTIVE",
  "consentGiven": true,
  "verificationDocumentRef": "doc-media-id-0001",
  "createdAt": "2024-06-15T10:05:00Z",
  "approvedAt": "2024-06-16T09:00:00Z",
  "expiresAt": "2025-06-16T09:00:00Z",
  "revokedAt": null
}]
```

### Workflow C — Submit activity on behalf of the athlete

```bash
curl -X POST '{{BASE_URL}}/athletes/b2c3d4e5-f6a7-8901-bcde-f12345678901/activities' \
  -H 'Authorization: Bearer {{ACCESS_TOKEN}}' \
  -H 'Content-Type: application/json' \
  -d '{
    "templateId": "tmpl-fb-match-v1",
    "activityTypeId": "at-match",
    "sportId": "sp-football",
    "title": "Academy Cup — Semi-final",
    "activityDate": "2024-06-20",
    "location": "National Stadium, Abuja",
    "visibilityLevel": "MANAGERS",
    "fieldValues": {
      "opponent": "Kogi Youths FC",
      "goals": "1",
      "minutesPlayed": "85"
    }
  }'
```

`visibilityLevel` is `PUBLIC`, `ORGANIZATION`, `MANAGERS` or `PRIVATE`. Use `MANAGERS` to keep the activity to people who manage the athlete (you, other guardians, and grant holders); scouts only ever see `PUBLIC` activities.

### Workflow D — Revoke the guardian relationship

```bash
curl -X POST '{{BASE_URL}}/guardian-relationships/gr-0002/revoke' \
  -H 'Authorization: Bearer {{ACCESS_TOKEN}}'
```

**Response (200 OK):**
```json
{
  "id": "gr-0002",
  "guardianUserId": "g1b2c3d4-e5f6-7890-cdef-ab1234567890",
  "athleteUserId": "b2c3d4e5-f6a7-8901-bcde-f12345678901",
  "relationshipType": "LEGAL_GUARDIAN",
  "relationship": "LEGAL_GUARDIAN",
  "permission": null,
  "status": "REVOKED",
  "consentGiven": true,
  "verificationDocumentRef": "doc-media-id-0001",
  "createdAt": "2024-06-15T10:05:00Z",
  "approvedAt": "2024-06-16T09:00:00Z",
  "expiresAt": "2025-06-16T09:00:00Z",
  "revokedAt": "2024-08-01T12:00:00Z"
}
```

---

## 5. Available Endpoints

> **Access:** Every `/athletes/{id}/...` call, plus activity and media calls for the athlete, works only while your `LEGAL_GUARDIAN` link is `ACTIVE` and not expired. Use `athleteUserId` from `GET /guardians/me/athletes` as `{id}`. After `expiresAt`, these calls return `403` until a renewed request is approved.

| Method | Path | Description |
|---|---|---|
| POST | `/auth/register` | Register account |
| POST | `/auth/login` | Log in |
| POST | `/auth/refresh` | Refresh tokens |
| POST | `/auth/logout` | Log out |
| GET | `/auth/verify-email` | Verify email |
| POST | `/users/me/roles` | Add an extra non-privileged role (`GUARDIAN` etc.) |
| GET | `/users/me` | Get own account info |
| POST | `/guardian-relationships` | Request LEGAL_GUARDIAN link |
| GET | `/guardian-relationships` | List own relationships |
| POST | `/guardian-relationships/{id}/revoke` | Revoke relationship |
| POST | `/guardian-relationships/{id}/approve`, `/reject` | Admin decision on a `PENDING_ADMIN_VERIFICATION` request |
| GET | `/guardians/me/athletes` | List linked athletes |
| POST | `/athletes/{id}/activities` | Submit activity on behalf of athlete |
| GET | `/athletes/{id}/activities` | View athlete activities |
| POST | `/activities/{id}/submit` | Submit activity |
| POST | `/activities/{id}/publish` | Publish activity |
| GET | `/athletes/{id}/development` | Development timeline |
| GET | `/athletes/{id}/performance` | Performance data |
| POST | `/media/uploads` | Initiate upload (documents or media) |
| POST | `/media/uploads/{id}/complete` | Complete upload |
| GET | `/media/{id}/playback` | Get playback URL |
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

With an `ACTIVE`, unexpired `LEGAL_GUARDIAN` relationship you have `MANAGE` access, so you can read, grant and withdraw consent for the athlete. Use the athlete's user id as `{id}`.

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

## 8. Safeguarding Notes — Extended

**Legal Guardian verification:**

- Every `LEGAL_GUARDIAN` relationship requires a signed court order, guardianship certificate, or equivalent document.
- Platform admins review the document before the relationship is activated. 
- Relationships expire 365 days after approval. After that your management access stops (calls return `403`) until a new request is approved. The athlete's data is not removed.
- If the relationship is revoked by the athlete or an admin, your access to the athlete's data is suspended immediately.

**Reporting concerns:**

- Use `safeguarding@talenthub.io` to report any safeguarding concern immediately.
- Emergency revocations (e.g., a court order is overturned) are processed within 4 hours during business hours.

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
| 403 | Forbidden | Link pending, rejected, revoked or expired | `{ "status": 403, "error": "Forbidden", "message": "You do not have MANAGE permission for this athlete." }` |
| 403 | Forbidden | Missing GUARDIAN role | `{ "status": 403, "error": "Forbidden", "message": "The GUARDIAN role is required to request a LEGAL_GUARDIAN relationship" }` |
| 404 | Not Found | Relationship or athlete not found | `{ "status": 404, "error": "Not Found", "message": "GuardianLink not found with id: gr-9999" }` |
| 409 | Conflict | Open relationship already exists | `{ "status": 409, "error": "Conflict", "message": "A LEGAL_GUARDIAN relationship with this athlete already exists" }` |
| 400 | Bad Request | Missing verification document | `{ "status": 400, "error": "Bad Request", "message": "verificationDocumentRef is required for a LEGAL_GUARDIAN relationship" }` |
| 400 | Bad Request | Request body failed validation | `{ "status": 400, "error": "Bad Request", "message": "Validation failed", "fieldErrors": [{ "field": "athleteId", "rejectedValue": null, "message": "must not be blank" }] }` |
