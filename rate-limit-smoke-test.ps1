$ErrorActionPreference = "Stop"

$loginUrl =
    "http://127.0.0.1:8081/api/v1/auth/login"

function Assert-True {
    param(
        [Parameter(Mandatory)]
        [bool]$Condition,

        [Parameter(Mandatory)]
        [string]$Message
    )

    if (-not $Condition) {
        throw "FAIL: $Message"
    }

    Write-Host "PASS: $Message" `
        -ForegroundColor Green
}

function Get-HeaderValue {
    param(
        [Parameter(Mandatory)]
        [string[]]$HeaderLines,

        [Parameter(Mandatory)]
        [string]$HeaderName
    )

    $pattern =
        "^$([regex]::Escape($HeaderName)):\s*(.+?)\s*$"

    $matches = @(
        $HeaderLines |
            Where-Object {
                $_ -match $pattern
            }
    )

    if ($matches.Count -eq 0) {
        return $null
    }

    $lastMatch = $matches[-1]

    [void](
        $lastMatch -match $pattern
    )

    return $Matches[1].Trim()
}

function Invoke-LoginAttempt {
    $headerFile =
        [System.IO.Path]::GetTempFileName()

    $bodyFile =
        [System.IO.Path]::GetTempFileName()

    $requestFile =
        [System.IO.Path]::GetTempFileName()

    try {
        $requestBody = @{
            email = "rate.limit@example.com"
            password = ""
        } |
            ConvertTo-Json -Compress

        [System.IO.File]::WriteAllText(
            $requestFile,
            $requestBody,
            [System.Text.UTF8Encoding]::new($false)
        )

        $arguments = @(
            "--silent"
            "--show-error"
            "--noproxy"
            "*"
            "--max-time"
            "30"
            "--dump-header"
            $headerFile
            "--output"
            $bodyFile
            "--write-out"
            "%{http_code}"
            "--request"
            "POST"
            "--header"
            "Content-Type: application/json"
            "--data-binary"
            "@$requestFile"
            $loginUrl
        )

        $statusOutput =
            & curl.exe @arguments

        $curlExitCode = $LASTEXITCODE

        if ($curlExitCode -ne 0) {
            throw "curl failed with exit code $curlExitCode"
        }

        $statusCode = [int](
            (
                $statusOutput |
                    Out-String
            ).Trim()
        )

        $headerLines = @(
            [System.IO.File]::ReadAllLines(
                $headerFile
            ) |
                Where-Object {
                    -not [string]::IsNullOrWhiteSpace(
                        $_
                    )
                }
        )

        if ($headerLines.Count -eq 0) {
            throw "HTTP response header-ləri alınmadı."
        }

        $bodyText =
            [System.IO.File]::ReadAllText(
                $bodyFile
            )

        $jsonBody = $null

        if (
            -not [string]::IsNullOrWhiteSpace(
                $bodyText
            )
        ) {
            try {
                $jsonBody =
                    $bodyText |
                    ConvertFrom-Json
            }
            catch {
                $jsonBody = $null
            }
        }

        return [pscustomobject]@{
            StatusCode = $statusCode

            Limit = Get-HeaderValue `
                -HeaderLines $headerLines `
                -HeaderName "X-RateLimit-Limit"

            Remaining = Get-HeaderValue `
                -HeaderLines $headerLines `
                -HeaderName "X-RateLimit-Remaining"

            RetryAfter = Get-HeaderValue `
                -HeaderLines $headerLines `
                -HeaderName "Retry-After"

            ContentType = Get-HeaderValue `
                -HeaderLines $headerLines `
                -HeaderName "Content-Type"

            Body = $jsonBody
            Text = $bodyText
        }
    }
    finally {
        @(
            $headerFile
            $bodyFile
            $requestFile
        ) |
            ForEach-Object {
                if (
                    $_ -and
                    (Test-Path $_)
                ) {
                    Remove-Item $_ -Force
                }
            }
    }
}

Write-Host ""
Write-Host "========================================" `
    -ForegroundColor DarkCyan
Write-Host "RATE LIMIT SMOKE TEST" `
    -ForegroundColor Cyan
Write-Host "========================================" `
    -ForegroundColor DarkCyan

$first = Invoke-LoginAttempt

Assert-True `
    ($first.StatusCode -eq 400) `
    "First request passed rate limit and returned validation 400"

Assert-True `
    ($first.Limit -eq "2") `
    "First response contains limit 2"

Assert-True `
    ($first.Remaining -eq "1") `
    "First response contains 1 remaining token"

$second = Invoke-LoginAttempt

Assert-True `
    ($second.StatusCode -eq 400) `
    "Second request passed rate limit and returned validation 400"

Assert-True `
    ($second.Limit -eq "2") `
    "Second response contains limit 2"

Assert-True `
    ($second.Remaining -eq "0") `
    "Second response exhausted the bucket"

$third = Invoke-LoginAttempt

Assert-True `
    ($third.StatusCode -eq 429) `
    "Third request was rejected with HTTP 429"

Assert-True `
    ($third.Limit -eq "2") `
    "Rejected response contains limit 2"

Assert-True `
    ($third.Remaining -eq "0") `
    "Rejected response contains 0 remaining tokens"

Assert-True `
    (
        -not [string]::IsNullOrWhiteSpace(
            $third.RetryAfter
        )
    ) `
    "Rejected response contains Retry-After"

Assert-True `
    (
        [long]$third.RetryAfter -gt 0
    ) `
    "Retry-After is positive"

Assert-True `
    (
        $third.ContentType -like
            "application/problem+json*"
    ) `
    "Rejected response uses Problem Details content type"

Assert-True `
    ($null -ne $third.Body) `
    "Rejected response contains JSON"

Assert-True `
    (
        [string]$third.Body.title -eq
            "Too Many Requests"
    ) `
    "Problem title is correct"

Assert-True `
    (
        [int]$third.Body.status -eq 429
    ) `
    "Problem status is correct"

Assert-True `
    (
        [string]$third.Body.code -eq
            "rate-limit-exceeded"
    ) `
    "Problem code is correct"

Assert-True `
    (
        [string]$third.Body.instance -eq
            "/api/v1/auth/login"
    ) `
    "Problem instance is correct"

Write-Host ""
Write-Host "RATE LIMIT SMOKE TEST PASSED" `
    -ForegroundColor Green