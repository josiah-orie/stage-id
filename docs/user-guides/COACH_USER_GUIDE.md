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
    "password": "C0achPass#2024!"
  }'
```

**Response (201 Created):**
```json
{
  "userId": "c1a2b3c4-d5e6-7890-abcd-ef0987654321",
  "message": "Registration successful. Please verify your email."
}
```

### Step 2 — Verify email, then assign COACH role

```bash
curl -X POST '{{BASE_URL}}/users/me/roles' \
  -H 'Authorization: Bearer {{ACCESS_TOKEN}}' \
  -H 'Content-Type: application/json' \
  -d '{ "role": "COACH" }'
```

### Step 3 — Accept organization invitation

When an organization owner or admin sends you an invitation, you receive an email with an invitation token. Accept it via:

```bash
curl -X POST '{{BASE_URL}}/organizations/org-001/invitations/accept' \
  -H 'Authorization: Bearer {{ACCESS_TOKEN}}' \
  -H 'Content-Type: application/json' \
  -d '{ "invitationToken": "inv-tok-abc123" }'
```

**Response (200 OK):**
```json
{
  "organizationId": "org-001",
  "organizationName": "FC Lagos Academy",
  "memberRole": "COACH",
  "joinedAt": "2024-06-15T10:00:00Z"
}
```

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
  "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
  "refreshToken": "c4d5e6f7-a8b9-0123-cdef-01234567890c",
  "tokenType": "Bearer",
  "expiresIn": 3600
}
```

### Token Refresh

```bash
curl -X POST '{{BASE_URL}}/auth/refresh' \
  -H 'Content-Type: application/json' \
  -d '{ "refreshToken": "c4d5e6f7-a8b9-0123-cdef-01234567890c" }'
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
    "activityTypeId": "FOOTBALL_TRAINING",
    "sportId": "FOOTBALL",
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

| Method | Path | Description |
|---|---|---|
| POST | `/auth/register` | Register account |
| POST | `/auth/login` | Log in |
| POST | `/auth/refresh` | Refresh tokens |
| POST | `/auth/logout` | Log out |
| GET | `/auth/verify-email` | Verify email |
| POST | `/users/me/roles` | Assign COACH role |
| GET | `/users/me` | Get account info |
| GET | `/organizations/{id}/members` | List organization members |
| GET | `/organizations/{id}/athletes` | List assigned athletes |
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

Coaches access TalentHub through their organization's subscription. Individual coach accounts on the FREE plan can submit activities for assigned athletes but cannot upload videos over 1 GB or access advanced analytics.

```bash
curl -X GET '{{BASE_URL}}/subscriptions/me' \
  -H 'Authorization: Bearer {{ACCESS_TOKEN}}'
```

**Response (200 OK):**
```json
{
  "planCode": "FREE",
  "status": "ACTIVE",
  "via": "ORGANIZATION",
  "organizationPlan": "ACADEMY_PRO",
  "entitlements": {
    "activitySubmissionsUnlimited": true,
    "videoUploadAllowed": true,
    "maxVideoStorageGb": 50
  }
}
```

---

## 7. Privacy and Consent Controls

Coaches can access athlete data only for athletes explicitly assigned to them within the organization. Accessing data outside this assignment returns `403 FORBIDDEN`.

---

## 8. Error Reference

| Error Code | HTTP Status | Meaning | Example JSON |
|---|---|---|---|
| `UNAUTHORIZED` | 401 | Missing or expired token | `{ "error": "UNAUTHORIZED", "message": "Full authentication is required." }` |
| `FORBIDDEN` | 403 | Not assigned to this athlete | `{ "error": "FORBIDDEN", "message": "You do not have MANAGE permission for this athlete." }` |
| `NOT_FOUND` | 404 | Resource not found | `{ "error": "NOT_FOUND", "message": "AthleteProfile not found." }` |
| `INVALID_STATE_TRANSITION` | 409 | Activity not in correct state | `{ "error": "INVALID_STATE_TRANSITION", "message": "Cannot publish a DRAFT activity. Submit first." }` |
| `VALIDATION_ERROR` | 400 | Bad request | `{ "error": "VALIDATION_ERROR", "errors": [{ "field": "title", "message": "must not be blank" }] }` |
