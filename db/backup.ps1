# =====================================================================
#  POBY DB 백업 (Windows / PowerShell)
# ---------------------------------------------------------------------
#  이 앱의 데이터는 다시 만들 수 없다.
#  진도·관찰·장면은 그 수업이 끝난 뒤에는 아무도 기억으로 복원하지 못한다.
#  그래서 백업은 "있으면 좋은 것"이 아니라 기능의 일부다.
#
#  비밀번호를 스크립트에 박지 않는다. 저장소에 올라가는 파일이다.
#  프로젝트 루트의 .env(git 추적 제외)에서 읽는다.
#
#  실행:
#    powershell -ExecutionPolicy Bypass -File db\backup.ps1
#    powershell -ExecutionPolicy Bypass -File db\backup.ps1 -KeepDays 60
#    powershell -ExecutionPolicy Bypass -File db\backup.ps1 -OutDir D:\poby-backups
#
#  ★ 덤프에는 학생 실명·학교·메모·장면·상담이 전부 평문으로 들어 있다.
#    그래서 기본 출력 경로를 저장소 바깥(%LOCALAPPDATA%\poby-house\backups)으로 둔다.
#    예전에는 db\backups 였는데, 이 저장소가 OneDrive 동기화 폴더 안에 있어서
#    덤프가 만들어지는 즉시 클라우드와 그 계정에 연결된 모든 기기로 올라갔다.
#    .gitignore 의 /db/backups 는 git 추적만 막고 동기화는 막지 못한다.
#    아래 Assert-NotSynced 가 동기화 폴더로 지정하는 것도 막는다.
# =====================================================================

param(
    [int]$KeepDays = 30,
    [string]$OutDir
)

$ErrorActionPreference = 'Stop'

$root    = Split-Path -Parent $PSScriptRoot
$envFile = Join-Path $root '.env'

# 기본 출력 경로는 저장소 바깥의 미동기화 위치다.
# LOCALAPPDATA 는 OneDrive 의 "알려진 폴더 이동" 대상이 아니라 문서·바탕화면과 달리 올라가지 않는다.
if ([string]::IsNullOrWhiteSpace($OutDir)) {
    $base = if ($env:LOCALAPPDATA) { $env:LOCALAPPDATA } else { $env:TEMP }
    $OutDir = Join-Path $base 'poby-house\backups'
}

# 주석에만 적어 둔 경고는 같은 기본값을 말리지 못했다. 그래서 검사해서 멈춘다.
# 실명이 든 덤프가 동기화 폴더에 떨어지면 삭제한 뒤에도 클라우드 휴지통과 버전 기록에 남는다.
function Assert-NotSynced([string]$path) {
    $full = [System.IO.Path]::GetFullPath($path)
    foreach ($bad in @('OneDrive', 'Dropbox', 'Google Drive', 'GoogleDrive', 'iCloudDrive', 'Yandex.Disk')) {
        if ($full -like "*\$bad\*" -or $full -like "*\$bad") {
            Write-Error @"
백업 경로가 동기화 폴더 안입니다: $full
학생 실명이 든 덤프가 클라우드로 올라가고, 지운 뒤에도 휴지통·버전 기록에 남습니다.
-OutDir 로 동기화되지 않는 경로를 지정하세요. 예: -OutDir D:\poby-backups
"@
        }
    }
}
Assert-NotSynced $OutDir

if (-not (Test-Path $envFile)) {
    Write-Error ".env 를 찾을 수 없습니다: $envFile  (DB_URL / DB_PORT / DB_USERNAME / DB_PASSWORD 가 필요합니다)"
}

# .env 는 KEY=VALUE 한 줄씩. 주석(#)과 빈 줄은 건너뛴다.
# 값에 = 가 들어갈 수 있으므로 첫 번째 = 에서만 자른다
$conf = @{}
foreach ($line in Get-Content -LiteralPath $envFile) {
    $t = $line.Trim()
    if ($t -eq '' -or $t.StartsWith('#')) { continue }
    $i = $t.IndexOf('=')
    if ($i -lt 1) { continue }
    $conf[$t.Substring(0, $i).Trim()] = $t.Substring($i + 1).Trim()
}

$dbHost = if ($conf.ContainsKey('DB_URL'))      { $conf['DB_URL'] }      else { '127.0.0.1' }
$dbPort = if ($conf.ContainsKey('DB_PORT'))     { $conf['DB_PORT'] }     else { '3306' }
$dbUser = if ($conf.ContainsKey('DB_USERNAME')) { $conf['DB_USERNAME'] } else { 'root' }
$dbName = if ($conf.ContainsKey('DB_NAME'))     { $conf['DB_NAME'] }     else { 'POBY' }
$dbPass = if ($conf.ContainsKey('DB_PASSWORD')) { $conf['DB_PASSWORD'] } else { '' }

if (-not (Get-Command mysqldump -ErrorAction SilentlyContinue)) {
    Write-Host 'mysqldump 를 PATH 에서 찾지 못했습니다.'
    Write-Host 'MySQL 을 compose 컨테이너로 띄워 쓰는 중이라면 README 의 docker compose exec 예시를 쓰세요.'
    exit 1
}

if (-not (Test-Path $OutDir)) {
    New-Item -ItemType Directory -Path $OutDir -Force | Out-Null
}

$stamp = Get-Date -Format 'yyyyMMdd-HHmmss'
$sql   = Join-Path $OutDir "$dbName-$stamp.sql"
$zip   = Join-Path $OutDir "$dbName-$stamp.zip"

# 비밀번호를 -p 인자로 넘기지 않는다. 명령행 인자는 작업 관리자의 프로세스 목록에 보인다.
# MYSQL_PWD 는 이 프로세스에만 붙고 끝나면 사라진다.
$env:MYSQL_PWD = $dbPass
try {
    # --single-transaction: 덤프 도중 수업 기록이 들어와도 한 시점의 일관된 상태를 뜬다 (InnoDB 라 락을 걸지 않는다)
    # --result-file: PowerShell 리다이렉션을 거치지 않는다. 5.1 의 Out-File 은 BOM 과 CRLF 를 섞어
    #                넣어서 나중에 mysql 로 되돌릴 때 첫 문장이 깨진다
    & mysqldump `
        --host=$dbHost `
        --port=$dbPort `
        --user=$dbUser `
        --single-transaction `
        --routines `
        --events `
        --default-character-set=utf8mb4 `
        --result-file="$sql" `
        $dbName
    if ($LASTEXITCODE -ne 0) {
        # 반쯤 쓰인 덤프를 남기지 않는다.
        # 깨진 파일을 "백업이 있다" 고 믿는 것이 백업이 없는 것보다 나쁘다
        if (Test-Path -LiteralPath $sql) { Remove-Item -LiteralPath $sql -Force }
        Write-Error "mysqldump 가 실패했습니다 (exit $LASTEXITCODE). 덤프를 지웠습니다."
    }
}
finally {
    Remove-Item Env:\MYSQL_PWD -ErrorAction SilentlyContinue
}

# 압축한다. backup.sh 는 gzip -9 를 쓰는데 이쪽만 평문 .sql 을 쌓고 있었다.
# 텍스트 덤프는 10분의 1 아래로 줄고, 실명이 든 파일이 미리보기·검색 색인에 그대로 걸리는 것도 줄어든다.
Compress-Archive -LiteralPath $sql -DestinationPath $zip -CompressionLevel Optimal -Force
Remove-Item -LiteralPath $sql -Force

$sizeKb = [math]::Round((Get-Item -LiteralPath $zip).Length / 1KB, 1)
Write-Host "백업 완료: $zip ($sizeKb KB)"

# 오래된 덤프 정리. 실명이 든 파일을 무한히 쌓아 두는 것도 위험이다
$cutoff  = (Get-Date).AddDays(-$KeepDays)
$expired = Get-ChildItem -LiteralPath $OutDir -Filter "$dbName-*.zip" |
           Where-Object { $_.LastWriteTime -lt $cutoff }
foreach ($f in $expired) {
    Remove-Item -LiteralPath $f.FullName -Force
    Write-Host "오래된 덤프 삭제: $($f.Name)"
}
Write-Host "보관 정책: $KeepDays 일"
