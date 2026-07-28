# 🏍️ BikeLog

BikeLog is a multi-user motorcycle logbook. It replaces a single-page, `localStorage`-based HTML app with a proper **Spring Boot + JPA backend**, secured by **Google OAuth 2.0** and **JWT**, so multiple people can each track their own bikes: rides, fuel fill-ups, oil changes, tyre pressure, service history, and monthly/overall statistics.

> **Status:** Actively developed, pre-1.0. Core ride/fuel tracking and the dashboard work end-to-end; several modules described in the API contract (oil changes, tyre checks, service history, oil/tyre status widgets) are still stubs. See [Project Status & Roadmap](#-project-status--roadmap) below — this is intentional, so the project has a clear on-ramp for contributors.

---

## Table of Contents

- [How it works](#how-it-works)
- [Features](#features)
- [Tech stack](#tech-stack)
- [Project structure](#project-structure)
- [Architecture](#architecture)
- [API overview](#api-overview)
- [Getting started](#getting-started)
- [Configuration](#configuration)
- [Project status & roadmap](#-project-status--roadmap)
- [Known issues](#known-issues)
- [Contributing](#contributing)
- [License](#license)

---

## How it works

1. The user logs in with **Google OAuth 2.0** from the frontend.
2. On successful login, the backend creates the user (if new) and issues a **JWT**, then redirects back to the frontend with the token in the URL.
3. The frontend stores the token and sends it as `Authorization: Bearer <token>` on every subsequent API call.
4. A custom `JwtFilter` validates the token on each request and puts the user's ID into the Spring Security context, so controllers can read the current user via `@AuthenticationPrincipal`.
5. Each user can own multiple **bikes**. Every ride, fuel entry, and maintenance record belongs to a bike, which belongs to a user — all queries are scoped so users can only ever see their own data.
6. The bike's `currentOdo` is recalculated automatically after every new ride or fuel entry, as the max of `initialOdo`, the latest ride odometer, and the latest fuel-entry odometer.
7. Fuel mileage (km/litre) is computed **retroactively**: when a new fuel entry is logged, the *previous* entry gets its `distanceKm` and `mileageKmPerLitre` filled in using the odometer difference between the two fill-ups. The most recent fuel entry always has `null` mileage until the next fill-up closes the loop.

## Features

**Implemented**
- Google OAuth 2.0 login → JWT issuance → stateless JWT auth on every request
- Multi-bike support per user, with an "active bike" preference
- Ride logging with auto-computed distance since the last odometer reading
- Fuel fill-up logging with auto-computed litres, cumulative litres, distance-per-tank, and mileage (km/l)
- Odometer integrity checks (new entries can't be logged behind the bike's current odometer)
- Monthly dashboard: km driven, litres used, spend, riding days, best/current mileage
- Overall (all-time) stats with a month-by-month breakdown for charting
- Month picker data source (`/{bikeId}/months`)

**Designed but not yet implemented** (see [roadmap](#-project-status--roadmap))
- Oil change tracking + oil-change-due reminder
- Tyre pressure check tracking + latest front/rear summary
- Workshop/service history
- Delete-bike (cascade delete of all its records)

## Tech stack

| Layer | Technology |
|---|---|
| Language / runtime | Java 21 |
| Framework | Spring Boot 4.1 (Web, Security, OAuth2 Client, Data JPA) |
| Auth | Google OAuth 2.0 (login) + JWT (API access), via [jjwt](https://github.com/jwtk/jjwt) |
| Database | H2 (in-memory, current) → MySQL (planned, see roadmap) |
| Mapping | MapStruct (entity ↔ DTO) |
| Build | Gradle |
| Frontend | Static HTML/CSS/JS in [`frontend/`](./frontend) (no framework/build step) |

## Project structure

```
bikelog/
├── src/main/java/com/mybikelog/api/
│   ├── config/          # SecurityConfig, CORS, OAuth2 success handler
│   ├── controller/      # REST controllers (one per resource)
│   ├── service/         # Business logic
│   ├── repository/      # Spring Data JPA repositories
│   ├── entity/          # JPA entities
│   ├── dto/             # Request/response DTOs
│   ├── mapper/          # MapStruct mapper interfaces
│   ├── filter/          # JwtFilter
│   └── util/            # JWTUtil, NumberUtil, AppConstants
├── src/main/resources/
│   ├── application.yaml
│   └── application-oauth.yml
├── frontend/            # Static HTML/CSS/JS client (separate app, same repo)
├── docs/
│   └── my-bike-log-swagger.yml      # Full API contract (source of truth for endpoints/DTOs)
├── build.gradle
└── README.md
```

> If you're browsing the repo and don't see `frontend/` or `docs/my-bike-log-swagger.yml` yet, they're being reorganized into this layout — see [issues](../../issues) for tracking.

## Architecture

- **Controller → Service → Repository**, standard layered Spring Boot design. Controllers stay thin: they just extract the authenticated user ID (`@AuthenticationPrincipal`), delegate to a service, and wrap the result in a `ResponseEntity`.
- **`CommonService`** centralizes logic shared across services — fetching a bike scoped to its owner, and recalculating `currentOdo` after any ride/fuel mutation.
- **MapStruct** (`MapperClass`) handles all entity ↔ DTO conversion, including a generic `Page<E>` → `PageDTO<D>` mapper used by every paginated list endpoint.
- **Security**: `SecurityConfig` wires up `oauth2Login()` for the browser-based Google login flow and a custom `JwtFilter` (ahead of `UsernamePasswordAuthenticationFilter`) for stateless API auth on every other request. `CustomAuthenticationSuccessHandler` is where a successful Google login turns into an app JWT and a redirect back to the frontend.
- **Ownership scoping**: nearly every repository query is `findBy...AndUserId(...)` (or via the bike, `AndBikeId(...)`) so cross-user data access isn't possible even if a bike/ride/entry ID is guessed.

## API overview

The full contract — every endpoint, request/response schema, and validation rule — lives in [`docs/my-bike-log-swagger.yml`](./docs/my-bike-log-swagger.yml). You can paste it into the [Swagger Editor](https://editor.swagger.io/) to browse it interactively. Highlights:

| Resource | Base path | Notes |
|---|---|---|
| Users | `/users/me` | Get/patch current user profile & active bike |
| Bikes | `/bikes` | CRUD for a user's bikes |
| Rides | `/rides/{bikeId}` | Log/list/delete odometer readings |
| Petrol | `/petrol-entries/{bikeId}` | Log/list/delete fuel fill-ups; mileage computed automatically |
| Oil Changes | `/oil-changes/{bikeId}` | *Planned* |
| Tyre Checks | `/tyre-checks/{bikeId}` | *Planned* |
| Services | `/services/{bikeId}` | *Planned* |
| Dashboard & Stats | `/{bikeId}/months`, `/{bikeId}/dashboard`, `/{bikeId}/overall-stats`, `/{bikeId}/oil-status`, `/{bikeId}/tyre-status` | Monthly and all-time stats; the last two are stubbed |

All endpoints except OAuth2 login require `Authorization: Bearer <jwt>`.

## Getting started

### Prerequisites
- JDK 21
- A [Google OAuth 2.0 client ID/secret](https://console.cloud.google.com/apis/credentials) (Web application type, with an authorized redirect URI of `http://localhost:8080/login/oauth2/code/google` for local dev)
- (Frontend) Any static file server — e.g. VS Code's Live Server extension, since the frontend currently expects to run at `http://127.0.0.1:5500`

### Backend

```bash
git clone https://github.com/arshadpatel/my-bike-log.git
cd my-bike-log

export JWT_SECRET="a-long-random-string-at-least-32-bytes"
export GOOGLE_CLIENT_ID="your-google-client-id"
export GOOGLE_CLIENT_SECRET="your-google-client-secret"

./gradlew bootRun
```

The API will be available at `http://localhost:8080`. H2 is in-memory, so data resets on every restart — the H2 console is available at `/h2-console` for inspecting it while developing.

### Frontend

```bash
cd frontend
# open index.html with a static server, e.g.:
npx live-server --port=5500
```

Log in via the "Sign in with Google" flow; the backend will redirect back to `http://127.0.0.1:5500/index.html?token=...` with the JWT.

## Configuration

| Variable | Required | Description |
|---|---|---|
| `JWT_SECRET` | Yes | HMAC signing key for JWTs. Use a long random string. |
| `GOOGLE_CLIENT_ID` | Yes | From Google Cloud Console OAuth credentials. |
| `GOOGLE_CLIENT_SECRET` | Yes | From Google Cloud Console OAuth credentials. |

`jwt.expiration-time` (in `application.yaml`) controls token lifetime in milliseconds; defaults to 1 hour.

---

## 🚧 Project status & roadmap

This project is being open-sourced specifically to grow through contributions, so here's an honest snapshot of what's solid vs. what's a good first issue.

### ✅ Working end-to-end
- Google OAuth login → JWT issuance
- Bike CRUD (create, list, get, update)
- Ride logging, listing (with month filter + pagination), deletion
- Petrol entry logging, listing, deletion, with auto-computed litres/mileage/cumulative litres
- Monthly dashboard stats
- Overall stats with monthly breakdown

### 🧩 Stubbed / not implemented (great first issues)
- **Delete bike** (`DELETE /bikes/{bikeId}`) — controller method exists but returns `null`; needs a service method that cascades deletes across rides/petrol/oil/tyre/service records.
- **Oil status** (`GET /{bikeId}/oil-status`) — controller returns `null`; needs the `OilChange` entity/repo/service plus the due/overdue calculation described in the OpenAPI spec.
- **Tyre status** (`GET /{bikeId}/tyre-status`) — same situation, needs the `TyreCheck` entity/repo/service.
- **Oil Changes module** — entity, repository, service, controller (`/oil-changes/{bikeId}`) don't exist yet; only speced in the OpenAPI contract.
- **Tyre Checks module** — same, for `/tyre-checks/{bikeId}`.
- **Service (workshop) history module** — same, for `/services/{bikeId}`.

### 🗺️ Planned / architectural
- **Migrate from H2 to MySQL** for real persistence (currently in-memory only, wiped on every restart).
- **Global exception handling** — replace scattered `RuntimeException`s with a `@ControllerAdvice` that returns the `ErrorResponse`/`ValidationErrorResponse` shapes already defined in the OpenAPI contract (with correct 400/404/409 status codes).
- **Bean Validation** on request DTOs to match the constraints already documented in the OpenAPI spec (e.g. `odo` must be positive, `name` length limits).
- **Externalize CORS/redirect URLs** — `http://127.0.0.1:5500` is currently hardcoded in `SecurityConfig` and `CustomAuthenticationSuccessHandler`, which blocks any non-local deployment.
- **Automated tests** — there currently aren't any; unit tests for services and integration tests for controllers would be very welcome.
- **Reorganize repo** into `backend/` (or keep at root) + `frontend/` + `docs/my-bike-log-swagger.yml` as described in [Project structure](#project-structure).


If you find issues, please [open an issue](../../issues) — see [CONTRIBUTING.md](./CONTRIBUTING.md).

## Contributing

Contributions are very welcome — see [CONTRIBUTING.md](./CONTRIBUTING.md) for how to get set up, coding conventions, and how issues/PRs are handled.

## License

[MIT](./LICENSE) — free to use, modify, and distribute, with attribution.
