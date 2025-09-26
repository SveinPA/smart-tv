<# Open two external PowerShell consoles:
   - Window 1: TV server
   - Waits until port is listening
   - Window 2: Remote client (interactive)
#>

param(
  [string]$ServerHost = "127.0.0.1",
  [int]$Port = 1238,
  [switch]$Rebuild,
  [int]$WaitSeconds = 30
)

$ErrorActionPreference = "Stop"

# Go to repo root (script may be called from anywhere)
$ROOT = Split-Path -Parent $PSScriptRoot
Set-Location -LiteralPath $ROOT

Write-Host "==> Maven build ($(if ($Rebuild) { 'clean install' } else { 'install' }))"
if ($Rebuild) { mvn -q -DskipITs clean install } else { mvn -q -DskipITs install }

# ----- Start Server window -----
$serverCmd = "Set-Location -LiteralPath '$ROOT'; " +
             "mvn -q -f tv-server/pom.xml exec:java -Dexec.args=\"--port $Port\""
$serverArgs = @('-NoLogo','-NoExit','-Command', $serverCmd)

Write-Host "==> Launching server window on port $Port ..."
Start-Process -FilePath "powershell.exe" -ArgumentList $serverArgs -WindowStyle Normal

# ----- Wait until server is listening (max configurable) -----
$deadline = (Get-Date).AddSeconds($WaitSeconds)
do {
  $ok = (Test-NetConnection $ServerHost -Port $Port -WarningAction SilentlyContinue).TcpTestSucceeded
  if (-not $ok) { Start-Sleep -Milliseconds 250 }
} until ($ok -or (Get-Date) -ge $deadline)
if (-not $ok) {
  Write-Error "Server didn't open $($ServerHost):$Port in time (waited $WaitSeconds s). Opened server window, but cannot start client."
  exit 1
}

# ----- Start Client window -----
# Quote the entire host/port argument pair so mvn exec sees it as one logical string
$clientCmd = "Set-Location -LiteralPath '$ROOT'; " +
             "mvn -q -f remote-client/pom.xml exec:java -Dexec.args=\"$ServerHost $Port\""
$clientArgs = @('-NoLogo','-NoExit','-Command', $clientCmd)

Write-Host "==> Launching client window (connecting to $($ServerHost):$Port) ..."
Start-Process -FilePath "powershell.exe" -ArgumentList $clientArgs -WindowStyle Normal

Write-Host "==> Done. Close each window when finished."

