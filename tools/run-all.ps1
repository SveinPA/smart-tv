<#  tools/run-all.ps1
    Build + start server + start client (one shot).

    Usage examples:
      powershell -ExecutionPolicy Bypass -File tools/run-all.ps1
      powershell -ExecutionPolicy Bypass -File tools/run-all.ps1 -ServerHost 127.0.0.1 -Port 1238 -Rebuild
#>

param(
  [string]$ServerHost = "127.0.0.1",
  [int]$Port = 1238,
  [switch]$Rebuild
)

$ErrorActionPreference = "Stop"

# Go to repo root (script may be called from anywhere)
$ROOT = Split-Path -Parent $PSScriptRoot
Set-Location $ROOT

Write-Host "==> Maven build ($(if ($Rebuild) { 'clean install' } else { 'install' }))"
if ($Rebuild) {
  mvn -q -DskipITs clean install
} else {
  mvn -q -DskipITs install
}

# Start server in a background job
Write-Host "==> Starting server on port $Port ..."
$serverJob = Start-Job -Name smarttv_server -ScriptBlock {
  param($root, $port)
  Set-Location $root
  & mvn -q -f "tv-server/pom.xml" exec:java "-Dexec.args=--port $port"
} -ArgumentList $ROOT, $Port

# Wait until the port is listening (max ~10s)
$deadline = (Get-Date).AddSeconds(10)
$up = $false
while ((Get-Date) -lt $deadline) {
  $test = Test-NetConnection $ServerHost -Port $Port -WarningAction SilentlyContinue
  if ($test.TcpTestSucceeded) { $up = $true; break }
  Start-Sleep -Milliseconds 250
}
if (-not $up) {
  Write-Error "Server didn't open $($ServerHost):$Port in time. Check the job output (Receive-Job smarttv_server)."
  Stop-Job $serverJob -Force | Out-Null
  exit 1
}

# Run client in foreground (interactive)
Write-Host "==> Connecting client to $($ServerHost):$Port ..."
try {
  & mvn -q -f "remote-client/pom.xml" exec:java "-Dexec.args=$ServerHost $Port"
}
finally {
  Write-Host "==> Stopping server ..."
  if ($serverJob -and (Get-Job -Id $serverJob.Id -ErrorAction SilentlyContinue)) {
    Stop-Job $serverJob -Force | Out-Null
    Receive-Job $serverJob -Keep | Select-Object -Last 20 | Out-Host
    Remove-Job $serverJob | Out-Null
  }
  Write-Host "==> Done."
}

