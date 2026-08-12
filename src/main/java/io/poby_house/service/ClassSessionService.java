package io.poby_house.service;

import io.poby_house.domain.Attendance;
import io.poby_house.domain.ClassGroup;
import io.poby_house.domain.ClassSession;
import io.poby_house.domain.Enrollment;
import io.poby_house.domain.ProgressLog;
import io.poby_house.domain.Student;
import io.poby_house.domain.Teacher;
import io.poby_house.dto.SessionRecordForm;
import io.poby_house.dto.StudentEntryForm;
import io.poby_house.repository.AttendanceRepository;
import io.poby_house.repository.ClassSessionRepository;
import io.poby_house.repository.EnrollmentRepository;
import io.poby_house.repository.ProgressLogRepository;
import io.poby_house.repository.TeacherRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

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

    public ClassSession get(Long id) {
        return sessionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("회차를 찾을 수 없습니다. id=" + id));
    }

    public List<ClassSession> findByClassGroup(Long classGroupId) {
        return sessionRepository.findByClassGroupIdOrderBySessionDateDesc(classGroupId);
    }

    /** 뭔가 적힌 회차만. 빈 껍데기는 빼고 최근 순으로 */
    public List<ClassSession> findRecorded(Long classGroupId) {
        return sessionRepository.findRecordedByClassGroupId(classGroupId);
    }

    /** 기록이 있는 달 목록. 최근 달이 앞에 온다 */
    public List<YearMonth> recordedMonths(Long classGroupId) {
        return findRecorded(classGroupId).stream()
                .map(s -> YearMonth.from(s.getSessionDate()))
                .distinct()
                .sorted(Comparator.reverseOrder())
                .toList();
    }

    /** 같은 반 같은 날짜는 하나만 둔다. 이미 있으면 그걸 연다 */
    @Transactional
    public ClassSession createOrGet(Long classGroupId, LocalDate date, Long teacherId) {
        return sessionRepository.findByClassGroupIdAndSessionDate(classGroupId, date)
                .map(existing -> {
                    existing.setHiddenAt(null); // 숨겼던 회차를 같은 날짜로 다시 열면 되살린다
                    return existing;
                })
                .orElseGet(() -> {
                    ClassGroup group = classGroupService.get(classGroupId);
                    Teacher teacher = teacherId == null ? null
                            : teacherRepository.findById(teacherId).orElse(null);
                    ClassSession created = ClassSession.of(group, date, teacher);
                    // 새 회차는 지난 회차의 교재를 이어받는다. 매번 다시 치지 않게
                    sessionRepository
                            .findFirstByClassGroupIdAndSessionDateLessThanOrderBySessionDateDesc(classGroupId, date)
                            .ifPresent(prev -> created.setTextbook(prev.getTextbook()));
                    return sessionRepository.save(created);
                });
    }

    /**
     * 회차 입력 폼. 진도는 반 공통이라 위쪽에 한 벌,
     * 학생은 현재 소속된 순서대로 한 줄씩 늘어놓는다.
     */
    public SessionRecordForm buildForm(Long sessionId) {
        ClassSession session = get(sessionId);

        Map<Long, Attendance> attendanceByStudent = attendanceRepository.findBySessionId(sessionId).stream()
                .collect(Collectors.toMap(a -> a.getStudent().getId(), Function.identity()));
        Map<Long, ProgressLog> progressByStudent = progressLogRepository.findBySessionId(sessionId).stream()
                .collect(Collectors.toMap(p -> p.getStudent().getId(), Function.identity()));

        SessionRecordForm form = new SessionRecordForm();
        form.setDailyNote(session.getDailyNote());
        form.setTextbook(session.getTextbook());
        form.setRangeText(session.getRangeText());
        form.setContent(session.getContent());
        form.setHomework(session.getHomework());

        List<Enrollment> enrollments = enrollmentRepository
                .findByClassGroupIdAndEndedOnIsNullOrderByStudentNameAsc(session.getClassGroup().getId());

        for (Enrollment enrollment : enrollments) {
            Student student = enrollment.getStudent();
            StudentEntryForm entry = new StudentEntryForm();
            entry.setStudentId(student.getId());
            entry.setStudentName(student.getName());
            entry.setStudentGrade(student.getGrade());
            entry.setStudentCode(student.getCode());

            Attendance attendance = attendanceByStudent.get(student.getId());
            if (attendance != null) {
                entry.setAttendance(attendance.getStatus());
            }
            ProgressLog log = progressByStudent.get(student.getId());
            if (log != null) {
                entry.setNote(log.getNote());
            }
            form.getEntries().add(entry);
        }
        return form;
    }

    /** 목록에서만 뺀다. 출결·메모는 그대로 남는다 */
    @Transactional
    public ClassSession hide(Long sessionId) {
        ClassSession session = get(sessionId);
        session.setHiddenAt(LocalDateTime.now());
        return session;
    }

    @Transactional
    public void save(Long sessionId, SessionRecordForm form, Long teacherId) {
        ClassSession session = get(sessionId);
        session.setDailyNote(trimToNull(form.getDailyNote()));
        session.setTextbook(trimToNull(form.getTextbook()));
        session.setRangeText(trimToNull(form.getRangeText()));
        session.setContent(trimToNull(form.getContent()));
        session.setHomework(trimToNull(form.getHomework()));

        Teacher teacher = teacherId == null ? null : teacherRepository.findById(teacherId).orElse(null);

        Map<Long, Student> studentsInClass = enrollmentRepository
                .findByClassGroupIdAndEndedOnIsNullOrderByStudentNameAsc(session.getClassGroup().getId()).stream()
                .collect(Collectors.toMap(e -> e.getStudent().getId(), Enrollment::getStudent));

        for (StudentEntryForm entry : form.getEntries()) {
            if (entry == null || entry.getStudentId() == null) {
                continue;
            }
            Student student = studentsInClass.get(entry.getStudentId());
            if (student == null) {
                continue; // 폼을 띄운 뒤 반에서 빠진 학생
            }
            saveAttendance(session, student, entry);
            saveNote(session, student, entry, teacher);
        }
    }

    /** 미표시로 남긴 학생은 행을 만들지 않는다. 나중에 "확인 안 함"과 "출석"을 구분할 수 있어야 한다 */
    private void saveAttendance(ClassSession session, Student student, StudentEntryForm entry) {
        Attendance attendance = attendanceRepository
                .findBySessionIdAndStudentId(session.getId(), student.getId())
                .orElse(null);

        if (entry.getAttendance() == null) {
            if (attendance != null) {
                attendanceRepository.delete(attendance); // 찍었다가 되돌린 경우
            }
            return;
        }
        if (attendance == null) {
            attendance = Attendance.of(session, student);
        }
        attendance.setStatus(entry.getAttendance());
        attendanceRepository.save(attendance);
    }

    private void saveNote(ClassSession session, Student student, StudentEntryForm entry, Teacher teacher) {
        String note = trimToNull(entry.getNote());
        ProgressLog log = progressLogRepository
                .findBySessionIdAndStudentId(session.getId(), student.getId())
                .orElse(null);

        if (log == null) {
            if (note == null) {
                return; // 빈 줄까지 만들지 않는다
            }
            log = ProgressLog.of(session, student);
            log.setCreatedBy(teacher);
        }
        log.setNote(note);
        progressLogRepository.save(log);
    }

    private String trimToNull(String s) {
        if (s == null) {
            return null;
        }
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }
}
