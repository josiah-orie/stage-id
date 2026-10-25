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
    "password": "Str0ng!Pass#2024"
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

### Step 3 — Assign the ATHLETE role

```bash
curl -X POST '{{BASE_URL}}/users/me/roles' \
  -H 'Authorization: Bearer {{ACCESS_TOKEN}}' \
  -H 'Content-Type: application/json' \
  -d '{ "role": "ATHLETE" }'
```

**Response (200 OK):**
```json
{ "role": "ATHLETE", "assignedAt": "2024-06-15T10:30:00Z" }
```

### Step 4 — Create your athlete profile

```bash
curl -X POST '{{BASE_URL}}/athletes' \
  -H 'Authorization: Bearer {{ACCESS_TOKEN}}' \
  -H 'Content-Type: application/json' \
  -d '{
    "primarySportCode": "FOOTBALL",
    "position": "Attacking Midfielder",
    "heightCm": 175,
    "weightKg": 70,
    "dominantFoot": "RIGHT",
    "nationality": "Nigeria"
  }'
```

**Response (201 Created):**
```json
{
  "id": "b2c3d4e5-f6a7-8901-bcde-f12345678901",
  "userId": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
  "athleteNumber": "TH-00001",
  "primarySportCode": "FOOTBALL",
  "position": "Attacking Midfielder",
  "heightCm": 175,
  "weightKg": 70,
  "dominantFoot": "RIGHT",
  "status": "ACTIVE",
  "nationality": "Nigeria"
}
```

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
  "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
  "refreshToken": "d9e8f7a6-b5c4-3210-fedc-ba9876543210",
  "tokenType": "Bearer",
  "expiresIn": 3600
}
```

### Token Refresh

```bash
curl -X POST '{{BASE_URL}}/auth/refresh' \
  -H 'Content-Type: application/json' \
  -d '{ "refreshToken": "d9e8f7a6-b5c4-3210-fedc-ba9876543210" }'
```

**Response (200 OK):**
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiJ9...<new>...",
  "refreshToken": "e0f1a2b3-c4d5-6789-0123-456789abcdef",
  "tokenType": "Bearer",
  "expiresIn": 3600
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
    "sportCode": "FUTSAL",
    "position": "Pivot",
    "isPrimary": false
  }'
```

**Response (201 Created):**
```json
{
  "id": "c3d4e5f6-a7b8-9012-cdef-012345678902",
  "athleteId": "b2c3d4e5-f6a7-8901-bcde-f12345678901",
  "sportCode": "FUTSAL",
  "position": "Pivot",
  "isPrimary": false
}
```

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
  "id": "d4e5f6a7-b8c9-0123-def0-123456789003",
  "athleteId": "b2c3d4e5-f6a7-8901-bcde-f12345678901",
  "title": "Best Young Player — Lagos Regional League U-17",
  "achievedAt": "2023-11-30"
}
```

### Workflow B — Log an activity

#### Step 1 — Get the activity schema for your sport

```bash
curl -X GET '{{BASE_URL}}/activity-types/FOOTBALL_MATCH/schema' \
  -H 'Authorization: Bearer {{ACCESS_TOKEN}}'
```

**Response (200 OK):**
```json
{
  "schemaVersionId": "sv-001",
  "templateId": "tmpl-fb-match-v1",
  "fields": [
    { "fieldCode": "opponent", "label": "Opponent", "type": "TEXT", "required": true },
    { "fieldCode": "goals", "label": "Goals Scored", "type": "INTEGER", "required": false },
    { "fieldCode": "assists", "label": "Assists", "type": "INTEGER", "required": false },
    { "fieldCode": "minutesPlayed", "label": "Minutes Played", "type": "INTEGER", "required": true }
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
    "activityTypeId": "FOOTBALL_MATCH",
    "sportId": "FOOTBALL",
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
  "primarySportCode": "FOOTBALL",
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
- `403 FORBIDDEN` — not the profile owner
- `404 NOT_FOUND` — athlete ID does not exist

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

#### GET /subscriptions/me

```bash
curl -X GET '{{BASE_URL}}/subscriptions/me' \
  -H 'Authorization: Bearer {{ACCESS_TOKEN}}'
```

**Response (200 OK) — Free plan:**
```json
{
  "planCode": "FREE",
  "status": "ACTIVE",
  "entitlements": {
    "maxActivitiesPerMonth": 10,
    "videoUploadAllowed": true,
    "maxVideoStorageGb": 1
  }
}
```

**403 when limit exceeded:**
```json
{
  "error": "ENTITLEMENT_LIMIT_EXCEEDED",
  "message": "You have reached the 10 activity limit for the FREE plan this month.",
  "upgradeUrl": "https://talenthub.io/pricing"
}
```

---

## 7. Privacy and Consent Controls

#### Grant discovery consent (make profile discoverable)

```bash
curl -X POST '{{BASE_URL}}/safeguarding/consent' \
  -H 'Authorization: Bearer {{ACCESS_TOKEN}}' \
  -H 'Content-Type: application/json' \
  -d '{ "action": "GRANT" }'
```

**Response (200 OK):**
```json
{ "consentStatus": "GRANTED", "updatedAt": "2024-06-15T12:00:00Z" }
```

#### Withdraw consent (remove from discovery, restrict media)

```bash
curl -X POST '{{BASE_URL}}/safeguarding/consent' \
  -H 'Authorization: Bearer {{ACCESS_TOKEN}}' \
  -H 'Content-Type: application/json' \
  -d '{ "action": "WITHDRAW" }'
```

**Response (200 OK):**
```json
{ "consentStatus": "WITHDRAWN", "updatedAt": "2024-06-15T14:00:00Z" }
```

After withdrawal your profile disappears from scout discovery results and your media becomes inaccessible to external viewers immediately.

#### View current consent status

```bash
curl -X GET '{{BASE_URL}}/safeguarding/consent' \
  -H 'Authorization: Bearer {{ACCESS_TOKEN}}'
```

---

## 8. Safeguarding Notes

Athletes who are minors (under 18) are subject to additional platform protections:

- A guardian or parent relationship must be established before the athlete's profile can be made discoverable to scouts.
- The guardian receives notifications when the athlete's visibility settings change.
- Consent withdrawal by either the athlete or the guardian takes immediate effect.
- If no guardian link is present, the athlete profile is restricted to private visibility regardless of the athlete's own settings.

To link a guardian, ask the parent/guardian to initiate a guardian relationship request (see PARENT_USER_GUIDE.md or GUARDIAN_USER_GUIDE.md) and approve it in your notifications.

---

## 9. Error Reference

| Error Code | HTTP Status | Meaning | Example JSON |
|---|---|---|---|
| `UNAUTHORIZED` | 401 | Missing or expired token | `{ "error": "UNAUTHORIZED", "message": "Full authentication is required." }` |
| `FORBIDDEN` | 403 | Insufficient permission | `{ "error": "FORBIDDEN", "message": "You do not have permission to access this resource." }` |
| `NOT_FOUND` | 404 | Resource does not exist | `{ "error": "NOT_FOUND", "message": "AthleteProfile not found for id: xyz" }` |
| `DUPLICATE_RESOURCE` | 409 | Resource already exists | `{ "error": "DUPLICATE_RESOURCE", "message": "AthleteProfile already exists for userId: abc" }` |
| `INVALID_STATE_TRANSITION` | 409 | Activity not in correct state | `{ "error": "INVALID_STATE_TRANSITION", "message": "Cannot transition from PUBLISHED to SUBMITTED" }` |
| `VALIDATION_ERROR` | 400 | Request body failed validation | `{ "error": "VALIDATION_ERROR", "errors": [{ "field": "email", "message": "must be a valid email" }] }` |
| `ENTITLEMENT_LIMIT_EXCEEDED` | 403 | Subscription limit reached | `{ "error": "ENTITLEMENT_LIMIT_EXCEEDED", "message": "Monthly activity limit reached." }` |
| `MEDIA_RESTRICTED` | 403 | Media inaccessible — consent withdrawn | `{ "error": "MEDIA_RESTRICTED", "message": "This media asset is not accessible." }` |
