package io.poby_house.service;

import io.poby_house.domain.Attendance;
import io.poby_house.domain.AttendanceStatus;
import io.poby_house.domain.ClassGroup;
import io.poby_house.domain.ClassSession;
import io.poby_house.domain.Enrollment;
import io.poby_house.domain.ProgressLog;
import io.poby_house.domain.Student;
import io.poby_house.domain.Teacher;
import io.poby_house.dto.SessionListRow;
import io.poby_house.dto.SessionRecordForm;
import io.poby_house.dto.StudentEntryForm;
import io.poby_house.repository.AttendanceRepository;
import io.poby_house.repository.ClassSessionRepository;
import io.poby_house.repository.EnrollmentRepository;
import io.poby_house.repository.ProgressLogRepository;
import io.poby_house.repository.TeacherRepository;
import io.poby_house.support.NotFoundException;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 수업 회차 = 반 x 날짜. 이 앱에서 매일 열리는 유일한 화면이다.
 *
 * 진도를 두 곳에 쓰는 이유:
 * 리포트는 학생 한 명의 진도 흐름을 요구하는데, 입력은 반 단위여야 30초에 끝난다.
 * 그래서 반 공통 진도 4칸(ClassSession)을 한 번 입력받아 학생별 ProgressLog 로 복사한다.
 * 둘 중 하나만 고르면 리포트가 비거나(반에만 남김) 아무도 기록을 안 한다(학생마다 재입력).
 *
 * 반 공통 진도를 참조가 아니라 복사로 두는 이유는 ProgressLog 주석에 있다.
 * 요약하면, 나중에 반 진도를 고쳐도 그날 이 학생이 무엇을 했는지는 그대로 남아야 한다.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ClassSessionService {

    private final ClassSessionRepository sessionRepository;
    private final AttendanceRepository attendanceRepository;
    private final ProgressLogRepository progressLogRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final TeacherRepository teacherRepository;
    private final ClassGroupService classGroupService;

    /**
     * 회차 목록은 "빈 회차를 뺀 목록 + 회차별 기록 개수" 를 한 번에 물어야 한다.
     * 그 쿼리가 ClassSessionRepository 에 없고, 리포지토리 파일은 이 단계의 소유가 아니라 늘리지 않았다.
     * 회차마다 개수를 따로 세면 회차 수만큼 쿼리가 나간다(N+1). 그래서 여기서 직접 쓴다.
     * 리포지토리로 옮기면 이 필드는 지운다.
     */
    @PersistenceContext
    private EntityManager entityManager;

    /**
     * 아무것도 안 적힌 빈 회차는 목록에 내보내지 않는다.
     * 기록하기만 눌러 보고 나온 껍데기까지 쌓이면 목록을 쓸 수 없게 된다.
     * 같은 날짜로 다시 열면 그 회차를 이어 쓰므로 목록에서 사라져도 잃는 것이 없다.
     *
     * 출결이나 진도기록만 있고 글은 하나도 없는 회차도 기록이 있는 회차다. exists 로 함께 본다.
     */
    private static final String RECORDED_ROWS = """
            select s,
                   (select count(a) from Attendance a where a.session = s),
                   (select count(p) from ProgressLog p where p.session = s)
            from ClassSession s
            where s.classGroup.id = :classGroupId
              and s.hiddenAt is null
              and (s.dailyNote is not null
                or s.textbook  is not null
                or s.rangeText is not null
                or s.content   is not null
                or s.homework  is not null
                or exists (select a2.id from Attendance  a2 where a2.session = s)
                or exists (select p2.id from ProgressLog p2 where p2.session = s))
            order by s.sessionDate desc
            """;

    public ClassSession get(Long id) {
        return sessionRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("회차를 찾을 수 없습니다. id=" + id));
    }

    /** 기록이 있는 회차만, 최근 순 */
    public List<SessionListRow> recordedRows(Long classGroupId) {
        List<Object[]> rows = entityManager.createQuery(RECORDED_ROWS, Object[].class)
                .setParameter("classGroupId", classGroupId)
                .getResultList();
        List<SessionListRow> result = new ArrayList<>(rows.size());
        for (Object[] row : rows) {
            result.add(new SessionListRow((ClassSession) row[0], (Long) row[1], (Long) row[2]));
        }
        return result;
    }

    /** 기록이 있는 달만. recordedRows 가 이미 날짜 내림차순이라 최근 달이 앞에 온다 */
    public List<String> recordedMonths(List<SessionListRow> rows) {
        return rows.stream().map(SessionListRow::getMonth).distinct().toList();
    }

    /** 고른 달의 회차만. 달이 없으면(기록이 하나도 없을 때) 그대로 돌려준다 */
    public List<SessionListRow> rowsIn(List<SessionListRow> rows, String month) {
        if (month == null) {
            return rows;
        }
        return rows.stream().filter(row -> month.equals(row.getMonth())).toList();
    }

    /** 그 날짜 회차가 이미 열려 있나. 목록을 만들지 않고 센다 */
    public boolean isOpened(Long classGroupId, LocalDate date) {
        return sessionRepository.countByClassGroupIdAndSessionDate(classGroupId, date) > 0;
    }

    /**
     * 반 x 날짜로 회차를 연다. 같은 날짜가 이미 있으면 그 회차를 이어 쓴다.
     *
     * 숨긴 회차도 찾아서 되살린다. (classGroupId, sessionDate) 가 유니크라
     * 숨김을 걸러 내고 새로 저장하면 제약 위반으로 저장이 통째로 실패한다.
     * 그리고 이것이 숨긴 회차를 되살리는 유일한 경로다.
     */
    @Transactional
    public ClassSession open(Long classGroupId, LocalDate date, Long teacherId) {
        LocalDate on = date == null ? LocalDate.now() : date;

        Optional<ClassSession> existing = sessionRepository.findByClassGroupIdAndSessionDate(classGroupId, on);
        if (existing.isPresent()) {
            ClassSession session = existing.get();
            session.unhide();
            return session;
        }

        ClassGroup group = classGroupService.get(classGroupId);
        ClassSession created = ClassSession.of(group, on, findTeacher(teacherId));
        // 새 회차는 지난 회차의 교재를 이어받는다. 범위는 비운다. 교재는 몇 달 그대로지만 범위는 매번 다르다
        previousSession(classGroupId, on).ifPresent(prev -> created.setTextbook(prev.getTextbook()));
        return sessionRepository.save(created);
    }

    /**
     * 이 날짜 직전 회차. 새 회차의 교재를 이어받고, 화면에 지난 과제를 보여 주는 데 쓴다.
     * 지난 과제를 안 보여 주면 "지난 과제 다 함/일부/안 함" 을 무엇에 대해 찍는지 알 수 없다.
     *
     * 회차 목록(날짜 내림차순)에서 첫 건을 고른다. 리포지토리에 직전 한 건을 뽑는 메서드가 없고
     * 그 파일은 이 단계의 소유가 아니다. 한 반의 회차 수는 목록 화면에서도 통째로 읽는 규모다.
     * 숨긴 회차는 제외한다. 잘못 열어 둔 회차의 교재를 물려받으면 틀린 교재가 계속 번식한다.
     */
    public Optional<ClassSession> previousSession(Long classGroupId, LocalDate date) {
        return sessionRepository.findByClassGroupIdAndHiddenAtIsNullOrderBySessionDateDesc(classGroupId).stream()
                .filter(session -> session.getSessionDate().isBefore(date))
                .findFirst();
    }

    /** 목록에서만 뺀다. 출결·진도기록은 그대로 남는다 */
    @Transactional
    public ClassSession hide(Long sessionId) {
        ClassSession session = get(sessionId);
        // 지우지 않는다. 행을 지우면 딸린 출결·진도기록이 CASCADE 로 함께 사라져 되돌릴 수 없다
        session.hide();
        return session;
    }

    /**
     * 회차 입력 폼. 진도는 반 공통이라 위쪽에 한 벌,
     * 학생은 그 반의 현재 소속(Enrollment.endedOn is null) 순서대로 한 줄씩이다.
     */
    public SessionRecordForm buildForm(Long sessionId) {
        ClassSession session = get(sessionId);

        SessionRecordForm form = new SessionRecordForm();
        form.setDailyNote(session.getDailyNote());
        form.setTextbook(session.getTextbook());
        form.setRangeText(session.getRangeText());
        form.setContent(session.getContent());
        form.setHomework(session.getHomework());

        // 출결과 진도를 각각 한 번만 읽는다.
        // 학생마다 조회를 던지면 매일 여는 이 화면의 쿼리 수가 학생 수에 비례해 늘어난다
        Map<Long, AttendanceStatus> saved = savedStatuses(sessionId);
        Map<Long, ProgressLog> savedProgress = savedProgress(sessionId);
        for (Student student : currentStudents(session)) {
            StudentEntryForm entry = new StudentEntryForm();
            entry.applyStudent(student);
            entry.setAttendance(saved.get(student.getId()));
            ProgressLog log = savedProgress.get(student.getId());
            if (log != null) {
                entry.setHomeworkStatus(log.getHomeworkStatus());
                entry.setNote(log.getNote());
            }
            form.getEntries().add(entry);
        }
        return form;
    }

    /**
     * 검증 실패로 같은 화면을 다시 그릴 때 쓴다.
     *
     * 이름·학년은 화면 표시용이라 폼으로 되돌아오지 않는다.
     * 그래서 학생 줄을 현재 명단 순서로 다시 세우고, 방금 입력한 값만 studentId 로 옮겨 담는다.
     * 이걸 안 하면 오류 화면에서 학생 이름이 전부 빈칸으로 나와 무엇을 고쳐야 할지 알 수 없다.
     */
    public void restoreEntries(Long sessionId, SessionRecordForm form) {
        ClassSession session = get(sessionId);

        Map<Long, StudentEntryForm> submitted = new HashMap<>();
        for (StudentEntryForm entry : form.getEntries()) {
            if (entry != null && entry.getStudentId() != null) {
                submitted.put(entry.getStudentId(), entry);
            }
        }

        form.getEntries().clear();
        for (Student student : currentStudents(session)) {
            StudentEntryForm entry = submitted.get(student.getId());
            if (entry == null) {
                entry = new StudentEntryForm();
            }
            entry.applyStudent(student);
            form.getEntries().add(entry);
        }
    }

    /**
     * 전원을 한 번에 저장한다.
     * 학생 한 명 저장할 때마다 화면이 넘어가면 아무도 안 쓴다.
     */
    @Transactional
    public ClassSession save(Long sessionId, SessionRecordForm form, Long teacherId) {
        ClassSession session = get(sessionId);
        session.setDailyNote(trimToNull(form.getDailyNote()));
        session.setTextbook(trimToNull(form.getTextbook()));
        session.setRangeText(trimToNull(form.getRangeText()));
        session.setContent(trimToNull(form.getContent()));
        session.setHomework(trimToNull(form.getHomework()));

        Teacher teacher = findTeacher(teacherId);

        Map<Long, Student> roster = new HashMap<>();
        for (Student student : currentStudents(session)) {
            roster.put(student.getId(), student);
        }
        Map<Long, Attendance> savedAttendance = new HashMap<>();
        for (Attendance attendance : attendanceRepository.findBySessionId(sessionId)) {
            savedAttendance.put(attendance.getStudent().getId(), attendance);
        }
        Map<Long, ProgressLog> savedProgress = savedProgress(sessionId);

        for (StudentEntryForm entry : uniqueEntries(form)) {
            Student student = roster.get(entry.getStudentId());
            if (student == null) {
                // 폼을 띄운 뒤 반에서 빠진 학생. 남의 반 학생 id 를 밀어 넣는 것도 여기서 막힌다
                continue;
            }
            applyAttendance(session, student, entry.getAttendance(), savedAttendance);
            applyProgress(session, student, entry, teacher, savedProgress);
        }
        return session;
    }

    /**
     * 학생당 한 줄만 남긴다. 같은 studentId 가 두 번 들어오면 뒤에 온 줄을 쓴다.
     *
     * 폼이 같은 학생을 두 줄 보내는 일이 실제로 생긴다. Enrollment 에는 유니크 키가 없어서
     * 배정 버튼을 두 번 누르면 endedOn 이 null 인 배정이 둘 생기고, 그러면 화면이 그 학생을 두 번 그린다.
     * 접지 않고 순회하면 출결·진도의 (sessionId, studentId) 유니크 제약에 걸려
     * 그 반의 회차 저장이 통째로 실패한다. restoreEntries 도 같은 방식으로 접는다.
     */
    private List<StudentEntryForm> uniqueEntries(SessionRecordForm form) {
        Map<Long, StudentEntryForm> byStudent = new LinkedHashMap<>();
        for (StudentEntryForm entry : form.getEntries()) {
            if (entry == null || entry.getStudentId() == null) {
                continue;
            }
            byStudent.put(entry.getStudentId(), entry);
        }
        return new ArrayList<>(byStudent.values());
    }

    /**
     * 미표시 학생은 행을 만들지 않는다. 확인하지 않은 것과 출석은 다른 정보다.
     * 미표시를 출석으로 채우거나 남겨 두면 리포트의 출석률이 거짓말을 한다.
     */
    private void applyAttendance(ClassSession session, Student student,
                                 AttendanceStatus status, Map<Long, Attendance> savedAttendance) {
        Attendance saved = savedAttendance.get(student.getId());
        if (status == null) {
            if (saved != null) {
                // 찍었다가 미표시로 되돌린 경우. 행을 남기면 화면에서 되돌릴 방법이 없어진다
                attendanceRepository.delete(saved);
                savedAttendance.remove(student.getId());
            }
            return;
        }
        if (saved == null) {
            Attendance created = Attendance.of(session, student, status);
            attendanceRepository.save(created);
            // 방금 만든 행을 맵에 함께 넣는다. 넣지 않으면 같은 학생이 한 번 더 들어올 때
            // 이 학생이 아직 없다고 보고 INSERT 를 또 내서 유니크 (sessionId, studentId) 에 걸린다
            savedAttendance.put(student.getId(), created);
            return;
        }
        // 유니크 (sessionId, studentId) 때문에 재저장 때 새로 만들면 저장이 통째로 실패한다
        saved.setStatus(status);
    }

    /**
     * 반 공통 진도를 이 학생의 진도기록으로 복사한다.
     *
     * 그날 이 학생에 대해 확인한 것이 하나도 없으면(출결 미표시 · 지난 과제 미확인 · 메모 없음)
     * 행을 만들지 않고, 이미 있던 행은 지운다. 진도만 복사된 유령 기록이 남으면
     * 리포트의 진도 흐름에 그날 왔는지도 모르는 수업이 실린다.
     */
    private void applyProgress(ClassSession session, Student student, StudentEntryForm entry,
                               Teacher teacher, Map<Long, ProgressLog> savedProgress) {
        String note = trimToNull(entry.getNote());
        boolean checked = entry.getAttendance() != null || entry.getHomeworkStatus() != null || note != null;

        ProgressLog log = savedProgress.get(student.getId());

        if (!checked) {
            if (log != null) {
                progressLogRepository.delete(log);
                savedProgress.remove(student.getId());
            }
            return;
        }
        if (log == null) {
            log = ProgressLog.of(session, student);
            log.setCreatedBy(teacher);
            // 출결과 같은 이유로 맵에 함께 넣는다. (sessionId, studentId) 가 유니크다
            savedProgress.put(student.getId(), log);
        }
        log.copyProgressFrom(session);
        log.setHomeworkStatus(entry.getHomeworkStatus());
        log.setNote(note);
        progressLogRepository.save(log);
    }

    /** 이 회차에 이미 적힌 진도기록. 학생 id 로 찾는다. 행이 없는 학생은 아직 아무것도 확인하지 않았다 */
    private Map<Long, ProgressLog> savedProgress(Long sessionId) {
        Map<Long, ProgressLog> logs = new HashMap<>();
        for (ProgressLog log : progressLogRepository.findBySessionId(sessionId)) {
            logs.put(log.getStudent().getId(), log);
        }
        return logs;
    }

    /** 이 회차에 이미 찍힌 출결. 행이 없는 학생은 미표시다 */
    private Map<Long, AttendanceStatus> savedStatuses(Long sessionId) {
        Map<Long, AttendanceStatus> statuses = new HashMap<>();
        for (Attendance attendance : attendanceRepository.findBySessionId(sessionId)) {
            statuses.put(attendance.getStudent().getId(), attendance.getStatus());
        }
        return statuses;
    }

    /** 회차에 속한 학생 = 그 반의 현재 소속 학생. 지난달에 나간 학생을 매일 보게 두지 않는다 */
    private List<Student> currentStudents(ClassSession session) {
        List<Enrollment> enrollments = enrollmentRepository
                .findByClassGroupIdAndEndedOnIsNullOrderByStudentNameAsc(session.getClassGroup().getId());
        List<Student> students = new ArrayList<>(enrollments.size());
        for (Enrollment enrollment : enrollments) {
            students.add(enrollment.getStudent());
        }
        return students;
    }

    private Teacher findTeacher(Long teacherId) {
        return teacherId == null ? null : teacherRepository.findById(teacherId).orElse(null);
    }

    /** 빈칸은 null 로 저장한다. 빈 문자열이 들어가면 목록의 빈 회차 판정과 화면의 ?: 표현이 어긋난다 */
    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
