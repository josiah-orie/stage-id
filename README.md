# TalentHub Backend

Athlete talent management platform backend — built with Spring Boot 4.1.2, Spring Modulith 2.1.1, and MySQL 8.4.

---

## Quick Start

### Prerequisites

- Java 21+ (JDK)
- Docker + Docker Compose
- Maven (or use `./mvnw`)

### 1. Start local infrastructure

```bash
docker compose -f docker-compose.dev.yml up -d
```

This starts MySQL 8.4 on port 3306, MinIO on 9000/9001, and MailHog on 1025/8025.

### 2. Run the application

```bash
./mvnw spring-boot:run
```

The API will be available at `http://localhost:8080`.

### 3. Swagger UI

Open `http://localhost:8080/swagger-ui.html` in your browser.

---

## Configuration

| File | Purpose |
|---|---|
| `application.properties` | Base config (sets active profile to `dev`) |
| `application-dev.properties` | Local development (MySQL + MinIO + MailHog) |
| `application-prod.properties` | Production (reads from env vars) |

Copy `.env.example` to `.env` and fill in your production values before running `docker compose -f docker-compose.prod.yml up -d`.

---

## Architecture

This project uses **Spring Modulith** to enforce clean module boundaries. Each module follows:

```
module/
  api/           ← Controllers, DTOs, public interfaces
  application/   ← Commands, queries, services, event listeners
  domain/        ← Entities, repositories, domain services, events
  infrastructure/← Configuration, persistence adapters
```

### Modules

| Module | Responsibility |
|---|---|
| `shared` | Cross-cutting: base entities, JWT, security config, exceptions |
| `identity` | User accounts, authentication (JWT), password reset |
| `profile` | User profiles, listens to `UserRegisteredEvent` |
| `sport` | Sport configuration reference data |
| `subscription` | Subscription plans and entitlement enforcement |
| `athlete` | Athlete profiles, unique number generation |
| `guardian` | Guardian links, safeguarding policies for minors |
| `organization` | Organisations (clubs, schools, academies) |
| `athleteaccess` | Permission grants for athlete profile access |
| `activityconfiguration` | Activity/session type templates |
| `activity` | Athlete activity and session logging |
| `development` | Development goals and progress tracking |
| `media` | Video/image uploads via MinIO |
| `verification` | Identity and document verification |
| `scout` | Scout profiles |
| `discovery` | Athlete search and discovery |
| `scouting` | Watchlists, favourites, evaluations |
| `notification` | Email and in-app notifications |
| `safeguarding` | Minor protection audit trail |
| `billing` | Payment processing (Paystack) |
| `administration` | Admin tools, audit logs, reporting |

### Athlete Number Format

- **Unaffiliated**: `TH-{LPAD(seq,5,'0')}` e.g. `TH-00001`
- **Org-affiliated**: `{ORG_CODE}-{SPORT_CODE}-{LPAD(seq,5,'0')}` e.g. `FCB-FB-00042`

Numbers are generated with `SELECT FOR UPDATE` on the `athlete_number_sequences` table to guarantee uniqueness under concurrent load.

---

## Database Migrations

Flyway migrations live in `src/main/resources/db/migration/`. They run automatically on startup.

| Migration | Contents |
|---|---|
| V1 | Schema baseline: `audit_logs` |
| V2 | Identity: `users`, `refresh_tokens` |
| V3 | Profiles: `user_profiles` |
| V4 | Sports: `sports` (10 seeded) |
| V5 | Subscription stub: `subscription_plans`, `user_subscriptions` |
| V6 | Athletes: `athlete_profiles`, `athlete_number_sequences` |
| V7 | Guardians: `guardian_links` |
| V8 | Organizations: `organizations` |
| V9 | Athlete access: `athlete_access_grants` |

---

## Running Tests

```bash
# Unit + modularity tests (no Docker required)
./mvnw test

# Integration tests (requires Docker)
./mvnw test -Dspring.profiles.active=test
```

---

## JWT Authentication

All endpoints except `/api/v1/auth/**`, `/swagger-ui/**`, and `/actuator/health` require a Bearer JWT in the `Authorization` header.

```
Authorization: Bearer <access_token>
```

Tokens are HMAC-SHA256 signed. Access tokens expire in 60 minutes by default. Refresh tokens are opaque UUIDs stored as BCrypt hashes with 30-day expiry.

---

## License

Proprietary — © Jovine360
