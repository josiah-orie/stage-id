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
    "password": "Sc0ut!Pro#2024"
  }'
```

**Response (201 Created):**
```json
{
  "userId": "s1a2b3c4-d5e6-7890-abcd-ef1234509876",
  "message": "Registration successful. Please verify your email."
}
```

### Step 2 — Assign SCOUT role

```bash
curl -X POST '{{BASE_URL}}/users/me/roles' \
  -H 'Authorization: Bearer {{ACCESS_TOKEN}}' \
  -H 'Content-Type: application/json' \
  -d '{ "role": "SCOUT" }'
```

### Step 3 — Create scout profile

```bash
curl -X POST '{{BASE_URL}}/scouts/profile' \
  -H 'Authorization: Bearer {{ACCESS_TOKEN}}' \
  -H 'Content-Type: application/json' \
  -d '{
    "displayName": "Kwame Mensah",
    "bio": "Youth talent scout with 10 years experience across West Africa",
    "organizationName": "Accra Talent Agency",
    "country": "Ghana"
  }'
```

**Response (201 Created):**
```json
{
  "scoutId": "sc-0001",
  "displayName": "Kwame Mensah",
  "country": "Ghana",
  "status": "PENDING_VERIFICATION"
}
```

### Step 4 — Submit verification (admin reviews)

An admin will verify your identity. Status transitions to `VERIFIED` once approved. You receive an in-app notification and email.

### Step 5 — Subscribe to a paid plan

Discovery and evaluation features require a paid plan.

```bash
curl -X POST '{{BASE_URL}}/subscriptions' \
  -H 'Authorization: Bearer {{ACCESS_TOKEN}}' \
  -H 'Content-Type: application/json' \
  -d '{
    "planCode": "SCOUT_PRO",
    "currency": "USD"
  }'
```

**Response (201 Created):**
```json
{
  "subscriptionId": "sub-0001",
  "paymentReference": "PAY-ref-20240615-001",
  "checkoutUrl": "https://checkout.paystack.com/k2j3l4m5n6o7p8q9"
}
```

Complete payment via the `checkoutUrl`. Once Paystack confirms payment, your subscription activates automatically.

---

## 3. Authentication

### Login

```bash
curl -X POST '{{BASE_URL}}/auth/login' \
  -H 'Content-Type: application/json' \
  -d '{
    "email": "kwame.mensah@scoutpro.io",
    "password": "Sc0ut!Pro#2024"
  }'
```

**Response (200 OK):**
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
  "refreshToken": "b3c4d5e6-f7a8-9012-bcde-f012345678ab",
  "tokenType": "Bearer",
  "expiresIn": 3600
}
```

### Token Refresh

```bash
curl -X POST '{{BASE_URL}}/auth/refresh' \
  -H 'Content-Type: application/json' \
  -d '{ "refreshToken": "b3c4d5e6-f7a8-9012-bcde-f012345678ab" }'
```

### Logout

```bash
curl -X POST '{{BASE_URL}}/auth/logout' \
  -H 'Authorization: Bearer {{ACCESS_TOKEN}}'
```

---

## 4. Core Workflows

### Workflow A — Add specializations to your scout profile

```bash
curl -X POST '{{BASE_URL}}/scouts/me/specializations' \
  -H 'Authorization: Bearer {{ACCESS_TOKEN}}' \
  -H 'Content-Type: application/json' \
  -d '{
    "sportCode": "FOOTBALL",
    "position": "Striker",
    "ageCategory": "U-17"
  }'
```

**Response (201 Created):**
```json
{
  "id": "spec-001",
  "sportCode": "FOOTBALL",
  "position": "Striker",
  "ageCategory": "U-17"
}
```

### Workflow B — Discover athletes

```bash
curl -X GET '{{BASE_URL}}/discovery/athletes?sport=FOOTBALL&country=Nigeria&position=Striker&page=0&size=20' \
  -H 'Authorization: Bearer {{ACCESS_TOKEN}}'
```

**Response (200 OK):**
```json
{
  "content": [
    {
      "athleteId": "b2c3d4e5-f6a7-8901-bcde-f12345678901",
      "athleteNumber": "TH-00001",
      "displayName": "Jordan O.",
      "primarySportCode": "FOOTBALL",
      "position": "Attacking Midfielder",
      "country": "Nigeria",
      "ageCategory": "U-17",
      "verificationStatus": "VERIFIED",
      "highlightMediaAvailable": true
    }
  ],
  "totalElements": 1,
  "page": 0,
  "size": 20
}
```

**403 on free plan:**
```json
{
  "error": "FEATURE_NOT_AVAILABLE",
  "message": "Athlete discovery requires a paid SCOUT or SCOUT_PRO plan.",
  "upgradeUrl": "https://talenthub.io/pricing"
}
```

### Workflow C — Save a search for repeated use

```bash
curl -X POST '{{BASE_URL}}/discovery/saved-searches' \
  -H 'Authorization: Bearer {{ACCESS_TOKEN}}' \
  -H 'Content-Type: application/json' \
  -d '{
    "name": "Nigeria U-17 Strikers",
    "filters": {
      "sport": "FOOTBALL",
      "country": "Nigeria",
      "position": "Striker",
      "ageCategory": "U-17"
    }
  }'
```

**Response (201 Created):**
```json
{
  "id": "ss-001",
  "name": "Nigeria U-17 Strikers",
  "filters": { "sport": "FOOTBALL", "country": "Nigeria", "position": "Striker", "ageCategory": "U-17" }
}
```

### Workflow D — Add athlete to favourites

```bash
curl -X POST '{{BASE_URL}}/scouting/favourites' \
  -H 'Authorization: Bearer {{ACCESS_TOKEN}}' \
  -H 'Content-Type: application/json' \
  -d '{ "athleteId": "b2c3d4e5-f6a7-8901-bcde-f12345678901" }'
```

**Response (201 Created):**
```json
{
  "id": "fav-001",
  "athleteId": "b2c3d4e5-f6a7-8901-bcde-f12345678901",
  "addedAt": "2024-06-15T14:00:00Z"
}
```

### Workflow E — Create a watchlist and add an athlete

```bash
# Create watchlist
curl -X POST '{{BASE_URL}}/scouting/watchlists' \
  -H 'Authorization: Bearer {{ACCESS_TOKEN}}' \
  -H 'Content-Type: application/json' \
  -d '{
    "name": "West Africa U-17 Shortlist",
    "description": "Top prospects for the upcoming regional tournament"
  }'
```

**Response (201 Created):**
```json
{
  "watchlistId": "wl-001",
  "name": "West Africa U-17 Shortlist",
  "athleteCount": 0
}
```

```bash
# Add athlete to watchlist
curl -X POST '{{BASE_URL}}/scouting/watchlists/wl-001/athletes' \
  -H 'Authorization: Bearer {{ACCESS_TOKEN}}' \
  -H 'Content-Type: application/json' \
  -d '{
    "athleteId": "b2c3d4e5-f6a7-8901-bcde-f12345678901",
    "notes": "Strong pressing game, good first touch in tight spaces."
  }'
```

**Response (201 Created):**
```json
{
  "id": "we-001",
  "watchlistId": "wl-001",
  "athleteId": "b2c3d4e5-f6a7-8901-bcde-f12345678901",
  "notes": "Strong pressing game, good first touch in tight spaces.",
  "addedAt": "2024-06-15T14:15:00Z"
}
```

### Workflow F — Follow an athlete (enables development notifications)

```bash
curl -X POST '{{BASE_URL}}/scouting/follows' \
  -H 'Authorization: Bearer {{ACCESS_TOKEN}}' \
  -H 'Content-Type: application/json' \
  -d '{ "athleteId": "b2c3d4e5-f6a7-8901-bcde-f12345678901" }'
```

**Response (201 Created):**
```json
{
  "followId": "fol-001",
  "athleteId": "b2c3d4e5-f6a7-8901-bcde-f12345678901",
  "followedAt": "2024-06-15T14:30:00Z"
}
```

You will now receive `DEVELOPMENT_UPDATE` notifications when the athlete publishes new activities.

### Workflow G — Create an evaluation

```bash
curl -X POST '{{BASE_URL}}/scouting/evaluations' \
  -H 'Authorization: Bearer {{ACCESS_TOKEN}}' \
  -H 'Content-Type: application/json' \
  -d '{
    "athleteId": "b2c3d4e5-f6a7-8901-bcde-f12345678901",
    "notes": "Observed in the quarter-final match on 10 June 2024."
  }'
```

**Response (201 Created):**
```json
{
  "evaluationId": "eval-001",
  "athleteId": "b2c3d4e5-f6a7-8901-bcde-f12345678901",
  "status": "DRAFT",
  "createdAt": "2024-06-15T15:00:00Z"
}
```

**403 on free plan:**
```json
{
  "error": "FEATURE_NOT_AVAILABLE",
  "message": "Evaluations require a SCOUT or SCOUT_PRO plan."
}
```

#### Add scores to the evaluation

```bash
curl -X POST '{{BASE_URL}}/scouting/evaluations/eval-001/scores' \
  -H 'Authorization: Bearer {{ACCESS_TOKEN}}' \
  -H 'Content-Type: application/json' \
  -d '{
    "criteria": {
      "technicalSkill": 8,
      "physicalAttributes": 7,
      "tacticalAwareness": 9,
      "mentalStrength": 8,
      "teamwork": 7
    }
  }'
```

**Response (201 Created):**
```json
{
  "evaluationId": "eval-001",
  "scores": {
    "technicalSkill": 8,
    "physicalAttributes": 7,
    "tacticalAwareness": 9,
    "mentalStrength": 8,
    "teamwork": 7,
    "overallAverage": 7.8
  }
}
```

#### Mark evaluation complete

```bash
curl -X POST '{{BASE_URL}}/scouting/evaluations/eval-001/complete' \
  -H 'Authorization: Bearer {{ACCESS_TOKEN}}'
```

**Response (200 OK):**
```json
{
  "evaluationId": "eval-001",
  "status": "COMPLETED",
  "completedAt": "2024-06-15T16:00:00Z"
}
```

### Workflow H — Add a scouting note

```bash
curl -X POST '{{BASE_URL}}/scouting/notes' \
  -H 'Authorization: Bearer {{ACCESS_TOKEN}}' \
  -H 'Content-Type: application/json' \
  -d '{
    "athleteId": "b2c3d4e5-f6a7-8901-bcde-f12345678901",
    "body": "Exceptional vision and composure under pressure. Recommend priority follow-up before the August transfer window.",
    "visibility": "PRIVATE"
  }'
```

**Response (201 Created):**
```json
{
  "noteId": "note-001",
  "athleteId": "b2c3d4e5-f6a7-8901-bcde-f12345678901",
  "visibility": "PRIVATE",
  "createdAt": "2024-06-15T16:30:00Z"
}
```

---

## 5. Available Endpoints

### Scout Profile

| Method | Path | Description |
|---|---|---|
| POST | `/scouts/profile` | Create scout profile |
| GET | `/scouts/me` | Get own profile |
| POST | `/scouts/me/specializations` | Add specialization |
| GET | `/scouts/me/specializations` | List specializations |

#### GET /scouts/me

```bash
curl -X GET '{{BASE_URL}}/scouts/me' \
  -H 'Authorization: Bearer {{ACCESS_TOKEN}}'
```

**Response (200 OK):**
```json
{
  "scoutId": "sc-0001",
  "displayName": "Kwame Mensah",
  "country": "Ghana",
  "status": "VERIFIED",
  "verifiedAt": "2024-06-16T09:00:00Z"
}
```

### Discovery

| Method | Path | Query Params | Description |
|---|---|---|---|
| GET | `/discovery/athletes` | sport, country, position, ageCategory, verificationStatus, page, size | Search athletes |
| GET | `/discovery/athletes/{id}` | — | Get discovery card |
| POST | `/discovery/saved-searches` | — | Save filter set |
| GET | `/discovery/saved-searches` | — | List saved searches |

### Scouting (Favourites, Watchlists, Evaluations)

| Method | Path | Description |
|---|---|---|
| POST | `/scouting/favourites` | Add to favourites |
| GET | `/scouting/favourites` | List favourites |
| DELETE | `/scouting/favourites/{id}` | Remove from favourites |
| POST | `/scouting/watchlists` | Create watchlist |
| GET | `/scouting/watchlists` | List watchlists |
| POST | `/scouting/watchlists/{id}/athletes` | Add athlete to watchlist |
| POST | `/scouting/follows` | Follow athlete |
| POST | `/scouting/evaluations` | Create evaluation |
| GET | `/scouting/evaluations` | List evaluations |
| POST | `/scouting/evaluations/{id}/scores` | Add scores |
| POST | `/scouting/evaluations/{id}/complete` | Complete evaluation |
| POST | `/scouting/notes` | Create private note |

### Subscriptions and Notifications

| Method | Path | Description |
|---|---|---|
| GET | `/subscriptions/me` | Current subscription |
| POST | `/subscriptions` | Purchase plan |
| GET | `/notifications` | List notifications |
| PATCH | `/notifications/{id}/read` | Mark read |

---

## 6. Entitlements and Subscription

#### GET /subscriptions/me

```bash
curl -X GET '{{BASE_URL}}/subscriptions/me' \
  -H 'Authorization: Bearer {{ACCESS_TOKEN}}'
```

**FREE plan response:**
```json
{
  "planCode": "FREE",
  "status": "ACTIVE",
  "entitlements": {
    "discoveryAllowed": false,
    "evaluationsAllowed": false,
    "watchlistsAllowed": false,
    "favouritesAllowed": true,
    "maxFavourites": 5
  }
}
```

**SCOUT plan response:**
```json
{
  "planCode": "SCOUT",
  "status": "ACTIVE",
  "validUntil": "2025-06-15T23:59:59Z",
  "entitlements": {
    "discoveryAllowed": true,
    "evaluationsAllowed": true,
    "maxEvaluationsPerMonth": 20,
    "watchlistsAllowed": true,
    "maxWatchlists": 5,
    "savedSearchesAllowed": true
  }
}
```

**SCOUT_PRO plan response:**
```json
{
  "planCode": "SCOUT_PRO",
  "status": "ACTIVE",
  "validUntil": "2025-06-15T23:59:59Z",
  "entitlements": {
    "discoveryAllowed": true,
    "evaluationsAllowed": true,
    "maxEvaluationsPerMonth": -1,
    "watchlistsAllowed": true,
    "maxWatchlists": -1,
    "savedSearchesAllowed": true,
    "organizationScouting": true,
    "apiAccess": true
  }
}
```

**403 FEATURE_NOT_AVAILABLE example:**
```json
{
  "error": "FEATURE_NOT_AVAILABLE",
  "message": "Athlete evaluations require a SCOUT or SCOUT_PRO plan.",
  "currentPlan": "FREE",
  "upgradeUrl": "https://talenthub.io/pricing"
}
```

---

## 7. Privacy and Consent Controls

Scouts only see athletes who have granted platform discovery consent. If an athlete withdraws consent after you have added them to a watchlist, their entry will show `consentWithdrawn: true` and their data becomes restricted.

```bash
# Check a discovery card — will 403 if consent withdrawn
curl -X GET '{{BASE_URL}}/discovery/athletes/b2c3d4e5-f6a7-8901-bcde-f12345678901' \
  -H 'Authorization: Bearer {{ACCESS_TOKEN}}'
```

**Response if consent withdrawn (403 Forbidden):**
```json
{
  "error": "MEDIA_RESTRICTED",
  "message": "This athlete has withdrawn platform consent. Their profile is no longer accessible."
}
```

---

## 8. Error Reference

| Error Code | HTTP Status | Meaning | Example JSON |
|---|---|---|---|
| `UNAUTHORIZED` | 401 | Missing or expired token | `{ "error": "UNAUTHORIZED", "message": "Full authentication is required." }` |
| `FORBIDDEN` | 403 | Scout not verified or plan insufficient | `{ "error": "FORBIDDEN", "message": "Scout account is not verified." }` |
| `FEATURE_NOT_AVAILABLE` | 403 | Paid feature on free plan | `{ "error": "FEATURE_NOT_AVAILABLE", "message": "Requires SCOUT plan." }` |
| `NOT_FOUND` | 404 | Resource not found | `{ "error": "NOT_FOUND", "message": "Watchlist not found." }` |
| `DUPLICATE_RESOURCE` | 409 | Already in favourites/watchlist | `{ "error": "DUPLICATE_RESOURCE", "message": "Athlete is already in this watchlist." }` |
| `MEDIA_RESTRICTED` | 403 | Athlete consent withdrawn | `{ "error": "MEDIA_RESTRICTED", "message": "Athlete has withdrawn consent." }` |
| `VALIDATION_ERROR` | 400 | Request body invalid | `{ "error": "VALIDATION_ERROR", "errors": [{ "field": "athleteId", "message": "must not be blank" }] }` |
