<#
.SYNOPSIS
  Dumps the Railway Postgres database into a timestamped file under ./backups.

.DESCRIPTION
  Uses pg_dump in compressed custom format (-Fc), so it can be restored with pg_restore.
  The connection string is NOT stored in this file. It is taken from, in order:
    1. the -Url parameter,
    2. the DATABASE_PUBLIC_URL environment variable,
    3. a hidden prompt.
  Use the PUBLIC url of the Railway Postgres service (Variables / Connect tab), not the internal one.

.EXAMPLE
  .\scripts\backup-db.ps1
  .\scripts\backup-db.ps1 -Keep 14        # afterwards delete all but the 14 newest backups
  .\scripts\backup-db.ps1 -Url "postgresql://postgres:PASSWORD@HOST.proxy.rlwy.net:PORT/railway"
#>
param(
    [string]$Url,
    [string]$OutDir = (Join-Path $PSScriptRoot '..\backups'),
    [int]$Keep = 0   # 0 = never delete anything
)

$ErrorActionPreference = 'Stop'

# --- connection string ---------------------------------------------------------------------
if (-not $Url) { $Url = $env:DATABASE_PUBLIC_URL }
if (-not $Url) {
    $secure = Read-Host 'Paste the Railway DATABASE_PUBLIC_URL' -AsSecureString
    $Url = [System.Net.NetworkCredential]::new('', $secure).Password
}
if ($Url -notmatch '^postgres(ql)?://') {
    throw 'That does not look like a postgresql:// connection string.'
}

# --- find pg_dump / pg_restore -------------------------------------------------------------
function Find-PgTool([string]$name) {
    $cmd = Get-Command $name -ErrorAction SilentlyContinue
    if ($cmd) { return $cmd.Source }
    $candidates = Get-ChildItem 'C:\Program Files\PostgreSQL\*\bin' -Directory -ErrorAction SilentlyContinue |
        Sort-Object Name -Descending |
        ForEach-Object { Join-Path $_.FullName "$name.exe" } |
        Where-Object { Test-Path $_ }
    if ($candidates) { return @($candidates)[0] }
    throw "$name not found. Install the PostgreSQL client tools and/or add their bin folder to PATH."
}
$pgDump = Find-PgTool 'pg_dump'
$pgRestore = Find-PgTool 'pg_restore'

# --- dump ----------------------------------------------------------------------------------
New-Item -ItemType Directory -Force -Path $OutDir | Out-Null
$stamp = Get-Date -Format 'yyyy-MM-dd_HH-mm-ss'
$file = Join-Path $OutDir "gigatester_$stamp.dump"

Write-Host "Dumping database to $file ..."
& $pgDump --format=custom --no-owner --file=$file $Url
if ($LASTEXITCODE -ne 0) {
    Remove-Item $file -ErrorAction SilentlyContinue
    throw "pg_dump failed (exit code $LASTEXITCODE). Is it the public URL, and is your pg_dump at least as new as the server?"
}

# --- sanity check: the file must be readable as a dump ------------------------------------
& $pgRestore --list $file *> $null
if ($LASTEXITCODE -ne 0) { throw "The dump was written but pg_restore cannot read it: $file" }

$sizeKb = [math]::Round((Get-Item $file).Length / 1KB, 1)
Write-Host "OK - $sizeKb KB written." -ForegroundColor Green

# --- optional rotation ---------------------------------------------------------------------
if ($Keep -gt 0) {
    $old = Get-ChildItem $OutDir -Filter 'gigatester_*.dump' | Sort-Object LastWriteTime -Descending | Select-Object -Skip $Keep
    foreach ($f in $old) {
        Write-Host "Removing old backup $($f.Name)"
        Remove-Item $f.FullName
    }
}

Write-Host ''
Write-Host 'Restore into an EMPTY database with:'
Write-Host "  pg_restore --no-owner --clean --if-exists -d <target-url> `"$file`""
