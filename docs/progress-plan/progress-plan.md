# Smart‑TV — Progress Plan (Living Document)

> Europe/Oslo. Deadline: October 31.
> If this replaces an earlier plan, this version consolidates tasks to hit the deadline, emphasizes broadcast events (SUB/UNSUB optional), and clarifies CLI acceptance (show channel + print async events).

## Assumptions
- Assignment requires: multi‑module Java 21 app with a TCP line protocol; PING → OK; async notifications on channel change (broadcast to all connections is sufficient; SUB/UNSUB optional).
- Robustness: trim/ignore blank lines, CRLF framing, 256 max line length → `ERR 400 LINE_TOO_LONG`.
- Client acceptance: shows current channel (on start via STATUS/GET; after each GET/SET/UP/DOWN) and prints incoming async events without crashing.

## Timeline to deadline (task clusters)

Each cluster lists: branch, objective, steps, acceptance, and quick verification.

### A) Client UX polish (today)
- Branch: `feat/client-ux-status`
- Objective: CLI shows current status/channel; keeps commands simple.
- Steps:
  - On startup, send `STATUS`; if `ON`, also send `GET` and print `CH=<n>`.
  - After each `GET/SET/UP/DOWN`, print the resulting channel from the server reply.
- Acceptance:
  - On start, CLI prints `OK OFF` or `OK ON` and channel if ON.
  - After `SET 3` / `UP` / `DOWN`, CLI prints `OK CH=<n>`.
- Verify:
  - Build: `mvn -q -DskipITs clean install`
  - Server: `mvn -q -f tv-server/pom.xml exec:java "-Dexec.args=--port 1238"`
  - Client: `mvn -q -f remote-client/pom.xml exec:java "-Dexec.args=127.0.0.1 1238"`

### B) Async notifications — broadcast (1–2 days)
- Branch: `feat/async-events-broadcast`
- Objective: After channel changes, emit `EVT CHANNEL <n>` to all connections.
- Steps:
  - Add broadcaster in server transport to track per‑connection writers.
  - After successful `SET/UP/DOWN`, publish `EVT CHANNEL <n>` to all clients.
  - Keep `SUB/UNSUB` optional (not required by brief).
- Acceptance:
  - Two clients connected; one does `SET 5`; both receive `EVT CHANNEL 5` (in addition to caller’s `OK CH=5`).
- Verify:
  - Manual: open two clients; observe `EVT CHANNEL <n>` on both.

### C) Multi‑client concurrency (1–2 days)
- Branch: `concurrency/thread-per-conn`
- Objective: Serve multiple clients concurrently.
- Steps:
  - Accept loop delegates each `Socket` to a worker thread; per‑client loop reads → handles → writes.
  - Ensure `SmartTv` (already synchronized) remains the single source of truth for channel state.
- Acceptance:
  - Two clients can operate without blocking each other.
- Verify:
  - Build: `mvn -q -DskipITs clean install`
  - Manual: client A spams `STATUS` while client B performs `SET/UP/DOWN`.

### D) Integration tests (1 day)
- Branch: `test/it-multiclient-notify`
- Objective: E2E proof for broadcast + robustness.
- Steps:
  - `it-tests`: start server on random free port; connect two sockets; verify broadcast on channel change.
  - Include tests for TV_OFF, OUT_OF_RANGE, and `ERR 400 LINE_TOO_LONG`.
- Acceptance:
  - `mvn -q verify` passes locally.
- Verify:
  - Integration build: `mvn -q verify`

### E) Docs & README (0.5 day)
- Branch: `docs/readme-quickstart`
- Objective: Quickstart and submission checklist are copy‑pasteable for Windows/macOS/Linux.
- Steps:
  - README: module‑targeted commands + OS notes + deadline reminder.
- Acceptance:
  - README renders on GitHub; commands run copy‑paste.
- Verify:
  - Manual run on your OS (Windows at minimum).

## Branching & PRs
- Naming: `type/concise-scope` (type ∈ {feat, fix, refactor, test, docs, chore})
  - `feat/async-events-broadcast`, `concurrency/thread-per-conn`, `test/it-multiclient-notify`, `docs/readme-quickstart`
- Each PR explicitly states out‑of‑scope to keep it small (e.g., broadcast PR is not responsible for client listener).

## Versioning & tags (upon merges to main)
- `v0.4.0-client-polish` — A: CLI shows status/channel; quickstart notes updated where needed.
- `v0.6.0-async-events` — B: broadcast `EVT CHANNEL <n>` after channel changes.
- `v0.7.0-multiclient` — C: thread‑per‑connection serving.
- `v0.8.0-it-tests` — D: E2E tests covering broadcasts + robustness.
- `v0.9.0-readme-docs` — E: README and docs final sweep.
- `v1.0.0` — Final delivery.

Definition of Done per tag:
- 0.4.0: CLI prints startup status/channel; build green.
- 0.6.0: Broadcast events verified manually with two clients.
- 0.7.0: Concurrent clients responsive.
- 0.8.0: `mvn -q verify` green on local machine.
- 0.9.0: Docs accurate; commands validated on Windows; macOS/Linux notes included.
- 1.0.0: All acceptance criteria below met.

## Definition of Done (project)
- Protocol behaviors match the assignment (PING → OK; events on channel change; robust parsing with CRLF and max length).
- Server handles multiple clients and broadcasts events.
- Client displays current channel and prints async events safely.
- Unit + integration tests green; module‑targeted commands work.
- Docs (architecture, protocol, README, this plan, weekly review) are consistent and copy‑pasteable.

## Risk register (top 5)
1) Concurrency errors
   - Mitigation: keep `SmartTv` synchronized; isolate per‑client IO; add smoke tests.
2) Slow/broken client during broadcast
   - Mitigation: publish on executor; handle write failures by dropping connection.
3) Flaky port binding in tests
   - Mitigation: pick a random free port; retry connect.
4) PowerShell quoting issues
   - Mitigation: use `"-Dexec.args=…"` or `--%`; script in `tools/` already helps.
5) Scope creep on optional SUB/UNSUB
   - Mitigation: defer; broadcast to all connections satisfies the brief.
