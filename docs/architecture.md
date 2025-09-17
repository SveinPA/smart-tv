# Smart-TV — Architecture

**Course:** IDATA2304  
**Package base:** `edu.ntnu.sveiap.idata2304.smarttv`  
**Java:** 21  
**Modules:** `common`, `tv-server`, `remote-client`, `it-tests`  
**Protocol spec:** see `docs/protocol.md`  
**Default port:** `1238`

---

## 1) Context & Scope
A small teaching project to practice sockets and protocol design.

- TV Server (TCP) exposes TV actions.
- Remote Client (TCP) connects to one TV and issues commands.
- Common contains domain, business logic, and protocol (IO-free).
- Goal: Part 1 simple E2E; Part 2 refactor + tests; Part 3 multi-client + async events.

Out of scope (v1): persistence across server restarts, authentication, full GUI.

---

## 2) Requirements (summary)

### Functional
- TV is OFF by default; only ON is allowed when OFF.
- When ON: report channel count `C`, get current channel, set channel in `[1..C]`.
- Client shows current channel and can toggle power.
- (Part 3) Multiple clients simultaneously; channel changes are pushed to all clients asynchronously (no polling).

### Non-functional
- Simple, human-testable protocol (telnet/netcat).
- Clean layering: logic independent of protocol/transport.
- Easy to change/extend protocol commands.
- Simple to swap TCP ↔ UDP later (logic unaffected).
- Unit tests for logic and protocol; integration tests for server↔client.

---

## 3) High-level Architecture

See `docs/smartTV_components_diagram.plantuml`

Layer roles:
- entity: plain domain objects (no IO)
- logic: business rules for TV (no sockets)
- protocol: commands, responses, codec (no sockets)
- transport: TCP concerns (sockets, threads)
- adapter: maps protocol messages ⇆ logic methods
- ui/app: entrypoints (CLI args, default config)

---

## 4) Modules & Dependencies

- `tv-server` → depends on `common`
- `remote-client` → depends on `common`
- `common` → no module dependencies
- `it-tests` → depends on all of the above (test scope)

Structure overview:

    common
    ├─ entity/
    ├─ logic/
    └─ protocol/

    tv-server
    ├─ app/
    ├─ adapter/
    ├─ transport/
    └─ broadcast/

    remote-client
    ├─ app/
    ├─ ui/
    ├─ adapter/
    └─ transport/

---

## 5) Domain Model (entity)

`TvState`
- `boolean on`
- `int channels`  (C ≥ 1)
- `int currentChannel`  (1 ≤ currentChannel ≤ channels)

Invariants
- When turning ON: set `currentChannel` to last used (default 1 on first ever start).
- When OFF: state is retained in memory; not persisted across server restarts.

(Optional) `Channel` value object if you later add metadata.

---

## 6) Business Logic (logic)

`SmartTv` (IO-free)
- `turnOn()`, `turnOff()`
- `getNumberOfChannels() : int`
- `getChannel() : int`
- `setChannel(int n)` with validation `[1..C]`

Thread safety (Part 3):
- Guard mutating methods (synchronized/lock).
- Reads should observe latest state.

---

## 7) Protocol Integration (protocol)

- Line-based UTF-8 over TCP; one message per `\r\n`.
- Requests: `<COMMAND> [ARGS]`
- Responses: `OK <payload?>` or `ERR <code> <message>`
- Events: `EVT <type> <payload?>` (server-initiated, after `SUB`)

Core commands (v1): `ON`, `OFF`, `STATUS`, `CHANNELS`, `GET`, `SET <n>`, `UP`, `DOWN`, `SUB`, `UNSUB`, `PING`.

Error codes: `400 BAD_COMMAND`, `401 TV_OFF`, `404 OUT_OF_RANGE`, `409 INVALID_STATE`, `500 SERVER_ERROR`.

(Full details and examples live in `docs/protocol.md`.)

---

## 8) Server Components (tv-server)

- `app/TvServerApp`  
  Parses `--port` (default 1238), wires dependencies, starts TCP listener.

- `transport/TcpServer`  
  Accept loop; per-connection handler (thread-per-connection or executor).

- `adapter/ProtocolHandler`  
  Parses incoming line → protocol model → invokes `SmartTv`; formats response lines.

- `broadcast/Broadcaster`  
  Thread-safe list of subscriber outputs; pushes `EVT CHANNEL <n>` on changes.

Concurrency model (Part 3):
- Thread-per-connection or cached thread pool.
- Back-pressure via socket timeouts or bounded queues.
- Critical sections in `SmartTv` around mutations.

---

## 9) Client Components (remote-client)

- `app/RemoteClientApp`  
  Args: `<host> <port>`.

- `transport/TcpClient`  
  Connects, sends lines, reads lines. Background reader processes async `EVT`.

- `adapter/ClientProtocolDriver`  
  Wraps sending commands and parsing `OK/ERR/EVT`.

- `ui/CliUi`  
  Minimal interactive CLI (optional in v1); shows current channel and status.

---

## 10) State Machine (TV)

    OFF  --ON-->  ON
     ON  --OFF--> OFF

Allowed commands by state
- OFF: `ON`, `STATUS`, `SUB`, `UNSUB`, `PING`
- ON:  `OFF`, `STATUS`, `CHANNELS`, `GET`, `SET`, `UP`, `DOWN`, `SUB`, `UNSUB`, `PING`

---

## 11) Error Handling & Validation

- Validate command syntax and argument ranges.
- Normalize to protocol error codes (no stack traces to clients).
- Trim/ignore extra whitespace; ignore empty lines.
- Max line length: reject with `400 BAD_COMMAND` if exceeded.
- Logging (server):
  - INFO: start/stop, connect/disconnect, channel changes
  - WARN: validation errors, malformed messages
  - ERROR: unexpected exceptions

---

## 12) Configuration

- Server: `--port <int>` (default `1238`), optional `application.properties` for defaults.
- Client: `<host> <port>`.
- Keep config minimal (teaching focus).

---

## 13) Testing Strategy

Unit (common)
- logic: default OFF, turnOn/turnOff flows, setChannel valid/invalid, invariants.
- protocol: parse/format of commands, errors, robustness against bad input.

Module tests
- server adapter: command → state change mapping (can be tested without real sockets using fakes).

Integration (`it-tests`)
- Boot server on random free port; run one or more clients:
  - Happy path ON/GET/SET
  - TV_OFF errors
  - OUT_OF_RANGE errors
  - Multi-client: `SUB`; client A `SET 5` ⇒ client B receives `EVT CHANNEL 5`

Manual smoke (README)
- Copy-paste commands for quick verification.

---

## 14) Delivery & Build

- Multi-module Maven build.
- Run server: `mvn -q -pl tv-server exec:java -Dexec.args="--port 1238"`
- Run client: `mvn -q -pl remote-client exec:java -Dexec.args="localhost 1238"`
- Integration tests: `mvn -q verify`
- Deliver GitHub repo link (public) or zip.

---

## 15) Security Notes (basic)

- No auth in v1; don’t expose publicly.
- Treat incoming text as untrusted; apply strict validation.
- Consider resource limits (max clients, timeouts).

---

## 16) Evolution & Extension Points

- UDP transport with same `logic`/`protocol` (new transport adapter).
- GUI client (JavaFX) replacing `ui/CliUi`.
- Configurable channels (e.g., load at startup).
- Keep-alive (`PING`) and idle timeouts.

---

## 17) Decision Log (mini ADRs)

| Topic                | Decision                         | Rationale                                                 |
|----------------------|----------------------------------|-----------------------------------------------------------|
| Transport            | TCP, line-based UTF-8            | Easiest for manual testing; trivial framing with `\r\n`.  |
| Concurrency (server) | Thread-per-connection (pool)     | Simple mental model; adequate for class-sized loads.      |
| Events               | Push via broadcaster + `SUB/EVT` | Asynchronous notifications without client polling.        |
| Layering             | IO-free `logic` and `protocol`   | Unit testable; supports swapping transport later.         |

---

## 18) Glossary

- EVT: Server-initiated event line (e.g., `EVT CHANNEL <n>`).
- SUB/UNSUB: Subscribe/unsubscribe to events on the current connection.
- Adapter: Glue translating protocol messages to logic calls (and back).
