# TalentHub API Contract (Role-Based)

Derived from the controllers and security configuration in `src/main/java` as of this commit. It describes what the code enforces today, not what the user guides describe. Where the two differ, this document is the reference.

Base path: `/api/v1`. Content type: `application/json`.

## 1. Conventions

### 1.1 Authentication

- Send `Authorization: Bearer <access_token>` on every request except the public endpoints in section 3.
- Tokens are JWTs carrying `userId`, `email` and a list of roles. Each role is mapped to the Spring authority `ROLE_<ROLE>`.
- A missing, malformed or expired token leaves the request unauthenticated. Protected endpoints then return `401` with the `ApiError` envelope.
- Authenticated but not permitted returns `403` with the `ApiError` envelope, whether it is rejected by the security filter or by a handler.
- Only these `/auth` routes are public: `register`, `login`, `refresh`, `verify-email`, `password-reset` and `password-reset/confirm`. `/auth/logout` and `/auth/mfa/verify` require a token.

### 1.2 Roles

`ATHLETE`, `COACH`, `PARENT`, `GUARDIAN`, `AGENT`, `SCOUT`, `ORG_ADMIN`, `ADMIN`, `SUPER_ADMIN`.

Access levels used below:

| Code | Meaning |
|---|---|
| Public | No token required |
| Auth | Any authenticated user, no role check in the controller |
| Roles | Enforced by `@PreAuthorize`. Only the listed roles are allowed |

"Auth" endpoints on athlete data apply the athlete access policy. A caller may act on an athlete's data only if they are:

- `ADMIN` or `SUPER_ADMIN`;
- the athlete themself (athlete ids in paths are the athlete's user id);
- a parent or legal guardian with an `ACTIVE`, consented, unexpired guardian link;
- the holder of an active, unexpired access grant of at least the permission the action needs (`VIEW_PROFILE` < `CONTACT` = `VIEW_STATS` < `VIEW_DOCUMENTS` < `MANAGE`). Approved agent relationships and organization affiliations create these grants.

Anyone else receives `403` with the `ApiError` envelope. Write actions need `MANAGE`.

### 1.3 Response shapes

- Success: the resource as plain JSON, no wrapper.
- List endpoints returning `PageResponse` use `page` (0-based, default 0), `size` (default 20) and `sort` (`field,asc|desc`), and return `{ content, page, size, totalElements, totalPages, first, last }`.
- Other list endpoints return a bare JSON array and are not paginated.
- `204 No Content` for deletes and logout.

### 1.4 Error envelope

```json
{
  "timestamp": "2026-11-13T10:15:30.123Z",
  "status": 404,
  "error": "Not Found",
  "message": "AthleteProfile not found for id: xyz",
  "path": "/api/v1/athletes/xyz",
  "fieldErrors": [ { "field": "email", "rejectedValue": "x", "message": "must be a valid email" } ]
}
```

`fieldErrors` appears only on `400` validation failures.

| Status | Raised for |
|---|---|
| 400 | Bean validation failure, malformed JSON, missing or mistyped request parameter, `IllegalArgumentException` (including unknown enum values) |
| 401 | Missing or invalid token, bad credentials, locked account |
| 402 | Entitlement or subscription limit exceeded |
| 403 | Access denied (message is `Access denied`), safeguarding violation, ownership failure raised by a service |
| 404 | Entity not found |
| 409 | Duplicate resource, invalid state transition |
| 500 | Anything unhandled, generic message |

## 2. Role access matrix

Rows are endpoint groups. A tick means the controller allows that role. "Any" means every authenticated role.

| Endpoint group | ATHLETE | COACH | PARENT | GUARDIAN | AGENT | SCOUT | ORG_ADMIN | ADMIN | SUPER_ADMIN |
|---|---|---|---|---|---|---|---|---|---|
| Auth (register, login, refresh, reset, verify) | Public | Public | Public | Public | Public | Public | Public | Public | Public |
| Profiles, athlete profiles, sports, activity config (read) | Any | Any | Any | Any | Any | Any | Any | Any | Any |
| Activities (create, edit, submit, publish, read) | Any | Any | Any | Any | Any | Any | Any | Any | Any |
| Media, development, measurements | Any | Any | Any | Any | Any | Any | Any | Any | Any |
| Consent, safeguarding reports, notifications, subscriptions | Any | Any | Any | Any | Any | Any | Any | Any | Any |
| Verify an activity | | ✔ | | | | | | ✔ | ✔ |
| Discovery search and saved searches | | | | | | ✔ | | | |
| Scouting (favourites, watchlists, evaluations) | | | | | | ✔ | | | |
| Scout profile create, read, submit verification | Any | Any | Any | Any | Any | Any | Any | Any | Any |
| Org scouting: activate workspace, add seat | | | | | | | ✔ | ✔ | ✔ |
| Org scouting: list watchlists, evaluations | | | | | | ✔ | ✔ | ✔ | ✔ |
| Scout verification approve or reject | | | | | | | | ✔ | ✔ |
| Safeguarding report review | | | | | | | | ✔ | ✔ |
| Activity types, templates, fields | | | | | | | | ✔ | ✔ |
| Platform dashboard, users list, suspend/reactivate, review queues, moderation, audit log (own entries) | | | | | | | | ✔ | ✔ |
| Suspend/reactivate an administrator account, see all audit entries | | | | | | | | | ✔ |
| Create ADMIN accounts, change roles, manage plans, sports and metrics, publish schemas | | | | | | | | | ✔ |
| Organizations: create, view | Any | Any | Any | Any | Any | Any | Any | Any | Any |
| Organizations: manage, invite, affiliate athletes | Org OWNER/ADMIN members | | | | | | | ✔ | ✔ |
| Guardian relationships | ✔ approve | | ✔ PARENT | ✔ LEGAL_GUARDIAN | ✔ AGENT | | | ✔ | ✔ |
| Payment webhook | Public (signature checked) | | | | | | | | |

## 3. Endpoints by access level

### 3.1 Public

| Method | Path | Body | Success | Notes |
|---|---|---|---|---|
| POST | `/auth/register` | `{ email, password (8-128), role }` | 201 `{ userId, message }` | `role` must be `ATHLETE`, `COACH`, `PARENT`, `GUARDIAN`, `AGENT` or `SCOUT`. `ADMIN`, `SUPER_ADMIN` and `ORG_ADMIN` return 400 |
| POST | `/auth/login` | `{ email, password }` | 200 `AuthResponse` | |
| POST | `/auth/refresh` | `{ refresh_token }` | 200 `AuthResponse` | |
| GET | `/auth/verify-email?token=` | | 200 `{ message }` | |
| POST | `/auth/password-reset` | `{ email }` | 200 `{ message }` | Always 200, prevents user enumeration |
| POST | `/auth/password-reset/confirm` | `{ token, newPassword (8-128) }` | 200 `{ message }` | |
| POST | `/payments/webhooks/{provider}` | raw provider payload | 200 `OK` or `DUPLICATE`. A verified successful payment marks the transaction `COMPLETED` and activates its subscription | For `paystack`, header `X-Paystack-Signature` is verified, `401` on mismatch. Machine-to-machine only |
| GET | `/actuator/health`, `/actuator/info`, `/v3/api-docs/**`, `/swagger-ui/**` | | 200 | Infrastructure |

`AuthResponse` uses snake_case on the wire:

```json
{
  "access_token": "...",
  "refresh_token": "...",
  "user_id": "uuid",
  "email": "a@b.com",
  "role": "ATHLETE",
  "expires_in": 3600
}
```


### 3.2 Any authenticated user

#### Account and profile

| Method | Path | Body | Success |
|---|---|---|---|
| POST | `/auth/logout` | | 204, 401 without a token |
| GET | `/users/me` | | 200 `{ userId, email, role, roles, status, emailVerified }` |
| POST | `/users/me/roles` | `{ role }` | 200 account, as above. 400 for `ADMIN`, `SUPER_ADMIN`, `ORG_ADMIN`. 409 if already held. The new role appears in tokens issued after the next login or refresh |
| POST | `/auth/mfa/verify` | `{ secret, code }` | 200 `{ verified, message }`. A wrong or expired code returns 401 with the `ApiError` envelope. Not enforced: no endpoint requires it (see section 6) |
| GET | `/profiles/me` | | 200 `UserProfileDto` |
| PATCH | `/profiles/me` | `UpdateProfileRequest` | 200 `UserProfileDto` |
| GET | `/profiles/{userId}` | | 200 `UserProfileDto`. The user themself and admins get every field. Everyone else gets the public fields only: `firstName`, `lastName`, `dateOfBirth`, `gender` and `phoneNumber` are `null` |

`UpdateProfileRequest`: `firstName`, `lastName` (max 100), `displayName` (max 150), `dateOfBirth` (date), `gender` (max 20), `phoneNumber` (max 30), `countryCode` (max 2), `city` (max 100), `bio` (max 500). All optional.

#### Athlete profile

| Method | Path | Body | Success |
|---|---|---|---|
| POST | `/athletes/me` | `CreateAthleteProfileRequest` | 201 `AthleteProfileDto` |
| GET | `/athletes/me` | | 200 |
| PATCH | `/athletes/me` | `CreateAthleteProfileRequest` | 200 |
| GET | `/athletes/{id}` | | 200 `AthleteProfileDto`. `{id}` is the athlete's user id. Needs `VIEW_PROFILE` |
| PATCH | `/athletes/{id}` | `CreateAthleteProfileRequest` | 200. Needs `MANAGE` (guardian, `MANAGE` agent or organization) |
| GET | `/athletes/user/{userId}` | | 200. Same as `GET /athletes/{id}`. Needs `VIEW_PROFILE` on that athlete (403 otherwise) |
| GET | `/athletes/{id}/sports` | | 200 `AthleteSport[]`: the primary sport first (`primary: true`, `id: null`), then secondary sports. Needs `VIEW_PROFILE` |
| POST | `/athletes/{id}/sports` | `{ sportCode, position? }` | 201 `AthleteSport`. 404 for an unknown sport code. 409 if it is already the primary or a secondary sport. Needs `MANAGE` |
| DELETE | `/athletes/{id}/sports/{athleteSportId}` | | 204. Needs `MANAGE` |
| GET | `/athletes/number/{athleteNumber}` | | 200, 404 if unknown. Callers without `VIEW_PROFILE` get the public projection: only `userId`, `athleteNumber`, `primarySportCode` and `status`, other fields `null` |

`AthleteSport`: `{ id, athleteUserId, sportId, sportCode, position, primary }`.

`CreateAthleteProfileRequest`: `primarySportCode`, `position` (max 100), `heightCm`, `weightKg`, `dominantFoot` (max 10), `nationality` (max 2).

#### Reference data

| Method | Path | Success |
|---|---|---|
| GET | `/sports` | 200 `SportDto[]` |
| GET | `/sports/{id}` | 200 |
| GET | `/sports/code/{code}` | 200 |
| GET | `/sports/{id}/disciplines` | 200 `{ id, sportId, code, name, active }[]` |
| GET | `/sports/{id}/metrics` | 200 `{ id, sportId, code, name, unit, higherIsBetter, active }[]` |
| GET | `/sports/{id}/activity-types` | 200 `ActivityTemplateDto[]`, active templates for that sport. `{id}` is the sport id or code |
| GET | `/activity-types/{id}/schema` | 200 `PublishedSchemaDto` `{ schemaVersionId, templateId, versionNumber, fields }`. `{id}` is the template id. 404 if it has no published schema |
| GET | `/subscription-plans` | 200 active plans |

#### Activities

| Method | Path | Body | Success |
|---|---|---|---|
| POST | `/athletes/{id}/activities` | `CreateActivityRequest` | 201 `AthleteActivity` (draft). Needs `MANAGE` |
| GET | `/athletes/{id}/activities` | `page`, `size`, `sort` | 200 `PageResponse<AthleteActivity>`. Related callers (`VIEW_STATS`) see all. A `SCOUT` sees only `PUBLISHED` activities with `PUBLIC` visibility. Anyone else gets 403 |
| GET | `/activities/{id}` | | 200. Same rule as the list. A scout asking for a non-public activity gets 404 |
| PATCH | `/activities/{id}` | `CreateActivityRequest` | 200. Needs `MANAGE` |
| POST | `/activities/{id}/submit` | | 200 (409 on invalid transition). Needs `MANAGE` |
| POST | `/activities/{id}/publish` | | 200 (409 on invalid transition). Needs `MANAGE` |
| POST | `/activities/{id}/media` | `{ ownerId, originalFilename, mimeType }` | 201 `InitiateUploadResponse`. Needs `MANAGE` on `ownerId` |

`CreateActivityRequest`: `templateId`, `activityTypeId`, `sportId`, `title`, `description`, `activityDate`, `location`, `organizationId`, `visibilityLevel`, `fieldValues` (map of string to string). Nothing is bean-validated on the PATCH.

#### Development and performance

| Method | Path | Body or query | Success |
|---|---|---|---|
| GET | `/athletes/{id}/development` | | 200 `DevelopmentTimelineResponse`. Needs `VIEW_STATS` |
| GET | `/athletes/{id}/performance` | `sport`, `metric` (optional) | 200 `PerformanceMeasurement[]`. With `metric` returns chart data, otherwise full history. `sport` is currently ignored. Needs `VIEW_STATS` |
| POST | `/athletes/{id}/measurements` | `{ metricCode, measuredValue, unit?, observedAt?, source? }` | 201. `source` defaults to `SELF_REPORTED`. An unknown value returns 400. Needs `MANAGE` |
| GET | `/athletes/{id}/achievements` | | 200 `Milestone[]`, newest first. Needs `VIEW_PROFILE` |
| POST | `/athletes/{id}/achievements` | `{ title, description?, achievedAt?, milestoneType?, sportId?, evidenceActivityId? }` | 201 `Milestone`. `milestoneType` defaults to `ACHIEVEMENT` (also `FIRST_APPEARANCE`, `PERSONAL_BEST`, `LEVEL_UP`, `OTHER`); `achievedAt` defaults to today. Appears in the development timeline. Needs `MANAGE` |
| GET | `/activities/{id}/measurements` | | 200 `PerformanceMeasurement[]` recorded from that activity (`[]` if none). Needs `VIEW_STATS` on the athlete |

#### Media

| Method | Path | Body | Success |
|---|---|---|---|
| POST | `/media/uploads` | `{ ownerId, ownerType, assetType, originalFilename, mimeType }` | 201 `InitiateUploadResponse`. `ATHLETE` owner needs `MANAGE`; `USER` owner must be the caller; `ORGANIZATION` owner needs `ORG_ADMIN` |
| POST | `/media/uploads/{id}/complete` | | 200 `MediaAsset`. Uploader or owner manager only |
| GET | `/media/{id}` | | 200. See media visibility below |
| GET | `/media/{id}/playback` | | 200 `{ url }`. See media visibility below |
| DELETE | `/media/{id}` | | 204 (soft delete). Uploader, owner manager or admin only, else 403 |

Media visibility: the uploader, anyone who can manage the owner, and admins can always view. A `RESTRICTED` asset is visible to nobody else (403, message `This media asset is not accessible.`). `ORG_LOGO` assets are visible to everyone. Athlete-owned assets are visible to holders of `VIEW_STATS`, and to a `SCOUT` for `ACTIVITY_VIDEO`, `VIDEO_THUMBNAIL` and `PROFILE_IMAGE` only. Anyone else gets 403.

`ownerType`: `ATHLETE`, `ORGANIZATION`, `USER`. `assetType`: `PROFILE_IMAGE`, `ACTIVITY_VIDEO`, `VIDEO_THUMBNAIL`, `ORG_LOGO`, `VERIFICATION_DOCUMENT`. Unknown values return 400.

#### Consent and safeguarding

| Method | Path | Body | Success |
|---|---|---|---|
| POST | `/athletes/{id}/consent` | `{ purpose, consentVersion?, authorizationSource? }` | 201 `ConsentRecord`. Needs `MANAGE`. `PROFILE_DISCOVERY` for a minor (under 18) without an active guardian link returns 403 |
| GET | `/athletes/{id}/consent` | | 200 `ConsentRecord[]`, newest first, including revoked records (`revokedAt` set). Needs `VIEW_PROFILE` |
| DELETE | `/athletes/{id}/consent/{purpose}` | | 204. Needs `MANAGE`. 404 if that purpose has no active consent |
| POST | `/safeguarding/data-subject-requests` | `{ requestType }` | 201 `DataSubjectRequest`. `requestType` is `ACCESS`, `DELETION` or `CORRECTION`. A second pending request of the same type returns 409 |
| GET | `/safeguarding/data-subject-requests` | | 200 the caller's `DataSubjectRequest[]` |
| POST | `/safeguarding/reports` | `{ reportedEntityId, entityType?, reportType, description? }` | 201 `SafeguardingReport` |
| GET | `/safeguarding/reports` | | 200 the caller's own reports |

`purpose`: `PROFILE_DISCOVERY`, `PERFORMANCE_SHARING`, `MEDIA_SHARING`, `RESEARCH`.

A safeguarding report with `entityType` `MEDIA` (or `MEDIA_ASSET`) and `reportedEntityId` set to a media asset id flags that asset (`moderationStatus` `FLAGGED`) for admin review. Flagged assets stay visible until an admin restricts them.

`MediaAsset` now carries `moderationStatus` (`CLEAN`, `FLAGGED`, `RESTRICTED`), `moderationNote`, `moderatedBy` and `moderatedAt`.

#### Organizations

| Method | Path | Body or query | Who | Success |
|---|---|---|---|---|
| POST | `/organizations` | `{ name, orgType, countryCode?, city?, description? }` | Any | 201 `Organization`, status `PENDING_VERIFICATION`. Caller becomes `OWNER` member and gains the `ORG_ADMIN` role |
| GET | `/organizations/mine` | | Any | 200 `Organization[]` the caller is a member of |
| GET | `/organizations/{id}` | | Any | 200 `Organization` |
| PATCH | `/organizations/{id}` | `{ name?, countryCode?, city?, description?, logoUrl? }` | OWNER/ADMIN member | 200 `Organization` |
| GET | `/organizations/{id}/members` | | Members | 200 `OrganizationMember[]` (active only) |
| DELETE | `/organizations/{id}/members/{userId}` | | OWNER/ADMIN member, or self | 204. Removing the `OWNER` returns 400 |
| POST | `/organizations/{id}/invitations` | `{ email, memberRole }` | OWNER/ADMIN member | 201 `Invitation` including `invitationToken`. Org must be `ACTIVE` (403 otherwise). `memberRole` `OWNER` returns 400. Duplicate pending invitation returns 409 |
| GET | `/organizations/{id}/invitations` | | OWNER/ADMIN member | 200 pending `Invitation[]`, `invitationToken` null |
| POST | `/organizations/{id}/invitations/accept` | `{ invitationToken }` | Invitee | 200 `OrganizationMember`. Caller email must match (403). Expired or used returns 409. Already a member returns 409 |
| POST | `/organizations/{id}/athlete-relationships` | `{ athleteId, permission? }` | OWNER/ADMIN/ATHLETE_MANAGER member | 201 `Affiliation`, `PENDING`. Org must be `ACTIVE`. `permission` defaults to `VIEW_STATS`. Open duplicate returns 409 |
| GET | `/organizations/{id}/athletes` | `status` (default `ACTIVE`) | Members | 200 `Affiliation[]` |
| GET | `/organization-affiliations` | | Athlete | 200 the caller's `Affiliation[]` |
| POST | `/organization-affiliations/{id}/approve` | | The athlete | 200 `Affiliation`, `ACTIVE`. 409 if not `PENDING` |
| POST | `/organization-affiliations/{id}/reject` | | The athlete | 200 `Affiliation`, `REJECTED` |
| POST | `/organization-affiliations/{id}/end` | | The athlete, or OWNER/ADMIN/ATHLETE_MANAGER member | 200 `Affiliation`, `ENDED` |

Member roles (`memberRole`): `OWNER`, `ADMIN`, `COACH`, `SCOUT`, `ATHLETE_MANAGER`, `STAFF`. Platform `ADMIN` and `SUPER_ADMIN` pass every membership check. Non-members get 403.

An `ACTIVE` affiliation grants the organization the requested `permission` on the athlete. Members with `OWNER`, `ADMIN`, `COACH` or `ATHLETE_MANAGER` inherit it through the athlete access policy. Ending the affiliation removes it.

`Organization`: `{ id, code, name, orgType, countryCode, city, adminUserId, logoUrl, status, description, verifiedAt, rejectionReason }`. `status` is `PENDING_VERIFICATION`, `ACTIVE`, `SUSPENDED` or `DEACTIVATED`.

`OrganizationMember`: `{ id, organizationId, userId, memberRole, status, joinedAt }`.

`Invitation`: `{ id, organizationId, email, memberRole, status, invitationToken, expiresAt }`. `status` is `PENDING`, `ACCEPTED` or `EXPIRED`. Invitations expire after 7 days.

`Affiliation`: `{ id, organizationId, athleteUserId, permission, status, requestedBy, createdAt, decidedAt, endedAt }`. `status` is `PENDING`, `ACTIVE`, `REJECTED` or `ENDED`.

#### Guardian, parent and agent relationships

| Method | Path | Body or query | Success |
|---|---|---|---|
| POST | `/guardian-relationships` | `{ athleteId, relationshipType, relationship?, verificationDocumentRef?, permission? }` | 201 `GuardianLink` |
| GET | `/guardian-relationships` | | 200 `GuardianLink[]`, every relationship the caller is in (as requester or athlete) |
| POST | `/guardian-relationships/{id}/approve` | | 200 `GuardianLink` |
| POST | `/guardian-relationships/{id}/reject` | `{ reason? }` (optional) | 200 `GuardianLink` |
| POST | `/guardian-relationships/{id}/revoke` | | 200 `GuardianLink` |
| GET | `/guardians/me/athletes` | | 200 `GuardianLink[]`, active PARENT and LEGAL_GUARDIAN links |

Rules:

- `relationshipType` is `PARENT` (needs the `PARENT` role), `LEGAL_GUARDIAN` (needs the `GUARDIAN` role and `verificationDocumentRef`, else 400) or `AGENT` (needs the `AGENT` role; optional `permission` of `VIEW_PROFILE`, `VIEW_STATS`, `VIEW_DOCUMENTS`, `CONTACT` or `MANAGE`, default `VIEW_STATS`). A missing role returns 403. `athleteId` is the athlete's user id and must be an athlete (404 otherwise).
- `PARENT` and `AGENT` start `PENDING` and are approved or rejected only by the athlete. `LEGAL_GUARDIAN` starts `PENDING_ADMIN_VERIFICATION` and is approved or rejected only by `ADMIN` or `SUPER_ADMIN`. Anyone else gets 403. Deciding a link that is not pending returns 409.
- An approved `LEGAL_GUARDIAN` link expires after 365 days. When it has expired, `status` reads `EXPIRED` and it no longer gives access. Renew by submitting a new request.
- Either party, or an admin, can revoke a pending or active link. Revoking an `AGENT` link removes its access grant.
- Approving an `AGENT` link creates an access grant at the requested `permission`. `PARENT` and `LEGAL_GUARDIAN` links give `MANAGE`-level access directly.
- A second open request of the same type between the same two users returns 409.
- A minor (under 18) cannot become discoverable without an active guardian link (see the consent endpoints).

`GuardianLink`:

```json
{
  "id": "gr-0001",
  "guardianUserId": "uuid",
  "athleteUserId": "uuid",
  "relationshipType": "PARENT",
  "relationship": "PARENT",
  "permission": null,
  "status": "PENDING",
  "consentGiven": false,
  "verificationDocumentRef": null,
  "createdAt": "2024-06-15T10:05:00Z",
  "approvedAt": null,
  "expiresAt": null,
  "revokedAt": null
}
```

`status` is one of `PENDING`, `PENDING_ADMIN_VERIFICATION`, `ACTIVE`, `EXPIRED`, `REVOKED`, `REJECTED`.

#### Notifications

| Method | Path | Body | Success |
|---|---|---|---|
| GET | `/notifications` | `page`, `size`, `sort` | 200 `PageResponse<Notification>` |
| PATCH | `/notifications/{id}/read` | | 200. Someone else's notification returns 404 |
| GET | `/notifications/preferences` | | 200 `NotificationPreference[]` |
| PUT | `/notifications/preferences` | `{ notificationType, emailEnabled, inAppEnabled }` | 200 |

#### Subscriptions

| Method | Path | Body or query | Success |
|---|---|---|---|
| POST | `/subscriptions` | `{ planId }` | 201 `UserSubscription`. A free plan (`pricePerMonth` 0) is `ACTIVE` at once; a paid plan is `PENDING` until checkout is paid. 404 for an unknown plan, 400 for an inactive one |
| POST | `/subscriptions/{id}/checkout` | | 201 `{ transactionId, reference, authorizationUrl, accessCode }`. Send the user to `authorizationUrl`. The provider webhook confirms payment and the subscription becomes `ACTIVE`. Owner only (403). 409 unless `PENDING`; 400 for a free plan |
| GET | `/subscriptions/me` | | 200 the caller's `ACTIVE` `UserSubscription` (with the nested `plan`), or 404 with an empty body when there is none (a `PENDING` one does not count) |
| POST | `/subscriptions/{id}/cancel` | `immediate` (default false), `reason?` | 200. Owner only (403): the subscriber, the subscriber's scout profile owner, an admin of the subscribing organization, or a platform admin |
| GET | `/subscriptions/{id}/addons` | | 200 `SubscriptionAddon[]` `{ id, subscriptionId, addonCode, quantity, active }`. Same owner rule as cancel |

#### Scout profile

| Method | Path | Body or query | Success |
|---|---|---|---|
| POST | `/scouts` | `{ professionalTitle?, biography?, yearsExperience? }` | 201 `ScoutProfileDto` |
| GET | `/scouts/{id}` | | 200 |
| GET | `/scouts/me` | | 200 the caller's `ScoutProfileDto`, 404 if none |
| POST | `/scouts/verification-requests` | | 200. Submits the caller's own scout profile. 404 if the caller has none |
| GET | `/scouts/me/specializations` | | 200 `Specialization[]` |
| POST | `/scouts/me/specializations` | `{ sportCode, level?, preferredRegions? }` | 201 `Specialization`. `level` is `YOUTH`, `AMATEUR`, `SEMI_PRO`, `PROFESSIONAL` or `ELITE`. One per sport (409) |
| DELETE | `/scouts/me/specializations/{id}` | | 204 |

`Specialization`: `{ id, scoutProfileId, sportId, sportCode, level, preferredRegions }`.

### 3.3 SCOUT

All routes below require `ROLE_SCOUT`. The scout profile is always the caller's own, taken from the token; none of these routes accept a scout profile id. Favourites, saved searches and evaluations return 404 if the caller has not created a scout profile (`POST /scouts`).

| Method | Path | Body or query | Success |
|---|---|---|---|
| GET | `/discovery/athletes` | `country?`, `ageCategory?`, `page`, `size`, `sort` | 200 `PageResponse<AthleteSearchProjection>`. Excludes athletes whose search profile is `PRIVATE`. Withdrawing `PROFILE_DISCOVERY` consent sets it to `PRIVATE`; granting it again sets it to `PUBLIC` |
| GET | `/discovery/athletes/{id}` | | 200. 404 when the athlete is hidden (`PRIVATE`) |
| POST | `/discovery/saved-searches` | `{ name, queryJson? }` | 201 `SavedSearch` |
| GET | `/discovery/saved-searches` | | 200 the caller's saved searches |
| POST | `/scouting/favourites` | `athleteId`, `notes?` (query) | 201. Already a favourite returns 409. Over `MAX_FAVOURITES` returns 402 |
| DELETE | `/scouting/favourites` | `athleteId` (query) | 204 |
| GET | `/scouting/favourites` | | 200 |
| POST | `/scouting/watchlists` | `{ name, description?, shared }` | 201 `Watchlist` |
| GET | `/scouting/watchlists` | | 200 the caller's own watchlists |
| GET | `/scouting/watchlists/{id}` | | 200. Owner, or anyone for a `shared` watchlist. 403 otherwise |
| POST | `/scouting/watchlists/{id}/athletes` | `athleteId`, `notes?` (query) | 201. Owner only, 403 otherwise. Already on the list returns 409. Also follows the athlete |
| DELETE | `/scouting/watchlists/{id}/athletes/{athleteId}` | | 204. Owner only, 403 otherwise |
| POST | `/scouting/follows` | `{ athleteId }` | 201 `AthleteFollow`. Following again returns the existing follow |
| GET | `/scouting/follows` | | 200 `AthleteFollow[]` |
| DELETE | `/scouting/follows/{athleteId}` | | 204. 404 if not followed |
| POST | `/scouting/notes` | `{ athleteId, noteText, noteDate?, isPrivate? }` | 201 `ScoutNote` (`isPrivate` is serialized as `private`, default true) |
| GET | `/scouting/notes` | `athleteId?` | 200 the caller's own notes |
| DELETE | `/scouting/notes/{id}` | | 204. Another scout's note returns 404 |
| GET | `/scouting/evaluation-criteria` | `category?` (e.g. `Football`) | 200 `EvaluationCriterion[]` `{ id, sportId, criterionCode, criterionName, description, category, displayOrder, active }` |
| POST | `/scouting/evaluations` | `{ athleteId, sportId?, title, evaluationDate? }` | 201 `ScoutEvaluation`. Needs an active subscription plan: otherwise 402 `SCOUT_EVALUATIONS feature not available` |
| GET | `/scouting/evaluations/{id}` | | 200 `EvaluationDetail`. Author only (403) |
| POST | `/scouting/evaluations/{id}/scores` | `{ scores: [{ criterionCode, score, notes? }] }` | 200 `EvaluationDetail`. Scores are 0 to 10; re-scoring a criterion replaces it. Author only (403); `DRAFT` only (409); unknown criterion code 404 |
| POST | `/scouting/evaluations/{id}/complete` | | 200. Author only, 403 otherwise. 409 if not `DRAFT` |

`EvaluationDetail`: `{ evaluation, scores: [{ id, criterionId, criterionCode, criterionName, score, maxScore, notes }], averageScore }`. `averageScore` is `null` with no scores.
| GET | `/scouting/evaluations` | | 200 the caller's evaluations |

### 3.4 COACH, ADMIN, SUPER_ADMIN

| Method | Path | Success |
|---|---|---|
| POST | `/activities/{id}/verify` | 200, empty body. A `COACH` needs `VIEW_STATS` on the athlete (organization affiliation or grant) and cannot verify their own activity; otherwise 403. Admins can verify any activity |

### 3.5 ORG_ADMIN, ADMIN, SUPER_ADMIN

`{orgId}` is the organization id. An `ORG_ADMIN` must also be an `OWNER` or `ADMIN` member of that organization (403 otherwise). Platform admins pass.

| Method | Path | Query | Success |
|---|---|---|---|
| POST | `/organizations/{orgId}/scouting/workspace/activate` | | 200, empty body |
| POST | `/organizations/{orgId}/scouting/seats` | `subscriptionId`, `userId` | 200 `SubscriptionSeat` `{ id, subscriptionId, seatType, assignedToUserId, assignedAt, revokedAt }`. 402 over `MAX_SCOUT_SEATS` |

### 3.6 ORG_ADMIN, SCOUT, ADMIN, SUPER_ADMIN

The caller must also be a member of that organization (403 otherwise). Platform admins pass.

| Method | Path | Success |
|---|---|---|
| GET | `/organizations/{orgId}/scouting/watchlists` | 200 `Watchlist[]` |
| GET | `/organizations/{orgId}/scouting/evaluations` | 200 `ScoutEvaluation[]` |

### 3.7 ADMIN, SUPER_ADMIN

| Method | Path | Body or query | Success |
|---|---|---|---|
| GET | `/admin/reports/platform` | | 200 `{ totalAthletes, verifiedAthletes, registeredScouts, verifiedScouts, registeredOrgs, activeSubscriptions, publishedActivities, uploadedVideos, pendingVerifications, pendingModerationReports }` |
| GET | `/admin/users` | `role?`, `status?`, `page`, `size`, `sort` | 200 `PageResponse<AdminUser>`. `role` matches primary or additional roles |
| GET | `/admin/users/{id}` | | 200 `AdminUser` |
| POST | `/admin/users/{id}/suspend` | | 200 `AdminUser`, status `SUSPENDED`. Revokes all refresh tokens. Target holding `ADMIN` or `SUPER_ADMIN` needs a `SUPER_ADMIN` caller (403). Yourself returns 400. Already suspended returns 409 |
| POST | `/admin/users/{id}/reactivate` | | 200 `AdminUser`, status `ACTIVE`. Same rules as suspend. Already active returns 409 |
| GET | `/admin/scouts` | `status` (default `PENDING`; `UNVERIFIED`, `VERIFIED`, `REJECTED` or `ALL`), `page`, `size` | 200 `PageResponse<ScoutProfileDto>` |
| GET | `/admin/safeguarding/reports` | `status?` (for example `PENDING`) | 200 `SafeguardingReport[]` |
| GET | `/admin/data-subject-requests` | `status?` (`PENDING`, `COMPLETED`, `REJECTED`) | 200 `DataSubjectRequest[]` |
| POST | `/admin/data-subject-requests/{id}/resolve` | `outcome` (query: `COMPLETED` or `REJECTED`) | 200 `DataSubjectRequest`. 409 unless `PENDING`. The request itself is handled offline by the data protection officer |
| GET | `/admin/audit-logs` | `actor?`, `action?`, `entityType?`, `entityId?`, `from?`, `to?` (ISO-8601 instants), `page`, `size` | 200 `PageResponse<AuditLog>`, newest first. `ADMIN` callers only ever see their own entries (`actor` is ignored). `SUPER_ADMIN` sees all actors |
| GET | `/admin/verifications/organizations` | `status` (default `PENDING_VERIFICATION`; any `OrgStatus`), `page`, `size` | 200 `PageResponse<Organization>` |
| POST | `/admin/organizations/{id}/verify` | | 200 `Organization`, status `ACTIVE`. 409 unless `PENDING_VERIFICATION` |
| POST | `/admin/organizations/{id}/reject` | `{ reason? }` (optional body) | 200 `Organization`, status `DEACTIVATED` with `rejectionReason`. 409 unless `PENDING_VERIFICATION` |
| GET | `/admin/media/flagged` | `status` (default `FLAGGED`; or `RESTRICTED`, `CLEAN`), `page`, `size` | 200 `PageResponse<MediaAsset>` |
| POST | `/admin/media/{id}/approve` | `{ reason? }` (optional body) | 200 `MediaAsset`, `moderationStatus` `CLEAN`. 409 if already `CLEAN` |
| POST | `/admin/media/{id}/restrict` | `{ reason? }` (optional body) | 200 `MediaAsset`, `moderationStatus` `RESTRICTED`. 409 if already `RESTRICTED` |
| POST | `/admin/scouts/{id}/verify` | | 200 `ScoutProfileDto` |
| POST | `/admin/scouts/{id}/reject` | `notes?` (query) | 200 `ScoutProfileDto` |
| GET | `/admin/guardian-relationships` | `status` (default `PENDING_ADMIN_VERIFICATION`), `page`, `size` | 200 `PageResponse<GuardianLink>` |
| POST | `/admin/safeguarding/reports/{id}/review` | `status` (query, required) | 200 `SafeguardingReport` |
| POST | `/admin/activity-types` | `{ code, name, description? }` | 201 saved `ActivityType`. `code` is one of `MATCH`, `TRAINING`, `TRIAL`, `PERSONAL_TRAINING`, `COMPETITION`, `FITNESS_ASSESSMENT`, `SKILLS_ASSESSMENT`, `TRAINING_CAMP`, `SHOWCASE`, `COMBINE`, `OTHER`. 409 if the code exists |
| POST | `/admin/activity-templates` | `{ activityTypeId, sportId?, name, description? }` | 201 `ActivityTemplate`. Also creates schema version 1 in `DRAFT` |
| GET | `/admin/activity-templates/{id}/schema-versions` | | 200 `ActivitySchemaVersion[]`, newest first |
| POST | `/admin/activity-templates/{id}/fields` | `{ fieldCode, fieldLabel, fieldType, required, displayOrder }` | 201 `FieldDefinitionDto`. Added to the template's current `DRAFT` version; if the latest version is published, a new draft is created first with its fields copied. `fieldType` is `TEXT`, `LONG_TEXT`, `INTEGER`, `DECIMAL`, `DATE`, `DATETIME`, `DURATION`, `BOOLEAN`, `SINGLE_SELECT`, `MULTI_SELECT` and others in `FieldType` |


`AdminUser`: `{ userId, email, role, roles, status, emailVerified, createdAt, lastLoginAt }`.

`AuditLog`: `{ id, createdAt, actorId, actorRole, action, entityType, entityId, details }`. Recorded actions include `DATA_SUBJECT_REQUEST_RESOLVED`, `SCHEMA_PUBLISHED`, `SCHEMA_DEPRECATED`, `ADMIN_CREATED`, `USER_ROLE_CHANGED`, `PLAN_CREATED`, `PLAN_UPDATED`, `PLAN_DEACTIVATED`, `SPORT_CREATED`, `SPORT_DISCIPLINE_ADDED`, `SPORT_METRIC_ADDED`, `USER_SUSPENDED`, `USER_REACTIVATED`, `ORGANIZATION_VERIFIED`, `ORGANIZATION_REJECTED`, `MEDIA_APPROVED`, `MEDIA_RESTRICTED`, `SCOUT_VERIFIED`, `SCOUT_REJECTED`, `SAFEGUARDING_REPORT_REVIEWED`, `GUARDIAN_LINK_APPROVED` and `GUARDIAN_LINK_REJECTED` (admin decisions only). `actorRole` is the actor's most privileged role.

Suspended or deactivated accounts cannot log in or refresh (401, message `This account is suspended. Contact support.`; shown only after a correct password). An access token issued before the suspension stays valid until it expires (at most `expires_in` seconds).

### 3.8 SUPER_ADMIN only

Everything in 3.7 is also available to `SUPER_ADMIN`. These routes are refused to `ADMIN` with 403. Every call is recorded in the audit log.

| Method | Path | Body | Success |
|---|---|---|---|
| POST | `/admin/users` | `{ email, password (8-128) }` | 201 `AdminUser` with role `ADMIN`, status `ACTIVE`, email verified. 409 if the email exists |
| PUT | `/admin/users/{id}/role` | `{ role }` | 200 `AdminUser`. Replaces the primary role (promote or demote). Demoting from `ADMIN`/`SUPER_ADMIN` also strips those from additional roles. Revokes the user's refresh tokens. Your own account returns 400 |
| GET | `/admin/subscription-plans` | | 200 `AdminPlan[]`, including inactive plans |
| GET | `/admin/subscription-plans/{id}` | | 200 `AdminPlan` |
| POST | `/admin/subscription-plans` | `{ code, name, targetRole, pricePerMonth, currency?, entitlements?, prices? }` | 201 `AdminPlan`. `code` is upper case `[A-Z0-9_]`. 409 if the code exists |
| PATCH | `/admin/subscription-plans/{id}` | `{ name?, pricePerMonth?, currency?, entitlements?, prices?, active? }` | 200 `AdminPlan`. `entitlements` and `prices`, when sent, replace the old values |
| POST | `/admin/subscription-plans/{id}/deactivate` | | 200 `AdminPlan`, `active: false`. Hidden from `GET /subscription-plans`; existing subscriptions keep running |
| POST | `/admin/sports` | `{ code, name, description? }` | 201 `SportDto`. `code` upper case, max 10. 409 if it exists |
| POST | `/admin/sports/{id}/disciplines` | `{ code, name }` | 201 `{ id, sportId, code, name, active }`. 409 if the code exists for that sport |
| POST | `/admin/sports/{id}/metrics` | `{ code, name, unit?, higherIsBetter? }` | 201 `{ id, sportId, code, name, unit, higherIsBetter, active }`. `higherIsBetter` defaults to true |
| POST | `/admin/activity-schema-versions/{id}/publish` | | 200 `ActivitySchemaVersion`. 409 unless the version is `DRAFT` |
| POST | `/admin/activity-schema-versions/{id}/deprecate` | | 200 `ActivitySchemaVersion`, `DEPRECATED`. 409 unless the version is `PUBLISHED` |

The audit log (`GET /admin/audit-logs`, section 3.7) is shared, but only `SUPER_ADMIN` sees other actors' entries.

`AdminPlan`: `{ id, code, name, targetRole, pricePerMonth, currency, active, entitlements, prices }`. `targetRole` is `ATHLETE`, `GUARDIAN`, `SCOUT`, `ORGANIZATION` or `PLATFORM`. `entitlements` maps an entitlement key (for example `MAX_WATCHLISTS`, `MAX_FAVOURITES`, `MAX_SCOUT_SEATS`) to its limit; keys not listed are unlimited. `prices` items are `{ id, currency, amount, billingPeriod, active }`, with `billingPeriod` `MONTHLY` or `ANNUAL`.

Read-only sport configuration is open to any signed-in user: `GET /sports/{id}/disciplines` and `GET /sports/{id}/metrics`.

## 4. Per-role summary

| Role | Can do (beyond the common authenticated set) |
|---|---|
| ATHLETE | Manage own profile, activities, media and consent. Approve or reject parent, agent and organization requests. Revoke any relationship |
| PARENT | Request a `PARENT` link. Once the athlete approves, full `MANAGE` access to that athlete |
| GUARDIAN | Request a `LEGAL_GUARDIAN` link with a document. Once an admin approves, full `MANAGE` access for one year |
| AGENT | Request an `AGENT` link at a chosen permission level. Once the athlete approves, access at that level |
| COACH | Verify activities. Work with athletes affiliated to organizations they are a member of, at the affiliation's permission |
| SCOUT | Discovery, favourites, watchlists, evaluations (own data only); public activities and media of athletes; org watchlists and evaluations of their organizations |
| ORG_ADMIN | Granted by creating an organization or joining as an org `ADMIN`. Workspace activation and seats for organizations they administer |
| ADMIN | Dashboard, user list, suspend and reactivate non-admin accounts, scout and organization verification, legal guardian review, safeguarding review, media moderation, activity configuration, own audit entries |
| SUPER_ADMIN | Everything ADMIN can do, plus: act on administrator accounts, create ADMIN accounts, change roles, manage subscription plans, sports, disciplines and metrics, publish activity schemas, see the full audit log |

## 5. Coverage

Every endpoint the role guides document now has a controller. The guide sections were rewritten against this contract.

Request and response shapes in the guides follow the code, including the snake_case auth fields (`access_token`, `refresh_token`, `user_id`, `expires_in`).

## 6. Findings that affect the contract

Found while reading the code. Not changed.

1. ~~Privilege escalation at registration~~ Resolved: registration rejects `ADMIN`, `SUPER_ADMIN` and `ORG_ADMIN`. Privileged roles are only granted by SUPER_ADMIN (task 11) or by creating an organization (task 7).
2. ~~`/auth/logout` and `/auth/mfa/verify` public matcher~~ Resolved: both now require authentication and return 401 without a token.
3. ~~Authorization gaps at controller level~~ Resolved: athlete, activity, media, development and consent endpoints now apply the athlete access policy (section 1.2).
4. ~~Client-supplied `scoutProfileId`~~ Resolved: scout routes derive the profile from the token and no longer accept the parameter.
5. ~~`DELETE /media/{id}` ownership~~ Resolved: delete is limited to the uploader, owner managers and admins.
6. ~~Unsaved activity types~~ Resolved: `POST /admin/activity-types` now saves the type and takes a required `code`.
7. ~~Empty 401/403 bodies~~ Resolved: the security filter chain now writes the `ApiError` envelope.
8. MFA is not enforced. `/auth/mfa/verify` checks a code against a secret the client supplies; the server stores no per-account secret, and no endpoint requires a verified session. Documented, not fixed.
9. Suspension does not invalidate access tokens already issued. They stay valid for up to `expires_in` seconds (60 minutes by default); refresh and login are blocked straight away.
10. Discovery is opt-out, not opt-in. A new athlete's search profile is `PUBLIC` until they withdraw `PROFILE_DISCOVERY` consent; there is no requirement to grant it first. Minors are only blocked from re-granting discovery consent without a guardian link (the projection itself does not check age). Making discovery opt-in would hide every existing athlete until they consent, so it is left as an open decision.
