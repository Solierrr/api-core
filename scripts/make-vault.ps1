param(
    [Parameter(Mandatory = $true)]
    [ValidateSet('config', 'auth', 'extract-env')]
    [string] $Action,

    [string] $ScriptsDir,
    [string] $Repo,
    [string] $ExtractEnvPath,
    [string] $Service,
    [string] $Environment,
    [string] $OutputPath
)

$ErrorActionPreference = 'Stop'

function Invoke-Git {
    param([Parameter(Mandatory = $true)][string[]] $GitArguments)

    & git @GitArguments
    if ($LASTEXITCODE -ne 0) {
        throw "git $($GitArguments -join ' ') failed with exit code $LASTEXITCODE."
    }
}

switch ($Action) {
    'config' {
        if ([string]::IsNullOrWhiteSpace($ScriptsDir)) {
            throw 'ScriptsDir is empty. Set USERPROFILE or pass ORG_SCRIPTS_DIR.'
        }
        if ([string]::IsNullOrWhiteSpace($Repo)) {
            throw 'Repo is empty. Set ORG_SCRIPTS_REPO.'
        }

        $ScriptsDir = [System.IO.Path]::GetFullPath($ScriptsDir)
        $GitMetadata = Join-Path $ScriptsDir '.git'

        if (Test-Path -LiteralPath $GitMetadata) {
            Write-Host "infra-scripts found at $ScriptsDir, updating..."
            Invoke-Git -GitArguments @('-C', $ScriptsDir, 'pull', '--ff-only')
        }
        elseif (Test-Path -LiteralPath $ScriptsDir) {
            throw "$ScriptsDir exists but is not a git clone. Remove or rename it, then run 'make vault-config' again."
        }
        else {
            $ParentDir = Split-Path -Parent $ScriptsDir
            if (-not (Test-Path -LiteralPath $ParentDir)) {
                New-Item -ItemType Directory -Path $ParentDir -Force | Out-Null
            }

            Write-Host "infra-scripts not found, cloning into $ScriptsDir..."
            Invoke-Git -GitArguments @('clone', $Repo, $ScriptsDir)
        }

        if (-not (Test-Path -LiteralPath $ExtractEnvPath -PathType Leaf)) {
            throw "infra-scripts is ready, but $ExtractEnvPath is missing. Check whether extract-env.ps1 moved or was renamed."
        }

        Write-Host "OK: infra-scripts ready at $ScriptsDir"
    }

    'auth' {
        $InfisicalCommand = Get-Command infisical.exe -ErrorAction SilentlyContinue
        if (-not $InfisicalCommand) {
            $InfisicalCommand = Get-Command infisical -ErrorAction SilentlyContinue
        }
        if (-not $InfisicalCommand) {
            throw "Infisical CLI not installed. Install it (https://infisical.com/docs/cli/overview), then run 'make vault-auth' again."
        }

        $null = & $InfisicalCommand.Source user get token --silent 2>$null
        if ($LASTEXITCODE -ne 0) {
            throw "No active Infisical session. Run 'infisical login', then retry 'make extract-env'."
        }

        Write-Host 'OK: Infisical authenticated.'
    }

    'extract-env' {
        if (-not (Test-Path -LiteralPath $ExtractEnvPath -PathType Leaf)) {
            throw "Cannot find extract-env.ps1 at $ExtractEnvPath. Run 'make vault-config' first."
        }

        $ExtractArguments = @{}
        if (-not [string]::IsNullOrWhiteSpace($Service)) {
            $ExtractArguments['Service'] = $Service
        }
        if (-not [string]::IsNullOrWhiteSpace($Environment)) {
            if ($Environment -notin @('local', 'qa', 'prod')) {
                throw "Invalid ENV '$Environment'. Use local, qa or prod (example: make extract-env ENV=qa)."
            }
            $ExtractArguments['Environment'] = $Environment
        }
        if (-not [string]::IsNullOrWhiteSpace($OutputPath)) {
            $ExtractArguments['OutputPath'] = $OutputPath
        }

        & $ExtractEnvPath @ExtractArguments
        if (-not $?) {
            exit 1
        }
        if ($LASTEXITCODE -and $LASTEXITCODE -ne 0) {
            exit $LASTEXITCODE
        }
    }
}
