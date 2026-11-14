# Scout User Guide

## 1. Role Overview

Talent scouts use TalentHub to discover athletes, build watchlists, create evaluations, and track athlete development over time. Scouts must complete identity verification and maintain a paid subscription to access discovery and evaluation features.

---

## 2. Getting Started

### Step 1 — Register an account

```bash
curl -X POST '{{BASE_URL}}/auth/register' \
  -H 'Content-Type: application/json' \
  -d '{
    "email": "kwame.mensah@scoutpro.io",
    "password": "Sc0ut!Pro#2024",
    "role": "SCOUT"
  }'
```

**Response (201 Created):**
```json
{
  "userId": "s1a2b3c4-d5e6-7890-abcd-ef1234509876",
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
  "role": "SCOUT",
  "roles": ["SCOUT"],
  "status": "ACTIVE",
  "emailVerified": true
}
```

To hold an additional role, call `POST {{BASE_URL}}/users/me/roles` with `{ "role": "COACH" }`. Allowed roles are `ATHLETE`, `COACH`, `PARENT`, `GUARDIAN`, `AGENT` and `SCOUT`. `ADMIN`, `SUPER_ADMIN` and `ORG_ADMIN` are rejected with `400`, and adding a role you already hold returns `409`. New roles appear in tokens issued after your next login or refresh.

### Token Refresh

```bash
curl -X POST '{{BASE_URL}}/auth/refresh' \
  -H 'Content-Type: application/json' \
  -d '{ "refresh_token": "b3c4d5e6-f7a8-9012-bcde-f012345678ab" }\'
```

### Logout

```bash
curl -X POST '{{BASE_URL}}/auth/logout' \
  -H 'Authorization: Bearer {{ACCESS_TOKEN}}'
```

---

## 4. Core Workflows

Every `/scouting/**` and `/discovery/**` route needs the `SCOUT` role. Your scout profile always comes from your token, so no request takes a scout profile id. Favourites, notes, specializations, saved searches and evaluations need a scout profile first; without one they return `404` with `No scout profile exists for this account. Create one with POST /scouts first.`

### Workflow A — Create your scout profile and specializations

```bash
curl -X POST '{{BASE_URL}}/scouts' \
  -H 'Authorization: Bearer {{ACCESS_TOKEN}}' \
  -H 'Content-Type: application/json' \
  -d '{
    "professionalTitle": "Senior Scout",
    "biography": "Ten years scouting West African youth football.",
    "yearsExperience": 10
  }'
```

**Response (201 Created):**
```json
{
  "id": "sc1a2b3c-0000-4000-8000-000000000001",
  "userAccountId": "u5c0u7aa-0000-4000-8000-000000000001",
  "professionalTitle": "Senior Scout",
  "biography": "Ten years scouting West African youth football.",
  "yearsExperience": 10,
  "currentOrganizationId": null,
  "verificationStatus": "UNVERIFIED"
}
```

A second profile returns `409`. `GET /scouts/me` returns yours, and `POST /scouts/verification-requests` (no body) submits it for admin verification.

Add the sports you focus on:

```bash
curl -X POST '{{BASE_URL}}/scouts/me/specializations' \
  -H 'Authorization: Bearer {{ACCESS_TOKEN}}' \
  -H 'Content-Type: application/json' \
  -d '{
    "sportCode": "FB",
    "level": "YOUTH",
    "preferredRegions": ["Lagos", "Accra"]
  }'
```

**Response (201 Created):**
```json
{
  "id": "spec-001",
  "scoutProfileId": "sc1a2b3c-0000-4000-8000-000000000001",
  "sportId": "sp-football",
  "sportCode": "FB",
  "level": "YOUTH",
  "preferredRegions": ["Lagos", "Accra"]
}
```

`level` is `YOUTH`, `AMATEUR`, `SEMI_PRO`, `PROFESSIONAL` or `ELITE`. You can have one specialization per sport; a second one returns `409`. List them with `GET /scouts/me/specializations` and remove one with `DELETE /scouts/me/specializations/{id}`.

### Workflow B — Discover athletes

```bash
curl -X GET '{{BASE_URL}}/discovery/athletes?country=NG&ageCategory=YOUTH&page=0&size=20' \
  -H 'Authorization: Bearer {{ACCESS_TOKEN}}'
```

Filters, both optional: `country` (the athlete's country of residence) and `ageCategory` (`YOUTH`, `JUNIOR`, `SENIOR`, `VETERAN`). Standard `page`, `size` and `sort` apply.

**Response (200 OK):**
```json
{
  "content": [
    {
      "id": "proj-0001",
      "athleteId": "b2c3d4e5-f6a7-8901-bcde-f12345678901",
      "athleteNumber": "TH-00001",
      "displayName": "Jordan O.",
      "sportIdsJson": "[\"sp-football\"]",
      "disciplineIdsJson": null,
      "positionIdsJson": null,
      "countryOfResidence": "NG",
      "stateRegion": "Lagos",
      "ageCategory": "YOUTH",
      "verificationStatus": "VERIFIED",
      "profileStatus": "ACTIVE",
      "lastActivityDate": "2024-06-10",
      "topMetricsJson": null,
      "visibilityLevel": "PUBLIC",
      "updatedAt": "2024-06-10T18:00:00Z"
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

`GET /discovery/athletes/{athleteId}` returns one card. `athleteId` is the athlete's user id, which you use in every other athlete path.

### Workflow C — Save a search

```bash
curl -X POST '{{BASE_URL}}/discovery/saved-searches' \
  -H 'Authorization: Bearer {{ACCESS_TOKEN}}' \
  -H 'Content-Type: application/json' \
  -d '{
    "name": "Nigeria youth",
    "queryJson": "{\"country\":\"NG\",\"ageCategory\":\"YOUTH\"}"
  }'
```

The search is saved to your own scout profile. `queryJson` is stored as given, as a JSON string.

**Response (201 Created):**
```json
{
  "id": "ss-001",
  "scoutProfileId": "sc1a2b3c-0000-4000-8000-000000000001",
  "name": "Nigeria youth",
  "queryJson": "{\"country\":\"NG\",\"ageCategory\":\"YOUTH\"}",
  "createdAt": "2024-06-15T13:00:00Z"
}
```

List yours with `GET /discovery/saved-searches`.

### Workflow D — Add athlete to favourites

```bash
curl -X POST '{{BASE_URL}}/scouting/favourites?athleteId=b2c3d4e5-f6a7-8901-bcde-f12345678901&notes=Watch%20in%20final' \
  -H 'Authorization: Bearer {{ACCESS_TOKEN}}'
```

`athleteId` and the optional `notes` are query parameters.

**Response (201 Created):**
```json
{
  "id": "fav-001",
  "scoutProfileId": "sc1a2b3c-0000-4000-8000-000000000001",
  "athleteId": "b2c3d4e5-f6a7-8901-bcde-f12345678901",
  "addedAt": "2024-06-15T14:00:00Z",
  "notes": "Watch in final"
}
```

Adding the same athlete twice returns `409`. List with `GET /scouting/favourites`; remove with `DELETE /scouting/favourites?athleteId=<id>` (`204`).

Plan limits: favourites (`MAX_FAVOURITES`) and watchlists (`MAX_WATCHLISTS`) are capped by your plan. Going over returns `402`:
```json
{
  "timestamp": "2024-06-15T15:00:00.000Z",
  "status": 402,
  "error": "Payment Required",
  "message": "Entitlement limit exceeded for 'MAX_WATCHLISTS': current=3, limit=3",
  "path": "/api/v1/scouting/watchlists"
}
```

### Workflow E — Create a watchlist and add an athlete

```bash
curl -X POST '{{BASE_URL}}/scouting/watchlists' \
  -H 'Authorization: Bearer {{ACCESS_TOKEN}}' \
  -H 'Content-Type: application/json' \
  -d '{
    "name": "West Africa U-17 Shortlist",
    "description": "Top prospects for the upcoming regional tournament",
    "shared": false
  }'
```

**Response (201 Created):**
```json
{
  "id": "wl-001",
  "ownerId": "u5c0u7aa-0000-4000-8000-000000000001",
  "ownerType": "SCOUT",
  "name": "West Africa U-17 Shortlist",
  "description": "Top prospects for the upcoming regional tournament",
  "shared": false
}
```

`GET /scouting/watchlists` lists your own watchlists. `GET /scouting/watchlists/{id}` works for your own and for any `shared` one; anything else returns `403`.

```bash
curl -X POST '{{BASE_URL}}/scouting/watchlists/wl-001/athletes?athleteId=b2c3d4e5-f6a7-8901-bcde-f12345678901&notes=Strong%20pressing%20game' \
  -H 'Authorization: Bearer {{ACCESS_TOKEN}}'
```

**Response (201 Created):**
```json
{
  "id": "wa-001",
  "watchlistId": "wl-001",
  "athleteId": "b2c3d4e5-f6a7-8901-bcde-f12345678901",
  "addedAt": "2024-06-15T14:15:00Z",
  "addedBy": "u5c0u7aa-0000-4000-8000-000000000001",
  "notes": "Strong pressing game"
}
```

Watchlist rules:
- Only the owner can add or remove athletes (`403` otherwise).
- An athlete already on the list returns `409`.
- Adding an athlete also follows them.
- Remove one with `DELETE /scouting/watchlists/{id}/athletes/{athleteId}`.

### Workflow F — Follow an athlete

```bash
curl -X POST '{{BASE_URL}}/scouting/follows' \
  -H 'Authorization: Bearer {{ACCESS_TOKEN}}' \
  -H 'Content-Type: application/json' \
  -d '{ "athleteId": "b2c3d4e5-f6a7-8901-bcde-f12345678901" }'
```

**Response (201 Created):**
```json
{
  "id": "fol-001",
  "followerId": "u5c0u7aa-0000-4000-8000-000000000001",
  "followerType": "SCOUT",
  "athleteId": "b2c3d4e5-f6a7-8901-bcde-f12345678901",
  "followedAt": "2024-06-15T14:30:00Z",
  "notificationsEnabled": true
}
```

Following again returns the existing follow. List with `GET /scouting/follows`; stop with `DELETE /scouting/follows/{athleteId}` (`404` if you were not following).

### Workflow G — Create and score an evaluation

Evaluations need an active subscription plan. Without one, creating an evaluation returns `402` with `SCOUT_EVALUATIONS feature not available`.

```bash
curl -X POST '{{BASE_URL}}/scouting/evaluations' \
  -H 'Authorization: Bearer {{ACCESS_TOKEN}}' \
  -H 'Content-Type: application/json' \
  -d '{
    "athleteId": "b2c3d4e5-f6a7-8901-bcde-f12345678901",
    "title": "Quarter-final observation",
    "evaluationDate": "2024-06-10"
  }'
```

`athleteId` and `title` are required; `sportId` and `evaluationDate` are optional.

**Response (201 Created):**
```json
{
  "id": "eval-001",
  "scoutProfileId": "sc1a2b3c-0000-4000-8000-000000000001",
  "athleteId": "b2c3d4e5-f6a7-8901-bcde-f12345678901",
  "athleteSportProfileId": null,
  "sportId": null,
  "evaluationTitle": "Quarter-final observation",
  "evaluationDate": "2024-06-10",
  "status": "DRAFT"
}
```

#### Add scores

List the criteria first. Football uses `FB_TECHNICAL`, `FB_TACTICAL`, `FB_PHYSICAL`, `FB_MENTAL` and others; basketball uses `BB_OFFENSE`, `BB_DEFENSE` and others.

```bash
curl -X GET '{{BASE_URL}}/scouting/evaluation-criteria?category=Football' \
  -H 'Authorization: Bearer {{ACCESS_TOKEN}}'
```

```bash
curl -X POST '{{BASE_URL}}/scouting/evaluations/eval-001/scores' \
  -H 'Authorization: Bearer {{ACCESS_TOKEN}}' \
  -H 'Content-Type: application/json' \
  -d '{
    "scores": [
      { "criterionCode": "FB_TECHNICAL", "score": 8 },
      { "criterionCode": "FB_TACTICAL", "score": 9, "notes": "Reads the press early" },
      { "criterionCode": "FB_PHYSICAL", "score": 7 }
    ]
  }'
```

**Response (200 OK):**
```json
{
  "evaluation": {
    "id": "eval-001",
    "scoutProfileId": "sc1a2b3c-0000-4000-8000-000000000001",
    "athleteId": "b2c3d4e5-f6a7-8901-bcde-f12345678901",
    "evaluationTitle": "Quarter-final observation",
    "evaluationDate": "2024-06-10",
    "status": "DRAFT"
  },
  "scores": [
    { "id": "es-1", "criterionId": "cr-1", "criterionCode": "FB_TECHNICAL", "criterionName": "Technical", "score": 8.0, "maxScore": 10.0, "notes": null },
    { "id": "es-2", "criterionId": "cr-2", "criterionCode": "FB_TACTICAL", "criterionName": "Tactical", "score": 9.0, "maxScore": 10.0, "notes": "Reads the press early" },
    { "id": "es-3", "criterionId": "cr-3", "criterionCode": "FB_PHYSICAL", "criterionName": "Physical", "score": 7.0, "maxScore": 10.0, "notes": null }
  ],
  "averageScore": 8.0
}
```

Scoring rules:
- Scores run from 0 to 10.
- Sending a criterion again replaces its score.
- An unknown criterion code returns `404`.
- Only the author can score, and only while the evaluation is `DRAFT` (`403` / `409`).
- `GET /scouting/evaluations/{id}` returns the same view.

#### Mark evaluation complete

```bash
curl -X POST '{{BASE_URL}}/scouting/evaluations/eval-001/complete' \
  -H 'Authorization: Bearer {{ACCESS_TOKEN}}'
```

**Response (200 OK):** the evaluation with `status: "COMPLETED"`. Only the author can complete it (`403`). An evaluation that is not `DRAFT` returns `409`.

### Workflow H — Add a scouting note

```bash
curl -X POST '{{BASE_URL}}/scouting/notes' \
  -H 'Authorization: Bearer {{ACCESS_TOKEN}}' \
  -H 'Content-Type: application/json' \
  -d '{
    "athleteId": "b2c3d4e5-f6a7-8901-bcde-f12345678901",
    "noteText": "Exceptional vision and composure under pressure. Recommend priority follow-up.",
    "noteDate": "2024-06-15",
    "isPrivate": true
  }'
```

**Response (201 Created):**
```json
{
  "id": "note-001",
  "scoutProfileId": "sc1a2b3c-0000-4000-8000-000000000001",
  "athleteId": "b2c3d4e5-f6a7-8901-bcde-f12345678901",
  "noteText": "Exceptional vision and composure under pressure. Recommend priority follow-up.",
  "noteDate": "2024-06-15",
  "private": true
}
```

Notes are only ever visible to you. `noteDate` defaults to today and `isPrivate` to `true` (it comes back as `private`). List with `GET /scouting/notes?athleteId=<id>` (or without the filter for all notes); delete with `DELETE /scouting/notes/{id}`. Another scout's note id returns `404`.

---

## 5. Available Endpoints

### Scout profile

| Method | Path | Description |
|---|---|---|
| POST | `/scouts` | Create your scout profile |
| GET | `/scouts/me` | Your scout profile |
| GET | `/scouts/{id}` | Any scout profile |
| POST | `/scouts/verification-requests` | Submit your profile for verification |
| POST | `/scouts/me/specializations` | Add specialization |
| GET | `/scouts/me/specializations` | List specializations |
| DELETE | `/scouts/me/specializations/{id}` | Remove specialization |

### Discovery

| Method | Path | Query Params | Description |
|---|---|---|---|
| GET | `/discovery/athletes` | country, ageCategory, page, size, sort | Search athletes |
| GET | `/discovery/athletes/{id}` | — | Get discovery card |
| POST | `/discovery/saved-searches` | — | Save a search (`{ name, queryJson? }`) |
| GET | `/discovery/saved-searches` | — | List your saved searches |

### Scouting (favourites, watchlists, follows, evaluations, notes)

| Method | Path | Description |
|---|---|---|
| POST | `/scouting/favourites` | Add to favourites (`athleteId`, `notes` query) |
| GET | `/scouting/favourites` | List favourites |
| DELETE | `/scouting/favourites` | Remove from favourites (`athleteId` query) |
| POST | `/scouting/watchlists` | Create watchlist |
| GET | `/scouting/watchlists` | List your watchlists |
| GET | `/scouting/watchlists/{id}` | Get a watchlist (yours or shared) |
| POST | `/scouting/watchlists/{id}/athletes` | Add athlete to watchlist |
| DELETE | `/scouting/watchlists/{id}/athletes/{athleteId}` | Remove athlete from watchlist |
| POST | `/scouting/follows` | Follow athlete |
| GET | `/scouting/follows` | List follows |
| DELETE | `/scouting/follows/{athleteId}` | Unfollow |
| GET | `/scouting/evaluation-criteria` | List scoring criteria (`category` query) |
| POST | `/scouting/evaluations` | Create evaluation |
| GET | `/scouting/evaluations` | List your evaluations |
| GET | `/scouting/evaluations/{id}` | Evaluation with scores |
| POST | `/scouting/evaluations/{id}/scores` | Add or replace scores |
| POST | `/scouting/evaluations/{id}/complete` | Complete evaluation |
| POST | `/scouting/notes` | Create private note |
| GET | `/scouting/notes` | List your notes (`athleteId` query) |
| DELETE | `/scouting/notes/{id}` | Delete a note |

### Athlete data

| Method | Path | Description |
|---|---|---|
| GET | `/athletes/{id}/activities` | Published, public activities only |
| GET | `/activities/{id}` | One published, public activity (`404` otherwise) |
| GET | `/media/{id}`, `/media/{id}/playback` | Activity videos, thumbnails, profile images |

### Subscriptions and Notifications

| Method | Path | Description |
|---|---|---|
| GET | `/subscription-plans` | Active plans |
| GET | `/subscriptions/me` | Current subscription |
| POST | `/subscriptions` | Subscribe to a plan (`{ planId }`) |
| GET | `/notifications` | List notifications |
| PATCH | `/notifications/{id}/read` | Mark read |

---

## 6. Entitlements and Subscription

Limits come from your active subscription plan. Each plan sets a limit per entitlement key (for example `MAX_FAVOURITES`, `MAX_WATCHLISTS`, `MAX_SCOUT_SEATS`). A key the plan doesn't list is unlimited. Creating an evaluation needs an active subscription; without one it returns `402` with `SCOUT_EVALUATIONS feature not available`. Without an active subscription, the free-tier defaults apply:

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

When an athlete withdraws `PROFILE_DISCOVERY` consent, or their search profile is `PRIVATE`, they disappear from `GET /discovery/athletes` straight away. `GET /discovery/athletes/{id}` then returns `404`, exactly as for an unknown athlete. If they grant consent again, they reappear.

Watchlist and favourite entries you already hold stay in place. What you can see of the athlete follows the normal rules: `PUBLISHED` activities with `PUBLIC` visibility, and their activity videos, thumbnails and profile image, unless an admin has restricted a media asset (`403`, `This media asset is not accessible.`).

```bash
curl -X GET '{{BASE_URL}}/discovery/athletes/b2c3d4e5-f6a7-8901-bcde-f12345678901' \
  -H 'Authorization: Bearer {{ACCESS_TOKEN}}'
```

**Response if the athlete is hidden (404 Not Found):**
```json
{
  "timestamp": "2024-06-15T15:00:00.000Z",
  "status": 404,
  "error": "Not Found",
  "message": "AthleteSearchProjection not found with id: b2c3d4e5-f6a7-8901-bcde-f12345678901",
  "path": "/api/v1/discovery/athletes/b2c3d4e5-f6a7-8901-bcde-f12345678901"
}
```

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
| 404 | Not Found | No scout profile yet | `{ "status": 404, "error": "Not Found", "message": "No scout profile exists for this account. Create one with POST /scouts first." }` |
| 402 | Payment Required | Evaluations without an active plan | `{ "status": 402, "error": "Payment Required", "message": "SCOUT_EVALUATIONS feature not available" }` |
| 402 | Payment Required | Favourite or watchlist limit reached | `{ "status": 402, "error": "Payment Required", "message": "Entitlement limit exceeded for 'MAX_WATCHLISTS': current=3, limit=3" }` |
| 403 | Forbidden | Another scout's watchlist or evaluation | `{ "status": 403, "error": "Forbidden", "message": "This watchlist belongs to another user" }` |
| 403 | Forbidden | Media restricted by an admin | `{ "status": 403, "error": "Forbidden", "message": "This media asset is not accessible." }` |
| 404 | Not Found | Athlete hidden from discovery or unknown | `{ "status": 404, "error": "Not Found", "message": "AthleteSearchProjection not found with id: b2c3d4e5-f6a7-8901-bcde-f12345678901" }` |
| 409 | Conflict | Already in favourites or the watchlist | `{ "status": 409, "error": "Conflict", "message": "Athlete is already in this watchlist" }` |
| 400 | Bad Request | Request body failed validation | `{ "status": 400, "error": "Bad Request", "message": "Validation failed", "fieldErrors": [{ "field": "athleteId", "rejectedValue": null, "message": "must not be blank" }] }` |
