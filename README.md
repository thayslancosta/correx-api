# CorreX API

Spring Boot API for a writing-assessment workflow. The service provides teacher
authentication, classes, students, assessment criteria, essay uploads, automated
correction requests and manual review.

This repository is prepared for the public `thayslancosta/correx-api` portfolio
repository. No production deployment is claimed by this document.

## Features

- JWT authentication for teachers.
- Class and student management with pagination.
- Configurable assessment criteria and weight validation.
- Text and PDF/DOC/DOCX/TXT essay submission.
- AI-assisted correction through an explicit OpenAI request.
- Manual review and delivery confirmation.
- Swagger/OpenAPI documentation.
- Request rate limiting through Bucket4j.
- H2 file database for development and PostgreSQL for production.

The API does not send student feedback through email or messaging services.

## Stack and structure

- Java 17 and Spring Boot 3.2.4
- Spring Security, JWT and BCrypt
- Spring Data JPA with Flyway
- H2 for local development and PostgreSQL for production
- WebClient, Apache POI and PDFBox
- Springdoc OpenAPI and Actuator

Main packages are organized under `src/main/java/com/correx/` by configuration,
controllers, DTOs, entities, repositories, services, security and utilities.
Database migrations are in `src/main/resources/db/migration/`.

## Configuration

Copy the safe template and provide local values:

```bash
cp .env.example .env
```

Important variables:

| Variable | Purpose |
| --- | --- |
| `JWT_SECRET` | JWT signing secret; required in every profile |
| `OPENAI_API_KEY` | Optional key for AI correction requests |
| `DATABASE_URL` | PostgreSQL JDBC URL in the `prod` profile |
| `DB_USER`, `DB_PASS` | PostgreSQL credentials |
| `CORS_ORIGINS` | Allowed frontend origins |

The OpenAI key is intentionally optional at startup. AI requests cannot run until
`OPENAI_API_KEY` is configured. Never place real keys in `application.yml`, `.env.example`
or Docker files.

## Run locally

Requirements: JDK 17+, Maven 3.9+, and Docker Compose only when using PostgreSQL.

```bash
mvn spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=dev"
```

The development profile uses a file-backed H2 database at `data/correxdb` and
listens on `http://localhost:8080`. Swagger UI is available at
`/swagger-ui/index.html`. The H2 console is a development-only convenience and
must not be treated as a production interface.

For PostgreSQL:

```bash
cp .env.example .env
# Set DB_NAME, DB_USER, DB_PASS and JWT_SECRET in .env.
docker compose up --build
```

Compose now requires database and JWT values instead of using credential-like
fallbacks. Keep `.env` local.

## API surface

All application endpoints use the `/api/v1` prefix.

| Area | Examples |
| --- | --- |
| Authentication | `POST /api/v1/auth/register`, `POST /api/v1/auth/login` |
| Classes | `GET/POST /api/v1/turmas`, `GET/PUT/DELETE /api/v1/turmas/{id}` |
| Students | `GET/POST /api/v1/alunos`, `GET/PUT/DELETE /api/v1/alunos/{id}` |
| Criteria | `GET/POST /api/v1/criterios`, `PUT/DELETE /api/v1/criterios/{id}` |
| Essays | `POST /api/v1/redacoes/texto`, `POST /api/v1/redacoes/upload`, `PUT /api/v1/redacoes/{id}/revisao` |
| Dashboard | `GET /api/v1/dashboard` |

The CorreX frontend candidate currently targets `http://localhost:8080/api/v1`.
Confirm the frontend/backend contract and CORS behavior in an integrated run before
claiming the two projects work together end to end.

## Testing

Unit and controller tests are present under `src/test/java/`. Run them without
calling OpenAI:

```bash
mvn test
mvn verify
```

A successful local build does not verify production credentials, a live database,
file upload limits or an actual OpenAI response. Those checks remain environment-
dependent.

## Security notes

- Secrets, logs, H2 files and build output are excluded by `.gitignore`.
- Actuator exposes health only, and health details are not public by default.
- The development H2 console is enabled only in the `dev` profile.
- Do not use the development profile or demo credentials in production.

The OpenAI key previously present in the local development configuration must be
revoked and rotated in the OpenAI dashboard. Removing it from the file does not
invalidate a key that may already have been exposed.

## Status and publication

This preparation documents and hardens the academic project. It does not claim
production readiness or complete test coverage. The repository has no license file;
licensing should be decided separately before redistribution.
