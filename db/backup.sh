#!/usr/bin/env bash
# =====================================================================
#  POBY DB 백업 (서버 / Linux)
# ---------------------------------------------------------------------
#  이 앱의 데이터는 다시 만들 수 없다.
#  진도·관찰·장면은 그 수업이 끝난 뒤에는 아무도 기억으로 복원하지 못한다.
#  그래서 백업은 "있으면 좋은 것"이 아니라 기능의 일부다.
#
#  비밀번호를 스크립트에 박지 않는다. 저장소에 올라가는 파일이다.
#  프로젝트 루트의 .env(git 추적 제외)에서 읽는다.
#
#  실행:
#    bash db/backup.sh
#    KEEP_DAYS=60 bash db/backup.sh
#    OUT_DIR=/srv/poby-backups bash db/backup.sh
#    (cron 예시는 README 참고)
#
#  ★ 덤프에는 학생 실명·학교·메모·장면·상담이 전부 들어 있다.
#    그래서 기본 출력 경로를 저장소 바깥(/var/backups/poby)으로 둔다.
#    예전 기본값은 db/backups 였다. 저장소가 동기화 폴더(OneDrive 등) 안에 있으면
#    덤프가 만들어지는 즉시 클라우드로 올라가고, .gitignore 는 그것을 막지 못한다.
#    아래 검사가 동기화 폴더를 출력 경로로 지정하는 것도 막는다.
# =====================================================================

set -euo pipefail

script_dir="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
root_dir="$(dirname "$script_dir")"
env_file="$root_dir/.env"
out_dir="${OUT_DIR:-/var/backups/poby}"
keep_days="${KEEP_DAYS:-30}"

# 주석에만 적어 둔 경고는 잘못된 기본값을 말리지 못했다. 그래서 검사해서 멈춘다.
# 동기화 폴더에 떨어진 덤프는 지운 뒤에도 클라우드 휴지통과 버전 기록에 남는다.
case "$out_dir" in
  *OneDrive*|*Dropbox*|*"Google Drive"*|*GoogleDrive*|*iCloud*)
    echo "백업 경로가 동기화 폴더 안입니다: $out_dir" >&2
    echo "학생 실명이 든 덤프가 클라우드로 올라갑니다. OUT_DIR 로 다른 경로를 지정하세요." >&2
    exit 1
    ;;
esac

if [ ! -f "$env_file" ]; then
  echo ".env 를 찾을 수 없습니다: $env_file (DB_URL / DB_PORT / DB_USERNAME / DB_PASSWORD 가 필요합니다)" >&2
  exit 1
fi

# .env 를 source 하지 않는다.
# source 는 값 안의 공백·따옴표·$ 를 셸이 해석해 버리고, 파일에 명령이 섞이면 그대로 실행된다.
# KEY=VALUE 만 뽑아 읽는다. 값에 = 가 들어갈 수 있으므로 첫 번째 = 에서만 자른다.
read_env() {
  local key="$1"
  local line
  line="$(grep -m1 -E "^[[:space:]]*${key}[[:space:]]*=" "$env_file" || true)"
  [ -z "$line" ] && return 0
  printf '%s' "${line#*=}" | sed -e 's/^[[:space:]]*//' -e 's/[[:space:]]*$//'
}

db_host="$(read_env DB_URL)";      db_host="${db_host:-127.0.0.1}"
db_port="$(read_env DB_PORT)";     db_port="${db_port:-3306}"
db_user="$(read_env DB_USERNAME)"; db_user="${db_user:-root}"
db_name="$(read_env DB_NAME)";     db_name="${db_name:-POBY}"
db_pass="$(read_env DB_PASSWORD)"

if ! command -v mysqldump >/dev/null 2>&1; then
  echo "mysqldump 가 없습니다. MySQL 이 컨테이너 안에 있으면 README 의 docker compose exec 예시를 쓰세요." >&2
  exit 1
fi

# 만들 수 없으면 여기서 멈춘다. 권한이 없어 실패한 것을 백업이 돈 것으로 착각하면 안 된다
if ! mkdir -p "$out_dir" 2>/dev/null; then
  echo "백업 폴더를 만들 수 없습니다: $out_dir" >&2
  echo "권한을 주거나 OUT_DIR 로 쓸 수 있는 경로를 지정하세요. (저장소 안은 쓰지 마세요)" >&2
  exit 1
fi
# 덤프에는 실명이 들어 있다. 같은 서버의 다른 계정이 읽지 못하게 막는다
chmod 700 "$out_dir" 2>/dev/null || true

stamp="$(date +%Y%m%d-%H%M%S)"
out="$out_dir/${db_name}-${stamp}.sql.gz"

# 비밀번호를 -p 인자로 넘기지 않는다. 명령행 인자는 ps 로 다 보인다.
# MYSQL_PWD 는 이 명령에만 붙는다.
# --single-transaction: 덤프 도중 수업 기록이 들어와도 한 시점의 일관된 상태를 뜬다.
#                      InnoDB 라 이 옵션이 테이블 락을 걸지 않는다.
#
# 파이프 중간이 실패해도 gz 파일은 남는다. 그래서 실패하면 지운다.
# 깨진 덤프를 "백업이 있다" 고 믿는 것이 백업이 없는 것보다 나쁘다.
if ! MYSQL_PWD="$db_pass" mysqldump \
      --host="$db_host" \
      --port="$db_port" \
      --user="$db_user" \
      --single-transaction \
      --routines \
      --events \
      --default-character-set=utf8mb4 \
      "$db_name" | gzip -9 > "$out"; then
  rm -f "$out"
  echo "mysqldump 실패. 덤프를 지웠습니다." >&2
  exit 1
fi

echo "백업 완료: $out ($(du -h "$out" | cut -f1))"

# 오래된 덤프 정리. 실명이 든 파일을 무한히 쌓아 두는 것도 위험이다
find "$out_dir" -maxdepth 1 -type f -name "${db_name}-*.sql.gz" -mtime "+${keep_days}" -print -delete
echo "보관 정책: ${keep_days}일"
