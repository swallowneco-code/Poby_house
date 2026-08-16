-- =====================================================================
--  20260818-01-records.sql
--  기록 기능(진도·관찰·장면·개입·평가·상담)을 붙이면서 모자란 칸을 채운다.
-- ---------------------------------------------------------------------
--  실행 방법
--    mysql -u <user> -p POBY < db/migration/20260818-01-records.sql
--    스키마 이름을 박지 않고 DATABASE() 를 쓴다. 접속한 DB 에 그대로 적용된다.
--
--  실행 순서
--    이 파일은 db/schema.sql 로 테이블이 이미 만들어진 DB 에 적용한다.
--    새로 만드는 DB 라면 schema.sql 에 이미 반영돼 있으므로 이 파일을 돌릴 필요가 없다
--    (돌려도 아래 이유로 아무 일도 일어나지 않는다).
--
--  왜 이렇게 길게 썼나
--    MySQL 8.0 은 ALTER TABLE ... ADD COLUMN IF NOT EXISTS 를 지원하지 않는다.
--    (MariaDB 에만 있는 문법이다. 8.0 에서 쓰면 문법 오류로 파일 전체가 멈춘다.)
--    그런데 이 컬럼들은 예전 작업 때 손으로 ALTER 를 돌려 로컬 DB 에 이미 들어가 있을 수 있다.
--    그대로 ALTER 를 쓰면 "Duplicate column name" (오류 1060) 으로 중간에 끊겨
--    뒤쪽 컬럼이 안 붙는다. 그래서 information_schema 를 먼저 보고
--    없을 때만 ALTER 를 실행하도록 PREPARE 로 감쌌다. 몇 번 돌려도 안전하다.
--
--  목적
--    1. ProgressLog.note        학생 개인 메모 칸. content 는 반 공통 진도라서 개인 메모를 담을 수 없다
--    2. ClassSession.hiddenAt   잘못 연 회차를 지우지 않고 숨긴다 (지우면 딸린 출결·진도가 CASCADE 로 사라진다)
--    3. ClassSession 진도 4칸   진도는 반 공통으로 한 번 입력받아 학생별 ProgressLog 로 복사한다.
--                              반 단위 원본을 회차에 남겨 두어야 다음 회차가 교재를 이어받을 수 있다
--    4. Student.updatedBy /     누가 마지막으로 고쳤는지. 여러 강사가 같은 학생을 만진다
--       ClassGroup.updatedBy
--  주의: 기존 행에 값을 채우지 않는다. 전부 NULL 허용이다.
--        NOT NULL 로 만들면 이미 있는 회차·기록이 통째로 막힌다.
-- =====================================================================

-- ---------------------------------------------------------------------
-- 1. ProgressLog.note  (학생 메모)
-- ---------------------------------------------------------------------
SET @sql = IF(
  (SELECT COUNT(*) FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'ProgressLog' AND COLUMN_NAME = 'note') > 0,
  'SELECT ''ProgressLog.note 이미 있음 - 건너뜀''',
  'ALTER TABLE ProgressLog ADD COLUMN note VARCHAR(500) NULL COMMENT ''학생 메모 / 그날 이 학생에게 눈에 띈 것'' AFTER homeworkStatus'
);
PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;

-- ---------------------------------------------------------------------
-- 2. ClassSession.hiddenAt  (숨김)
-- ---------------------------------------------------------------------
SET @sql = IF(
  (SELECT COUNT(*) FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'ClassSession' AND COLUMN_NAME = 'hiddenAt') > 0,
  'SELECT ''ClassSession.hiddenAt 이미 있음 - 건너뜀''',
  'ALTER TABLE ClassSession ADD COLUMN hiddenAt DATETIME NULL COMMENT ''숨긴 시각. 지우지 않고 목록에서만 뺀다'' AFTER dailyNote'
);
PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;

-- ---------------------------------------------------------------------
-- 3. ClassSession 진도 4칸  (반 공통 원본)
-- ---------------------------------------------------------------------
SET @sql = IF(
  (SELECT COUNT(*) FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'ClassSession' AND COLUMN_NAME = 'textbook') > 0,
  'SELECT ''ClassSession.textbook 이미 있음 - 건너뜀''',
  'ALTER TABLE ClassSession ADD COLUMN textbook VARCHAR(100) NULL COMMENT ''교재명 / 반 공통'' AFTER dailyNote'
);
PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;

SET @sql = IF(
  (SELECT COUNT(*) FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'ClassSession' AND COLUMN_NAME = 'rangeText') > 0,
  'SELECT ''ClassSession.rangeText 이미 있음 - 건너뜀''',
  'ALTER TABLE ClassSession ADD COLUMN rangeText VARCHAR(100) NULL COMMENT ''p.42~55 형태 / 반 공통'' AFTER textbook'
);
PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;

SET @sql = IF(
  (SELECT COUNT(*) FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'ClassSession' AND COLUMN_NAME = 'content') > 0,
  'SELECT ''ClassSession.content 이미 있음 - 건너뜀''',
  'ALTER TABLE ClassSession ADD COLUMN content VARCHAR(500) NULL COMMENT ''오늘 한 것 / 반 공통'' AFTER rangeText'
);
PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;

SET @sql = IF(
  (SELECT COUNT(*) FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'ClassSession' AND COLUMN_NAME = 'homework') > 0,
  'SELECT ''ClassSession.homework 이미 있음 - 건너뜀''',
  'ALTER TABLE ClassSession ADD COLUMN homework VARCHAR(500) NULL COMMENT ''오늘 내준 과제 / 반 공통'' AFTER content'
);
PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;

-- ---------------------------------------------------------------------
-- 4. Student.updatedBy / ClassGroup.updatedBy  (마지막 수정자)
--    외래키를 함께 건다. 없으면 강사 계정을 지웠을 때 값이 붕 뜨고,
--    엔티티가 그 id 로 강사를 찾다가 화면이 통째로 터진다.
-- ---------------------------------------------------------------------
SET @sql = IF(
  (SELECT COUNT(*) FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'Student' AND COLUMN_NAME = 'updatedBy') > 0,
  'SELECT ''Student.updatedBy 이미 있음 - 건너뜀''',
  'ALTER TABLE Student ADD COLUMN updatedBy BIGINT NULL COMMENT ''마지막으로 고친 강사'' AFTER updatedAt'
);
PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;

SET @sql = IF(
  (SELECT COUNT(*) FROM information_schema.TABLE_CONSTRAINTS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'Student'
      AND CONSTRAINT_NAME = 'fk_student_updatedby') > 0,
  'SELECT ''fk_student_updatedby 이미 있음 - 건너뜀''',
  'ALTER TABLE Student ADD CONSTRAINT fk_student_updatedby FOREIGN KEY (updatedBy) REFERENCES Teacher(id) ON DELETE SET NULL'
);
PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;

SET @sql = IF(
  (SELECT COUNT(*) FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'ClassGroup' AND COLUMN_NAME = 'updatedBy') > 0,
  'SELECT ''ClassGroup.updatedBy 이미 있음 - 건너뜀''',
  'ALTER TABLE ClassGroup ADD COLUMN updatedBy BIGINT NULL COMMENT ''마지막으로 고친 강사'' AFTER createdAt'
);
PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;

SET @sql = IF(
  (SELECT COUNT(*) FROM information_schema.TABLE_CONSTRAINTS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'ClassGroup'
      AND CONSTRAINT_NAME = 'fk_classgroup_updatedby') > 0,
  'SELECT ''fk_classgroup_updatedby 이미 있음 - 건너뜀''',
  'ALTER TABLE ClassGroup ADD CONSTRAINT fk_classgroup_updatedby FOREIGN KEY (updatedBy) REFERENCES Teacher(id) ON DELETE SET NULL'
);
PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;

-- ---------------------------------------------------------------------
-- 5. Attendance.status 코멘트에 PAUSED 를 추가한다.
--    값은 그대로다. 코멘트만 실제로 저장되는 값과 맞춘다.
--    MODIFY 는 몇 번 돌려도 같은 결과라 조건을 걸지 않는다.
-- ---------------------------------------------------------------------
ALTER TABLE Attendance
  MODIFY COLUMN status VARCHAR(20) NOT NULL DEFAULT 'PRESENT'
  COMMENT 'PRESENT | LATE | ABSENT | MAKEUP | PAUSED';

-- ---------------------------------------------------------------------
-- 확인용. 다 붙었으면 8줄이 나온다.
-- ---------------------------------------------------------------------
SELECT TABLE_NAME, COLUMN_NAME
FROM information_schema.COLUMNS
WHERE TABLE_SCHEMA = DATABASE()
  AND ((TABLE_NAME = 'ProgressLog'  AND COLUMN_NAME = 'note')
    OR (TABLE_NAME = 'ClassSession' AND COLUMN_NAME IN ('hiddenAt', 'textbook', 'rangeText', 'content', 'homework'))
    OR (TABLE_NAME = 'Student'      AND COLUMN_NAME = 'updatedBy')
    OR (TABLE_NAME = 'ClassGroup'   AND COLUMN_NAME = 'updatedBy'))
ORDER BY TABLE_NAME, COLUMN_NAME;
