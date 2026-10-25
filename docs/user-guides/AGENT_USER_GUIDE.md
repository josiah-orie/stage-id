# Agent User Guide

## 1. Role Overview

Sports agents use TalentHub to represent athletes they manage. With an approved AGENT management relationship, an agent can view the athlete's full profile and development history, manage visibility settings (if the athlete grants ATHLETE_PROFILE_EDIT permission), and receive notifications when the athlete's activities are published.

---

## 2. Getting Started

### Step 1 — Register an account

```bash
curl -X POST '{{BASE_URL}}/auth/register' \
  -H 'Content-Type: application/json' \
  -d '{
    "email": "fatima.nwosu@eliteagency.io",
    "password": "Ag3ntPr0#2024!"
  }'
```

**Response (201 Created):**
```json
{
  "userId": "ag1b2c3d-e5f6-7890-abcd-ef1234560000",
  "message": "Registration successful. Please verify your email."
}
```

### Step 2 — Assign AGENT role

```bash
curl -X POST '{{BASE_URL}}/users/me/roles' \
  -H 'Authorization: Bearer {{ACCESS_TOKEN}}' \
  -H 'Content-Type: application/json' \
  -d '{ "role": "AGENT" }'
```

**Response (200 OK):**
```json
{ "role": "AGENT", "assignedAt": "2024-06-15T09:00:00Z" }
```

### Step 3 — Request a management relationship with an athlete

```bash
curl -X POST '{{BASE_URL}}/guardian-relationships' \
  -H 'Authorization: Bearer {{ACCESS_TOKEN}}' \
  -H 'Content-Type: application/json' \
  -d '{
    "athleteId": "b2c3d4e5-f6a7-8901-bcde-f12345678901",
    "relationshipType": "AGENT"
  }'
```

**Response (201 Created):**
```json
{
  "id": "gr-0010",
  "athleteId": "b2c3d4e5-f6a7-8901-bcde-f12345678901",
  "agentId": "ag1b2c3d-e5f6-7890-abcd-ef1234560000",
  "relationshipType": "AGENT",
  "status": "PENDING_APPROVAL"
}
```

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
    "email": "fatima.nwosu@eliteagency.io",
    "password": "Ag3ntPr0#2024!"
  }'
```

**Response (200 OK):**
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
  "refreshToken": "d5e6f7a8-b9c0-1234-def0-12345678901a",
  "tokenType": "Bearer",
  "expiresIn": 3600
}
```

### Token Refresh

```bash
curl -X POST '{{BASE_URL}}/auth/refresh' \
  -H 'Content-Type: application/json' \
  -d '{ "refreshToken": "d5e6f7a8-b9c0-1234-def0-12345678901a" }'
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
  "id": "b2c3d4e5-f6a7-8901-bcde-f12345678901",
  "athleteNumber": "TH-00001",
  "primarySportCode": "FOOTBALL",
  "position": "Attacking Midfielder",
  "nationality": "Nigeria",
  "heightCm": 175,
  "weightKg": 70,
  "status": "ACTIVE"
}
```

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

### Workflow D — Update athlete profile (if ATHLETE_PROFILE_EDIT permission granted)

The athlete must explicitly grant the ATHLETE_PROFILE_EDIT permission to the agent. Once granted:

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

**403 if permission not granted:**
```json
{
  "error": "FORBIDDEN",
  "message": "ATHLETE_PROFILE_EDIT permission not granted to this agent."
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
{ "id": "gr-0010", "status": "REVOKED", "revokedAt": "2024-09-01T08:00:00Z" }
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
| POST | `/users/me/roles` | Assign AGENT role |
| GET | `/users/me` | Get account info |
| POST | `/guardian-relationships` | Request AGENT relationship |
| GET | `/guardian-relationships` | List relationships |
| POST | `/guardian-relationships/{id}/revoke` | Revoke relationship |
| GET | `/athletes/{id}` | View athlete profile (requires relationship) |
| GET | `/athletes/{id}/development` | Development timeline |
| GET | `/athletes/{id}/activities` | List athlete activities |
| GET | `/athletes/{id}/performance` | Performance data |
| PATCH | `/athletes/{id}` | Update profile (requires ATHLETE_PROFILE_EDIT permission) |
| GET | `/subscriptions/me` | View subscription |
| GET | `/notifications` | List notifications |
| PATCH | `/notifications/{id}/read` | Mark notification read |

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
    "agentManagementAllowed": true,
    "managedAthletes": 3,
    "profileEditAllowed": false
  }
}
```

Upgrade to an AGENT_PRO plan to manage more athletes and unlock profile editing entitlements:

```bash
curl -X POST '{{BASE_URL}}/subscriptions' \
  -H 'Authorization: Bearer {{ACCESS_TOKEN}}' \
  -H 'Content-Type: application/json' \
  -d '{ "planCode": "AGENT_PRO", "currency": "USD" }'
```

---

## 7. Privacy and Consent Controls

Agent relationships grant read access to the athlete's data as configured by the athlete. If the athlete withdraws discovery consent, you can still view their profile (you have an approved relationship) but their profile disappears from scout discovery.

To end your access entirely, revoke the management relationship (see Workflow F).

---

## 8. Error Reference

| Error Code | HTTP Status | Meaning | Example JSON |
|---|---|---|---|
| `UNAUTHORIZED` | 401 | Missing or expired token | `{ "error": "UNAUTHORIZED", "message": "Full authentication is required." }` |
| `FORBIDDEN` | 403 | No approved agent relationship | `{ "error": "FORBIDDEN", "message": "No approved AGENT relationship for this athlete." }` |
| `FORBIDDEN` | 403 | Missing specific permission | `{ "error": "FORBIDDEN", "message": "ATHLETE_PROFILE_EDIT permission not granted." }` |
| `NOT_FOUND` | 404 | Resource not found | `{ "error": "NOT_FOUND", "message": "AthleteProfile not found." }` |
| `DUPLICATE_RESOURCE` | 409 | Relationship already exists | `{ "error": "DUPLICATE_RESOURCE", "message": "An AGENT relationship already exists for this athlete." }` |
| `VALIDATION_ERROR` | 400 | Bad request | `{ "error": "VALIDATION_ERROR", "errors": [{ "field": "athleteId", "message": "must not be blank" }] }` |
