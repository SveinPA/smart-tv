param(
  [string]$ServerHost = "127.0.0.1",
  [int]$Port = 1238,
  [switch]$Rebuild
)

$ErrorActionPreference = "Stop"
$ROOT = (Split-Path -Parent $PSScriptRoot)
Set-Location -LiteralPath $ROOT

# Build first (optional clean)
if ($Rebuild) { mvn -q -DskipITs clean install } else { mvn -q -DskipITs install }

# Ensure Windows Terminal exists
if (-not (Get-Command wt.exe -ErrorAction SilentlyContinue)) {
  Write-Error "Windows Terminal (wt.exe) not found. Install it from Microsoft Store or use run-two-windows.ps1."
  exit 1
}

# Build the inner PowerShell commands that each pane should run
# Use format strings to keep quoting correct with spaces in $ROOT
$serverCmd = ("Set-Location -LiteralPath '{0}'; " +
              "mvn -q -f tv-server/pom.xml exec:java ""-Dexec.args=--port {1}""" ) -f $ROOT, $Port

$clientCmd = ("Set-Location -LiteralPath '{0}'; " +
              "mvn -q -f remote-client/pom.xml exec:java ""-Dexec.args={1} {2}""" ) -f $ROOT, $ServerHost, $Port

# Build the full 'wt' argument list as tokens (so ';' is passed to wt, not PowerShell)
$wtArgs = @(
  'new-tab','--title','TV Server',
  'powershell.exe','-NoLogo','-NoExit','-Command', $serverCmd,
  ';',
  'split-pane','-H','--title','Remote Client',
  'powershell.exe','-NoLogo','-NoExit','-Command', $clientCmd
)

# Launch a single Windows Terminal window with two panes
& wt @wtArgs
