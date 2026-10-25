# Organization User Guide

## 1. Role Overview

Organizations on TalentHub represent clubs, academies, schools, and professional teams. Within an organization, members operate in differentiated roles:

| Member Role | Responsibilities |
|---|---|
| OWNER | Registers the organization, manages subscription, assigns seat allocations |
| ADMIN | Manages members, reviews athlete affiliations, configures org settings |
| COACH | Submits activities, verifies performance for assigned athletes |
| SCOUT | Uses shared scouting workspace, creates org-level evaluations and watchlists |
| ATHLETE_MANAGER | Requests athlete affiliations, manages the club roster |
| STAFF | View-only access to org activity and reports |

---

## 2. Getting Started

### OWNER: Register an organization

```bash
curl -X POST '{{BASE_URL}}/organizations' \
  -H 'Authorization: Bearer {{ACCESS_TOKEN}}' \
  -H 'Content-Type: application/json' \
  -d '{
    "name": "FC Lagos Academy",
    "type": "FOOTBALL_CLUB",
    "country": "Nigeria",
    "description": "Premier youth development academy based in Lagos"
  }'
```

**Response (201 Created):**
```json
{
  "id": "org-001",
  "name": "FC Lagos Academy",
  "type": "FOOTBALL_CLUB",
  "country": "Nigeria",
  "status": "PENDING_VERIFICATION",
  "orgCode": "FCL"
}
```

### OWNER: Submit organization for verification

An admin will verify the organization before it becomes fully active. You receive an email and in-app notification when the decision is made.

### OWNER: Invite staff

```bash
curl -X POST '{{BASE_URL}}/organizations/org-001/invitations' \
  -H 'Authorization: Bearer {{ACCESS_TOKEN}}' \
  -H 'Content-Type: application/json' \
  -d '{
    "email": "tunde.adeyemi@fclagos.ng",
    "memberRole": "COACH"
  }'
```

**Response (201 Created):**
```json
{
  "invitationId": "inv-001",
  "email": "tunde.adeyemi@fclagos.ng",
  "memberRole": "COACH",
  "expiresAt": "2024-06-22T09:00:00Z"
}
```

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
  "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
  "refreshToken": "e6f7a8b9-c0d1-2345-ef01-234567890abc",
  "tokenType": "Bearer",
  "expiresIn": 3600
}
```

### Token Refresh

```bash
curl -X POST '{{BASE_URL}}/auth/refresh' \
  -H 'Content-Type: application/json' \
  -d '{ "refreshToken": "e6f7a8b9-c0d1-2345-ef01-234567890abc" }'
```

---

## 4. Core Workflows by Role

### OWNER — Activate scouting workspace

```bash
curl -X POST '{{BASE_URL}}/organizations/org-001/scouting/workspace/activate' \
  -H 'Authorization: Bearer {{ACCESS_TOKEN}}'
```

**Response (200 OK):**
```json
{
  "organizationId": "org-001",
  "scoutingWorkspaceActive": true,
  "activatedAt": "2024-06-16T10:00:00Z"
}
```

### OWNER — Assign scout seat allocations

```bash
curl -X POST '{{BASE_URL}}/organizations/org-001/scouting/seats' \
  -H 'Authorization: Bearer {{ACCESS_TOKEN}}' \
  -H 'Content-Type: application/json' \
  -d '{ "seats": 5 }'
```

**Response (200 OK):**
```json
{
  "organizationId": "org-001",
  "allocatedSeats": 5,
  "usedSeats": 0
}
```

### ADMIN — View and manage members

```bash
curl -X GET '{{BASE_URL}}/organizations/org-001/members?page=0&size=20' \
  -H 'Authorization: Bearer {{ACCESS_TOKEN}}'
```

**Response (200 OK):**
```json
{
  "content": [
    {
      "userId": "c1a2b3c4-d5e6-7890-abcd-ef0987654321",
      "fullName": "Tunde Adeyemi",
      "email": "tunde.adeyemi@fclagos.ng",
      "memberRole": "COACH",
      "joinedAt": "2024-06-15T10:00:00Z"
    },
    {
      "userId": "ag1b2c3d-e5f6-7890-abcd-ef1234560000",
      "fullName": "Kwame Mensah",
      "email": "kwame.mensah@fclagos.ng",
      "memberRole": "SCOUT",
      "joinedAt": "2024-06-15T10:30:00Z"
    }
  ],
  "totalElements": 2
}
```

### ADMIN — View affiliated athletes

```bash
curl -X GET '{{BASE_URL}}/organizations/org-001/athletes?page=0&size=20' \
  -H 'Authorization: Bearer {{ACCESS_TOKEN}}'
```

**Response (200 OK):**
```json
{
  "content": [
    {
      "athleteId": "b2c3d4e5-f6a7-8901-bcde-f12345678901",
      "athleteNumber": "FCL-FB-00001",
      "displayName": "Jordan O.",
      "primarySportCode": "FOOTBALL",
      "affiliationStatus": "ACTIVE"
    }
  ],
  "totalElements": 1
}
```

### ATHLETE_MANAGER — Request athlete affiliation

```bash
curl -X POST '{{BASE_URL}}/organizations/org-001/athlete-relationships' \
  -H 'Authorization: Bearer {{ACCESS_TOKEN}}' \
  -H 'Content-Type: application/json' \
  -d '{ "athleteId": "b2c3d4e5-f6a7-8901-bcde-f12345678901" }'
```

**Response (201 Created):**
```json
{
  "id": "aff-001",
  "organizationId": "org-001",
  "athleteId": "b2c3d4e5-f6a7-8901-bcde-f12345678901",
  "status": "PENDING_ATHLETE_APPROVAL",
  "requestedAt": "2024-06-15T11:00:00Z"
}
```

The athlete receives a notification and must approve the affiliation.

### COACH — Submit and verify activity (org context)

```bash
# Submit activity for athlete in the organization
curl -X POST '{{BASE_URL}}/athletes/b2c3d4e5-f6a7-8901-bcde-f12345678901/activities' \
  -H 'Authorization: Bearer {{ACCESS_TOKEN}}' \
  -H 'Content-Type: application/json' \
  -d '{
    "templateId": "tmpl-fb-training-v1",
    "activityTypeId": "FOOTBALL_TRAINING",
    "sportId": "FOOTBALL",
    "title": "Sprint conditioning — phase 1",
    "activityDate": "2024-06-13",
    "organizationId": "org-001",
    "visibilityLevel": "ORGANIZATION",
    "fieldValues": {
      "durationMinutes": "60",
      "intensityLevel": "MEDIUM"
    }
  }'
```

### SCOUT (org context) — View shared watchlists

```bash
curl -X GET '{{BASE_URL}}/organizations/org-001/scouting/watchlists' \
  -H 'Authorization: Bearer {{ACCESS_TOKEN}}'
```

**Response (200 OK):**
```json
{
  "content": [
    {
      "watchlistId": "wl-org-001",
      "name": "Summer 2024 Prospects",
      "ownerType": "ORGANIZATION",
      "ownerId": "org-001",
      "athleteCount": 8
    }
  ]
}
```

### SCOUT (org context) — View shared evaluations

```bash
curl -X GET '{{BASE_URL}}/organizations/org-001/scouting/evaluations' \
  -H 'Authorization: Bearer {{ACCESS_TOKEN}}'
```

---

## 5. Available Endpoints

### Organization Management

| Method | Path | Description |
|---|---|---|
| POST | `/organizations` | Register organization |
| GET | `/organizations/{id}` | Get organization details |
| PATCH | `/organizations/{id}` | Update organization |
| POST | `/organizations/{id}/invitations` | Invite a member |
| GET | `/organizations/{id}/members` | List members |
| GET | `/organizations/{id}/athletes` | List affiliated athletes |
| POST | `/organizations/{id}/athlete-relationships` | Request athlete affiliation |
| POST | `/organizations/{id}/scouting/workspace/activate` | Activate scouting workspace |
| POST | `/organizations/{id}/scouting/seats` | Assign scout seat allocations |
| GET | `/organizations/{id}/scouting/watchlists` | List shared watchlists |
| GET | `/organizations/{id}/scouting/evaluations` | List shared evaluations |

### Scouting (org-scoped)

Organization scouts also use all endpoints documented in the [SCOUT_USER_GUIDE.md](./SCOUT_USER_GUIDE.md). Evaluations and watchlists created with `organizationId` set become shared org resources.

---

## 6. Entitlements and Subscription

#### GET /subscriptions/me

```bash
curl -X GET '{{BASE_URL}}/subscriptions/me' \
  -H 'Authorization: Bearer {{ACCESS_TOKEN}}'
```

**Response (200 OK) — ACADEMY_PRO plan:**
```json
{
  "planCode": "ACADEMY_PRO",
  "status": "ACTIVE",
  "validUntil": "2025-06-15T23:59:59Z",
  "entitlements": {
    "maxMembers": 50,
    "maxAffiliatedAthletes": 200,
    "scoutingWorkspace": true,
    "scoutSeats": 10,
    "sharedWatchlists": true,
    "sharedEvaluations": true,
    "videoStorageGb": 500
  }
}
```

---

## 7. Privacy and Consent Controls

Athletes must approve organization affiliation before their data becomes visible to org members. Athletes can revoke affiliation at any time. Revoking removes coach access to submit activities on their behalf but does not delete existing published activities.

---

## 8. Error Reference

| Error Code | HTTP Status | Meaning | Example JSON |
|---|---|---|---|
| `UNAUTHORIZED` | 401 | Missing or expired token | `{ "error": "UNAUTHORIZED", "message": "Full authentication is required." }` |
| `FORBIDDEN` | 403 | Insufficient organization role | `{ "error": "FORBIDDEN", "message": "ADMIN or OWNER role required for this operation." }` |
| `NOT_FOUND` | 404 | Organization or resource not found | `{ "error": "NOT_FOUND", "message": "Organization not found for id: org-999" }` |
| `PENDING_VERIFICATION` | 403 | Org pending admin verification | `{ "error": "FORBIDDEN", "message": "Organization is pending verification." }` |
| `DUPLICATE_RESOURCE` | 409 | Member or affiliation already exists | `{ "error": "DUPLICATE_RESOURCE", "message": "User is already a member of this organization." }` |
| `ENTITLEMENT_LIMIT_EXCEEDED` | 403 | Seat or member limit reached | `{ "error": "ENTITLEMENT_LIMIT_EXCEEDED", "message": "Scout seat limit reached for your plan." }` |
| `VALIDATION_ERROR` | 400 | Bad request | `{ "error": "VALIDATION_ERROR", "errors": [{ "field": "name", "message": "must not be blank" }] }` |
