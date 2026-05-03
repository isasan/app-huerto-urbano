# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project

HuertoApp — fullstack app for managing urban home gardens. Portfolio project demonstrating Spring Boot 3 + Vue 3.

## Commands

### Backend (run from `backend/`)
```bash
# Start dev server (port 8080)
mvn spring-boot:run

# Build JAR
mvn clean package -DskipTests

# Run tests
mvn test

# Run a single test class
mvn test -Dtest=AuthServiceTest
```

### Frontend (run from `frontend/`)
```bash
# Install dependencies
npm install

# Start dev server (port 5173, proxies /api → 8080)
npm run dev

# Build for production
npm run build
```

Both servers must run simultaneously during development. The Vite proxy forwards all `/api` requests to Spring Boot, so the frontend always calls `/api/...` (never `localhost:8080` directly).

## Architecture

### Backend — `com.huertoapp`

Layered Spring Boot REST API. All controllers return JSON; there are no Thymeleaf views.

**Package layout:**
- `model/` — JPA entities: `User`, `Garden`, `Plot`, `Crop`, `Task`, `HarvestLog`. `Garden → Plot → Crop` is the ownership hierarchy.
- `repository/` — Spring Data JPA repositories, one per entity.
- `service/` — Business logic. Services receive the authenticated `username` from the controller and enforce ownership — they never trust a resource ID alone.
- `controller/` — `@RestController` classes, one per entity. Inject `@AuthenticationPrincipal UserDetails` to get the current user.
- `dto/request/` and `dto/response/` — Separate DTO classes for input and output. Entities are never serialised directly.
- `security/` — `JwtTokenProvider` (generate/validate/extract), `JwtAuthenticationFilter` (OncePerRequestFilter), `UserDetailsServiceImpl`.
- `config/` — `SecurityConfig` (filter chain, CORS, BCrypt), `OpenApiConfig` (Swagger Bearer scheme).
- `exception/` — `GlobalExceptionHandler` (@RestControllerAdvice) maps exceptions to structured JSON: `{timestamp, status, error, message}`. Validation errors return `{errors: {field: message}}`.
- `data/SeedingCalendarData.java` — Static in-memory data for 12 plants × 2 hemispheres (NORTE/SUR). No DB table; served by `CalendarService`.

**Security flow:** `JwtAuthenticationFilter` validates the Bearer token on every request, loads the user via `UserDetailsServiceImpl`, and sets `SecurityContextHolder`. Routes under `/api/auth/**`, `/api/calendar/**`, and `/api/weather/**` are public; `/api/admin/**` requires `ROLE_ADMIN`; everything else requires authentication.

**JWT config** (`application.properties`):
```
app.jwt.secret=...        # signing key
app.jwt.expiration=86400000  # ms (24 h)
```
`JwtTokenProvider` derives the `SecretKey` by Base64-encoding the raw secret bytes. `@Builder.Default` is required on `AuthResponse.tokenType` because Lombok's builder ignores field initialisers without it.

**Weather** — `WeatherService` uses Spring WebFlux `WebClient` (not `RestTemplate`) to call **Open-Meteo** (no API key required). Two-step flow: geocoding API converts city name + optional ISO-3166-1 alpha-2 `countryCode` → lat/lon, then forecast API fetches current conditions. WMO weather codes are mapped to Spanish descriptions and Bootstrap icon class names. In-memory cache (30 min) keyed by `city_countrycode`. Config in `application.properties`:
```
app.weather.base-url=https://api.open-meteo.com/v1
app.weather.geocoding-url=https://geocoding-api.open-meteo.com/v1
```

**User profile** — `UserController` exposes `GET/PUT /api/users/me` and `PUT /api/users/me/password`. The `User` entity has `city`, `countryCode` (2-char ISO code, used to disambiguate geocoding), and `hemisphere` (NORTE/SUR). Frontend: `ProfileView.vue` at `/profile` (auth required), `userService.js` for API calls.

### Frontend — `src/`

Vue 3 SPA using the Composition API (`<script setup>`) throughout.

**Key wiring:**
- `services/api.js` — single Axios instance with base URL `/api`. Request interceptor attaches `Authorization: Bearer <token>` from `localStorage`. Response interceptor clears storage and redirects to `/login` on 401.
- `stores/auth.js` (Pinia) — source of truth for `token` and `user`. Hydrates from `localStorage` on page load. `isAdmin` computed checks `user.role === 'ADMIN'`.
- `router/index.js` — navigation guard checks `meta.requiresAuth`, `meta.guestOnly`, and `meta.requiresAdmin` on every route transition.
- All other services (`gardenService`, `cropService`, etc.) import from `api.js` and never create their own Axios instances.

**Alias** — `@` maps to `src/`. Use it for all imports.

## H2 Console

Available at `http://localhost:8080/h2-console` during development.
- JDBC URL: `jdbc:h2:file:./data/huertoapp`
- Username: `sa`, Password: *(empty)*

## Swagger UI

`http://localhost:8080/swagger-ui.html` — use the **Authorize** button (top right) to paste a Bearer token and test protected endpoints.
