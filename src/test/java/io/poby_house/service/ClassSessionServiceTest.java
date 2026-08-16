package io.poby_house.service;

import io.poby_house.domain.Attendance;
import io.poby_house.domain.AttendanceStatus;
import io.poby_house.domain.ClassGroup;
import io.poby_house.domain.ClassSession;
import io.poby_house.domain.Enrollment;
import io.poby_house.domain.HomeworkStatus;
import io.poby_house.domain.ProgressLog;
import io.poby_house.domain.Student;
import io.poby_house.dto.SessionRecordForm;
import io.poby_house.dto.StudentEntryForm;
import io.poby_house.repository.AttendanceRepository;
import io.poby_house.repository.ClassSessionRepository;
import io.poby_house.repository.EnrollmentRepository;
import io.poby_house.repository.ProgressLogRepository;
import io.poby_house.repository.TeacherRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class ClassSessionServiceTest {

    private static final long CLASS_ID = 7L;
    private static final long SESSION_ID = 3L;
    private static final long STUDENT_ID = 11L;
    private static final LocalDate DAY = LocalDate.of(2026, 8, 18);

    @Mock
    ClassSessionRepository sessionRepository;

    @Mock
    AttendanceRepository attendanceRepository;

    @Mock
    ProgressLogRepository progressLogRepository;

    @Mock
    EnrollmentRepository enrollmentRepository;

    @Mock
    TeacherRepository teacherRepository;

    @Mock
    ClassGroupService classGroupService;

    @InjectMocks
    ClassSessionService classSessionService;

    private final ClassGroup group = group();
    private final Student minjun = student(STUDENT_ID, "김민준");

    private ClassGroup group() {
        ClassGroup g = new ClassGroup();
        g.setId(CLASS_ID);
        g.setName("화목 A반");
        return g;
    }

    private Student student(long id, String name) {
        Student s = new Student();
        s.setId(id);
        s.setName(name);
        return s;
    }

    private ClassSession session() {
        ClassSession session = ClassSession.of(group, DAY, null);
        session.setId(SESSION_ID);
        return session;
    }

    /** 반 공통 진도가 채워진 폼. 교재 앞뒤 공백과 빈 수업 메모는 일부러 넣었다 */
    private SessionRecordForm form(StudentEntryForm... entries) {
        SessionRecordForm form = new SessionRecordForm();
        form.setTextbook(" 개념원리 ");
        form.setRangeText("p.42~55");
        form.setContent("분수의 나눗셈");
        form.setHomework("유형서 12~18번");
        form.setDailyNote("   ");
        for (StudentEntryForm entry : entries) {
            form.getEntries().add(entry);
        }
        return form;
    }

    private StudentEntryForm entry(Long studentId, AttendanceStatus status, HomeworkStatus homework, String note) {
        StudentEntryForm entry = new StudentEntryForm();
        entry.setStudentId(studentId);
        entry.setAttendance(status);
        entry.setHomeworkStatus(homework);
        entry.setNote(note);
        return entry;
    }

    private void givenRoster(ClassSession session) {
        given(sessionRepository.findById(SESSION_ID)).willReturn(Optional.of(session));
        given(enrollmentRepository.findByClassGroupIdAndEndedOnIsNullOrderByStudentNameAsc(CLASS_ID))
                .willReturn(List.of(Enrollment.of(minjun, group, DAY.minusMonths(2))));
    }

    @Test
    @DisplayName("같은 반 같은 날짜로 두 번 열면 회차는 하나다")
    void open_sameDayTwice() {
        ClassSession existing = session();
        given(sessionRepository.findByClassGroupIdAndSessionDate(CLASS_ID, DAY)).willReturn(Optional.of(existing));

        ClassSession opened = classSessionService.open(CLASS_ID, DAY, null);

        assertThat(opened).isSameAs(existing);
        then(sessionRepository).should(never()).save(any(ClassSession.class));
    }

    @Test
    @DisplayName("숨긴 회차를 같은 날짜로 다시 열면 되살아난다")
    void open_revivesHidden() {
        ClassSession hidden = session();
        hidden.hide();
        given(sessionRepository.findByClassGroupIdAndSessionDate(CLASS_ID, DAY)).willReturn(Optional.of(hidden));

        ClassSession opened = classSessionService.open(CLASS_ID, DAY, null);

        // 유니크 (classGroupId, sessionDate) 때문에 새로 만들 수도 없다. 되살리기가 유일한 길이다
        assertThat(opened.isHidden()).isFalse();
        then(sessionRepository).should(never()).save(any(ClassSession.class));
    }

    @Test
    @DisplayName("새 회차는 지난 회차의 교재를 이어받고 범위는 비운다")
    void open_inheritsTextbookOnly() {
        given(sessionRepository.findByClassGroupIdAndSessionDate(CLASS_ID, DAY)).willReturn(Optional.empty());
        given(classGroupService.get(CLASS_ID)).willReturn(group);
        ClassSession prev = ClassSession.of(group, DAY.minusDays(3), null);
        prev.setTextbook("개념원리");
        prev.setRangeText("p.30~41");
        given(sessionRepository.findByClassGroupIdAndHiddenAtIsNullOrderBySessionDateDesc(CLASS_ID))
                .willReturn(List.of(prev));
        given(sessionRepository.save(any(ClassSession.class))).willAnswer(inv -> inv.getArgument(0));

        ClassSession opened = classSessionService.open(CLASS_ID, DAY, null);

        assertThat(opened.getTextbook()).isEqualTo("개념원리");
        // 범위는 매번 다르다. 물려받으면 지난주 범위가 그대로 저장된다
        assertThat(opened.getRangeText()).isNull();
    }

    @Test
    @DisplayName("미표시 학생은 출결도 진도기록도 만들지 않는다")
    void save_unmarkedStudentLeavesNoRow() {
        ClassSession session = session();
        givenRoster(session);
        given(attendanceRepository.findBySessionId(SESSION_ID)).willReturn(List.of());
        given(progressLogRepository.findBySessionId(SESSION_ID)).willReturn(List.of());

        classSessionService.save(SESSION_ID, form(entry(STUDENT_ID, null, null, "  ")), null);

        // 미표시를 출석으로 채우거나 빈 진도기록을 남기면 리포트의 출석률과 진도 흐름이 거짓말을 한다
        then(attendanceRepository).should(never()).save(any(Attendance.class));
        then(progressLogRepository).should(never()).save(any(ProgressLog.class));
    }

    @Test
    @DisplayName("반 공통 진도는 학생별 진도기록으로 복사된다")
    void save_copiesClassProgressToStudent() {
        ClassSession session = session();
        givenRoster(session);
        given(attendanceRepository.findBySessionId(SESSION_ID)).willReturn(List.of());
        given(progressLogRepository.findBySessionId(SESSION_ID)).willReturn(List.of());

        classSessionService.save(SESSION_ID,
                form(entry(STUDENT_ID, AttendanceStatus.PRESENT, HomeworkStatus.PARTIAL, "  ")), null);

        ArgumentCaptor<ProgressLog> captor = ArgumentCaptor.forClass(ProgressLog.class);
        then(progressLogRepository).should().save(captor.capture());
        ProgressLog log = captor.getValue();
        assertThat(log.getStudent()).isSameAs(minjun);
        assertThat(log.getTextbook()).isEqualTo("개념원리");
        assertThat(log.getRangeText()).isEqualTo("p.42~55");
        assertThat(log.getContent()).isEqualTo("분수의 나눗셈");
        assertThat(log.getHomework()).isEqualTo("유형서 12~18번");
        assertThat(log.getHomeworkStatus()).isEqualTo(HomeworkStatus.PARTIAL);
        // 빈 학생 메모는 null. 빈 문자열이 남으면 화면의 ?: 표현이 안 걸린다
        assertThat(log.getNote()).isNull();

        // 반 공통 원본도 회차에 남는다. 앞뒤 공백은 지우고, 안 적은 수업 메모는 null 이다
        assertThat(session.getTextbook()).isEqualTo("개념원리");
        assertThat(session.getDailyNote()).isNull();
        then(attendanceRepository).should().save(any(Attendance.class));
    }

    @Test
    @DisplayName("찍었다가 미표시로 되돌리면 출결과 진도기록이 지워진다")
    void save_revertToUnmarkedRemovesRows() {
        ClassSession session = session();
        givenRoster(session);
        Attendance saved = Attendance.of(session, minjun, AttendanceStatus.PRESENT);
        given(attendanceRepository.findBySessionId(SESSION_ID)).willReturn(List.of(saved));
        ProgressLog savedLog = ProgressLog.of(session, minjun);
        given(progressLogRepository.findBySessionId(SESSION_ID)).willReturn(List.of(savedLog));

        classSessionService.save(SESSION_ID, form(entry(STUDENT_ID, null, null, null)), null);

        // 남겨 두면 화면에서 되돌릴 방법이 없고, 진도만 복사된 유령 기록이 리포트에 실린다
        then(attendanceRepository).should().delete(saved);
        then(progressLogRepository).should().delete(savedLog);
    }

    @Test
    @DisplayName("이 반 학생이 아닌 id 로 보낸 줄은 저장하지 않는다")
    void save_ignoresStudentOutsideClass() {
        ClassSession session = session();
        givenRoster(session);
        given(attendanceRepository.findBySessionId(SESSION_ID)).willReturn(List.of());

        classSessionService.save(SESSION_ID, form(entry(99L, AttendanceStatus.PRESENT, null, "남의 반 학생")), null);

        then(attendanceRepository).should(never()).save(any(Attendance.class));
        then(progressLogRepository).should(never()).save(any(ProgressLog.class));
    }

    @Test
    @DisplayName("같은 학생이 두 줄로 들어와도 출결·진도는 한 번만 저장한다")
    void save_foldsDuplicateStudentRows() {
        ClassSession session = session();
        givenRoster(session);
        given(attendanceRepository.findBySessionId(SESSION_ID)).willReturn(List.of());
        given(progressLogRepository.findBySessionId(SESSION_ID)).willReturn(List.of());

        // 배정 버튼을 두 번 누르면 endedOn 이 null 인 배정이 둘 생기고 화면이 같은 학생을 두 줄 그린다.
        // 두 줄을 그대로 저장하면 (sessionId, studentId) 유니크 위반으로 회차 저장이 통째로 실패한다
        classSessionService.save(SESSION_ID, form(
                entry(STUDENT_ID, AttendanceStatus.PRESENT, null, null),
                entry(STUDENT_ID, AttendanceStatus.LATE, null, null)), null);

        ArgumentCaptor<Attendance> captor = ArgumentCaptor.forClass(Attendance.class);
        then(attendanceRepository).should().save(captor.capture());
        // 뒤에 온 줄이 이긴다. 화면에서 마지막으로 찍은 값이 그것이다
        assertThat(captor.getValue().getStatus()).isEqualTo(AttendanceStatus.LATE);
        then(progressLogRepository).should().save(any(ProgressLog.class));
    }

    @Test
    @DisplayName("회차 숨기기는 지우지 않는다")
    void hide_keepsRow() {
        ClassSession session = session();
        given(sessionRepository.findById(SESSION_ID)).willReturn(Optional.of(session));

        ClassSession hidden = classSessionService.hide(SESSION_ID);

        assertThat(hidden.isHidden()).isTrue();
        // 행을 지우면 딸린 출결·진도기록이 CASCADE 로 함께 사라진다
        then(sessionRepository).should(never()).delete(any(ClassSession.class));
    }

    @Test
    @DisplayName("회차 폼의 학생 줄은 그 반의 현재 소속 학생이다")
    void buildForm_currentStudentsOnly() {
        ClassSession session = session();
        session.setTextbook("개념원리");
        givenRoster(session);
        given(attendanceRepository.findBySessionId(SESSION_ID))
                .willReturn(List.of(Attendance.of(session, minjun, AttendanceStatus.LATE)));
        given(progressLogRepository.findBySessionId(SESSION_ID)).willReturn(List.of());

        SessionRecordForm form = classSessionService.buildForm(SESSION_ID);

        assertThat(form.getTextbook()).isEqualTo("개념원리");
        assertThat(form.getEntries()).hasSize(1);
        StudentEntryForm entry = form.getEntries().get(0);
        assertThat(entry.getStudentId()).isEqualTo(STUDENT_ID);
        assertThat(entry.getStudentName()).isEqualTo("김민준");
        assertThat(entry.getAttendance()).isEqualTo(AttendanceStatus.LATE);
    }
}
