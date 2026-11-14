# Athlete User Guide

## 1. Role Overview

Athletes use TalentHub to build a verified digital profile that showcases their sport performance, development history, and media highlights. Coaches, scouts, and agents can discover athletes based on the information athletes choose to make public.

---

## 2. Getting Started

### Step 1 — Register an account

```bash
curl -X POST '{{BASE_URL}}/auth/register' \
  -H 'Content-Type: application/json' \
  -d '{
    "email": "jordan.okafor@example.com",
    "password": "Str0ng!Pass#2024",
    "role": "ATHLETE"
  }'
```

**Response (201 Created):**
```json
{
  "userId": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
  "message": "Registration successful. Please verify your email."
}
```

Check your email inbox and click the verification link before logging in.

### Step 2 — Verify your email

```bash
curl -X GET '{{BASE_URL}}/auth/verify-email?token=<TOKEN_FROM_EMAIL>'
```

**Response (200 OK):**
```json
{ "message": "Email verified successfully." }
```

### Step 3 — Confirm your role

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
  "role": "ATHLETE",
  "roles": ["ATHLETE"],
  "status": "ACTIVE",
  "emailVerified": true
}
```

To hold an additional role, call `POST {{BASE_URL}}/users/me/roles` with `{ "role": "COACH" }`. Allowed roles are `ATHLETE`, `COACH`, `PARENT`, `GUARDIAN`, `AGENT` and `SCOUT`. `ADMIN`, `SUPER_ADMIN` and `ORG_ADMIN` are rejected with `400`, and adding a role you already hold returns `409`. New roles appear in tokens issued after your next login or refresh.

### Step 4 — Create your athlete profile

```bash
curl -X POST '{{BASE_URL}}/athletes/me' \
  -H 'Authorization: Bearer {{ACCESS_TOKEN}}' \
  -H 'Content-Type: application/json' \
  -d '{
    "primarySportCode": "FB",
    "position": "Attacking Midfielder",
    "heightCm": 175,
    "weightKg": 70,
    "dominantFoot": "RIGHT",
    "nationality": "NG"
  }'
```

**Response (201 Created):**
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

`primarySportCode` is a sport code from `GET /sports` (for example `FB` for football). `nationality` is a 2-letter country code. Use your **user id** (`userId`, the same as `user_id` from login) wherever a path says `/athletes/{id}`. Creating a second profile returns `409`.

---

## 3. Authentication

### Login

```bash
curl -X POST '{{BASE_URL}}/auth/login' \
  -H 'Content-Type: application/json' \
  -d '{
    "email": "jordan.okafor@example.com",
    "password": "Str0ng!Pass#2024"
  }'
```

**Response (200 OK):**
```json
{
  "access_token": "eyJhbGciOiJIUzI1NiJ9...",
  "refresh_token": "d9e8f7a6-b5c4-3210-fedc-ba9876543210",
  "user_id": "b2c3d4e5-f6a7-8901-bcde-f12345678901",
  "email": "user@example.com",
  "role": "ATHLETE",
  "expires_in": 3600
}
```

### Token Refresh

```bash
curl -X POST '{{BASE_URL}}/auth/refresh' \
  -H 'Content-Type: application/json' \
  -d '{ "refresh_token": "d9e8f7a6-b5c4-3210-fedc-ba9876543210" }\'
```

**Response (200 OK):**
```json
{
  "access_token": "eyJhbGciOiJIUzI1NiJ9...<new>...",
  "refresh_token": "e0f1a2b3-c4d5-6789-0123-456789abcdef",
  "user_id": "b2c3d4e5-f6a7-8901-bcde-f12345678901",
  "email": "user@example.com",
  "role": "ATHLETE",
  "expires_in": 3600
}
```

### Logout

```bash
curl -X POST '{{BASE_URL}}/auth/logout' \
  -H 'Authorization: Bearer {{ACCESS_TOKEN}}'
```

**Response (204 No Content)**

---

## 4. Core Workflows

### Workflow A — Build your profile

#### Add a secondary sport

```bash
curl -X POST '{{BASE_URL}}/athletes/b2c3d4e5-f6a7-8901-bcde-f12345678901/sports' \
  -H 'Authorization: Bearer {{ACCESS_TOKEN}}' \
  -H 'Content-Type: application/json' \
  -d '{
    "sportCode": "BK",
    "position": "Point Guard"
  }'
```

**Response (201 Created):**
```json
{
  "id": "as-0001",
  "athleteUserId": "b2c3d4e5-f6a7-8901-bcde-f12345678901",
  "sportId": "sp-basketball",
  "sportCode": "BK",
  "position": "Point Guard",
  "primary": false
}
```

Your primary sport stays on the profile. `GET /athletes/{id}/sports` lists the primary sport first (`primary: true`), then secondary ones. Adding a sport you already have returns `409`; remove one with `DELETE /athletes/{id}/sports/{athleteSportId}`.

#### Add an achievement

```bash
curl -X POST '{{BASE_URL}}/athletes/b2c3d4e5-f6a7-8901-bcde-f12345678901/achievements' \
  -H 'Authorization: Bearer {{ACCESS_TOKEN}}' \
  -H 'Content-Type: application/json' \
  -d '{
    "title": "Best Young Player — Lagos Regional League U-17",
    "description": "Awarded for 12 goals and 8 assists across 15 matches.",
    "achievedAt": "2023-11-30"
  }'
```

**Response (201 Created):**
```json
{
  "id": "ms-0001",
  "athleteId": "b2c3d4e5-f6a7-8901-bcde-f12345678901",
  "sportId": null,
  "milestoneType": "ACHIEVEMENT",
  "title": "Best Young Player — Lagos Regional League U-17",
  "description": "Awarded for 12 goals and 8 assists across 15 matches.",
  "milestoneDate": "2023-11-30",
  "evidenceActivityId": null,
  "createdBy": "b2c3d4e5-f6a7-8901-bcde-f12345678901"
}
```

`milestoneType` defaults to `ACHIEVEMENT`; you can also send `FIRST_APPEARANCE`, `PERSONAL_BEST`, `LEVEL_UP` or `OTHER`. `achievedAt` defaults to today. Achievements show up in your development timeline, and `GET /athletes/{id}/achievements` lists them.

### Workflow B — Log an activity

#### Step 1 — Find a template and its schema

List the activity templates for your sport (by sport id or code), then fetch the published schema of the one you want:

```bash
curl -X GET '{{BASE_URL}}/sports/FB/activity-types' \
  -H 'Authorization: Bearer {{ACCESS_TOKEN}}'

curl -X GET '{{BASE_URL}}/activity-types/tmpl-fb-match-v1/schema' \
  -H 'Authorization: Bearer {{ACCESS_TOKEN}}'
```

The `{id}` in `/activity-types/{id}/schema` is the **template id**. It returns `404` if the template has no published schema yet.

**Response (200 OK):**
```json
{
  "schemaVersionId": "sv-001",
  "templateId": "tmpl-fb-match-v1",
  "versionNumber": 1,
  "fields": [
    { "id": "fd-1", "fieldCode": "opponent", "fieldLabel": "Opponent", "fieldType": "TEXT", "required": true, "displayOrder": 1, "validationRulesJson": null, "helpText": null },
    { "id": "fd-2", "fieldCode": "goals", "fieldLabel": "Goals Scored", "fieldType": "INTEGER", "required": false, "displayOrder": 2, "validationRulesJson": null, "helpText": null },
    { "id": "fd-3", "fieldCode": "minutesPlayed", "fieldLabel": "Minutes Played", "fieldType": "INTEGER", "required": true, "displayOrder": 3, "validationRulesJson": null, "helpText": null }
  ]
}
```

#### Step 2 — Create a draft activity

```bash
curl -X POST '{{BASE_URL}}/athletes/b2c3d4e5-f6a7-8901-bcde-f12345678901/activities' \
  -H 'Authorization: Bearer {{ACCESS_TOKEN}}' \
  -H 'Content-Type: application/json' \
  -d '{
    "templateId": "tmpl-fb-match-v1",
    "activityTypeId": "at-match",
    "sportId": "sp-football",
    "title": "Quarter-final vs Kano Stars FC",
    "activityDate": "2024-06-10",
    "location": "Teslim Balogun Stadium, Lagos",
    "visibilityLevel": "PUBLIC",
    "fieldValues": {
      "opponent": "Kano Stars FC",
      "goals": "2",
      "assists": "1",
      "minutesPlayed": "90"
    }
  }'
```

**Response (201 Created):**
```json
{
  "id": "e5f6a7b8-c9d0-1234-ef01-23456789004",
  "athleteId": "b2c3d4e5-f6a7-8901-bcde-f12345678901",
  "title": "Quarter-final vs Kano Stars FC",
  "status": "DRAFT",
  "activityDate": "2024-06-10"
}
```

#### Step 3 — Submit the activity

```bash
curl -X POST '{{BASE_URL}}/activities/e5f6a7b8-c9d0-1234-ef01-23456789004/submit' \
  -H 'Authorization: Bearer {{ACCESS_TOKEN}}'
```

**Response (200 OK):**
```json
{
  "id": "e5f6a7b8-c9d0-1234-ef01-23456789004",
  "status": "SUBMITTED",
  "submittedAt": "2024-06-11T08:45:00Z"
}
```

#### Step 4 — Publish the activity

```bash
curl -X POST '{{BASE_URL}}/activities/e5f6a7b8-c9d0-1234-ef01-23456789004/publish' \
  -H 'Authorization: Bearer {{ACCESS_TOKEN}}'
```

**Response (200 OK):**
```json
{
  "id": "e5f6a7b8-c9d0-1234-ef01-23456789004",
  "status": "PUBLISHED"
}
```

### Workflow C — Upload a video highlight

#### Step 1 — Initiate upload

```bash
curl -X POST '{{BASE_URL}}/media/uploads' \
  -H 'Authorization: Bearer {{ACCESS_TOKEN}}' \
  -H 'Content-Type: application/json' \
  -d '{
    "filename": "quarter-final-highlights.mp4",
    "mimeType": "video/mp4",
    "fileSizeBytes": 52428800
  }'
```

**Response (201 Created):**
```json
{
  "uploadId": "f6a7b8c9-d0e1-2345-f012-3456789a0005",
  "presignedUrl": "https://minio.talenthub.io/talenthub-media/uploads/f6a7b8c9...?X-Amz-Signature=...",
  "expiresAt": "2024-06-11T09:45:00Z"
}
```

#### Step 2 — Upload the file to the presigned URL

```bash
curl -X PUT '<presignedUrl>' \
  -H 'Content-Type: video/mp4' \
  --data-binary @quarter-final-highlights.mp4
```

#### Step 3 — Complete the upload

```bash
curl -X POST '{{BASE_URL}}/media/uploads/f6a7b8c9-d0e1-2345-f012-3456789a0005/complete' \
  -H 'Authorization: Bearer {{ACCESS_TOKEN}}'
```

**Response (200 OK):**
```json
{
  "mediaAssetId": "f6a7b8c9-d0e1-2345-f012-3456789a0005",
  "status": "PROCESSING"
}
```

#### Step 4 — Get a playback URL (once processing is READY)

```bash
curl -X GET '{{BASE_URL}}/media/f6a7b8c9-d0e1-2345-f012-3456789a0005/playback' \
  -H 'Authorization: Bearer {{ACCESS_TOKEN}}'
```

**Response (200 OK):**
```json
{
  "playbackUrl": "https://minio.talenthub.io/talenthub-media/f6a7b8c9...?X-Amz-Signature=...",
  "expiresAt": "2024-06-11T10:00:00Z"
}
```

---

## 5. Available Endpoints

> **Access:** You can always read and change your own data. Parents and legal guardians with an `ACTIVE` link, agents holding a grant you approved, and administrators can also act on it. Scouts see only your `PUBLISHED` activities with `PUBLIC` visibility, and your activity videos, thumbnails and profile image. Anyone else gets `403`. People without access who look you up by athlete number get only `userId`, `athleteNumber`, `primarySportCode` and `status`.

### Authentication

| Method | Path | Description |
|---|---|---|
| POST | `/auth/register` | Register account |
| POST | `/auth/login` | Log in |
| POST | `/auth/refresh` | Refresh tokens |
| POST | `/auth/logout` | Revoke all refresh tokens |
| GET | `/auth/verify-email` | Verify email from link |
| POST | `/auth/password-reset` | Request reset email |
| POST | `/auth/password-reset/confirm` | Complete password reset |

### Profile & Account

#### GET /users/me

```bash
curl -X GET '{{BASE_URL}}/users/me' \
  -H 'Authorization: Bearer {{ACCESS_TOKEN}}'
```

**Response (200 OK):**
```json
{
  "userId": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
  "email": "jordan.okafor@example.com",
  "role": "ATHLETE",
  "status": "ACTIVE",
  "emailVerified": true,
  "createdAt": "2024-06-01T09:00:00Z"
}
```

#### GET /athletes/me

```bash
curl -X GET '{{BASE_URL}}/athletes/me' \
  -H 'Authorization: Bearer {{ACCESS_TOKEN}}'
```

**Response (200 OK):**
```json
{
  "id": "b2c3d4e5-f6a7-8901-bcde-f12345678901",
  "athleteNumber": "TH-00001",
  "primarySportCode": "FB",
  "position": "Attacking Midfielder",
  "status": "ACTIVE"
}
```

#### PATCH /athletes/{id}

```bash
curl -X PATCH '{{BASE_URL}}/athletes/b2c3d4e5-f6a7-8901-bcde-f12345678901' \
  -H 'Authorization: Bearer {{ACCESS_TOKEN}}' \
  -H 'Content-Type: application/json' \
  -d '{ "heightCm": 177, "weightKg": 71 }'
```

**Response (200 OK):** updated athlete profile object.

**Common errors:**
- `403` — you lack `MANAGE` on this athlete
- `404` — no athlete profile for that user id

### Activities

#### GET /athletes/{id}/activities

```bash
curl -X GET '{{BASE_URL}}/athletes/b2c3d4e5-f6a7-8901-bcde-f12345678901/activities?page=0&size=10' \
  -H 'Authorization: Bearer {{ACCESS_TOKEN}}'
```

**Response (200 OK):**
```json
{
  "content": [
    {
      "id": "e5f6a7b8-c9d0-1234-ef01-23456789004",
      "title": "Quarter-final vs Kano Stars FC",
      "activityDate": "2024-06-10",
      "status": "PUBLISHED"
    }
  ],
  "totalElements": 1,
  "totalPages": 1,
  "page": 0,
  "size": 10
}
```

#### PATCH /activities/{id}

```bash
curl -X PATCH '{{BASE_URL}}/activities/e5f6a7b8-c9d0-1234-ef01-23456789004' \
  -H 'Authorization: Bearer {{ACCESS_TOKEN}}' \
  -H 'Content-Type: application/json' \
  -d '{ "description": "Updated match notes after coach review." }'
```

Only DRAFT activities can be patched. Returns `409` if status is not DRAFT.

### Development

#### GET /athletes/{id}/development

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
      "label": "First published activity",
      "recordedAt": "2024-06-11"
    }
  ]
}
```

#### GET /athletes/{id}/performance

```bash
curl -X GET '{{BASE_URL}}/athletes/b2c3d4e5-f6a7-8901-bcde-f12345678901/performance' \
  -H 'Authorization: Bearer {{ACCESS_TOKEN}}'
```

### Notifications

#### GET /notifications

```bash
curl -X GET '{{BASE_URL}}/notifications?unreadOnly=true' \
  -H 'Authorization: Bearer {{ACCESS_TOKEN}}'
```

**Response (200 OK):**
```json
{
  "content": [
    {
      "id": "notif-001",
      "type": "ACTIVITY_VERIFIED",
      "message": "Your activity 'Quarter-final vs Kano Stars FC' has been verified.",
      "read": false,
      "createdAt": "2024-06-12T07:00:00Z"
    }
  ],
  "totalElements": 1
}
```

#### PATCH /notifications/{id}/read

```bash
curl -X PATCH '{{BASE_URL}}/notifications/notif-001/read' \
  -H 'Authorization: Bearer {{ACCESS_TOKEN}}'
```

**Response (200 OK):**
```json
{ "id": "notif-001", "read": true }
```

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

Consent is recorded per purpose: `PROFILE_DISCOVERY`, `PERFORMANCE_SHARING`, `MEDIA_SHARING` or `RESEARCH`. You, a linked parent or guardian, or an admin can grant and revoke it.

#### Grant consent for a purpose

```bash
curl -X POST '{{BASE_URL}}/athletes/b2c3d4e5-f6a7-8901-bcde-f12345678901/consent' \
  -H 'Authorization: Bearer {{ACCESS_TOKEN}}' \
  -H 'Content-Type: application/json' \
  -d '{ "purpose": "PROFILE_DISCOVERY", "consentVersion": "2024-06", "authorizationSource": "SELF" }'
```

**Response (201 Created):**
```json
{
  "id": "cr-0001",
  "athleteId": "b2c3d4e5-f6a7-8901-bcde-f12345678901",
  "purposeCode": "PROFILE_DISCOVERY",
  "consentVersion": "2024-06",
  "consentedBy": "b2c3d4e5-f6a7-8901-bcde-f12345678901",
  "consentedAt": "2024-06-15T12:00:00Z",
  "revokedAt": null,
  "revokedBy": null,
  "authorizationSource": "SELF"
}
```

If you are under 18, `PROFILE_DISCOVERY` needs an active parent or legal guardian link first; without one the call returns `403`.

#### Withdraw consent

```bash
curl -X DELETE '{{BASE_URL}}/athletes/b2c3d4e5-f6a7-8901-bcde-f12345678901/consent/PROFILE_DISCOVERY' \
  -H 'Authorization: Bearer {{ACCESS_TOKEN}}'
```

**Response:** `204 No Content`. `404` if that purpose has no active consent.

#### View your consent records

```bash
curl -X GET '{{BASE_URL}}/athletes/b2c3d4e5-f6a7-8901-bcde-f12345678901/consent' \
  -H 'Authorization: Bearer {{ACCESS_TOKEN}}'
```

**Response (200 OK):** an array of consent records, newest first. Withdrawn ones have `revokedAt` set.

#### Ask for your data, or for it to be deleted or corrected

```bash
curl -X POST '{{BASE_URL}}/safeguarding/data-subject-requests' \
  -H 'Authorization: Bearer {{ACCESS_TOKEN}}' \
  -H 'Content-Type: application/json' \
  -d '{ "requestType": "ACCESS" }'
```

`requestType` is `ACCESS`, `DELETION` or `CORRECTION`. **Response (201 Created):** `{ "id", "requesterId", "requestType", "status": "PENDING", "submittedAt", "resolvedAt": null }`. A second pending request of the same type returns `409`. Track them with `GET /safeguarding/data-subject-requests`.

---

## 8. Safeguarding Notes

Athletes who are minors (under 18) are subject to additional platform protections:

- Granting `PROFILE_DISCOVERY` consent needs an `ACTIVE` parent or legal guardian link first. Without one, the call returns `403`.
- Consent withdrawal by either the athlete or the guardian takes immediate effect. Withdrawing `PROFILE_DISCOVERY` hides the profile from scout discovery straight away.
- Your age comes from the `dateOfBirth` on your user profile (`PATCH /profiles/me`).

To link a guardian, ask the parent or agent to send a request (see PARENT_USER_GUIDE.md and AGENT_USER_GUIDE.md). Find it with `GET {{BASE_URL}}/guardian-relationships`, where it has `status: "PENDING"`, then accept it with `POST {{BASE_URL}}/guardian-relationships/{id}/approve` or decline it with `POST {{BASE_URL}}/guardian-relationships/{id}/reject`. Legal guardian requests are decided by an administrator instead. You can end any relationship with `POST {{BASE_URL}}/guardian-relationships/{id}/revoke`.

Organizations (clubs, academies) ask to affiliate with you in the same way. List requests with `GET {{BASE_URL}}/organization-affiliations`, then approve with `POST {{BASE_URL}}/organization-affiliations/{id}/approve` or decline with `/reject`. Each request shows the `permission` the organization's staff will get. End an affiliation at any time with `POST {{BASE_URL}}/organization-affiliations/{id}/end`, which removes their access straight away.

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
| 403 | Forbidden | No access to this athlete's data | `{ "status": 403, "error": "Forbidden", "message": "You do not have VIEW_STATS permission for this athlete." }` |
| 403 | Forbidden | Minor granting discovery consent without a guardian link | `{ "status": 403, "error": "Forbidden", "message": "Athlete is a minor and requires active guardian consent to proceed." }` |
| 403 | Forbidden | Media restricted by an admin | `{ "status": 403, "error": "Forbidden", "message": "This media asset is not accessible." }` |
| 404 | Not Found | Resource does not exist | `{ "status": 404, "error": "Not Found", "message": "AthleteProfile not found with id: b2c3d4e5-f6a7-8901-bcde-f12345678901" }` |
| 409 | Conflict | Profile already exists | `{ "status": 409, "error": "Conflict", "message": "AthleteProfile already exists with userId: b2c3d4e5-f6a7-8901-bcde-f12345678901" }` |
| 409 | Conflict | Activity not in the right state | `{ "status": 409, "error": "Conflict", "message": "Cannot transition AthleteActivity from 'PUBLISHED' to 'SUBMITTED'" }` |
| 402 | Payment Required | Plan limit reached | `{ "status": 402, "error": "Payment Required", "message": "Entitlement limit exceeded for 'max_video_uploads': current=3, limit=3" }` |
| 400 | Bad Request | Request body failed validation | `{ "status": 400, "error": "Bad Request", "message": "Validation failed", "fieldErrors": [{ "field": "title", "rejectedValue": null, "message": "must not be blank" }] }` |
