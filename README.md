# Poby House

학원 강사의 **내 기록 도구**. 최종 목적물은 **퇴원생 리포트** 하나다.

이 앱이 담는 모든 기록은 그 리포트의 재료다.
"이 아이가 우리와 함께한 동안 무엇이 달라졌는가"를 근거를 들어 말하려면
기억이 아니라 그날그날 남긴 기록이 필요하다. 그래서 이 앱이 있다.

원본 설계는 `db/schema.sql` 머리말이다. 이 README 는 그 요약이고,
설계 판단이 갈릴 때는 스키마 쪽이 맞다.

---

## 1. 무엇을 하고, 무엇을 하지 않는가

**한다**

- 학생 · 반 · 반 배정 이력
- 수업 회차와 출결
- 기록 3단 (아래)
- 평가와 오답 유형 태그
- 학부모 상담일지 (사실 / 걱정 / 약속 세 칸으로 분리)

**하지 않는다** (일부러 뺐다. 새 컬럼으로 몰래 들이지 않는다)

- **보호자 연락처** — 학원 명의로 다뤄야 하는 정보다. 원장 합의 전까지 수집하지 않는다.
- **알림톡 / 문자 발송** — 같은 이유. 발송 주체가 개인이 될 수 없다.
- **가정 사정 · 형제 비교 · 경제 상황** — 리포트에 쓸 수 없는 정보다.
  적어 두면 언젠가 새어 나가고, 그때 얻는 것은 없다.
- 결제 · 수납 · 교재 재고

원본 시험지 같은 학원 자산도 반출하지 않는다. `Assessment.sourceLocation` 에
**어디에 있는지만** 적는다.

### 기록 3단 (설계의 핵심)

| 주기 | 무엇 | 걸리는 시간 | 리포트에서 |
|---|---|---|---|
| 매 수업 | `ProgressLog` 진도 | 30초 | 무엇을 얼마나 했는가 |
| 1~4주 | `Observation` 이해도·태도·자기평가 | 5분 | before / after 비교 |
| 분기 | `Scene` 장면, `Intervention` 개입 로그 | 3분 | **리포트의 심장** |

`Scene` 과 `Intervention` 이 이 앱의 이유다. 시험지에는 남지 않는 데이터다.
리포트 4-1 편지와 3-1 "통한 것 / 안 통한 것" 은 이 두 테이블 없이는 쓸 수 없다.

`Observation` 중 `kind=BASELINE` 은 입회 시점 스냅샷이다.
**학생을 등록하면 곧바로 기준선 화면으로 넘어간다.** 그때 안 남기면
나중에 무엇이 달라졌는지 증명할 근거가 영구히 없다.

### 개인정보를 다루는 방식

- 외부로 나가는 문서에는 실명 대신 **식별번호**(`S2026001`)를 쓴다. 한 번 발급하면 바뀌지 않는다.
- 학생 실명을 **로그에 남기지 않는다.** log4jdbc 가 바인딩 값을 SQL 에 인라인하므로
  배포 프로파일에서 해당 로거를 끈다(`application-staging.yml`).
- 기록은 지우지 않는다. 종료일과 숨김 플래그로 남긴다
  (`Enrollment.endedOn`, `ClassSession.hiddenAt`).
- 퇴원생의 개인 정보는 **퇴원일 + 3년**까지만 보관한다(`Student.purgeAfter`).
  그 뒤에는 원장 화면에서 **익명화**한다 → 5절.

---

## 2. 로컬 실행

필요한 것: JDK 17, Docker Desktop.

### (1) `.env` 를 만든다

프로젝트 루트에 `.env`. **git 추적 대상이 아니다**(`.gitignore` 에 들어 있고,
`git ls-files .env` 가 비어 있는 것을 확인했다). 커밋하지 마라.

```properties
DB_URL=localhost
DB_PORT=3306
DB_USERNAME=root
DB_PASSWORD=1234

# 자동 로그인 쿠키 서명 키. 없으면 개발용 기본값이 쓰인다.
# 바꾸면 기존 자동 로그인 쿠키가 전부 무효가 된다
REMEMBER_ME_KEY=아무-긴-문자열
```

`compose.yml` 의 기본값과 맞춘 것이다(`MYSQL_ROOT_PASSWORD=1234`, `MYSQL_DATABASE=POBY`).

> 지금 `.env` 에 `AWS_*` 와 `BIZTALK_*` 가 남아 있으면 지워도 된다.
> 이 앱은 읽지 않는다. 알림톡은 수집·발송하지 않기로 한 영역이고,
> 쓰지 않는 자격증명을 파일에 두는 것 자체가 위험이다.

### (2) DB 를 띄우고 스키마를 넣는다

```bash
docker compose up -d
```

**새 DB** — `db/schema.sql` 한 번만 넣으면 된다. 최신 컬럼이 다 들어 있다.

```bash
docker compose exec -T my-db mysql -uroot -p1234 POBY < db/schema.sql
```

**이미 쓰고 있던 DB** — `schema.sql` 을 다시 넣지 말고 마이그레이션만 돌린다.
번호 순서대로, 여러 번 돌려도 안전하게 써 두었다.

```bash
docker compose exec -T my-db mysql -uroot -p1234 POBY < db/migration/20260818-01-records.sql
```

`ddl-auto: none` 이다. **엔티티가 테이블을 만들지 않는다.**
컬럼이 필요하면 `db/schema.sql` 을 고치고 `db/migration/` 에 ALTER 파일을 남긴다.

### (3) 앱을 띄운다

```bash
./gradlew bootRun
```

Windows 라면 `run-dev.bat` 을 두 번 눌러도 된다(MySQL 컨테이너까지 함께 띄운다).

`http://localhost:8080` → 계정이 하나도 없으면 `/setup` 으로 보내 원장 계정을 만들게 한다.

---

## 3. 배포

- GitHub Actions(`.github/workflows/poby-house-staging.yaml`)가 `main` 푸시에 돌면서
  빌드 → 도커 이미지 푸시 → 서버에서 컨테이너 교체까지 한다.
- 서버는 **`/poby` 하위 경로**로 뜬다. `CONTEXT_PATH=/poby` 환경변수 하나로 정해진다.
  - 앱이 접두어를 직접 갖는다. nginx 가 접두어를 떼어 주는 방식은 쓰지 않는다.
    그러면 서버가 렌더링한 주소가 죄다 접두어 없이 나가서 화면이 깨진다.
  - **그래서 템플릿의 모든 경로는 `th:href="@{/...}"` 로 쓴다.**
    문자열로 `/students` 를 박으면 배포 환경에서만 깨진다.
- `SPRING_PROFILES_ACTIVE=staging` 이 템플릿 캐시를 켜고 SQL 로그를 끈다.
- 컨테이너에 넘기는 것: `DB_URL DB_PORT DB_USERNAME DB_PASSWORD REMEMBER_ME_KEY CONTEXT_PATH`.
- 세션 타임아웃은 8시간이다. 기본 30분은 "수업 끝나고 폰으로 기록하는" 도중에 끊긴다.
- **첫 계정은 서버에서 `/setup` 으로 만든다.** `db/schema.sql` 은 계정을 심지 않는다.
  Teacher 행이 하나라도 있으면 `/setup` 이 영구히 닫히고, 유일한 계정이 저장소에 적힌 값이 된다.
  `db/seed/local-teacher.sql` 은 로컬 전용이다. 서버에서 돌리지 마라.

### nginx 접근 로그에서 쿼리스트링을 뺀다 (저장소 밖 설정)

학생 검색은 `GET /poby/students?keyword=...` 다. 주소를 공유·재방문할 수 있어야 해서 GET 을 유지하는데,
nginx 기본 `combined` 포맷은 요청 라인 전체(쿼리스트링 포함)를 남기므로 **학생 실명이 평문 로그로 쌓인다.**
이 앱은 나머지 PII 로그 통로를 다 닫아 두었다(서비스·컨트롤러에 `log.*` 호출 0건,
staging 에서 `jdbc.sqlonly: off`). 남은 통로가 이것 하나다.

```nginx
# $request 대신 $uri 를 쓴다. $uri 에는 쿼리스트링이 없다
log_format poby '$remote_addr - $remote_user [$time_local] "$request_method $uri $server_protocol" '
                '$status $body_bytes_sent "$http_referer" "$http_user_agent"';
access_log /var/log/nginx/poby.access.log poby;
```

접근 로그 보관 기간도 짧게 잡는다(`logrotate` 에서 `rotate 7` 정도).

---

## 4. 화면 지도

주 사용 환경은 **수업 끝나고 폰**이다. 모바일 우선이고, 타수가 늘어나는 설계는 실패로 본다.

### 로그인 전

| URL | 화면 |
|---|---|
| `GET /login` | 로그인 |
| `GET /setup` | 최초 원장 계정 만들기 (계정이 0개일 때만) |

### 처음 화면

| URL | 화면 |
|---|---|
| `GET /` | 오늘 할 일 대시보드 (기록이 빈 곳을 먼저 보여 준다) |

### 학생

| URL | 화면 |
|---|---|
| `GET /students` | 목록. 상태 필터 + 검색(이름·식별번호·학교) + 페이지(50명씩) |
| `GET /students/new` · `POST /students` | 등록 → 저장하면 **입회 기준선 화면으로 넘어간다** |
| `GET /students/{id}` | 상세. 아래 모든 기록이 여기서 갈라진다 |
| `GET /students/{id}/edit` · `POST /students/{id}` | 수정. **사실 정보만** 다룬다 |
| `GET /students/{id}/leave` · `POST` | 퇴원 확인 화면 → 5절 |
| `POST /students/{id}/pause` | 휴원 (반 배정 유지) |
| `POST /students/{id}/resume` | 재원 복귀 / 퇴원 취소 |

### 반과 수업

| URL | 화면 |
|---|---|
| `GET /classes` · `/classes/new` · `/classes/{id}` · `/classes/{id}/edit` | 반 |
| `POST /classes/{id}/students` · `.../{enrollmentId}/release` | 반 배정 넣기 / 빼기 |
| `GET /classes/{classId}/sessions` · `POST` | 수업 회차 |
| `GET /sessions/{id}` · `POST` · `POST /sessions/{id}/hide` | 회차 상세(출결·진도) / 숨기기 |

### 기록 (전부 학생 상세에서 들어간다)

| URL | 화면 |
|---|---|
| `/students/{id}/observations` · `/observations/new?kind=BASELINE` · `/observations/{oid}/edit` | 관찰 · 기준선 |
| `/students/{id}/scenes` · `/scenes/{sid}/edit` | 장면 |
| `/students/{id}/interventions` · `/interventions/{iid}/review` | 개입 로그 · 결과 확인 |
| `/students/{id}/assessments` · `/assessments/{aid}` · `/assessments/{aid}/edit` | 평가와 오답 태그 |
| `/students/{id}/consultations` · `/consultations/{cid}/edit` | 상담일지 |
| `/students/{id}/report` | **퇴원생 리포트 초안** — 위 기록을 모아 보여 준다 |

### 내 계정 · 원장 전용

| URL | 화면 |
|---|---|
| `GET /account` · `/account/password` | 내 정보 · 비밀번호 |
| `/admin/teachers/**` | 계정 관리 (원장) |
| `/admin/purge` · `/admin/purge/{id}` | 보관 만료 파기 (원장) |

`/admin/**` 은 `ROLE_ADMIN` 만 들어간다. 원장은 강사가 할 수 있는 것을 전부 할 수 있다
(`RoleHierarchy`: `ROLE_ADMIN > ROLE_TEACHER`).

---

## 5. 퇴원 · 휴원 · 파기

되돌리기 어려운 조작은 **확인 단계를 지난다.** 수정 폼의 드롭다운에 숨기지 않는다.

**퇴원** `GET /students/{id}/leave`
누르면 함께 일어나는 일을 먼저 보여 준다.

1. 상태가 퇴원으로 바뀐다
2. 보관 만료일이 **퇴원일 + 3년**으로 세워진다
3. **현재 반 배정도 같은 날짜로 끝난다** — 이게 빠져 있어서 퇴원생이 반 목록에 유령으로 남았다

지난 기록은 하나도 지우지 않는다.

**휴원** `POST /students/{id}/pause`
**반 배정을 그대로 둔다.** 휴원은 돌아올 자리를 비워 두는 상태다.

**재원 복귀** `POST /students/{id}/resume`
퇴원 취소도 이 길이다. 퇴원일 · 사유 · 보관 만료일을 지운다.
퇴원 때 끊은 **반 배정은 자동으로 되살리지 않는다.** 어느 반으로 돌아갈지는 사람이 정한다.
휴원 되돌리기는 확인 없이 한 번에 되지만, **퇴원 취소는 확인을 지난다** —
퇴원 사유는 사람이 쓴 문장이라 되돌릴 수 없고, 보관 만료일이 지워지면
그 학생은 파기 목록에 다시 오르지 못해 보관 3년 방침이 조용히 무효가 된다.

**보관 만료 파기** `/admin/purge` (원장)
보관 만료일이 지난 퇴원생이 올라온다. 여기서 하는 일은 **삭제가 아니라 익명화**다.

- 지운다: 이름(식별번호로 덮음) · 학교 · 특이사항 · 퇴원 사유
- 남긴다: 식별번호 · 날짜 · 상태 · 반 배정 이력 · 출결 · 진도 · 평가

학생 행을 지우면 `Enrollment` · `Attendance` · `ProgressLog` 가 CASCADE 로 함께 사라진다.
그러면 지난 회차의 인원과 반 단위 통계가 **소급해서 바뀌어** 이미 내보낸 리포트와 어긋난다.
남는 값으로는 개인을 특정할 수 없으므로 보관 기간 방침과도 어긋나지 않는다.

기록에 적힌 **자유 텍스트에는 실명이 섞여 있을 수 있다**(아이가 한 말, 형·동생 이름, 학교 이름 등).
그것까지 없앨지는 확인 화면의 체크박스로 고른다. 기본은 남기는 것이다.
체크하면 범위는 **사람이 문장으로 쓴 칸 전부**다. 한 종류라도 빠뜨리면
화면은 "실명을 지웠다" 고 알리면서 실명이 남고, `purgeAfter` 가 비어 파기 목록에 다시 오르지도 않는다.

- 행째로 지운다: 관찰 · 장면 · 상담 (문장이 기록의 본체라 비우면 껍데기만 남는다)
- 행은 남기고 문장 칸만 비운다: 평가의 `studentQuote`·`note`,
  개입의 `problem`·`action`·`resultNote`, 진도기록의 `note`
  (점수 · 오답 태그 · 단원 상태 · 출결 · 개입 결과 코드는 통계와 지난 리포트의 근거라 그대로 둔다)

확인 화면에서는 **학생 이름을 직접 입력해야** 버튼이 열린다. 서버도 같은 확인을 다시 한다.

---

## 6. 백업

이 데이터는 다시 만들 수 없다. 진도 · 관찰 · 장면은 그 수업이 지나면
아무도 기억으로 복원하지 못한다. 백업은 기능의 일부다.

비밀번호는 스크립트에 박지 않는다. 두 스크립트 모두 루트의 `.env` 를 읽는다.

**덤프는 저장소 안에 만들지 않는다.** 이 저장소는 OneDrive 동기화 폴더 안에 있을 수 있고,
그러면 실명이 든 덤프가 만들어지는 즉시 클라우드와 그 계정의 모든 기기로 올라간다.
`.gitignore` 는 git 추적만 막고 동기화는 막지 못한다.
그래서 두 스크립트의 기본 출력 경로가 저장소 밖이고, 동기화 폴더를 지정하면 **멈춘다.**

```powershell
# Windows — 기본 출력: %LOCALAPPDATA%\poby-house\backups (동기화되지 않는 위치)
powershell -ExecutionPolicy Bypass -File db\backup.ps1
powershell -ExecutionPolicy Bypass -File db\backup.ps1 -KeepDays 60
powershell -ExecutionPolicy Bypass -File db\backup.ps1 -OutDir D:\poby-backups
```

```bash
# 서버 — 기본 출력: /var/backups/poby
bash db/backup.sh
KEEP_DAYS=60 bash db/backup.sh
OUT_DIR=/srv/poby-backups bash db/backup.sh

# 매일 새벽 4시 (로그도 저장소 밖에 남긴다)
0 4 * * * cd /srv/poby_house && bash db/backup.sh >> /var/log/poby-backup.log 2>&1
```

덤프는 `POBY-20260818-040000.zip`(Windows) / `.sql.gz`(서버)로 쌓이고,
30일이 지난 것은 지운다. 둘 다 압축한다 — 평문 `.sql` 은 미리보기·검색 색인에 실명이 그대로 걸린다.

`mysqldump` 가 PATH 에 없고 MySQL 이 컨테이너 안에만 있으면 (출력 경로를 저장소 밖으로 잡는다):

```bash
mkdir -p "$LOCALAPPDATA/poby-house/backups"   # 서버라면 /var/backups/poby
docker compose exec -T my-db sh -c 'MYSQL_PWD=1234 mysqldump -uroot \
  --single-transaction --default-character-set=utf8mb4 POBY' \
  | gzip -9 > "$LOCALAPPDATA/poby-house/backups/POBY-$(date +%Y%m%d-%H%M%S).sql.gz"
```

**덤프 파일에는 학생 실명이 그대로 들어 있다.** git 에 올리지 말고,
다른 사람과 공유하는 동기화 폴더에 두지 마라.

### 이미 만들어진 덤프 정리 (한 번은 사람이 해야 한다)

기본 경로가 `db/backups` 였던 동안 돌린 적이 있으면 이미 클라우드에 올라가 있다.
OneDrive 는 파일을 지운 뒤에도 **휴지통과 버전 기록**에 남긴다.

1. `db/backups/` 안의 `.sql` / `.sql.gz` / `backup.log` 를 지운다.
2. OneDrive 웹 → 휴지통에서 그 파일들을 **완전 삭제**한다.
3. OneDrive 웹 → 해당 폴더의 **버전 기록**을 확인한다.
4. 같은 계정을 붙인 다른 기기의 로컬 사본도 확인한다.

되돌릴 때:

```bash
gunzip -c /var/backups/poby/POBY-20260818-040000.sql.gz \
  | docker compose exec -T my-db mysql -uroot -p1234 POBY
```

---

## 7. 코드 규약 (고치기 전에 읽을 것)

- 테이블 `PascalCase` / 컬럼 `camelCase`. `db/schema.sql` 이 원본이고 엔티티가 따라간다.
- 주석은 한국어로, **"무엇"이 아니라 "왜"** 를 쓴다. 특히 "이렇게 안 하면 무엇이 깨지는가".
- 서비스: 조회 실패는 `NotFoundException`, 규칙 위반은 `BusinessRuleException`.
  컨트롤러에서 try/catch 하지 않는다. `GlobalExceptionHandler` 가 화면으로 바꾼다.
- 컨트롤러: POST 뒤에는 항상 redirect(PRG). 성공은 `message`, 실패는 `errorMessage` flash.
- 목록에서 쓸 연관은 `@EntityGraph` 로 한 번에 가져온다. 개수는 `count` 쿼리로 센다.
- 화면 껍데기는 `templates/layout/base.html`, 기록 화면 공용 조각은 `templates/layout/record.html`.
- CSS 는 `static/css/app.css` 하나다. 색은 그 파일의 변수만 쓴다.
- 한글 IME: 텍스트 입력에서 Enter 를 가로챌 때 `e.isComposing || e.keyCode === 229` 를 반드시 본다.
  빼면 글자 확정 Enter 로 폼이 제출된다.
- 테스트는 `src/test/java/io/poby_house/service/` 에 Mockito 단위 테스트.
  **판단이 들어간 규칙만** 테스트한다(getter 테스트 금지).
