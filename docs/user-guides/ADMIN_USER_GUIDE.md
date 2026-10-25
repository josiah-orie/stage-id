# Admin User Guide

## 1. Role Overview

Delegated administrators (ADMIN role) handle platform operations: verifying organizations and scouts, moderating flagged media, reviewing safeguarding reports, querying audit logs, and managing user accounts. Admins cannot modify subscription plans or sports schemas — those operations require the SUPER_ADMIN role.

---

## 2. Getting Started

Admin accounts are created and promoted by a SUPER_ADMIN. You will receive an email invitation with temporary credentials.

### Login (first time)

```bash
curl -X POST '{{BASE_URL}}/auth/login' \
  -H 'Content-Type: application/json' \
  -d '{
    "email": "admin.seun@talenthub.io",
    "password": "T3mpAdm!n#2024"
  }'
```

**Response (200 OK):**
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
  "refreshToken": "f7a8b9c0-d1e2-3456-f012-3456789abcde",
  "tokenType": "Bearer",
  "expiresIn": 3600
}
```

Change your password immediately after the first login.

### Token Refresh

```bash
curl -X POST '{{BASE_URL}}/auth/refresh' \
  -H 'Content-Type: application/json' \
  -d '{ "refreshToken": "f7a8b9c0-d1e2-3456-f012-3456789abcde" }'
```

### Logout

```bash
curl -X POST '{{BASE_URL}}/auth/logout' \
  -H 'Authorization: Bearer {{ACCESS_TOKEN}}'
```

---

## 3. Authentication

All admin endpoints require a valid JWT with the ADMIN (or SUPER_ADMIN) role.

---

## 4. Core Workflows

### Workflow A — View the platform dashboard

```bash
curl -X GET '{{BASE_URL}}/admin/reports/platform' \
  -H 'Authorization: Bearer {{ACCESS_TOKEN}}'
```

**Response (200 OK):**
```json
{
  "totalUsers": 4821,
  "activeAthletes": 3102,
  "verifiedScouts": 214,
  "verifiedOrganizations": 87,
  "pendingVerifications": {
    "organizations": 5,
    "scouts": 12,
    "media": 3
  },
  "activitiesThisMonth": 1840,
  "openSafeguardingReports": 2,
  "reportGeneratedAt": "2024-06-15T08:00:00Z"
}
```

### Workflow B — Verify an organization

#### Step 1 — List pending organization verifications

```bash
curl -X GET '{{BASE_URL}}/admin/verifications/organizations?status=PENDING&page=0&size=20' \
  -H 'Authorization: Bearer {{ACCESS_TOKEN}}'
```

**Response (200 OK):**
```json
{
  "content": [
    {
      "organizationId": "org-002",
      "name": "Kano Youth FC",
      "type": "FOOTBALL_CLUB",
      "country": "Nigeria",
      "submittedAt": "2024-06-14T09:00:00Z",
      "status": "PENDING_VERIFICATION"
    }
  ],
  "totalElements": 5
}
```

#### Step 2 — Approve the organization

```bash
curl -X POST '{{BASE_URL}}/admin/organizations/org-002/verify' \
  -H 'Authorization: Bearer {{ACCESS_TOKEN}}'
```

**Response (200 OK):**
```json
{
  "organizationId": "org-002",
  "status": "VERIFIED",
  "verifiedBy": "admin.seun@talenthub.io",
  "verifiedAt": "2024-06-15T10:00:00Z"
}
```

#### Or reject with a reason

```bash
curl -X POST '{{BASE_URL}}/admin/organizations/org-002/reject' \
  -H 'Authorization: Bearer {{ACCESS_TOKEN}}' \
  -H 'Content-Type: application/json' \
  -d '{ "reason": "Submitted documents are expired. Please re-submit with current registration certificate." }'
```

**Response (200 OK):**
```json
{
  "organizationId": "org-002",
  "status": "REJECTED",
  "reason": "Submitted documents are expired.",
  "rejectedBy": "admin.seun@talenthub.io"
}
```

### Workflow C — Verify a scout

```bash
# List pending scouts
curl -X GET '{{BASE_URL}}/admin/scouts?status=PENDING_VERIFICATION' \
  -H 'Authorization: Bearer {{ACCESS_TOKEN}}'
```

**Response (200 OK):**
```json
{
  "content": [
    {
      "scoutId": "sc-0050",
      "displayName": "Yemi Babatunde",
      "country": "Nigeria",
      "organizationName": "Lagos Talent Spotters",
      "submittedAt": "2024-06-13T11:00:00Z"
    }
  ]
}
```

```bash
# Approve scout
curl -X POST '{{BASE_URL}}/admin/scouts/sc-0050/verify' \
  -H 'Authorization: Bearer {{ACCESS_TOKEN}}'
```

**Response (200 OK):**
```json
{
  "scoutId": "sc-0050",
  "status": "VERIFIED",
  "verifiedAt": "2024-06-15T10:30:00Z"
}
```

```bash
# Reject scout
curl -X POST '{{BASE_URL}}/admin/scouts/sc-0050/reject' \
  -H 'Authorization: Bearer {{ACCESS_TOKEN}}' \
  -H 'Content-Type: application/json' \
  -d '{ "reason": "Identity document could not be verified. Please submit a government-issued ID." }'
```

### Workflow D — Moderate flagged media

```bash
# List flagged media
curl -X GET '{{BASE_URL}}/admin/media/flagged?page=0&size=20' \
  -H 'Authorization: Bearer {{ACCESS_TOKEN}}'
```

**Response (200 OK):**
```json
{
  "content": [
    {
      "mediaAssetId": "media-001",
      "ownerId": "b2c3d4e5-f6a7-8901-bcde-f12345678901",
      "mimeType": "video/mp4",
      "flagReason": "AUTO_DETECT_INAPPROPRIATE",
      "flaggedAt": "2024-06-14T08:00:00Z"
    }
  ]
}
```

```bash
# Approve media
curl -X POST '{{BASE_URL}}/admin/media/media-001/approve' \
  -H 'Authorization: Bearer {{ACCESS_TOKEN}}'
```

```bash
# Restrict media
curl -X POST '{{BASE_URL}}/admin/media/media-001/restrict' \
  -H 'Authorization: Bearer {{ACCESS_TOKEN}}' \
  -H 'Content-Type: application/json' \
  -d '{ "reason": "Content violates platform guidelines — inappropriate imagery." }'
```

### Workflow E — Review safeguarding reports

```bash
# List open safeguarding reports
curl -X GET '{{BASE_URL}}/admin/safeguarding/reports?status=OPEN' \
  -H 'Authorization: Bearer {{ACCESS_TOKEN}}'
```

**Response (200 OK):**
```json
{
  "content": [
    {
      "reportId": "sg-rep-001",
      "athleteId": "b2c3d4e5-f6a7-8901-bcde-f12345678901",
      "reportedBy": "parent-user-id",
      "category": "UNAUTHORIZED_CONTACT",
      "description": "Unknown adult sent direct messages requesting personal contact details.",
      "status": "OPEN",
      "createdAt": "2024-06-14T07:00:00Z"
    }
  ]
}
```

```bash
# Review and act on report
curl -X POST '{{BASE_URL}}/admin/safeguarding/reports/sg-rep-001/review' \
  -H 'Authorization: Bearer {{ACCESS_TOKEN}}' \
  -H 'Content-Type: application/json' \
  -d '{
    "outcome": "ACCOUNT_SUSPENDED",
    "notes": "User has been suspended pending investigation. Contact details passed to safeguarding officer."
  }'
```

**Response (200 OK):**
```json
{
  "reportId": "sg-rep-001",
  "status": "REVIEWED",
  "outcome": "ACCOUNT_SUSPENDED",
  "reviewedBy": "admin.seun@talenthub.io",
  "reviewedAt": "2024-06-15T09:00:00Z"
}
```

### Workflow F — Manage user accounts

```bash
# List all users
curl -X GET '{{BASE_URL}}/admin/users?page=0&size=20' \
  -H 'Authorization: Bearer {{ACCESS_TOKEN}}'
```

**Response (200 OK):**
```json
{
  "content": [
    {
      "userId": "b2c3d4e5-f6a7-8901-bcde-f12345678901",
      "email": "jordan.okafor@example.com",
      "role": "ATHLETE",
      "status": "ACTIVE",
      "createdAt": "2024-06-01T09:00:00Z"
    }
  ],
  "totalElements": 4821
}
```

```bash
# Suspend a user
curl -X POST '{{BASE_URL}}/admin/users/b2c3d4e5-f6a7-8901-bcde-f12345678901/suspend' \
  -H 'Authorization: Bearer {{ACCESS_TOKEN}}' \
  -H 'Content-Type: application/json' \
  -d '{ "reason": "Multiple reports of inappropriate contact with minor athletes." }'
```

**Response (200 OK):**
```json
{
  "userId": "b2c3d4e5-f6a7-8901-bcde-f12345678901",
  "status": "SUSPENDED",
  "suspendedAt": "2024-06-15T11:00:00Z",
  "reason": "Multiple reports of inappropriate contact with minor athletes."
}
```

```bash
# Reactivate a suspended user
curl -X POST '{{BASE_URL}}/admin/users/b2c3d4e5-f6a7-8901-bcde-f12345678901/reactivate' \
  -H 'Authorization: Bearer {{ACCESS_TOKEN}}'
```

### Workflow G — Query audit logs

```bash
curl -X GET '{{BASE_URL}}/admin/audit-logs?action=USER_SUSPENDED&from=2024-06-01T00:00:00Z&page=0&size=20' \
  -H 'Authorization: Bearer {{ACCESS_TOKEN}}'
```

**Response (200 OK):**
```json
{
  "content": [
    {
      "id": "audit-001",
      "actorId": "admin.seun@talenthub.io",
      "action": "USER_SUSPENDED",
      "targetEntityType": "USER",
      "targetEntityId": "b2c3d4e5-f6a7-8901-bcde-f12345678901",
      "metadata": { "reason": "Multiple reports..." },
      "createdAt": "2024-06-15T11:00:00Z"
    }
  ],
  "totalElements": 1
}
```

---

## 5. Available Endpoints

| Method | Path | Description |
|---|---|---|
| GET | `/admin/reports/platform` | Platform dashboard |
| GET | `/admin/users` | List users |
| POST | `/admin/users/{id}/suspend` | Suspend user |
| POST | `/admin/users/{id}/reactivate` | Reactivate user |
| GET | `/admin/verifications/organizations` | List pending org verifications |
| POST | `/admin/organizations/{id}/verify` | Approve organization |
| POST | `/admin/organizations/{id}/reject` | Reject organization |
| GET | `/admin/scouts` | List scouts pending verification |
| POST | `/admin/scouts/{id}/verify` | Approve scout |
| POST | `/admin/scouts/{id}/reject` | Reject scout |
| GET | `/admin/media/flagged` | List flagged media |
| POST | `/admin/media/{id}/approve` | Approve media |
| POST | `/admin/media/{id}/restrict` | Restrict media |
| GET | `/admin/safeguarding/reports` | List safeguarding reports |
| POST | `/admin/safeguarding/reports/{id}/review` | Review a report |
| GET | `/admin/audit-logs` | Query audit logs (own scope) |
| POST | `/admin/activity-types` | Create activity type |

---

## 6. Entitlements and Subscription

Admin accounts operate under the platform's internal ADMIN entitlement plan and are not subject to athlete/scout subscription limits. Admin access is controlled by role assignment, not subscription tier.

---

## 7. Privacy and Consent Controls

Admins can access user data for the purpose of moderation, verification, and safeguarding. All admin actions are logged in the audit log with the admin's user ID, action type, target entity, and timestamp.

Admins must not access or share athlete data beyond what is needed for the specific moderation task (data minimisation principle).

---

## 8. Safeguarding Notes

**Admin responsibilities for safeguarding:**

- All OPEN safeguarding reports must be reviewed within 48 hours.
- Reports involving minors must be escalated to the designated safeguarding lead.
- Outcome options: NO_ACTION, WARNING_ISSUED, ACCOUNT_SUSPENDED, ESCALATED.
- Account suspensions triggered by safeguarding reports are permanent until reviewed by a SUPER_ADMIN.
- If a report indicates imminent risk to a child, immediately suspend the involved account and escalate to `safeguarding@talenthub.io` and relevant authorities.
- All safeguarding review actions are permanently retained in the audit log and cannot be deleted.

---

## 9. Error Reference

| Error Code | HTTP Status | Meaning | Example JSON |
|---|---|---|---|
| `UNAUTHORIZED` | 401 | Missing or expired token | `{ "error": "UNAUTHORIZED", "message": "Full authentication is required." }` |
| `FORBIDDEN` | 403 | Insufficient role (ADMIN or SUPER_ADMIN required) | `{ "error": "FORBIDDEN", "message": "Admin role required." }` |
| `NOT_FOUND` | 404 | Resource not found | `{ "error": "NOT_FOUND", "message": "Organization not found for id: org-999" }` |
| `INVALID_STATE_TRANSITION` | 409 | Already verified/rejected | `{ "error": "INVALID_STATE_TRANSITION", "message": "Organization is already VERIFIED." }` |
| `VALIDATION_ERROR` | 400 | Bad request body | `{ "error": "VALIDATION_ERROR", "errors": [{ "field": "reason", "message": "must not be blank" }] }` |
