# Smart‑TV — Weekly Review (ISO Week 43)

> Student‑friendly review based on the attached assignment PDF and the current repository (Europe/Oslo).

## Executive summary

The core layering and basic protocol flow are solid: domain/logic/protocol are cleanly separated, server transport is simple and robust, and unit tests cover logic and parsing well. What’s missing for the assignment’s later part is multi‑client handling and async notifications after channel changes. The client CLI also needs a tiny UX boost to show the current channel and print events without crashing. Next steps: implement broadcast‑style events to all connected clients, enable thread‑per‑connection on the server, add a background reader in the CLI, and write a few end‑to‑end integration tests.

---

## Compliance matrix (requirements → evidence → status → fix)

| Requirement | Evidence (module/file/lines) | Status | Concrete fix suggestion |
|---|---|---|---|
| Build with Java 21 (multi‑module) | `pom.xml` (Java 21 via `<maven.compiler.release>`), modules `common`, `tv-server`, `remote-client`, `it-tests` | Met | — |
| Layered architecture: logic ↔ protocol ↔ transport | `common/*` (entity/logic/protocol), `tv-server/adapter+transport`, `remote-client/*` | Met | — |
| Commands: STATUS/ON/OFF/CHANNELS/GET/SET/UP/DOWN | Adapter `tv-server/.../ProtocolHandler.handleLine` | Met | — |
| PING semantics | `ProtocolHandler.handlePing` → `OK` | Met | Keep `PING → OK` (no PONG) |
| Errors: TV_OFF/OUT_OF_RANGE/INVALID_STATE/SERVER_ERROR | Mapping in `ProtocolHandler` + helpers in `common.protocol.Codec` | Met | — |
| Robustness: trim/ignore blanks, CRLF, max line length | `tv-server/.../TcpServer.serve` (trim + ignore empty), `Codec` appends CRLF, length check → `ERR 400 LINE_TOO_LONG` | Met | Keep `ERR 400 LINE_TOO_LONG` rule |
| Multi‑client + async notifications on channel change (Part 3) | Current server serves one client; no events broadcast | Missing | Add broadcaster and emit `EVT CHANNEL <n>` to all connected clients after `SET/UP/DOWN`. `SUB/UNSUB` optional |
| SUB/UNSUB optional | Not implemented; currently 400 | Met (optional) | Leave optional; broadcast to all connections is sufficient |
| Transport easy to swap (TCP→UDP) | Logic & protocol are IO‑free; adapter is pure mapping | Met | New UDP transport reading datagrams → call `ProtocolHandler` → reply; no changes to logic/protocol |
| CLI displays current channel & prints async events | `remote-client/ui/CliUi.java` prints replies; no startup `STATUS`, no event listener | Partial | On startup, call `STATUS` (and `GET` if ON). Add background reader to print incoming `EVT` lines |
| Integration tests (it-tests) | Module exists; no tests yet | Missing | Add E2E: two clients, broadcast on `SET`/`UP`/`DOWN`, robustness cases |

Notes
- `Codec.evtChannel(int)` currently returns `EVT CHANNEL<n>` (missing space). Add a space when implementing events.

---

## Architecture & layering
- Domain/entity: `common.entity.TvState`
- Business logic (IO‑free): `common.logic.SmartTv` (synchronized methods)
- Protocol: `common.protocol.(Command, Request, Limits, Codec)`
- Server adapter: `tv-server.adapter.ProtocolHandler` (parsing → logic → formatting)
- Server transport: `tv-server.transport.TcpServer` (read/write CRLF, logging)
- Client: `remote-client.transport.TcpClient`, `remote-client.ui.CliUi`, `remote-client.app.RemoteClientApp`
- Swapability: To support UDP, implement a UDP transport that receives a datagram, uses `ProtocolHandler` to compute the reply, and sends a datagram back; keep logic/protocol unchanged.

---

## Functionality vs spec
- Case‑insensitive commands; arguments validated for SET.
- PING returns `OK` (keep; PDF doesn’t prescribe PING).
- Events not yet emitted; Part 3 requires async notifications on channel change. `SUB/UNSUB` are optional sugar: broadcasting to all connections is sufficient to meet the brief.
- Error mapping aligns with the spec and tests; CRLF framing is respected.

---

## Robustness & error handling
- Trims and ignores blank lines (server side).
- Max line length: 256; longer lines → `ERR 400 LINE_TOO_LONG` and connection stays open.
- Bad syntax → `ERR 400 BAD_COMMAND` (adapter maps parse errors consistently).
- Unexpected exceptions → `ERR 500 SERVER_ERROR` (server logs detail, client gets generic message).

---

## Testing summary and high‑value missing tests
Present
- common: entity/logic/protocol unit tests (bounds, OFF guards, parsing/formatting, limits)
- tv-server: adapter mapping tests; basic socket test (STATUS, long line)

High‑value missing tests
- remote-client: TcpClient send/receive loopback smoke test
- it-tests: multi‑client broadcast and robustness

Example (JUnit sketch, E2E broadcast):
```java
@Test
void broadcastsChannelChangeToMultipleClients() throws Exception {
  int port = pickFreePort();
  startServerInBackground(port); // real server
  try (Socket a = connect(port);
       Socket b = connect(port);
       var inA = reader(a); var outA = writer(a);
       var inB = reader(b); var outB = writer(b)) {
    send(outA, "ON"); assertEquals("OK", inA.readLine());
    send(outB, "STATUS"); assertTrue(inB.readLine().startsWith("OK "));
    send(outA, "SET 5"); assertEquals("OK CH=5", inA.readLine());
    // Expect async event on both (broadcast)
    assertEquals("EVT CHANNEL 5", inA.readLine());
    assertEquals("EVT CHANNEL 5", inB.readLine());
  }
}
```

---

## Build & run ergonomics (module‑targeted; auto‑checked)
- Build (unit tests):
```powershell
mvn -q -DskipITs clean install
```
- Server:
```powershell
mvn -q -f tv-server/pom.xml exec:java "-Dexec.args=--port 1238"
```
- Client:
```powershell
mvn -q -f remote-client/pom.xml exec:java "-Dexec.args=127.0.0.1 1238"
```
If PowerShell quoting is problematic, use stop‑parsing `--%`.

---

## (Optional) PR plan — small, focused changes
- `feat/async-events-broadcast`
  - Scope: broadcaster; emit `EVT CHANNEL <n>` after `SET/UP/DOWN`.
  - Out‑of‑scope: SUB/UNSUB, client event listener.
  - Verify: two clients receive `EVT CHANNEL <n>`.
- `concurrency/thread-per-conn`
  - Scope: per‑connection worker thread and per‑client handler loop.
  - Out‑of‑scope: rate limiting.
  - Verify: two clients operate concurrently.
- `feat/client-event-listener`
  - Scope: client background reader prints `EVT`; startup `STATUS` then `GET`.
  - Out‑of‑scope: advanced parsing/UI.
  - Verify: client prints channel and events.
- `test/it-multiclient-notify`
  - Scope: E2E broadcast and robustness tests under `it-tests`.
  - Verify: `mvn -q verify`.

---

## Risks & mitigations
- Concurrency bugs when adding multi‑client → keep `SmartTv` synchronized; small, focused tests.
- Slow/broken client during broadcast → publish via executor; drop broken connections.
- Flaky integration tests on port binding → use random free port + short retry.

---

## Before submit checklist (deadline Oct 31)
- [ ] Repo public (or zip ready) with clear structure.
- [ ] README quickstart with module‑targeted commands (Windows/macOS/Linux notes).
- [ ] Verified run on your OS (document which).
- [ ] Tags aligned with milestones (see progress plan) and pushed.
- [ ] Final build: `mvn -q verify` green.
