package io.poby_house.service;

import io.poby_house.domain.Assessment;
import io.poby_house.domain.ClassGroup;
import io.poby_house.domain.Enrollment;
import io.poby_house.domain.Intervention;
import io.poby_house.domain.InterventionResult;
import io.poby_house.domain.ProgressLog;
import io.poby_house.domain.Student;
import io.poby_house.domain.StudentStatus;
import io.poby_house.dto.LeaveForm;
import io.poby_house.dto.PurgeForm;
import io.poby_house.dto.StudentForm;
import io.poby_house.repository.AssessmentRepository;
import io.poby_house.repository.ConsultationRepository;
import io.poby_house.repository.EnrollmentRepository;
import io.poby_house.repository.InterventionRepository;
import io.poby_house.repository.ObservationRepository;
import io.poby_house.repository.ProgressLogRepository;
import io.poby_house.repository.SceneRepository;
import io.poby_house.repository.StudentRepository;
import io.poby_house.repository.TeacherRepository;
import io.poby_house.support.BusinessRuleException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class StudentServiceTest {

    private static final Long STUDENT_ID = 1L;

    @Mock
    StudentRepository studentRepository;

    @Mock
    EnrollmentRepository enrollmentRepository;

    @Mock
    TeacherRepository teacherRepository;

    @Mock
    ObservationRepository observationRepository;

    @Mock
    SceneRepository sceneRepository;

    @Mock
    ConsultationRepository consultationRepository;

    @Mock
    AssessmentRepository assessmentRepository;

    @Mock
    InterventionRepository interventionRepository;

    @Mock
    ProgressLogRepository progressLogRepository;

    @InjectMocks
    StudentService studentService;

    private StudentForm form(String name, LocalDate enrolledOn) {
        StudentForm form = new StudentForm();
        form.setName(name);
        form.setEnrolledOn(enrolledOn);
        return form;
    }

    /** teacherId 를 null 로 넘긴다. 마지막 수정자는 여기서 검증할 규칙이 아니다 */
    private Student create(StudentForm form) {
        given(studentRepository.save(any(Student.class))).willAnswer(inv -> inv.getArgument(0));
        return studentService.create(form, null);
    }

    private Student student(String name) {
        Student student = new Student();
        student.setId(STUDENT_ID);
        student.assignCode("S2020001");
        student.setName(name);
        student.setSchool("대치초");
        student.setMemo("알러지 있음");
        student.setEnrolledOn(LocalDate.of(2020, 3, 2));
        return student;
    }

    /** 보관 기간이 이미 지난 퇴원생. 파기 대상이다 */
    private Student purgeReadyStudent() {
        Student student = student("김민준");
        student.markLeft(LocalDate.of(2020, 8, 18), "이사");
        return student;
    }

    private void givenStudent(Student student) {
        given(studentRepository.findById(STUDENT_ID)).willReturn(Optional.of(student));
    }

    private Enrollment enrollment(Student student, LocalDate startedOn) {
        ClassGroup group = new ClassGroup();
        group.setId(9L);
        group.setName("화목 A반");
        return Enrollment.of(student, group, startedOn);
    }

    private LeaveForm leaveForm(LocalDate leftOn, String reason) {
        LeaveForm form = new LeaveForm();
        form.setLeftOn(leftOn);
        form.setLeaveReason(reason);
        return form;
    }

    private PurgeForm purgeForm(String confirmName, boolean purgeTexts) {
        PurgeForm form = new PurgeForm();
        form.setConfirmName(confirmName);
        form.setPurgeTexts(purgeTexts);
        return form;
    }

    @Test
    @DisplayName("그 해 첫 학생이면 001 번을 받는다")
    void nextCode_firstOfYear() {
        given(studentRepository.maxCodeSequence(eq("S2026"), anyInt())).willReturn(null);

        assertThat(create(form("김민준", LocalDate.of(2026, 3, 2))).getCode()).isEqualTo("S2026001");
    }

    @Test
    @DisplayName("마지막 번호 다음을 받는다")
    void nextCode_afterExisting() {
        given(studentRepository.maxCodeSequence(eq("S2026"), anyInt())).willReturn(7);

        assertThat(create(form("김민준", LocalDate.of(2026, 3, 2))).getCode()).isEqualTo("S2026008");
    }

    @Test
    @DisplayName("999 를 넘겨도 번호가 멈추지 않는다")
    void nextCode_pastNineHundredNinetyNine() {
        given(studentRepository.maxCodeSequence(eq("S2026"), anyInt())).willReturn(999);

        // 문자열 정렬로 마지막 code 를 찾던 예전 방식은 여기서 S2026999 가 S20261000 보다
        // 크다고 판단해 같은 번호를 다시 발급하려 들었다
        assertThat(create(form("김민준", LocalDate.of(2026, 3, 2))).getCode()).isEqualTo("S20261000");
    }

    @Test
    @DisplayName("등록일 연도를 쓴다. 등록일이 없으면 올해")
    void nextCode_usesEnrolledYear() {
        given(studentRepository.maxCodeSequence(eq("S2025"), anyInt())).willReturn(null);

        assertThat(create(form("김민준", LocalDate.of(2025, 12, 31))).getCode()).isEqualTo("S2025001");
    }

    @Test
    @DisplayName("빈 입력칸은 null 로 저장한다. 화면에서 — 로 보여야 한다")
    void apply_blankBecomesNull() {
        given(studentRepository.maxCodeSequence(eq("S2026"), anyInt())).willReturn(null);
        StudentForm form = form("김민준", LocalDate.of(2026, 3, 2));
        form.setSchool("");
        form.setGrade("   ");
        form.setMemo("");

        Student student = create(form);

        assertThat(student.getSchool()).isNull();
        assertThat(student.getGrade()).isNull();
        assertThat(student.getMemo()).isNull();
    }

    @Test
    @DisplayName("식별번호는 한 번 정하면 다시 발급하지 않는다")
    void assignCode_onlyOnce() {
        Student student = new Student();
        student.assignCode("S2026001");

        assertThatThrownBy(() -> student.assignCode("S2026002"))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("퇴원 처리하면 보관 만료일이 퇴원일 + 3년으로 잡힌다")
    void leave_setsPurgeAfter() {
        Student student = student("김민준");
        givenStudent(student);
        given(enrollmentRepository.findByStudentIdAndEndedOnIsNull(STUDENT_ID)).willReturn(Optional.empty());

        studentService.leave(STUDENT_ID, leaveForm(LocalDate.of(2026, 8, 16), "이사"), null);

        assertThat(student.getStatus()).isEqualTo(StudentStatus.LEFT);
        assertThat(student.getLeftOn()).isEqualTo(LocalDate.of(2026, 8, 16));
        assertThat(student.getLeaveReason()).isEqualTo("이사");
        assertThat(student.getPurgeAfter()).isEqualTo(LocalDate.of(2029, 8, 16));
    }

    @Test
    @DisplayName("퇴원하면 현재 반 배정도 같은 날짜로 끝난다")
    void leave_endsCurrentEnrollment() {
        Student student = student("김민준");
        givenStudent(student);
        Enrollment current = enrollment(student, LocalDate.of(2026, 3, 2));
        given(enrollmentRepository.findByStudentIdAndEndedOnIsNull(STUDENT_ID)).willReturn(Optional.of(current));

        studentService.leave(STUDENT_ID, leaveForm(LocalDate.of(2026, 8, 16), "이사"), null);

        // 이 한 줄이 없어서 퇴원생이 반 목록에 유령으로 남았다.
        // endedOn 이 null 인 동안은 반 인원과 배정 후보 계산이 계속 이 학생을 센다
        assertThat(current.getEndedOn()).isEqualTo(LocalDate.of(2026, 8, 16));
        assertThat(current.isCurrent()).isFalse();
    }

    @Test
    @DisplayName("배정 시작보다 이른 퇴원일을 넣어도 배정 종료일이 시작보다 앞서지 않는다")
    void leave_doesNotCreateNegativeEnrollmentSpan() {
        Student student = student("김민준");
        givenStudent(student);
        Enrollment current = enrollment(student, LocalDate.of(2026, 3, 2));
        given(enrollmentRepository.findByStudentIdAndEndedOnIsNull(STUDENT_ID)).willReturn(Optional.of(current));

        studentService.leave(STUDENT_ID, leaveForm(LocalDate.of(2026, 1, 5), null), null);

        // 함께한 기간을 세는 리포트가 음수 일수를 받으면 안 된다
        assertThat(current.getEndedOn()).isEqualTo(LocalDate.of(2026, 3, 2));
    }

    @Test
    @DisplayName("휴원은 반 배정을 유지한다. 돌아올 자리다")
    void pause_keepsEnrollment() {
        Student student = student("김민준");
        givenStudent(student);

        studentService.pause(STUDENT_ID, null);

        assertThat(student.getStatus()).isEqualTo(StudentStatus.PAUSED);
        // 배정을 찾지도 않는다. 찾아서 끊으면 복귀할 때 어느 반이었는지를 사람이 기억해야 한다
        then(enrollmentRepository).should(never()).findByStudentIdAndEndedOnIsNull(any());
    }

    @Test
    @DisplayName("휴원은 퇴원 정보를 만들지 않는다")
    void pause_doesNotTouchLeaveFields() {
        Student student = student("김민준");
        givenStudent(student);

        studentService.pause(STUDENT_ID, null);

        assertThat(student.getLeftOn()).isNull();
        assertThat(student.getPurgeAfter()).isNull();
    }

    @Test
    @DisplayName("재원 복귀는 퇴원일·사유·보관 만료일을 지운다")
    void resume_clearsLeaveInfo() {
        Student student = purgeReadyStudent();
        givenStudent(student);

        studentService.resume(STUDENT_ID, null);

        assertThat(student.getStatus()).isEqualTo(StudentStatus.ACTIVE);
        assertThat(student.getLeftOn()).isNull();
        assertThat(student.getLeaveReason()).isNull();
        // 남겨 두면 다니는 학생이 파기 목록에 올라온다
        assertThat(student.getPurgeAfter()).isNull();
    }

    @Test
    @DisplayName("파기는 실명·학교·특이사항을 지우고 식별번호는 남긴다")
    void anonymize_keepsCodeOnly() {
        Student student = purgeReadyStudent();
        givenStudent(student);

        studentService.anonymize(STUDENT_ID, purgeForm("김민준", false), null);

        assertThat(student.getCode()).isEqualTo("S2020001");
        assertThat(student.getName()).isEqualTo("S2020001");
        assertThat(student.getSchool()).isNull();
        assertThat(student.getMemo()).isNull();
        assertThat(student.getLeaveReason()).isNull();
        // 목록에서 사라지게 하는 표시
        assertThat(student.getPurgeAfter()).isNull();
        // 퇴원했다는 사실 자체는 지울 대상이 아니다
        assertThat(student.getStatus()).isEqualTo(StudentStatus.LEFT);
    }

    @Test
    @DisplayName("파기해도 학생 행은 지우지 않는다. 지우면 출결·진도가 CASCADE 로 함께 사라진다")
    void anonymize_doesNotDeleteStudentRow() {
        Student student = purgeReadyStudent();
        givenStudent(student);

        studentService.anonymize(STUDENT_ID, purgeForm("김민준", false), null);

        then(studentRepository).should(never()).delete(any(Student.class));
        then(studentRepository).should(never()).deleteById(any());
    }

    @Test
    @DisplayName("체크하지 않으면 어느 기록의 자유 텍스트도 건드리지 않는다")
    void anonymize_keepsTextRecordsByDefault() {
        Student student = purgeReadyStudent();
        givenStudent(student);

        studentService.anonymize(STUDENT_ID, purgeForm("김민준", false), null);

        then(observationRepository).should(never()).deleteAll(anyList());
        then(sceneRepository).should(never()).deleteAll(anyList());
        then(consultationRepository).should(never()).deleteAll(anyList());
        // 평가·개입·진도는 읽지도 않는다. 읽어서 비우면 체크하지 않은 사람의 기록이 사라진다
        then(assessmentRepository).should(never()).findByStudentIdOrderByAssessedOnDesc(any());
        then(interventionRepository).should(never()).findByStudentIdOrderByTriedOnDesc(any());
        then(progressLogRepository).should(never()).findByStudentIdOrderBySessionSessionDateDesc(any());
    }

    @Test
    @DisplayName("체크하면 평가·개입·진도의 문장 칸도 비운다. 점수·결과 코드는 남는다")
    void anonymize_scrubsFreeTextOfRecordsThatStay() {
        Student student = purgeReadyStudent();
        givenStudent(student);
        given(observationRepository.findByStudentIdOrderByPeriodEndDesc(STUDENT_ID)).willReturn(List.of());
        given(sceneRepository.findByStudentIdOrderByOccurredOnDesc(STUDENT_ID)).willReturn(List.of());
        given(consultationRepository.findByStudentIdOrderByConsultedOnDesc(STUDENT_ID)).willReturn(List.of());

        Assessment assessment = new Assessment();
        assessment.setScore(BigDecimal.valueOf(18));
        assessment.setStudentQuote("민준이가 형 이름을 대며 비교했다");
        assessment.setNote("대치초 진도와 맞춤");
        given(assessmentRepository.findByStudentIdOrderByAssessedOnDesc(STUDENT_ID)).willReturn(List.of(assessment));

        Intervention intervention = new Intervention();
        intervention.setProblem("형과 비교하며 자기를 낮춘다");
        intervention.setAction("지난 회차의 자기 답과 견주게 했다");
        intervention.setResultNote("다음 주에 스스로 그 말을 했다");
        intervention.setResult(InterventionResult.WORKED);
        given(interventionRepository.findByStudentIdOrderByTriedOnDesc(STUDENT_ID)).willReturn(List.of(intervention));

        ProgressLog log = new ProgressLog();
        log.setTextbook("개념원리");
        log.setNote("대치초 시험 기간이라 늦게 왔다");
        given(progressLogRepository.findByStudentIdOrderBySessionSessionDateDesc(STUDENT_ID))
                .willReturn(List.of(log));

        studentService.anonymize(STUDENT_ID, purgeForm("김민준", true), null);

        // 원장은 실명 제거를 목적으로 체크한다. 여기서 문장이 남으면 화면이 거짓을 알린 것이 된다
        assertThat(assessment.getStudentQuote()).isNull();
        assertThat(assessment.getNote()).isNull();
        assertThat(intervention.getResultNote()).isNull();
        // problem·action 은 DB 가 NOT NULL 이라 비우는 대신 지웠다는 표시를 남긴다
        assertThat(intervention.getProblem()).isEqualTo(Intervention.PURGED_TEXT);
        assertThat(intervention.getAction()).isEqualTo(Intervention.PURGED_TEXT);
        assertThat(log.getNote()).isNull();

        // 점수·결과 코드·반 공통 진도는 통계와 이미 내보낸 리포트의 근거라 그대로 남는다
        assertThat(assessment.getScore()).isEqualByComparingTo("18");
        assertThat(intervention.getResult()).isEqualTo(InterventionResult.WORKED);
        assertThat(log.getTextbook()).isEqualTo("개념원리");
    }

    @Test
    @DisplayName("체크하면 관찰·장면·상담을 함께 지운다. 자유 텍스트에 실명이 섞일 수 있다")
    void anonymize_purgesTextRecordsWhenAsked() {
        Student student = purgeReadyStudent();
        givenStudent(student);
        given(observationRepository.findByStudentIdOrderByPeriodEndDesc(STUDENT_ID)).willReturn(List.of());
        given(sceneRepository.findByStudentIdOrderByOccurredOnDesc(STUDENT_ID)).willReturn(List.of());
        given(consultationRepository.findByStudentIdOrderByConsultedOnDesc(STUDENT_ID)).willReturn(List.of());

        studentService.anonymize(STUDENT_ID, purgeForm("김민준", true), null);

        then(observationRepository).should().deleteAll(anyList());
        then(sceneRepository).should().deleteAll(anyList());
        then(consultationRepository).should().deleteAll(anyList());
    }

    @Test
    @DisplayName("이름을 잘못 입력하면 파기하지 않는다. 화면의 JS 를 신뢰하지 않는다")
    void anonymize_rejectsWrongName() {
        Student student = purgeReadyStudent();
        givenStudent(student);

        assertThatThrownBy(() -> studentService.anonymize(STUDENT_ID, purgeForm("김민서", false), null))
                .isInstanceOf(BusinessRuleException.class);

        assertThat(student.getName()).isEqualTo("김민준");
        assertThat(student.getPurgeAfter()).isNotNull();
    }

    @Test
    @DisplayName("보관 기간이 남아 있으면 파기하지 않는다")
    void anonymize_rejectsBeforePurgeDate() {
        Student student = student("김민준");
        student.markLeft(LocalDate.now(), "이사");
        givenStudent(student);

        assertThatThrownBy(() -> studentService.anonymize(STUDENT_ID, purgeForm("김민준", false), null))
                .isInstanceOf(BusinessRuleException.class);

        assertThat(student.getName()).isEqualTo("김민준");
    }

    @Test
    @DisplayName("퇴원하지 않은 학생은 파기 대상이 아니다")
    void anonymize_rejectsActiveStudent() {
        Student student = student("김민준");
        givenStudent(student);

        assertThatThrownBy(() -> studentService.anonymize(STUDENT_ID, purgeForm("김민준", false), null))
                .isInstanceOf(BusinessRuleException.class);
    }
}
