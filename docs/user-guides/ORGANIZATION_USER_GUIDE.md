# Organization User Guide

## 1. Role Overview

Organizations on TalentHub represent clubs, academies, schools, and professional teams. Within an organization, members operate in differentiated roles:

| Member Role | Responsibilities |
|---|---|
| OWNER | Registers the organization, manages subscription, assigns seat allocations. Exactly one per organization |
| ADMIN | Manages members, reviews athlete affiliations, configures org settings |
| COACH | Submits activities, verifies performance for assigned athletes |
| SCOUT | Uses shared scouting workspace, creates org-level evaluations and watchlists |
| ATHLETE_MANAGER | Requests athlete affiliations, manages the club roster |
| STAFF | View-only access to org activity and reports |

---

## 2. Getting Started

### OWNER: Register an organization

Any signed-in user can register an organization. You become its `OWNER` member and gain the `ORG_ADMIN` platform role. Log in again (or refresh your token) so the new role appears in your token.

```bash
curl -X POST '{{BASE_URL}}/organizations' \
  -H 'Authorization: Bearer {{ACCESS_TOKEN}}' \
  -H 'Content-Type: application/json' \
  -d '{
    "name": "FC Lagos Academy",
    "orgType": "ACADEMY",
    "countryCode": "NG",
    "city": "Lagos",
    "description": "Premier youth development academy based in Lagos"
  }'
```

`orgType` is one of `CLUB`, `SCHOOL`, `ACADEMY`, `FEDERATION`, `NATIONAL_TEAM`, `UNIVERSITY`, `COLLEGE`, `PROFESSIONAL_TEAM`, `SCOUTING_AGENCY`. `countryCode` is a 2-letter ISO code.

**Response (201 Created):**
```json
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
```

### OWNER: Wait for verification

New organizations are `PENDING_VERIFICATION`. A platform administrator approves (status becomes `ACTIVE`) or rejects (status becomes `DEACTIVATED`, with a `rejectionReason`). Until then you can view and edit the organization, but invitations and athlete affiliation requests return `403` with the message `Organization is pending verification`.

Check the status at any time:

```bash
curl -X GET '{{BASE_URL}}/organizations/0f6c2a7e-0000-4000-8000-000000000001' \
  -H 'Authorization: Bearer {{ACCESS_TOKEN}}'
```

### OWNER or ADMIN: Invite staff

```bash
curl -X POST '{{BASE_URL}}/organizations/0f6c2a7e-0000-4000-8000-000000000001/invitations' \
  -H 'Authorization: Bearer {{ACCESS_TOKEN}}' \
  -H 'Content-Type: application/json' \
  -d '{
    "email": "tunde.adeyemi@fclagos.ng",
    "memberRole": "COACH"
  }'
```

`memberRole` is `ADMIN`, `COACH`, `SCOUT`, `ATHLETE_MANAGER` or `STAFF`. `OWNER` cannot be invited (`400`). A second pending invitation for the same email returns `409`.

**Response (201 Created):**
```json
{
  "id": "inv-0001",
  "organizationId": "0f6c2a7e-0000-4000-8000-000000000001",
  "email": "tunde.adeyemi@fclagos.ng",
  "memberRole": "COACH",
  "status": "PENDING",
  "invitationToken": "7b1f0e9a-3c55-4d2e-9a61-0c4f5e2b8d11",
  "expiresAt": "2024-06-22T09:00:00Z"
}
```

Send the `invitationToken` to the invitee. It is only returned here; `GET /organizations/{id}/invitations` lists pending invitations without tokens. Invitations expire after 7 days.

### Invitee: Accept the invitation

The invitee signs in with the invited email address and accepts:

```bash
curl -X POST '{{BASE_URL}}/organizations/0f6c2a7e-0000-4000-8000-000000000001/invitations/accept' \
  -H 'Authorization: Bearer {{ACCESS_TOKEN}}' \
  -H 'Content-Type: application/json' \
  -d '{ "invitationToken": "7b1f0e9a-3c55-4d2e-9a61-0c4f5e2b8d11" }'
```

**Response (200 OK):**
```json
{
  "id": "mem-0002",
  "organizationId": "0f6c2a7e-0000-4000-8000-000000000001",
  "userId": "c1a2b3c4-d5e6-7890-abcd-ef0987654321",
  "memberRole": "COACH",
  "status": "ACTIVE",
  "joinedAt": "2024-06-15T10:00:00Z"
}
```

A different account email returns `403`. An expired or already-used invitation returns `409`. Accepting as `ADMIN` also grants the `ORG_ADMIN` platform role.

---

## 3. Authentication

All organization members authenticate using the standard flow.

### Login

```bash
curl -X POST '{{BASE_URL}}/auth/login' \
  -H 'Content-Type: application/json' \
  -d '{
    "email": "emeka.obi@fclagos.ng",
    "password": "OrgOwner#2024!"
  }'
```

**Response (200 OK):**
```json
{
  "access_token": "eyJhbGciOiJIUzI1NiJ9...",
  "refresh_token": "e6f7a8b9-c0d1-2345-ef01-234567890abc",
  "user_id": "e1o2w3n4-0000-4000-8000-000000000001",
  "email": "emeka.obi@fclagos.ng",
  "role": "ATHLETE",
  "expires_in": 3600
}
```

`role` is your primary role. The token's `roles` claim carries every role you hold, including `ORG_ADMIN`.

### Token Refresh

```bash
curl -X POST '{{BASE_URL}}/auth/refresh' \
  -H 'Content-Type: application/json' \
  -d '{ "refresh_token": "e6f7a8b9-c0d1-2345-ef01-234567890abc" }'
```

---

## 4. Core Workflows by Role

Member roles are per organization. The platform role (`ORG_ADMIN`, `COACH`, `SCOUT`) decides which endpoint families you can call; your member role in this organization decides what you can do inside it.

| Action | OWNER | ADMIN | ATHLETE_MANAGER | COACH | SCOUT | STAFF |
|---|---|---|---|---|---|---|
| View organization, members, athletes | ✔ | ✔ | ✔ | ✔ | ✔ | ✔ |
| Edit organization, invite, remove members | ✔ | ✔ | | | | |
| Request and end athlete affiliations | ✔ | ✔ | ✔ | | | |
| Work with affiliated athletes' data | ✔ | ✔ | ✔ | ✔ | | |
| Activate scouting workspace, add seats | ✔ | ✔ | | | | |
| View org watchlists and evaluations | ✔ | ✔ | ✔ | ✔ | ✔ | ✔ |

Platform `ADMIN` and `SUPER_ADMIN` can do all of these for any organization. Non-members get `403`.

### OWNER or ADMIN — Activate scouting workspace

```bash
curl -X POST '{{BASE_URL}}/organizations/0f6c2a7e-0000-4000-8000-000000000001/scouting/workspace/activate' \
  -H 'Authorization: Bearer {{ACCESS_TOKEN}}'
```

**Response (200 OK):** empty body. Requires the `ORG_ADMIN` platform role and `OWNER` or `ADMIN` membership of this organization.

### OWNER or ADMIN — Assign a scout seat

```bash
curl -X POST '{{BASE_URL}}/organizations/0f6c2a7e-0000-4000-8000-000000000001/scouting/seats?subscriptionId=sub-0001&userId=sc1a2b3c-0000-4000-8000-000000000001' \
  -H 'Authorization: Bearer {{ACCESS_TOKEN}}'
```

`subscriptionId` and `userId` are query parameters. Going over the plan's `MAX_SCOUT_SEATS` returns `402`.

**Response (200 OK):**
```json
{
  "id": "seat-0001",
  "subscriptionId": "sub-0001",
  "seatType": "SCOUT",
  "assignedToUserId": "sc1a2b3c-0000-4000-8000-000000000001",
  "assignedAt": "2024-06-16T10:05:00Z",
  "revokedAt": null
}
```

### Any member — View members

```bash
curl -X GET '{{BASE_URL}}/organizations/0f6c2a7e-0000-4000-8000-000000000001/members' \
  -H 'Authorization: Bearer {{ACCESS_TOKEN}}'
```

**Response (200 OK):** an array, not paginated.
```json
[
  {
    "id": "mem-0001",
    "organizationId": "0f6c2a7e-0000-4000-8000-000000000001",
    "userId": "e1o2w3n4-0000-4000-8000-000000000001",
    "memberRole": "OWNER",
    "status": "ACTIVE",
    "joinedAt": "2024-06-14T09:00:00Z"
  },
  {
    "id": "mem-0002",
    "organizationId": "0f6c2a7e-0000-4000-8000-000000000001",
    "userId": "c1a2b3c4-d5e6-7890-abcd-ef0987654321",
    "memberRole": "COACH",
    "status": "ACTIVE",
    "joinedAt": "2024-06-15T10:00:00Z"
  }
]
```

Remove a member with `DELETE /organizations/{id}/members/{userId}` (`OWNER` or `ADMIN`), or leave yourself by passing your own user id. The `OWNER` cannot be removed (`400`).

### OWNER, ADMIN or ATHLETE_MANAGER — Request athlete affiliation

```bash
curl -X POST '{{BASE_URL}}/organizations/0f6c2a7e-0000-4000-8000-000000000001/athlete-relationships' \
  -H 'Authorization: Bearer {{ACCESS_TOKEN}}' \
  -H 'Content-Type: application/json' \
  -d '{
    "athleteId": "b2c3d4e5-f6a7-8901-bcde-f12345678901",
    "permission": "MANAGE"
  }'
```

`athleteId` is the athlete's user id. `permission` is the access level your staff get once approved: `VIEW_PROFILE`, `VIEW_STATS` (default), `VIEW_DOCUMENTS`, `CONTACT` or `MANAGE`. A pending or active affiliation for the same athlete returns `409`.

**Response (201 Created):**
```json
{
  "id": "aff-0001",
  "organizationId": "0f6c2a7e-0000-4000-8000-000000000001",
  "athleteUserId": "b2c3d4e5-f6a7-8901-bcde-f12345678901",
  "permission": "MANAGE",
  "status": "PENDING",
  "requestedBy": "e1o2w3n4-0000-4000-8000-000000000001",
  "createdAt": "2024-06-15T11:00:00Z",
  "decidedAt": null,
  "endedAt": null
}
```

The athlete approves it with `POST /organization-affiliations/{id}/approve` (or rejects with `/reject`). Once `ACTIVE`, members with the `OWNER`, `ADMIN`, `COACH` or `ATHLETE_MANAGER` role get that permission on the athlete's data. `POST /organization-affiliations/{id}/end` ends it (athlete or an organization manager) and removes the access.

### Any member — View affiliated athletes

```bash
curl -X GET '{{BASE_URL}}/organizations/0f6c2a7e-0000-4000-8000-000000000001/athletes?status=ACTIVE' \
  -H 'Authorization: Bearer {{ACCESS_TOKEN}}'
```

`status` defaults to `ACTIVE`; use `PENDING`, `REJECTED` or `ENDED` to see others. Returns an array of affiliations as shown above.

### COACH — Submit an activity for an affiliated athlete

Needs a `MANAGE` affiliation. With `VIEW_STATS` you can read the athlete's activities, development and performance but not change them.

```bash
curl -X POST '{{BASE_URL}}/athletes/b2c3d4e5-f6a7-8901-bcde-f12345678901/activities' \
  -H 'Authorization: Bearer {{ACCESS_TOKEN}}' \
  -H 'Content-Type: application/json' \
  -d '{
    "templateId": "tmpl-fb-training-v1",
    "activityTypeId": "at-training",
    "sportId": "sp-football",
    "title": "Sprint conditioning — phase 1",
    "activityDate": "2024-06-13",
    "organizationId": "0f6c2a7e-0000-4000-8000-000000000001",
    "visibilityLevel": "ORGANIZATION",
    "fieldValues": {
      "durationMinutes": "60",
      "intensityLevel": "MEDIUM"
    }
  }'
```

### Any member — View org watchlists and evaluations

```bash
curl -X GET '{{BASE_URL}}/organizations/0f6c2a7e-0000-4000-8000-000000000001/scouting/watchlists' \
  -H 'Authorization: Bearer {{ACCESS_TOKEN}}'
curl -X GET '{{BASE_URL}}/organizations/0f6c2a7e-0000-4000-8000-000000000001/scouting/evaluations' \
  -H 'Authorization: Bearer {{ACCESS_TOKEN}}'
```

Both return arrays. They need the `ORG_ADMIN` or `SCOUT` platform role and membership of this organization.

---

## 5. Available Endpoints

### Organization Management

| Method | Path | Who | Description |
|---|---|---|---|
| POST | `/organizations` | Any signed-in user | Register organization; caller becomes `OWNER` and `ORG_ADMIN` |
| GET | `/organizations/mine` | Any signed-in user | Organizations you are a member of |
| GET | `/organizations/{id}` | Any signed-in user | Organization details |
| PATCH | `/organizations/{id}` | OWNER, ADMIN | Update `name`, `countryCode`, `city`, `description`, `logoUrl` |
| GET | `/organizations/{id}/members` | Members | List active members |
| DELETE | `/organizations/{id}/members/{userId}` | OWNER, ADMIN, or yourself | Remove a member or leave |
| POST | `/organizations/{id}/invitations` | OWNER, ADMIN | Invite a member (org must be `ACTIVE`) |
| GET | `/organizations/{id}/invitations` | OWNER, ADMIN | Pending invitations (no tokens) |
| POST | `/organizations/{id}/invitations/accept` | Invitee | Accept with `invitationToken` |
| POST | `/organizations/{id}/athlete-relationships` | OWNER, ADMIN, ATHLETE_MANAGER | Request athlete affiliation (org must be `ACTIVE`) |
| GET | `/organizations/{id}/athletes` | Members | Affiliations, filtered by `status` (default `ACTIVE`) |
| GET | `/organization-affiliations` | Athlete | Your own affiliations |
| POST | `/organization-affiliations/{id}/approve` | Athlete | Approve a pending affiliation |
| POST | `/organization-affiliations/{id}/reject` | Athlete | Reject a pending affiliation |
| POST | `/organization-affiliations/{id}/end` | Athlete, OWNER, ADMIN, ATHLETE_MANAGER | End a pending or active affiliation |
| POST | `/organizations/{id}/scouting/workspace/activate` | ORG_ADMIN + OWNER/ADMIN | Activate scouting workspace |
| POST | `/organizations/{id}/scouting/seats` | ORG_ADMIN + OWNER/ADMIN | Assign a scout seat |
| GET | `/organizations/{id}/scouting/watchlists` | ORG_ADMIN or SCOUT + member | Shared watchlists |
| GET | `/organizations/{id}/scouting/evaluations` | ORG_ADMIN or SCOUT + member | Shared evaluations |

### Scouting (org-scoped)

Organization scouts also use the endpoints in [SCOUT_USER_GUIDE.md](./SCOUT_USER_GUIDE.md) for their own favourites, watchlists and evaluations.

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

An organization's subscription is keyed by the organization id. Scout seats are limited by `MAX_SCOUT_SEATS`.

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

Athletes must approve organization affiliation before their data becomes visible to org members. Athletes can revoke affiliation at any time. Revoking removes coach access to submit activities on their behalf but does not delete existing published activities.

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
| 403 | Forbidden | Not an OWNER or ADMIN member | `{ "status": 403, "error": "Forbidden", "message": "Only the organization's OWNER or ADMIN can do this" }` |
| 403 | Forbidden | Organization pending verification | `{ "status": 403, "error": "Forbidden", "message": "Organization is pending verification" }` |
| 404 | Not Found | Organization not found | `{ "status": 404, "error": "Not Found", "message": "Organization not found with id: org-999" }` |
| 409 | Conflict | Already a member | `{ "status": 409, "error": "Conflict", "message": "You are already a member of this organization" }` |
| 409 | Conflict | Affiliation already open | `{ "status": 409, "error": "Conflict", "message": "This athlete already has a pending or active affiliation with the organization" }` |
| 402 | Payment Required | Seat limit reached | `{ "status": 402, "error": "Payment Required", "message": "Entitlement limit exceeded for 'MAX_SCOUT_SEATS': current=5, limit=5" }` |
| 400 | Bad Request | Request body failed validation | `{ "status": 400, "error": "Bad Request", "message": "Validation failed", "fieldErrors": [{ "field": "name", "rejectedValue": null, "message": "must not be blank" }] }` |
