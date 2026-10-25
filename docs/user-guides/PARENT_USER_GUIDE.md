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
    "password": "Secur3Parent#2024"
  }'
```

**Response (201 Created):**
```json
{
  "userId": "p1a2b3c4-d5e6-7890-abcd-ef9876543210",
  "message": "Registration successful. Please verify your email."
}
```

### Step 2 — Verify email, then assign PARENT role

```bash
curl -X POST '{{BASE_URL}}/users/me/roles' \
  -H 'Authorization: Bearer {{ACCESS_TOKEN}}' \
  -H 'Content-Type: application/json' \
  -d '{ "role": "PARENT" }'
```

### Step 3 — Create your guardian profile

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
  "athleteId": "b2c3d4e5-f6a7-8901-bcde-f12345678901",
  "guardianId": "p1a2b3c4-d5e6-7890-abcd-ef9876543210",
  "relationshipType": "PARENT",
  "status": "PENDING_APPROVAL"
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
  "status": "APPROVED",
  "approvedAt": "2024-06-15T11:00:00Z"
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
  "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
  "refreshToken": "f1e2d3c4-b5a6-9870-dcba-098765432101",
  "tokenType": "Bearer",
  "expiresIn": 3600
}
```

### Token Refresh

```bash
curl -X POST '{{BASE_URL}}/auth/refresh' \
  -H 'Content-Type: application/json' \
  -d '{ "refreshToken": "f1e2d3c4-b5a6-9870-dcba-098765432101" }'
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
[
  {
    "athleteId": "b2c3d4e5-f6a7-8901-bcde-f12345678901",
    "athleteNumber": "TH-00001",
    "fullName": "Jordan Okafor",
    "primarySportCode": "FOOTBALL",
    "relationshipType": "PARENT",
    "status": "APPROVED"
  }
]
```

### Workflow B — Submit an activity on behalf of your child

This uses the same activity endpoint as the athlete but is called with your parent access token. Your guardian relationship grants you MANAGE permission over the athlete's activities.

```bash
curl -X POST '{{BASE_URL}}/athletes/b2c3d4e5-f6a7-8901-bcde-f12345678901/activities' \
  -H 'Authorization: Bearer {{ACCESS_TOKEN}}' \
  -H 'Content-Type: application/json' \
  -d '{
    "templateId": "tmpl-fb-match-v1",
    "activityTypeId": "FOOTBALL_MATCH",
    "sportId": "FOOTBALL",
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

**Response (201 Created):** relationship object with `status: "PENDING_APPROVAL"`.

**Common errors:**
- `409` — relationship already exists for this pair
- `404` — athleteId not found

#### GET /guardian-relationships

```bash
curl -X GET '{{BASE_URL}}/guardian-relationships' \
  -H 'Authorization: Bearer {{ACCESS_TOKEN}}'
```

**Response (200 OK):** list of relationships (pending, approved, revoked).

#### POST /guardian-relationships/{id}/approve

Approve a request (called by the athlete, not the parent).

#### POST /guardian-relationships/{id}/revoke

```bash
curl -X POST '{{BASE_URL}}/guardian-relationships/gr-0001/revoke' \
  -H 'Authorization: Bearer {{ACCESS_TOKEN}}'
```

**Response (200 OK):**
```json
{ "id": "gr-0001", "status": "REVOKED", "revokedAt": "2024-07-01T08:00:00Z" }
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

```bash
curl -X GET '{{BASE_URL}}/subscriptions/me' \
  -H 'Authorization: Bearer {{ACCESS_TOKEN}}'
```

**Response (200 OK):**
```json
{
  "planCode": "FREE",
  "status": "ACTIVE",
  "entitlements": {
    "guardianAccountAllowed": true,
    "managedAthletes": 2
  }
}
```

**403 when managing more athletes than plan allows:**
```json
{
  "error": "ENTITLEMENT_LIMIT_EXCEEDED",
  "message": "Your FREE plan allows managing up to 2 athletes.",
  "upgradeUrl": "https://talenthub.io/pricing"
}
```

---

## 7. Privacy and Consent Controls

As a parent with an approved relationship, you can view but not override an athlete's consent settings. Only the athlete (or an admin) can grant or withdraw consent on behalf of the profile.

To discuss consent settings with your child, encourage them to use the athlete's own consent endpoint:

```bash
# Child athlete must call this:
curl -X POST '{{BASE_URL}}/safeguarding/consent' \
  -H 'Authorization: Bearer {{ATHLETE_ACCESS_TOKEN}}' \
  -H 'Content-Type: application/json' \
  -d '{ "action": "WITHDRAW" }'
```

---

## 8. Safeguarding Notes

**Minor athlete protections on TalentHub:**

- A parent relationship must be in `APPROVED` status before the child's profile becomes visible to scouts.
- Parents receive notifications when the child's visibility or consent settings change.
- Either party (athlete or parent) can revoke the guardian relationship at any time. Revocation does not delete activities or media, but removes the parent's management permissions immediately.
- If the athlete is under 13, platform access is restricted to verified parent accounts only.
- Parents can contact `safeguarding@talenthub.io` to report a concern or escalate to the platform safeguarding team.

---

## 9. Error Reference

| Error Code | HTTP Status | Meaning | Example JSON |
|---|---|---|---|
| `UNAUTHORIZED` | 401 | Missing or expired token | `{ "error": "UNAUTHORIZED", "message": "Full authentication is required." }` |
| `FORBIDDEN` | 403 | No guardian relationship or insufficient permission | `{ "error": "FORBIDDEN", "message": "You do not have MANAGE permission for this athlete." }` |
| `NOT_FOUND` | 404 | Resource not found | `{ "error": "NOT_FOUND", "message": "GuardianRelationship not found for id: gr-9999" }` |
| `DUPLICATE_RESOURCE` | 409 | Relationship already exists | `{ "error": "DUPLICATE_RESOURCE", "message": "A guardian relationship already exists." }` |
| `INVALID_STATE_TRANSITION` | 409 | Relationship in wrong state | `{ "error": "INVALID_STATE_TRANSITION", "message": "Cannot approve a REVOKED relationship." }` |
| `VALIDATION_ERROR` | 400 | Bad request body | `{ "error": "VALIDATION_ERROR", "errors": [{ "field": "athleteId", "message": "must not be blank" }] }` |
