# Checklist — feat/skeleton-status

## Scope (what this branch delivers)
- End-to-end `STATUS` works: remote asks, server replies with power state.
- Minimal wiring across all layers; no channels yet.

## Criteria
- From the client, sending `STATUS` returns `OK OFF` (fresh server) or `OK ON` (if later turned on).
- Works via Maven exec commands (server + client).
- No crashes on unknown lines; extra whitespace is ignored.
- Unit tests for logic + protocol parsing are green.

## Tasks

### 0) Git
- [x] Create branch: `git checkout -b feat/skeleton-status`

### 1) Common (IO-free)
- [ ] `common/entity/TvState` with field: `boolean on` (default **false**)
- [ ] `common/logic/SmartTv`
  - [ ] constructor initializes `TvState` (OFF)
  - [ ] `boolean isOn()`
  - [ ] (prep for later) `void turnOn()`, `void turnOff()` — empty/straightforward
- [ ] `common/protocol`
  - [ ] `Command` enum with `STATUS`
  - [ ] `Message` (optional for now; simple if you want)
  - [ ] `Codec`:
        - [ ] parse a line → `Command.STATUS` (case-insensitive, trim spaces)
        - [ ] format reply for status: `OK ON\r\n` or `OK OFF\r\n`
        - [ ] reject unknown commands with `ERR 400 BAD_COMMAND\r\n`

### 2) TV server
- [ ] `transport/TcpServer` (single-client skeleton)
  - [ ] listen on port (default **1238**)
  - [ ] accept **one** connection; read lines (UTF-8), write replies
- [ ] `adapter/ProtocolHandler`
  - [ ] on `STATUS` → call `SmartTv.isOn()` → return `OK ON/OFF`
  - [ ] on unknown/empty → `ERR 400 BAD_COMMAND`
- [ ] `app/TvServerApp` bootstraps: create `SmartTv`, start `TcpServer`
- [ ] Logging: print connect/disconnect + received lines (simple `System.out.println`)

### 3) Remote client
- [ ] `transport/TcpClient`:
  - [ ] connect to `<host> <port>`
  - [ ] send one line, read one line (blocking is fine)
- [ ] `ui/CliUi` (very small):
  - [ ] if started with args `<host> <port>`, send `STATUS\r\n`, print the reply as text
- [ ] `app/RemoteClientApp` wires it

### 4) Docs
- [ ] Update `docs/protocol.md` (already added) — ensure `STATUS` example is present
- [ ] In `README`, add a tiny “smoke test” section for `STATUS`

### 5) Tests
- [ ] **Unit (common/logic):**
  - [ ] new `SmartTv` starts **OFF**
  - [ ] `isOn()` returns false initially
- [ ] **Unit (protocol/codec):**
  - [ ] `"STATUS"` → `Command.STATUS` (also `" status  "` with spaces/mixed case)
  - [ ] format reply: ON/OFF
  - [ ] unknown command → `ERR 400 BAD_COMMAND`
- [ ] **(Optional) Integration:** start server on random port, client sends `STATUS`, receives `OK OFF`

### 6) Run / verify
- [ ] Build: `mvn -q -DskipITs clean package`
- [ ] Server: `mvn -q -pl tv-server exec:java "-Dexec.args=--port 1238"`
- [ ] Client (new terminal): `mvn -q -pl remote-client exec:java "-Dexec.args=localhost 1238"`
- [ ] Confirm output equals `OK OFF` on fresh start

### 7) PR
- [ ] Commit clean history (no .idea/target, etc.)
- [ ] Open PR → `develop` with title: `feat: skeleton STATUS E2E`
- [ ] PR description: scope, how to run, what’s tested
- [ ] CI/build passes (if you set it up)

## Out of scope (not in this branch)
- Channels (`GET/SET/UP/DOWN`, `CHANNELS`)
- Events/broadcast (`SUB/EVT`)
- Multi-client / threading
- Fancy CLI/UX

## Pitfalls / tips
- Handle trimming & case-insensitive parsing of `STATUS`.
- Always end server replies with `\r\n`.
- If PowerShell swallows args, quote exec args like `"-Dexec.args=localhost 1238"`.
