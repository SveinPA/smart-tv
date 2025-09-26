# Commands to run application

# Run Smart-TV (build, server, client) — step-by-step (PowerShell)

> Run these from the **repo root**. Open **two terminals** (one for server, one for client).

---

## Run Powershell script (./tools/run-all.ps1)
```powershell
powershell -ExecutionPolicy Bypass -File tools/run-all.ps1
# or with options
powershell -ExecutionPolicy Bypass -File tools/run-all.ps1 -ServerHost 127.0.0.1 -Port 1238 -Rebuild
```

## windows terminal
powershell -ExecutionPolicy Bypass -File tools/run-wt.ps1

## Two-seperate windows
```powershell
powershell -ExecutionPolicy Bypass -File tools/run-two-windows.ps1
# or with options
powershell -ExecutionPolicy Bypass -File tools/run-two-windows.ps1 -ServerHost 127.0.0.1 -Port 1238 -Rebuild
```

# Manual excecution steps to run server/client

## 0) Build everything
```powershell
mvn -q -DskipITs clean install
```

## 1) Start the TV server (Terminal A)
# Recommended
mvn -q -f tv-server/pom.xml exec:java "-Dexec.args=--port 1238"

# If PowerShell ever mangles the args, use stop-parsing:
mvn --% -q -f tv-server/pom.xml exec:java -Dexec.args=--port 1238

# This should appear in the teminal
[TvServerApp] Starting on port 1238
[TcpServer] Listening on port 1238...

## Start remote client (Terminal B)
mvn -q -f remote-client/pom.xml exec:java "-Dexec.args=127.0.0.1 1238"

# This should appear in the terminal
smarttv>
