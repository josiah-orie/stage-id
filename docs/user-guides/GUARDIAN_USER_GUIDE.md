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
    "password": "L3galGuard#2024!"
  }'
```

**Response (201 Created):**
```json
{
  "userId": "g1b2c3d4-e5f6-7890-cdef-ab1234567890",
  "message": "Registration successful. Please verify your email."
}
```

### Step 2 — Verify email, then assign GUARDIAN role

```bash
curl -X POST '{{BASE_URL}}/users/me/roles' \
  -H 'Authorization: Bearer {{ACCESS_TOKEN}}' \
  -H 'Content-Type: application/json' \
  -d '{ "role": "GUARDIAN" }'
```

**Response (200 OK):**
```json
{ "role": "GUARDIAN", "assignedAt": "2024-06-15T10:00:00Z" }
```

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
  "athleteId": "b2c3d4e5-f6a7-8901-bcde-f12345678901",
  "guardianId": "g1b2c3d4-e5f6-7890-cdef-ab1234567890",
  "relationshipType": "LEGAL_GUARDIAN",
  "status": "PENDING_ADMIN_VERIFICATION",
  "createdAt": "2024-06-15T10:05:00Z"
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
[
  {
    "id": "gr-0002",
    "relationshipType": "LEGAL_GUARDIAN",
    "status": "APPROVED",
    "approvedAt": "2024-06-16T09:00:00Z",
    "expiresAt": "2025-06-15T23:59:59Z"
  }
]
```

Note the `expiresAt` field — legal guardian relationships require annual renewal.

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
  "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
  "refreshToken": "a2b3c4d5-e6f7-8901-bcde-f01234567890",
  "tokenType": "Bearer",
  "expiresIn": 3600
}
```

### Token Refresh

```bash
curl -X POST '{{BASE_URL}}/auth/refresh' \
  -H 'Content-Type: application/json' \
  -d '{ "refreshToken": "a2b3c4d5-e6f7-8901-bcde-f01234567890" }'
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
[
  {
    "athleteId": "b2c3d4e5-f6a7-8901-bcde-f12345678901",
    "athleteNumber": "TH-00001",
    "fullName": "Jordan Okafor",
    "relationshipType": "LEGAL_GUARDIAN",
    "status": "APPROVED",
    "expiresAt": "2025-06-15T23:59:59Z"
  }
]
```

### Workflow C — Submit activity on behalf of the athlete

```bash
curl -X POST '{{BASE_URL}}/athletes/b2c3d4e5-f6a7-8901-bcde-f12345678901/activities' \
  -H 'Authorization: Bearer {{ACCESS_TOKEN}}' \
  -H 'Content-Type: application/json' \
  -d '{
    "templateId": "tmpl-fb-match-v1",
    "activityTypeId": "FOOTBALL_MATCH",
    "sportId": "FOOTBALL",
    "title": "Academy Cup — Semi-final",
    "activityDate": "2024-06-20",
    "location": "National Stadium, Abuja",
    "visibilityLevel": "GUARDIAN_ONLY",
    "fieldValues": {
      "opponent": "Kogi Youths FC",
      "goals": "1",
      "minutesPlayed": "85"
    }
  }'
```

Use `visibilityLevel: "GUARDIAN_ONLY"` to restrict visibility to the guardian and assigned coaches only.

### Workflow D — Revoke the guardian relationship

```bash
curl -X POST '{{BASE_URL}}/guardian-relationships/gr-0002/revoke' \
  -H 'Authorization: Bearer {{ACCESS_TOKEN}}'
```

**Response (200 OK):**
```json
{ "id": "gr-0002", "status": "REVOKED", "revokedAt": "2024-08-01T12:00:00Z" }
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
| POST | `/users/me/roles` | Assign GUARDIAN role |
| GET | `/users/me` | Get own account info |
| POST | `/guardian-relationships` | Request LEGAL_GUARDIAN link |
| GET | `/guardian-relationships` | List own relationships |
| POST | `/guardian-relationships/{id}/revoke` | Revoke relationship |
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
    "legalGuardianVerificationAllowed": true,
    "managedAthletes": 2
  }
}
```

**403 when feature not available:**
```json
{
  "error": "FEATURE_NOT_AVAILABLE",
  "message": "Legal guardian verification requires the platform GUARDIAN plan.",
  "upgradeUrl": "https://talenthub.io/pricing"
}
```

---

## 7. Privacy and Consent Controls

As a LEGAL_GUARDIAN with an approved relationship, you may review the athlete's consent status:

```bash
curl -X GET '{{BASE_URL}}/safeguarding/consent' \
  -H 'Authorization: Bearer {{ACCESS_TOKEN}}'
```

You cannot unilaterally withdraw the athlete's consent. Contact `safeguarding@talenthub.io` if you need emergency consent withdrawal for a minor in your care.

---

## 8. Safeguarding Notes — Extended

**Legal Guardian verification:**

- Every `LEGAL_GUARDIAN` relationship requires a signed court order, guardianship certificate, or equivalent document.
- Platform admins review the document before the relationship is activated. This typically takes 1–2 business days.
- Relationships expire annually. You will receive a renewal reminder 30 days before expiry. Failure to renew suspends your management permissions without removing the athlete's data.
- If the relationship is revoked by the athlete or an admin, your access to the athlete's data is suspended immediately.

**Reporting concerns:**

- Use `safeguarding@talenthub.io` to report any safeguarding concern immediately.
- Emergency revocations (e.g., a court order is overturned) are processed within 4 hours during business hours.

---

## 9. Error Reference

| Error Code | HTTP Status | Meaning | Example JSON |
|---|---|---|---|
| `UNAUTHORIZED` | 401 | Missing or expired token | `{ "error": "UNAUTHORIZED", "message": "Full authentication is required." }` |
| `FORBIDDEN` | 403 | No approved guardian relationship | `{ "error": "FORBIDDEN", "message": "No approved LEGAL_GUARDIAN relationship found." }` |
| `NOT_FOUND` | 404 | Resource not found | `{ "error": "NOT_FOUND", "message": "GuardianRelationship not found." }` |
| `PENDING_VERIFICATION` | 403 | Relationship awaiting admin review | `{ "error": "FORBIDDEN", "message": "Guardian relationship is pending admin verification." }` |
| `RELATIONSHIP_EXPIRED` | 403 | Annual verification lapsed | `{ "error": "FORBIDDEN", "message": "LEGAL_GUARDIAN relationship has expired. Please renew." }` |
| `DUPLICATE_RESOURCE` | 409 | Link already exists | `{ "error": "DUPLICATE_RESOURCE", "message": "A legal guardian relationship already exists." }` |
| `VALIDATION_ERROR` | 400 | Bad request | `{ "error": "VALIDATION_ERROR", "errors": [{ "field": "verificationDocumentRef", "message": "must not be blank" }] }` |
