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

---

## Production Deployment Guide

### Docker Compose (Production)

Use the production Docker Compose file with an `.env` file for all secrets and environment-specific configuration.

```bash
# Copy the example env file and fill in production values
cp .env.example .env

# Start the full production stack
docker compose -f docker-compose.prod.yml up -d
```

The `docker-compose.prod.yml` file starts:
- `talenthub-api` (Spring Boot app, reads `.env` for all configuration)
- `mysql` (MySQL 8.4 with persistent volume)
- `minio` (object storage for media assets)

### Environment Variable Reference

| Variable | Required | Example | Description |
|---|---|---|---|
| `DB_HOST` | Yes | `mysql` | MySQL hostname |
| `DB_PORT` | Yes | `3306` | MySQL port |
| `DB_NAME` | Yes | `talenthub` | Database name |
| `DB_USERNAME` | Yes | `talenthub_user` | DB username |
| `DB_PASSWORD` | Yes | *(secret)* | DB password |
| `JWT_SECRET` | Yes | *(256-bit hex)* | HMAC-SHA256 signing key — minimum 256 bits |
| `JWT_ACCESS_TTL_MINUTES` | No | `60` | Access token TTL in minutes (default: 60) |
| `JWT_REFRESH_TTL_DAYS` | No | `30` | Refresh token TTL in days (default: 30) |
| `MINIO_ENDPOINT` | Yes | `http://minio:9000` | MinIO endpoint URL |
| `MINIO_ACCESS_KEY` | Yes | *(key)* | MinIO access key |
| `MINIO_SECRET_KEY` | Yes | *(secret)* | MinIO secret key |
| `MINIO_BUCKET_MEDIA` | Yes | `talenthub-media` | Bucket name for athlete media |
| `MAIL_HOST` | Yes | `smtp.mailgun.org` | SMTP host |
| `MAIL_PORT` | Yes | `587` | SMTP port |
| `MAIL_USERNAME` | Yes | *(user)* | SMTP username |
| `MAIL_PASSWORD` | Yes | *(secret)* | SMTP password |
| `MAIL_FROM` | Yes | `noreply@talenthub.io` | From address |
| `PAYMENT_PAYSTACK_SECRET_KEY` | Yes | `sk_live_...` | Paystack live secret key |
| `PAYMENT_PAYSTACK_PUBLIC_KEY` | No | `pk_live_...` | Paystack public key |
| `APP_BASE_URL` | Yes | `https://api.talenthub.io` | Base URL for email links |
| `SPRING_PROFILES_ACTIVE` | Yes | `prod` | Must be `prod` in production |
| `SERVER_PORT` | No | `8080` | HTTP port (default: 8080) |
| `FLYWAY_LOCATIONS` | No | `classpath:db/migration` | Migration locations |

### Backup Procedure

**Database:**
```bash
# Take a logical dump
docker exec talenthub-mysql \
  mysqldump -u"$DB_USERNAME" -p"$DB_PASSWORD" "$DB_NAME" \
  --single-transaction --routines --events \
  > "backup-$(date +%Y%m%d%H%M%S).sql"

# Compress and archive
gzip "backup-$(date +%Y%m%d%H%M%S).sql"
```

Automate with a cron job or your cloud provider's managed backup service. Retain backups for at least 30 days in an offsite location (S3, GCS, or equivalent).

**MinIO media objects:**
```bash
# Sync all media to a secondary bucket or local directory
mc mirror minio/talenthub-media /backup/talenthub-media
```

Test restores quarterly. Validate row counts and application startup after each restore drill.

### Monitoring Guide

**Spring Boot Actuator endpoints:**

| Endpoint | Description |
|---|---|
| `GET /actuator/health` | Liveness + readiness probe |
| `GET /actuator/info` | Build and version info |
| `GET /actuator/metrics` | Micrometer metrics index |
| `GET /actuator/metrics/{name}` | Individual metric value |
| `GET /actuator/prometheus` | Prometheus-compatible scrape endpoint |

In `application-prod.properties`, expose only safe endpoints:
```properties
management.endpoints.web.exposure.include=health,info,metrics,prometheus
management.endpoint.health.show-details=authorized
```

**Custom application metrics:**

| Metric | Description |
|---|---|
| `athlete.registrations.total` | Total athlete profiles created |
| `activity.submissions.total` | Total activities submitted |
| `payment.webhooks.processed.total` | Total webhook events received |
| `media.processing.jobs.total` | Total media processing jobs started |

**Prometheus + Grafana setup:**

1. Add `talenthub-api:8080` as a Prometheus scrape target at path `/actuator/prometheus`.
2. Import the Spring Boot Micrometer dashboard (Grafana dashboard ID 4701) as a baseline.
3. Add custom panels for the four TalentHub counters listed above.
4. Set up alerts for `athlete.registrations.total` rate drops (may indicate registration flow issues) and `payment.webhooks.processed.total` anomalies.

