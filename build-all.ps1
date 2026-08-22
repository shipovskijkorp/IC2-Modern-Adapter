$ErrorActionPreference = "Stop"

$root = $PSScriptRoot
$configPath = Join-Path $root "build.properties"
$out = Join-Path $root "build\libs"

if (-not (Test-Path $configPath)) {
    throw "Missing shared build configuration: $configPath"
}

$config = @{}
Get-Content $configPath | ForEach-Object {
    $line = $_.Trim()
    if ($line.Length -eq 0 -or $line.StartsWith("#")) {
        return
    }

    $parts = $line.Split("=", 2)
    if ($parts.Length -eq 2) {
        $config[$parts[0].Trim()] = $parts[1].Trim()
    }
}

$familiesValue = $config["sourceFamilies"]
if ([string]::IsNullOrWhiteSpace($familiesValue)) {
    throw "Missing sourceFamilies in $configPath"
}

$families = $familiesValue.Split(",") | ForEach-Object { $_.Trim() } | Where-Object { $_ }

if (Test-Path $out) {
    Remove-Item $out -Recurse -Force
}
New-Item $out -ItemType Directory -Force | Out-Null

foreach ($family in $families) {
    $familyDir = Join-Path $root $family
    if (-not (Test-Path $familyDir)) {
        throw "Configured source family does not exist: $familyDir"
    }

    Write-Host "==> Building $family family"
    Push-Location $familyDir
    try {
        & .\gradlew.bat buildAndCollect --no-daemon
        if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }
    } finally {
        Pop-Location
    }
}

foreach ($family in $families) {
    $familyOut = Join-Path (Join-Path $root $family) "build\libs"
    if (Test-Path $familyOut) {
        Get-ChildItem $familyOut -Filter "*.jar" -File | Copy-Item -Destination $out -Force
    }
}

Write-Host "==> Collected artifacts:"
Get-ChildItem $out -Filter "*.jar" -File | Sort-Object Name | ForEach-Object { Write-Host "    $($_.Name)" }
