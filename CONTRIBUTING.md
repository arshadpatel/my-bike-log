# Contributing to BikeLog

Thanks for considering a contribution — this project was open-sourced specifically to grow past what one person can build alone, so issues, PRs, bug reports, and even questions are genuinely welcome.

## Ways to contribute

- Pick up an open [issue](../../issues), especially ones labeled `good first issue` or `help wanted`
- Report a bug you hit while running the app
- Improve documentation (including this file and the README!)
- Propose a feature — open an issue first so we can discuss scope before you write code
- Review open pull requests

If you're not sure where to start, the README's [Project Status & Roadmap](./README.md#-project-status--roadmap) section lists concrete unimplemented modules (Oil Changes, Tyre Checks, Service history, delete-bike, oil/tyre status) — each is a good self-contained first contribution.

## Getting set up

1. Fork the repo and clone your fork:
   ```bash
   git clone https://github.com/arshadpatel/my-bike-log.git
   cd my-bike-log
   ```
2. Get a free Google OAuth 2.0 client ID/secret from the [Google Cloud Console](https://console.cloud.google.com/apis/credentials) (Web application, redirect URI `http://localhost:8080/login/oauth2/code/google`).
3. Set the required environment variables (see the README's [Configuration](./README.md#configuration) table).
4. Run the backend: `./gradlew bootRun`
5. Run the frontend: serve `frontend/` with any static file server on port 5500 (e.g. `npx live-server --port=5500`).
6. Confirm you can log in and the JWT round-trips correctly before making changes.

## Before you open a PR

- **Open or claim an issue first** for anything beyond a trivial fix (typo, small bug), so two people don't work on the same thing.
- **Keep PRs focused.** One feature or fix per PR is much easier to review than a bundle of unrelated changes.
- **Follow the existing structure.** New resources (e.g. Oil Changes) should mirror the existing Ride/Petrol pattern:
  - `entity/` — JPA entity
  - `repository/` — Spring Data JPA repository, scoped by `bikeId` (and `userId` where the resource is fetched directly, following `CommonService`'s pattern)
  - `dto/` — request/response DTOs
  - `mapper/` — add mappings to `MapperClass`
  - `service/` — business logic; reuse `CommonService.getBikeDetails(...)` for ownership checks
  - `controller/` — thin controller, matching the paths already defined in `docs/my-bike-log-swagger.yml`
- **Match the API contract.** `docs/my-bike-log-swagger.yml` is the source of truth for endpoint paths, request/response shapes, and status codes. If your change needs to deviate from it, update the spec in the same PR and explain why in the PR description.
- **Scope data to the authenticated user.** Every query on user-owned data must be filtered by the authenticated user's ID (directly or via the owning bike) — this is a security-sensitive project, not just a CRUD demo.
- **No secrets in code.** Config values (client IDs, secrets, JWT secret) belong in environment variables, never hardcoded or committed.

## Code style

- Standard Java conventions; follow the formatting already present in the codebase (4-space indentation, Lombok `@AllArgsConstructor` + field injection via constructor for services/controllers).
- Prefer constructor injection (via Lombok) over `@Autowired` fields.
- Keep controllers free of business logic — that belongs in the service layer.
- Use MapStruct for entity↔DTO conversion rather than hand-written mapping code, to stay consistent with `MapperClass`.

## Commit messages

Keep them short and descriptive, ideally in the imperative mood:

```
Add oil change entity, repository, and CRUD endpoints
Fix currentOdo being overwritten by client on bike update
```

## Pull request process

1. Push your branch to your fork and open a PR against `main`.
2. Fill in a short description of what changed and why, and link the issue it addresses (e.g. `Closes #12`).
3. Make sure the project builds (`./gradlew build`) before requesting review.
4. Be responsive to review feedback — most PRs will need at least one round of changes, which is normal and not a reflection on the contribution.

## Reporting bugs

Open an issue with:
- What you expected to happen
- What actually happened
- Steps to reproduce
- Relevant logs/stack traces if you have them

## Code of conduct

Be respectful and constructive. This is a small hobby-turned-open-source project — assume good faith, keep feedback focused on the code, and remember everyone here is contributing their free time.

---

Questions? Open a [discussion](../../discussions) or an issue tagged `question`.
