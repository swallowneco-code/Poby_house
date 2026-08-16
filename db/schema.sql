-- =====================================================================
--  Poby House 학원관리 스키마 v2
-- ---------------------------------------------------------------------
--  명명규칙: 테이블 PascalCase / 컬럼 camelCase
--    -> application.yml 의 PhysicalNamingStrategyStandardImpl 설정에 맞춤
--
--  v2 방향: "내 기록 도구" (A안)
--    - 보호자 연락처 / 알림톡 발송 테이블은 넣지 않는다.
--      학원 명의가 필요한 영역이라, 원장 합의 전까지 수집하지 않는다.
--    - 대신 퇴원생 리포트가 요구하는 데이터를 전부 담는다.
--
--  기록 주기 3단
--    ProgressLog  매 수업     30초   진도
--    Observation  1~4주       5분    이해도·태도·자기평가
--    Scene /      분기        3분    장면·개입로그   <- 리포트의 심장
--    Intervention
-- =====================================================================

SET NAMES utf8mb4;

-- =====================================================================
--  1. 기본
-- =====================================================================

-- ---------------------------------------------------------------------
-- 강사 계정
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS Teacher (
  id         BIGINT       NOT NULL AUTO_INCREMENT,
  loginId    VARCHAR(50)  NOT NULL COMMENT '로그인 아이디',
  password   VARCHAR(255) NOT NULL COMMENT '암호화 저장 (평문 금지)',
  name       VARCHAR(50)  NOT NULL,
  role       VARCHAR(20)  NOT NULL DEFAULT 'TEACHER' COMMENT 'ADMIN | TEACHER',
  active     BOOLEAN      NOT NULL DEFAULT TRUE,
  createdAt  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uk_teacher_loginId (loginId)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='강사 계정';

-- ---------------------------------------------------------------------
-- 학생
--   code: 외부로 나가는 문서(리포트 초안·엑셀)에는 실명 대신 이 번호를 쓴다
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS Student (
  id           BIGINT      NOT NULL AUTO_INCREMENT,
  code         VARCHAR(20) NOT NULL COMMENT '식별번호 S2026001 형태 / 외부 반출용',
  name         VARCHAR(50) NOT NULL COMMENT '실명 / 시스템 내부 화면에서만 사용',
  school       VARCHAR(50),
  grade        VARCHAR(20)          COMMENT '초4, 중1(자유학년) 등 / 반 편성과 무관',
  status       VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' COMMENT 'ACTIVE 재원 | PAUSED 휴원 | LEFT 퇴원',
  enrolledOn   DATE        NOT NULL COMMENT '등록일',
  leftOn       DATE                 COMMENT '퇴원일',
  leaveReason  VARCHAR(255)         COMMENT '퇴원 사유',
  purgeAfter   DATE                 COMMENT '보관 만료일 / 퇴원 + 3년',
  memo         TEXT                 COMMENT '기본 특이사항',
  createdAt    DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updatedAt    DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  updatedBy    BIGINT               COMMENT '마지막으로 고친 강사 / 여러 명이 같은 학생을 만지므로 누가 고쳤는지가 남아야 한다',
  PRIMARY KEY (id),
  UNIQUE KEY uk_student_code (code),
  KEY idx_student_status (status),
  KEY idx_student_name (name),
  CONSTRAINT fk_student_updatedby FOREIGN KEY (updatedBy) REFERENCES Teacher(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='학생';

-- ---------------------------------------------------------------------
-- 반 / 반 배정 이력
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS ClassGroup (
  id         BIGINT      NOT NULL AUTO_INCREMENT,
  name       VARCHAR(50) NOT NULL COMMENT '화목 A반 등',
  level      VARCHAR(50)          COMMENT '진도 기준 레벨 (분수심화 등) / 학년 아님',
  dayOfWeek  VARCHAR(20)          COMMENT '월수금 / 화목 등',
  startTime  TIME,
  endTime    TIME,
  teacherId  BIGINT,
  active     BOOLEAN     NOT NULL DEFAULT TRUE,
  createdAt  DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updatedBy  BIGINT               COMMENT '마지막으로 고친 강사',
  PRIMARY KEY (id),
  KEY idx_classgroup_teacher (teacherId),
  CONSTRAINT fk_classgroup_teacher   FOREIGN KEY (teacherId) REFERENCES Teacher(id) ON DELETE SET NULL,
  CONSTRAINT fk_classgroup_updatedby FOREIGN KEY (updatedBy) REFERENCES Teacher(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='반';

CREATE TABLE IF NOT EXISTS Enrollment (
  id            BIGINT NOT NULL AUTO_INCREMENT,
  studentId     BIGINT NOT NULL,
  classGroupId  BIGINT NOT NULL,
  startedOn     DATE   NOT NULL,
  endedOn       DATE            COMMENT 'NULL이면 현재 소속중',
  PRIMARY KEY (id),
  KEY idx_enrollment_student (studentId),
  KEY idx_enrollment_class (classGroupId),
  CONSTRAINT fk_enrollment_student FOREIGN KEY (studentId)    REFERENCES Student(id)    ON DELETE CASCADE,
  CONSTRAINT fk_enrollment_class   FOREIGN KEY (classGroupId) REFERENCES ClassGroup(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='학생-반 배정 이력';

-- =====================================================================
--  2. 수업 단위 기록
-- =====================================================================

CREATE TABLE IF NOT EXISTS ClassSession (
  id            BIGINT   NOT NULL AUTO_INCREMENT,
  classGroupId  BIGINT   NOT NULL,
  sessionDate   DATE     NOT NULL,
  startTime     TIME,
  endTime       TIME,
  teacherId     BIGINT,
  dailyNote     TEXT              COMMENT '수업 메모 / 그날 반 전체에 있었던 일',
  -- 진도는 반 공통이다. 한 반이 같은 진도를 나가므로 학생 수만큼 다시 입력받지 않는다.
  -- 반 단위로 한 번 입력받아 학생별 ProgressLog 로 복사 기록하고, 원본은 여기 남긴다.
  textbook      VARCHAR(100)      COMMENT '교재명 / 반 공통',
  rangeText     VARCHAR(100)      COMMENT 'p.42~55 형태 / 반 공통',
  content       VARCHAR(500)      COMMENT '오늘 한 것 / 반 공통',
  homework      VARCHAR(500)      COMMENT '오늘 내준 과제 / 반 공통',
  hiddenAt      DATETIME          COMMENT '숨긴 시각. 잘못 연 회차를 지우지 않고 목록에서만 뺀다. 같은 날짜로 다시 열면 되살아난다',
  createdAt     DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uk_session (classGroupId, sessionDate),
  KEY idx_session_date (sessionDate),
  CONSTRAINT fk_session_class   FOREIGN KEY (classGroupId) REFERENCES ClassGroup(id) ON DELETE CASCADE,
  CONSTRAINT fk_session_teacher FOREIGN KEY (teacherId)    REFERENCES Teacher(id)    ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='수업 회차';

CREATE TABLE IF NOT EXISTS Attendance (
  id         BIGINT      NOT NULL AUTO_INCREMENT,
  sessionId  BIGINT      NOT NULL,
  studentId  BIGINT      NOT NULL,
  -- DEFAULT 는 남겨 두지만 애플리케이션은 이 기본값에 기대지 않는다.
  -- 미표시(확인하지 않음)는 행을 만들지 않는 것으로 표현한다. 출석으로 채우면 출석률이 거짓말을 한다.
  status     VARCHAR(20) NOT NULL DEFAULT 'PRESENT' COMMENT 'PRESENT | LATE | ABSENT | MAKEUP | PAUSED',
  note       VARCHAR(255),
  PRIMARY KEY (id),
  UNIQUE KEY uk_attendance (sessionId, studentId),
  KEY idx_attendance_student (studentId),
  CONSTRAINT fk_attendance_session FOREIGN KEY (sessionId) REFERENCES ClassSession(id) ON DELETE CASCADE,
  CONSTRAINT fk_attendance_student FOREIGN KEY (studentId) REFERENCES Student(id)      ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='출결 / 리포트 1-2 함께한 시간의 근거';

-- ---------------------------------------------------------------------
-- 진도기록  ★ 매 수업 30초
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS ProgressLog (
  id              BIGINT       NOT NULL AUTO_INCREMENT,
  sessionId       BIGINT       NOT NULL,
  studentId       BIGINT       NOT NULL,
  -- 아래 네 칸은 ClassSession 의 반 공통 진도를 복사해 둔 값이다.
  -- 참조로 두지 않는 이유: 나중에 반 진도를 고쳐도 그날 이 학생이 무엇을 했는지는 그대로 남아야 한다.
  textbook        VARCHAR(100)          COMMENT '교재명',
  rangeText       VARCHAR(100)          COMMENT 'p.42~55 형태',
  content         VARCHAR(500)          COMMENT '오늘 한 것',
  homework        VARCHAR(500)          COMMENT '오늘 내준 과제',
  homeworkStatus  VARCHAR(20)           COMMENT '지난 과제 제출: DONE | PARTIAL | NONE',
  note            VARCHAR(500)          COMMENT '학생 메모 / 그날 이 학생에게 눈에 띈 것. content 는 반 공통이라 개인 메모를 담을 칸이 없다',
  createdBy       BIGINT,
  createdAt       DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updatedAt       DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uk_progress (sessionId, studentId),
  KEY idx_progress_student (studentId, createdAt),
  CONSTRAINT fk_progress_session FOREIGN KEY (sessionId) REFERENCES ClassSession(id) ON DELETE CASCADE,
  CONSTRAINT fk_progress_student FOREIGN KEY (studentId) REFERENCES Student(id)      ON DELETE CASCADE,
  CONSTRAINT fk_progress_teacher FOREIGN KEY (createdBy) REFERENCES Teacher(id)      ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='진도기록 (매 수업)';

-- =====================================================================
--  3. 학생 단위 기록  — 퇴원생 리포트의 원천
-- =====================================================================

-- ---------------------------------------------------------------------
-- 관찰기록  ★ 1~4주 5분
--   kind=BASELINE 이면 입회 시점 스냅샷. 나중에 이게 before 가 된다.
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS Observation (
  id              BIGINT      NOT NULL AUTO_INCREMENT,
  studentId       BIGINT      NOT NULL,
  kind            VARCHAR(20) NOT NULL DEFAULT 'PERIODIC' COMMENT 'BASELINE 입회기준선 | PERIODIC 정기 | QUARTERLY 분기점검',
  periodStart     DATE        NOT NULL,
  periodEnd       DATE        NOT NULL,
  understanding   TINYINT              COMMENT '이해도 5단계 / 5 최상 ~ 1 최하',
  homeworkRate    TINYINT              COMMENT '과제 수행도 5단계 / 5 최상 ~ 1 최하',
  attitudeNote    TEXT                 COMMENT '태도·특이사항',
  strength        TEXT                 COMMENT '잘하는 점 / 근거 없는 칭찬 금지',
  weakness        TEXT                 COMMENT '보완할 점 / 처방 없는 지적 금지',
  selfAssessment  VARCHAR(500)         COMMENT '학생 자기평가 한 줄 (아이 말 그대로)',
  teacherComment  TEXT                 COMMENT '종합 코멘트',
  createdBy       BIGINT,
  createdAt       DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updatedAt       DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  KEY idx_observation_student (studentId, periodEnd),
  KEY idx_observation_kind (studentId, kind),
  CONSTRAINT fk_observation_student FOREIGN KEY (studentId) REFERENCES Student(id) ON DELETE CASCADE,
  CONSTRAINT fk_observation_teacher FOREIGN KEY (createdBy) REFERENCES Teacher(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='관찰기록 (1~4주) / 기준선 포함';

-- ---------------------------------------------------------------------
-- 장면  ★ 분기 1개
--   시험지에 남지 않는 데이터. 리포트 4-1 편지는 이것 없이 못 쓴다.
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS Scene (
  id          BIGINT   NOT NULL AUTO_INCREMENT,
  studentId   BIGINT   NOT NULL,
  occurredOn  DATE     NOT NULL COMMENT '있었던 날',
  situation   VARCHAR(500)      COMMENT '어떤 상황이었나',
  content     TEXT     NOT NULL COMMENT '무엇을 했고 무슨 말을 했나 / 성격이 아니라 행동을 쓴다',
  createdBy   BIGINT,
  createdAt   DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  KEY idx_scene_student (studentId, occurredOn),
  CONSTRAINT fk_scene_student FOREIGN KEY (studentId) REFERENCES Student(id) ON DELETE CASCADE,
  CONSTRAINT fk_scene_teacher FOREIGN KEY (createdBy) REFERENCES Teacher(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='장면 (분기 1개) / 리포트 4-1 편지의 재료';

-- ---------------------------------------------------------------------
-- 개입 로그  ★ 분기 갱신
--   리포트 3-1 "통한 것 / 안 통한 것" 은 전적으로 이 테이블에서 나온다.
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS Intervention (
  id          BIGINT      NOT NULL AUTO_INCREMENT,
  studentId   BIGINT      NOT NULL,
  triedOn     DATE        NOT NULL COMMENT '시도한 날',
  problem     VARCHAR(500) NOT NULL COMMENT '무엇을 해결하려 했나',
  action      TEXT        NOT NULL  COMMENT '무엇을 시도했나',
  result      VARCHAR(20)           COMMENT 'WORKED 통함 | PARTIAL 일부 | FAILED 안통함 | ONGOING 진행중',
  resultNote  TEXT                  COMMENT '어떻게 알았나 (근거)',
  reviewedOn  DATE                  COMMENT '결과를 확인한 날',
  createdBy   BIGINT,
  createdAt   DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updatedAt   DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  KEY idx_intervention_student (studentId, triedOn),
  KEY idx_intervention_result (studentId, result),
  CONSTRAINT fk_intervention_student FOREIGN KEY (studentId) REFERENCES Student(id) ON DELETE CASCADE,
  CONSTRAINT fk_intervention_teacher FOREIGN KEY (createdBy) REFERENCES Teacher(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='개입 로그 (시도 -> 결과)';

-- =====================================================================
--  4. 평가와 오답
-- =====================================================================

-- ---------------------------------------------------------------------
-- 평가 1건
--   BLANK(백지테스트) 일 때만 4축 점수를 채운다: 용어/조건/예외/인과
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS Assessment (
  id                BIGINT      NOT NULL AUTO_INCREMENT,
  studentId         BIGINT      NOT NULL,
  assessedOn        DATE        NOT NULL,
  type              VARCHAR(20) NOT NULL COMMENT 'BLANK 백지 | CUMULATIVE 누적 | UNIT 단원평가 | COMPREHENSIVE 총괄',
  unitName          VARCHAR(100)         COMMENT '단원명',
  score             DECIMAL(5,1)         COMMENT '득점',
  maxScore          DECIMAL(5,1)         COMMENT '만점',
  unitStatus        VARCHAR(20)          COMMENT '단원 상태 EXCELLENT ◎ | OK ○ | WEAK △',
  blankTerm         TINYINT              COMMENT '백지 4축: 용어',
  blankCondition    TINYINT              COMMENT '백지 4축: 조건',
  blankException    TINYINT              COMMENT '백지 4축: 예외',
  blankCausality    TINYINT              COMMENT '백지 4축: 인과',
  studentQuote      VARCHAR(500)         COMMENT '아이가 쓴 문장 한 줄 그대로',
  sourceLocation    VARCHAR(255)         COMMENT '원본 시험지 위치만 적는다 / 학원 자산은 반출하지 않는다',
  note              TEXT,
  createdBy         BIGINT,
  createdAt         DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  KEY idx_assessment_student (studentId, assessedOn),
  KEY idx_assessment_type (studentId, type),
  CONSTRAINT fk_assessment_student FOREIGN KEY (studentId) REFERENCES Student(id) ON DELETE CASCADE,
  CONSTRAINT fk_assessment_teacher FOREIGN KEY (createdBy) REFERENCES Teacher(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='평가 (백지·누적·단원·총괄)';

-- ---------------------------------------------------------------------
-- 오답 유형 태그  ★ 태그 분포가 곧 처방이다
--   자유 텍스트로 받으면 집계가 안 된다. 반드시 코드로 저장한다.
--   CALC 계산 | COND 조건 | CONCEPT 개념 | APPLY 적용 | TIME 시간 | NOTRY 무시도 | WRITE 서술
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS AssessmentErrorTag (
  id            BIGINT      NOT NULL AUTO_INCREMENT,
  assessmentId  BIGINT      NOT NULL,
  tagCode       VARCHAR(20) NOT NULL COMMENT 'CALC | COND | CONCEPT | APPLY | TIME | NOTRY | WRITE',
  cnt           INT         NOT NULL DEFAULT 1 COMMENT '해당 유형 오답 개수',
  PRIMARY KEY (id),
  UNIQUE KEY uk_errortag (assessmentId, tagCode),
  KEY idx_errortag_code (tagCode),
  CONSTRAINT fk_errortag_assessment FOREIGN KEY (assessmentId) REFERENCES Assessment(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='오답 유형 태그 / 집계용';

-- =====================================================================
--  5. 상담
-- =====================================================================

-- ---------------------------------------------------------------------
-- 상담일지
--   요약을 사실 / 걱정 / 약속 세 칸으로 분리한다.
--   가정 사정·형제 비교·경제 상황은 적지 않는다. 리포트에 못 쓰는 정보다.
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS Consultation (
  id           BIGINT      NOT NULL AUTO_INCREMENT,
  studentId    BIGINT      NOT NULL,
  consultedOn  DATE        NOT NULL,
  type         VARCHAR(20)          COMMENT 'PHONE 전화 | VISIT 방문 | MESSAGE 메시지',
  factNote     TEXT                 COMMENT '사실 - 오간 내용',
  worryNote    TEXT                 COMMENT '걱정 - 학부모가 우려한 것',
  promiseNote  TEXT                 COMMENT '약속 - 내가 하기로 한 것',
  createdBy    BIGINT,
  createdAt    DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  KEY idx_consultation_student (studentId, consultedOn),
  CONSTRAINT fk_consultation_student FOREIGN KEY (studentId) REFERENCES Student(id) ON DELETE CASCADE,
  CONSTRAINT fk_consultation_teacher FOREIGN KEY (createdBy) REFERENCES Teacher(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='학부모 상담일지 (사실/걱정/약속)';


-- =====================================================================
--  계정은 이 파일에서 만들지 않는다.
-- ---------------------------------------------------------------------
--  Teacher 행이 하나라도 있으면 TeacherService.noAccountYet() 이 false 가 되어
--  최초 설정 화면(/setup)이 영구히 닫힌다. 그러면 서버에 올린 순간부터
--  유일한 계정이 이 파일에 평문으로 박힌 아이디·해시가 되고,
--  그 계정은 ADMIN 이라 파기 목록에서 퇴원생 실명까지 본다.
--
--  첫 계정은 /setup 이 만든다.
--  로컬 개발용 계정이 필요하면 db/seed/local-teacher.sql 을 따로 돌린다.
--  그 파일은 서버 배포 절차에서는 돌리지 않는다.
-- =====================================================================
