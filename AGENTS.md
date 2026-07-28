# AGENTS.md

Keybout is a web-based multiplayer keyboard racing game: players type random words
before other players do. Monorepo with an Angular frontend and a Spring Boot
(Kotlin) backend, communicating over SockJS/WebSocket.

## Project layout

- `keybout-frontend/` — Angular 17 app (TypeScript), UI and WebSocket client.
- `keybout-backend/` — Spring Boot 4 app (Kotlin, JVM 21), game logic, REST API,
  WebSocket endpoint, optional MongoDB persistence.
- `pom.xml` (root) — parent POM aggregating both modules for a combined build.

## Build / run / test

Prerequisites: Java 21, Node.js 16+ with NPM.

Frontend (`keybout-frontend/`):
- `npm install` — install dependencies.
- `npm start` (`ng serve`) — run the dev server at `http://localhost:4200`.
- `npm run build` — production build.
- `npm test` (`ng test`) — Karma/Jasmine unit tests.
- `npm run lint` — lint.

Backend (`keybout-backend/`):
- `../mvnw spring-boot:run` — run the backend at `http://localhost:8080`.
- `../mvnw test` — run Kotlin/JUnit5 tests.
- `../mvnw package` — build the backend jar.

Combined fat jar (from repo root):
- `./mvnw package -Pfull` — builds the frontend, embeds it into the backend jar
  (`keybout-backend/target/keybout-backend.jar`), which then serves both.
- Omit `-Pfull` to build backend and frontend as separate artifacts.

When developing, run frontend and backend separately (`ng serve` + `spring-boot:run`);
the frontend dev server proxies/talks to the backend over `http://localhost:8080`.
The WebSocket handler only allows origins `http://localhost:4200`, `http://localhost:8080`,
and `https://keybout.herokuapp.com` — see
[WebSocketConfig.kt](keybout-backend/src/main/kotlin/com/adeliosys/keybout/config/WebSocketConfig.kt).

## Backend architecture (`keybout-backend/src/main/kotlin/com/adeliosys/keybout/`)

- `api/` — REST/WebSocket controllers: `PlayController` (the WebSocket game handler),
  `ScoreController`, `StatsController`.
- `config/` — `WebSocketConfig`, `MongoConfig`.
- `model/` — domain types: `GameDescriptor`, `GameMode` (capture/race),
  `GameStyle` (regular/hidden/anagram/calculus), `Language` (en/fr), `Difficulty`,
  `Word`, `Score`, `Stats`, `ClientState`, `Action`, `Notifications`.
- `service/` — game logic: `PlayService`, `BaseGameService` with `CaptureGameService`
  and `RaceGameService` subclasses, `CalculusService`, `DictionaryService`
  (loads `src/main/resources/data/words-en.txt` / `words-fr.txt`), `AwardService`,
  `ScoreService`, `StatsService`, `UserNameService`, `DatabaseAwareService`.
- `repository/` — MongoDB repositories (`ScoreRepository`, `StatsRepository`);
  only active when `spring.mongodb.uri` is configured.
- `util/` — helpers (`Counter`, `WebSocketExtensions`, dictionary utilities).

Spring configuration is in `src/main/resources/application.yml`.

## Frontend architecture (`keybout-frontend/src/app/`)

Standalone Angular components (no NgModules), routed via `app.routes.ts`:
- `home/`, `about/`, `documentation/`, `demo/`, `scores/`, `stats/` — informational pages.
- `play/` — the core game flow: `connect/` (join/create), `games/` (game list/lobby),
  `game/` (live gameplay), `results/` (round/game scores), plus `play.service.ts`
  (WebSocket client) and `model.ts` (shared types mirroring the backend model).

## Conventions

- Kotlin backend, TypeScript frontend; keep new backend code in `src/main/kotlin`
  (Maven is configured with `sourceDirectory` pointing there, not the Java default).
- Indentation: 2 spaces, UTF-8, final newline, trim trailing whitespace
  (see `.editorconfig`); TypeScript strings use single quotes.
- Prefer adding backend tests under `keybout-backend/src/test/kotlin` mirroring the
  package of the code under test (existing tests use JUnit5 + Kotlin test assertions).
- When changing the WebSocket message model (`Action`, `Notifications`, `model.ts`),
  update both backend and frontend together — they are not generated from a shared schema.
