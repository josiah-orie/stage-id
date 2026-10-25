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
2. `POST {{BASE_URL}}/auth/login` — receive `accessToken` + `refreshToken`
3. Use `Authorization: Bearer {{ACCESS_TOKEN}}` on all subsequent requests

Tokens expire after 60 minutes. Use `POST {{BASE_URL}}/auth/refresh` to renew without re-login.

## Swagger UI

Interactive API documentation is available at:

```
http://localhost:8080/swagger-ui.html   (local)
https://api.talenthub.io/swagger-ui.html  (production, requires auth)
```
