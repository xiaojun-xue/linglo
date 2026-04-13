# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

**玲珑 (Linglo)** — an IPD (Integrated Product Development) project management platform for small-to-medium R&D teams. Monorepo with a Spring Boot backend and Vue 3 frontend.

## Build & Run Commands

### Backend (Spring Boot 3 + Java 17)
```bash
cd src/backend
mvn spring-boot:run              # Start dev server (H2 in-memory DB, port 8080)
mvn test                         # Run all tests
mvn test -Dtest=AuthServiceTest  # Run a single test class
mvn package -DskipTests          # Build JAR without tests
```

### Frontend (Vue 3 + Vite + TypeScript)
```bash
cd src/frontend
pnpm install                     # Install dependencies (pnpm preferred; npm also works)
pnpm run dev                     # Start dev server on port 3000
pnpm run build                   # Production build to dist/
pnpm run lint                    # ESLint with auto-fix (.vue,.ts,.js,.tsx,.jsx)
npx playwright test              # Run E2E tests (Playwright, Chromium)
npx playwright test tests/e2e/auth.spec.ts  # Run a single E2E test
```

### Docker (production)
```bash
cd deploy
docker compose up -d             # Starts nginx, backend, PostgreSQL, Redis, MinIO
```

## Architecture

### Backend (`src/backend`)
- **Framework**: Spring Boot 3.2, Java 17, Spring Security + JWT auth
- **ORM**: Spring Data JPA + MyBatis-Plus (dual persistence)
- **Database**: H2 in-memory for dev (`application-dev.yml`), PostgreSQL for prod
- **API prefix**: All endpoints are under `/api` (configured via `server.servlet.context-path`)
- **Package structure**: `com.ipd.platform.{controller,service,repository,entity,dto,security,config}`
- **Entities use table-name prefixes**: `Sys*` (system), `Prj*` (project), `Req*` (requirement), `Rev*` (review), `Qa*` (QA), `Doc*` (document), `Mdm*` (master data)
- **Swagger UI**: available at `/api/swagger-ui.html` in dev
- **H2 console**: available at `/api/h2-console` in dev

### Backend Conventions
- **BaseEntity**: All entities extend `BaseEntity` which provides `id` (auto-increment Long), `createdAt`, `updatedAt`, `createdBy`, `updatedBy` via JPA auditing. New entities must extend it.
- **ApiResponse wrapper**: All controllers return `ApiResponse<T>` with `{code, message, data, timestamp}`. Use factory methods: `ApiResponse.success(data)`, `ApiResponse.error(msg)`, `ApiResponse.badRequest(msg)`, `ApiResponse.notFound(msg)`. Frontend checks `res.code !== 200` to detect errors.
- **Seed data**: `DataInitializer` (CommandLineRunner) creates 10 roles, permissions, and an admin user on startup. In dev (`ddl-auto: create-drop`) the DB is rebuilt every restart — seed data is always fresh. In prod (`ddl-auto: update`) it only inserts if missing (idempotent checks).
- **Roles**: Defined as constants in `SysRole.RoleCode` — ADMIN, PM (Product Manager), PGM (Project Manager), TL (Tech Lead), DEV, QAM (QA Manager), QA, OPS, EXECUTIVE, VIEWER.
- **Permissions**: Defined as constants in `SysPermission.Code`, type 1 = menu, type 2 = button/operation. ADMIN role gets all permissions automatically.
- **Security**: `@EnableMethodSecurity` is active — use `@PreAuthorize` on controller methods for fine-grained access control. JWT filter runs before `UsernamePasswordAuthenticationFilter`.

### Frontend (`src/frontend`)
- **Framework**: Vue 3 Composition API + TypeScript + Element Plus + ECharts
- **State**: Pinia (`src/stores/user.ts`)
- **Routing**: Vue Router with JWT auth guard — unauthenticated users redirect to `/login`
- **API layer**: Axios wrapper at `src/api/request.ts` (baseURL `/api`, auto-attaches Bearer token). Each domain has its own API module in `src/api/`. The response interceptor unwraps `ApiResponse` — callers receive the full `{code, message, data}` object, not raw Axios response.
- **Path alias**: `@` maps to `src/`
- **Vite proxy**: `/api` requests proxy to `http://localhost:8080` in dev
- **Views**: one `.vue` file per major feature (Dashboard, Requirements, Projects, Tasks, Sprints, Reviews, QaManagement, Documents, Profile) wrapped by `Layout.vue`

### Auth Flow
- Login via `/api/auth/login` returns JWT token stored in `localStorage`
- Frontend attaches `Authorization: Bearer <token>` on all requests
- 401 responses clear token and redirect to `/login`
- Default credentials: `admin` / `admin123`

### IPD Domain Modules
The platform implements core IPD processes: Requirements → Projects → Tasks → Sprints → Reviews (CDCP/PDCP/TR4/ADCP) → QA (test cases + bugs) → Documents.

## Testing
- **Backend**: JUnit 5 tests in `src/backend/src/test/`. Tests run against H2 in-memory DB.
- **Frontend E2E**: Playwright tests in `src/frontend/tests/e2e/`. Config in `playwright.config.ts`. Requires both backend and frontend running (webServer auto-starts frontend via `pnpm run dev`). Tests run single-threaded (`workers: 1`, `fullyParallel: false`).

## Deployment
Docker Compose in `deploy/` orchestrates: Nginx (port 80), Spring Boot backend (port 8080), PostgreSQL 16, Redis 7, MinIO (optional object storage).

## Documentation
Design docs in `docs/` — competitive analysis (`01-竞品分析/`), solution design (`02-解决方案设计/`), and test plans (`03-测试计划/`). These inform product decisions but are not auto-generated from code.
