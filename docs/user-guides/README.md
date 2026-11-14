# TalentHub API — User Guides

Role-specific guides with curl examples and full endpoint references.

| Guide | Role |
|---|---|
| [ATHLETE_USER_GUIDE.md](./ATHLETE_USER_GUIDE.md) | Athletes managing their own profiles, activities, and media |
| [PARENT_USER_GUIDE.md](./PARENT_USER_GUIDE.md) | Parents acting on behalf of their child athletes |
| [GUARDIAN_USER_GUIDE.md](./GUARDIAN_USER_GUIDE.md) | Legal guardians with formal platform authorization |
| [SCOUT_USER_GUIDE.md](./SCOUT_USER_GUIDE.md) | Talent scouts discovering, evaluating, and tracking athletes |
| [COACH_USER_GUIDE.md](./COACH_USER_GUIDE.md) | Coaches managing assigned athletes within an organization |
| [AGENT_USER_GUIDE.md](./AGENT_USER_GUIDE.md) | Sports agents representing and managing athlete visibility |
| [ORGANIZATION_USER_GUIDE.md](./ORGANIZATION_USER_GUIDE.md) | Organization owners, admins, coaches, and scouts within a registered club or academy |
| [ADMIN_USER_GUIDE.md](./ADMIN_USER_GUIDE.md) | Delegated platform administrators managing verifications, moderation, and reporting |
| [SUPER_ADMIN_USER_GUIDE.md](./SUPER_ADMIN_USER_GUIDE.md) | Full platform super-administrators with unrestricted access and configuration authority |

## Base URL

All curl examples use:

```
{{BASE_URL}} = https://api.talenthub.io/api/v1
{{ACCESS_TOKEN}} = <your JWT access token>
```

## Authentication Flow

Every role follows the same three-step start:

1. `POST {{BASE_URL}}/auth/register` — create account
2. `POST {{BASE_URL}}/auth/login` — receive `access_token` + `refresh_token` (snake_case)
3. Use `Authorization: Bearer {{ACCESS_TOKEN}}` on all subsequent requests

Tokens expire after 60 minutes (`expires_in`). Use `POST {{BASE_URL}}/auth/refresh` with `{ "refresh_token": "..." }` to renew without re-login.

## Roles and access

- Register with the role you need (`ATHLETE`, `COACH`, `PARENT`, `GUARDIAN`, `AGENT`, `SCOUT`). Add more later with `POST /users/me/roles`. `ADMIN`, `SUPER_ADMIN` and `ORG_ADMIN` can't be self-assigned: creating an organization grants `ORG_ADMIN`, and a `SUPER_ADMIN` creates admins.
- Athlete ids in paths (`/athletes/{id}/...`) are the athlete's **user id**.
- Access to an athlete's data comes from being the athlete, an approved parent or legal guardian, an approved agent or organization affiliation, or an admin. Anyone else gets `403`.
- The full endpoint-by-role reference is [../API_CONTRACT.md](../API_CONTRACT.md).

## Response Conventions

Every guide ends with a "Response Shapes, Pagination and Errors" section covering the details. In short:

- Successful responses return the resource as plain JSON with no wrapper. Deletes and other actions with no result return `204 No Content`.
- List endpoints take `page` (zero-based, default `0`), `size` (default `20`) and `sort` (`field,asc|desc`), and return `{ content, page, size, totalElements, totalPages, first, last }`.
- Errors use one envelope: `{ timestamp, status, error, message, path }`, plus `fieldErrors` (`field`, `rejectedValue`, `message`) on `400` validation failures. Branch on `status`, not on `message`.
- Missing or invalid bearer tokens (`401`) and permission failures (`403`) also use the JSON error envelope.

## Swagger UI

Interactive API documentation is available at:

```
http://localhost:8080/swagger-ui.html   (local)
https://api.talenthub.io/swagger-ui.html  (production, requires auth)
```
