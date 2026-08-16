package io.poby_house.service;

import io.poby_house.domain.Student;
import io.poby_house.domain.StudentStatus;
import io.poby_house.dto.StudentForm;
import io.poby_house.repository.EnrollmentRepository;
import io.poby_house.repository.StudentRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class StudentServiceTest {

    @Mock
    StudentRepository studentRepository;

    @Mock
    EnrollmentRepository enrollmentRepository;

    @InjectMocks
    StudentService studentService;

    private StudentForm form(String name, LocalDate enrolledOn) {
        StudentForm form = new StudentForm();
        form.setName(name);
        form.setEnrolledOn(enrolledOn);
        form.setStatus(StudentStatus.ACTIVE);
        return form;
    }

    private Student create(StudentForm form) {
        given(studentRepository.save(any(Student.class))).willAnswer(inv -> inv.getArgument(0));
        return studentService.create(form);
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
    @DisplayName("퇴원 처리하면 보관 만료일이 퇴원일 + 3년으로 잡힌다")
    void markLeft_setsPurgeAfter() {
        given(studentRepository.maxCodeSequence(eq("S2026"), anyInt())).willReturn(null);
        StudentForm form = form("김민준", LocalDate.of(2026, 3, 2));
        form.setStatus(StudentStatus.LEFT);
        form.setLeftOn(LocalDate.of(2026, 8, 16));
        form.setLeaveReason("이사");

        Student student = create(form);

        assertThat(student.getStatus()).isEqualTo(StudentStatus.LEFT);
        assertThat(student.getPurgeAfter()).isEqualTo(LocalDate.of(2029, 8, 16));
        assertThat(student.getLeaveReason()).isEqualTo("이사");
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

        org.assertj.core.api.Assertions
                .assertThatThrownBy(() -> student.assignCode("S2026002"))
                .isInstanceOf(IllegalStateException.class);
    }
}
