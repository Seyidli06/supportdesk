$ErrorActionPreference = "Stop"

$baseUrl = "http://127.0.0.1:8081/api/v1"
$apiDocsUrl = "http://127.0.0.1:8081/v3/api-docs"
$password = "Password123!"
$suffix = [guid]::NewGuid().ToString("N").Substring(0, 8)

function Write-Step {
    param(
        [Parameter(Mandatory)]
        [string]$Message
    )

    Write-Host ""
    Write-Host "========================================" -ForegroundColor DarkCyan
    Write-Host $Message -ForegroundColor Cyan
    Write-Host "========================================" -ForegroundColor DarkCyan
}

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

    Write-Host "PASS: $Message" -ForegroundColor Green
}

function Invoke-Api {
    param(
        [Parameter(Mandatory)]
        [ValidateSet("GET", "POST", "PATCH", "PUT", "DELETE")]
        [string]$Method,

        [Parameter(Mandatory)]
        [string]$Url,

        [hashtable]$Headers = @{},

        [object]$Body = $null,

        [int[]]$ExpectedStatus = @(200)
    )

    $responseFile = [System.IO.Path]::GetTempFileName()
    $requestFile = $null

    try {
        $curlArguments = @(
            "--silent"
            "--show-error"
            "--noproxy"
            "*"
            "--max-time"
            "30"
            "--output"
            $responseFile
            "--write-out"
            "%{http_code}"
            "--request"
            $Method
        )

        foreach ($headerName in $Headers.Keys) {
            $headerValue = $Headers[$headerName]

            $curlArguments += @(
                "--header"
                "$headerName`: $headerValue"
            )
        }

        if ($null -ne $Body) {
            if ($Body -is [string]) {
                $jsonBody = $Body
            }
            else {
                $jsonBody = $Body |
                    ConvertTo-Json -Depth 20 -Compress
            }

            $requestFile = [System.IO.Path]::GetTempFileName()

            $utf8WithoutBom =
                New-Object System.Text.UTF8Encoding($false)

            [System.IO.File]::WriteAllText(
                $requestFile,
                $jsonBody,
                $utf8WithoutBom
            )

            $curlArguments += @(
                "--header"
                "Content-Type: application/json"
                "--data-binary"
                "@$requestFile"
            )
        }

        $curlArguments += $Url

        $statusOutput = & curl.exe @curlArguments
        $curlExitCode = $LASTEXITCODE

        $statusText = (
            $statusOutput |
                Out-String
        ).Trim()

        if (Test-Path $responseFile) {
            $responseText =
                [System.IO.File]::ReadAllText($responseFile)
        }
        else {
            $responseText = ""
        }

        if ($curlExitCode -ne 0) {
            throw @"
curl request failed.

Method: $Method
URL: $Url
curl exit code: $curlExitCode
Response: $responseText
"@
        }

        if ([string]::IsNullOrWhiteSpace($statusText)) {
            throw "HTTP status code was not returned for $Method $Url"
        }

        $statusCode = [int]$statusText
        $jsonResponse = $null

        if (-not [string]::IsNullOrWhiteSpace($responseText)) {
            try {
                $jsonResponse =
                    $responseText |
                    ConvertFrom-Json
            }
            catch {
                $jsonResponse = $null
            }
        }

        if ($ExpectedStatus -notcontains $statusCode) {
            throw @"
Unexpected HTTP response.

Method: $Method
URL: $Url
Expected: $($ExpectedStatus -join ", ")
Actual: $statusCode
Response:
$responseText
"@
        }

        return [pscustomobject]@{
            StatusCode = $statusCode
            Text       = $responseText
            Json       = $jsonResponse
        }
    }
    finally {
        if (
            $responseFile -and
            (Test-Path $responseFile)
        ) {
            Remove-Item $responseFile -Force
        }

        if (
            $requestFile -and
            (Test-Path $requestFile)
        ) {
            Remove-Item $requestFile -Force
        }
    }
}

try {
    Write-Step "1. APPLICATION CHECK"

    $healthResponse = Invoke-Api `
        -Method GET `
        -Url $apiDocsUrl `
        -ExpectedStatus @(200)

    Assert-True `
        ($healthResponse.StatusCode -eq 200) `
        "SupportDesk API is running on port 8081"


    Write-Step "2. REGISTER USER"

    $userEmail = "user.$suffix@supportdesk.local"

    $userRegisterResponse = Invoke-Api `
        -Method POST `
        -Url "$baseUrl/auth/register" `
        -Body @{
            email    = $userEmail
            password = $password
            fullName = "PowerShell Smoke User"
        } `
        -ExpectedStatus @(200, 201)

    $userAuth = $userRegisterResponse.Json

    Assert-True `
        ($null -ne $userAuth) `
        "User registration returned JSON"

    $userId = [string]$userAuth.userId
    $userToken = [string]$userAuth.accessToken

    Assert-True `
        (-not [string]::IsNullOrWhiteSpace($userId)) `
        "USER account was created"

    Assert-True `
        (-not [string]::IsNullOrWhiteSpace($userToken)) `
        "USER JWT was received"

    Assert-True `
        (@($userAuth.roles) -contains "USER") `
        "USER token contains USER role"

    $userHeaders = @{
        Authorization = "Bearer $userToken"
    }


    Write-Step "3. CREATE TICKET"

    $createTicketResponse = Invoke-Api `
        -Method POST `
        -Url "$baseUrl/tickets" `
        -Headers $userHeaders `
        -Body @{
            title       = "PowerShell smoke test ticket"
            description = "Ticket created by automated PowerShell smoke test."
            priority    = "HIGH"
        } `
        -ExpectedStatus @(200, 201)

    $ticket = $createTicketResponse.Json
    $ticketId = [string]$ticket.id

    Assert-True `
        (-not [string]::IsNullOrWhiteSpace($ticketId)) `
        "Ticket was created"

    Assert-True `
        ([string]$ticket.requesterId -eq $userId) `
        "Ticket requester is correct"

    Assert-True `
        ([string]$ticket.status -eq "OPEN") `
        "New ticket status is OPEN"

    Assert-True `
        ([string]$ticket.priority -eq "HIGH") `
        "Ticket priority is HIGH"


    Write-Step "4. USER TICKET LIST"

    $userListResponse = Invoke-Api `
        -Method GET `
        -Url "$baseUrl/tickets?page=0&size=20" `
        -Headers $userHeaders `
        -ExpectedStatus @(200)

    $userTicketPage = $userListResponse.Json

    $userTicketIds = @(
        $userTicketPage.content |
            ForEach-Object {
                [string]$_.id
            }
    )

    Assert-True `
        ($userTicketIds -contains $ticketId) `
        "Created ticket is visible in USER ticket list"


    Write-Step "5. TICKET DETAIL"

    $detailResponse = Invoke-Api `
        -Method GET `
        -Url "$baseUrl/tickets/$ticketId" `
        -Headers $userHeaders `
        -ExpectedStatus @(200)

    $ticketDetail = $detailResponse.Json

    Assert-True `
        ([string]$ticketDetail.id -eq $ticketId) `
        "Ticket detail was returned"

    Assert-True `
        ([string]$ticketDetail.title -eq "PowerShell smoke test ticket") `
        "Ticket detail title is correct"


    Write-Step "6. USER COMMENT"

    $userCommentText =
        "Comment added by USER from PowerShell smoke test."

    $userCommentResponse = Invoke-Api `
        -Method POST `
        -Url "$baseUrl/tickets/$ticketId/comments" `
        -Headers $userHeaders `
        -Body @{
            content = $userCommentText
        } `
        -ExpectedStatus @(200, 201)

    $userCommentTicket = $userCommentResponse.Json

    Assert-True `
        ($null -ne $userCommentTicket) `
        "Comment request returned JSON"

    $commentsAfterUser = @($userCommentTicket.comments)

    Assert-True `
        ($commentsAfterUser.Count -ge 1) `
        "USER comment is included in ticket response"

    $savedUserComment = @(
        $commentsAfterUser |
            Where-Object {
                $_.content -eq $userCommentText
            }
    )

    Assert-True `
        ($savedUserComment.Count -ge 1) `
        "USER comment content is correct"


    Write-Step "7. USER STATUS CHANGE MUST RETURN 403"

    $blockedStatusResponse = Invoke-Api `
        -Method PATCH `
        -Url "$baseUrl/tickets/$ticketId/status" `
        -Headers $userHeaders `
        -Body @{
            status = "IN_PROGRESS"
        } `
        -ExpectedStatus @(403)

    Assert-True `
        ($blockedStatusResponse.StatusCode -eq 403) `
        "USER cannot change ticket status"


    Write-Step "8. REGISTER AGENT AND ADMIN"

    $agentEmail = "agent.$suffix@supportdesk.local"
    $adminEmail = "admin.$suffix@supportdesk.local"

    $agentRegisterResponse = Invoke-Api `
        -Method POST `
        -Url "$baseUrl/auth/register" `
        -Body @{
            email    = $agentEmail
            password = $password
            fullName = "PowerShell Smoke Agent"
        } `
        -ExpectedStatus @(200, 201)

    $agentId = [string]$agentRegisterResponse.Json.userId

    $adminRegisterResponse = Invoke-Api `
        -Method POST `
        -Url "$baseUrl/auth/register" `
        -Body @{
            email    = $adminEmail
            password = $password
            fullName = "PowerShell Smoke Admin"
        } `
        -ExpectedStatus @(200, 201)

    $adminId = [string]$adminRegisterResponse.Json.userId

    Assert-True `
        (-not [string]::IsNullOrWhiteSpace($agentId)) `
        "Agent account was created"

    Assert-True `
        (-not [string]::IsNullOrWhiteSpace($adminId)) `
        "Admin account was created"


    Write-Step "9. BOOTSTRAP FIRST ADMIN"

    $containerRunning = (
        & docker inspect `
            --format "{{.State.Running}}" `
            supportdesk-db 2>$null |
            Out-String
    ).Trim()

    if (
        $LASTEXITCODE -ne 0 -or
        $containerRunning -ne "true"
    ) {
        throw "Docker container supportdesk-db is not running."
    }

    $adminRoleSql = @"
DELETE FROM user_roles
WHERE user_id = '$adminId';

INSERT INTO user_roles (user_id, role)
VALUES ('$adminId', 'ADMIN');
"@

    & docker exec `
        supportdesk-db `
        psql `
        -U postgres `
        -d supportdesk_db `
        -v ON_ERROR_STOP=1 `
        -c $adminRoleSql

    if ($LASTEXITCODE -ne 0) {
        throw "Initial ADMIN role could not be written to database."
    }

    Write-Host `
        "PASS: Initial administrator was bootstrapped in database" `
        -ForegroundColor Green


    Write-Step "10. LOGIN ADMIN AFTER ROLE CHANGE"

    $adminLoginResponse = Invoke-Api `
        -Method POST `
        -Url "$baseUrl/auth/login" `
        -Body @{
            email    = $adminEmail
            password = $password
        } `
        -ExpectedStatus @(200)

    $adminAuth = $adminLoginResponse.Json
    $adminToken = [string]$adminAuth.accessToken

    Assert-True `
        (-not [string]::IsNullOrWhiteSpace($adminToken)) `
        "ADMIN JWT was received"

    Assert-True `
        (@($adminAuth.roles) -contains "ADMIN") `
        "New ADMIN token contains ADMIN role"

    $adminHeaders = @{
        Authorization = "Bearer $adminToken"
    }


    Write-Step "11. ADMIN MANAGES AGENT ROLES THROUGH API"

    $agentBeforeResponse = Invoke-Api `
        -Method GET `
        -Url "$baseUrl/users/$agentId" `
        -Headers $adminHeaders `
        -ExpectedStatus @(200)

    $agentBefore = $agentBeforeResponse.Json

    Assert-True `
        (@($agentBefore.roles) -contains "USER") `
        "Newly registered agent initially has USER role"

    Assert-True `
        (
            $agentBefore.PSObject.Properties.Name `
                -notcontains "passwordHash"
        ) `
        "User management response does not expose passwordHash"

    $agentRoleUpdateResponse = Invoke-Api `
        -Method PATCH `
        -Url "$baseUrl/users/$agentId/roles" `
        -Headers $adminHeaders `
        -Body @{
            roles = @("AGENT")
        } `
        -ExpectedStatus @(200)

    $updatedAgent = $agentRoleUpdateResponse.Json

    Assert-True `
        (@($updatedAgent.roles) -contains "AGENT") `
        "ADMIN assigned AGENT role through API"

    Assert-True `
        (@($updatedAgent.roles) -notcontains "USER") `
        "Existing USER role was replaced rather than appended"

    $agentDetailResponse = Invoke-Api `
        -Method GET `
        -Url "$baseUrl/users/$agentId" `
        -Headers $adminHeaders `
        -ExpectedStatus @(200)

    $agentDetail = $agentDetailResponse.Json

    Assert-True `
        (@($agentDetail.roles) -contains "AGENT") `
        "Updated AGENT role was persisted"

    $encodedAgentEmail = [System.Uri]::EscapeDataString(
        $agentEmail
    )

    $agentSearchResponse = Invoke-Api `
        -Method GET `
        -Url "$baseUrl/users?role=AGENT&email=$encodedAgentEmail&page=0&size=20" `
        -Headers $adminHeaders `
        -ExpectedStatus @(200)

    $agentSearchIds = @(
        $agentSearchResponse.Json.content |
            ForEach-Object {
                [string]$_.id
            }
    )

    Assert-True `
        ($agentSearchIds -contains $agentId) `
        "Agent appears in filtered ADMIN user list"


    Write-Step "12. LOGIN AGENT AFTER ROLE CHANGE"

    $agentLoginResponse = Invoke-Api `
        -Method POST `
        -Url "$baseUrl/auth/login" `
        -Body @{
            email    = $agentEmail
            password = $password
        } `
        -ExpectedStatus @(200)

    $agentAuth = $agentLoginResponse.Json
    $agentToken = [string]$agentAuth.accessToken

    Assert-True `
        (-not [string]::IsNullOrWhiteSpace($agentToken)) `
        "AGENT JWT was received"

    Assert-True `
        (@($agentAuth.roles) -contains "AGENT") `
        "New AGENT token contains AGENT role"

    Assert-True `
        (@($agentAuth.roles) -notcontains "USER") `
        "New AGENT token no longer contains USER role"

    $agentHeaders = @{
        Authorization = "Bearer $agentToken"
    }

    Write-Step "13. ADMIN ASSIGNS TICKET TO AGENT"

    $assignmentResponse = Invoke-Api `
        -Method PATCH `
        -Url "$baseUrl/tickets/$ticketId/assignment" `
        -Headers $adminHeaders `
        -Body @{
            agentId = $agentId
        } `
        -ExpectedStatus @(200)

    $assignedTicket = $assignmentResponse.Json

    Assert-True `
        ([string]$assignedTicket.assignedAgentId -eq $agentId) `
        "Admin assigned ticket to agent"


    Write-Step "14. AGENT TICKET LIST"

    $agentListResponse = Invoke-Api `
        -Method GET `
        -Url "$baseUrl/tickets?page=0&size=20" `
        -Headers $agentHeaders `
        -ExpectedStatus @(200)

    $agentTicketIds = @(
        $agentListResponse.Json.content |
            ForEach-Object {
                [string]$_.id
            }
    )

    Assert-True `
        ($agentTicketIds -contains $ticketId) `
        "Assigned ticket is visible in AGENT list"


    Write-Step "15. AGENT CHANGES STATUS"

    $statusResponse = Invoke-Api `
        -Method PATCH `
        -Url "$baseUrl/tickets/$ticketId/status" `
        -Headers $agentHeaders `
        -Body @{
            status = "IN_PROGRESS"
        } `
        -ExpectedStatus @(200)

    $inProgressTicket = $statusResponse.Json

    Assert-True `
        ([string]$inProgressTicket.status -eq "IN_PROGRESS") `
        "Agent changed status to IN_PROGRESS"


    Write-Step "16. AGENT COMMENT"

    $agentCommentText =
        "Comment added by assigned AGENT from PowerShell smoke test."

    $agentCommentResponse = Invoke-Api `
        -Method POST `
        -Url "$baseUrl/tickets/$ticketId/comments" `
        -Headers $agentHeaders `
        -Body @{
            content = $agentCommentText
        } `
        -ExpectedStatus @(200, 201)

    $commentsAfterAgent =
        @($agentCommentResponse.Json.comments)

    $savedAgentComment = @(
        $commentsAfterAgent |
            Where-Object {
                $_.content -eq $agentCommentText
            }
    )

    Assert-True `
        ($commentsAfterAgent.Count -ge 2) `
        "USER and AGENT comments are returned"

    Assert-True `
        ($savedAgentComment.Count -ge 1) `
        "AGENT comment content is correct"


    Write-Step "17. ADMIN TICKET LIST"

    $adminListResponse = Invoke-Api `
        -Method GET `
        -Url "$baseUrl/tickets?page=0&size=100" `
        -Headers $adminHeaders `
        -ExpectedStatus @(200)

    $adminTicketIds = @(
        $adminListResponse.Json.content |
            ForEach-Object {
                [string]$_.id
            }
    )

    Assert-True `
        ($adminTicketIds -contains $ticketId) `
        "Ticket is visible in ADMIN list"


    Write-Step "18. FINAL VERIFICATION"

    $finalResponse = Invoke-Api `
        -Method GET `
        -Url "$baseUrl/tickets/$ticketId" `
        -Headers $userHeaders `
        -ExpectedStatus @(200)

    $finalTicket = $finalResponse.Json

    Assert-True `
        ([string]$finalTicket.status -eq "IN_PROGRESS") `
        "Final ticket status is IN_PROGRESS"

    Assert-True `
        ([string]$finalTicket.assignedAgentId -eq $agentId) `
        "Final agent assignment is correct"

    $finalComments = @($finalTicket.comments)

    Assert-True `
        ($finalComments.Count -ge 2) `
        "Final ticket contains at least two comments"


    Write-Host ""
    Write-Host "============================================" `
        -ForegroundColor Green

    Write-Host "ALL SUPPORTDESK SMOKE TESTS PASSED" `
        -ForegroundColor Green

    Write-Host "============================================" `
        -ForegroundColor Green

    Write-Host ""
    Write-Host "USER EMAIL:  $userEmail"
    Write-Host "AGENT EMAIL: $agentEmail"
    Write-Host "ADMIN EMAIL: $adminEmail"
    Write-Host "PASSWORD:    $password"
    Write-Host "TICKET ID:   $ticketId"
}
catch {
    Write-Host ""
    Write-Host "============================================" `
        -ForegroundColor Red

    Write-Host "SMOKE TEST FAILED" `
        -ForegroundColor Red

    Write-Host "============================================" `
        -ForegroundColor Red

    Write-Host ""
    Write-Host $_.Exception.Message `
        -ForegroundColor Red

    exit 1
}