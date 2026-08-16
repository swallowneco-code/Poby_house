package io.poby_house.service;

import io.poby_house.domain.Enrollment;
import io.poby_house.domain.Student;
import io.poby_house.domain.StudentStatus;
import io.poby_house.dto.StudentForm;
import io.poby_house.repository.EnrollmentRepository;
import io.poby_house.repository.StudentRepository;
import io.poby_house.support.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StudentService {

    private final StudentRepository studentRepository;
    private final EnrollmentRepository enrollmentRepository;

    /** 상태와 검색어는 함께 걸린다. 검색한다고 상태 필터가 풀리지 않는다 */
    public List<Student> findAll(StudentStatus status, String keyword) {
        return studentRepository.search(status, StringUtils.hasText(keyword) ? keyword.trim() : null);
    }

    public Student get(Long id) {
        return studentRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("학생을 찾을 수 없습니다. id=" + id));
    }

    /** 이 학생이 거쳐 온 반. 최근 배정이 위로 */
    public List<Enrollment> enrollmentHistory(Long studentId) {
        return enrollmentRepository.findByStudentIdOrderByStartedOnDesc(studentId);
    }

    @Transactional
    public Student create(StudentForm form) {
        Student student = new Student();
        student.setCode(nextCode(form.getEnrolledOn()));
        apply(student, form);
        return studentRepository.save(student);
    }

    @Transactional
    public Student update(Long id, StudentForm form) {
        Student student = get(id);
        apply(student, form);
        return student;
    }

    private void apply(Student student, StudentForm form) {
        student.setName(form.getName().trim());
        student.setSchool(blankToNull(form.getSchool()));
        student.setGrade(blankToNull(form.getGrade()));
        student.setEnrolledOn(form.getEnrolledOn());
        student.setMemo(blankToNull(form.getMemo()));

        StudentStatus status = form.getStatus() == null ? StudentStatus.ACTIVE : form.getStatus();
        if (status == StudentStatus.LEFT) {
            LocalDate leftOn = form.getLeftOn() == null ? LocalDate.now() : form.getLeftOn();
            student.markLeft(leftOn, form.getLeaveReason());
        } else {
            student.setStatus(status);
            student.setLeftOn(null);
            student.setLeaveReason(blankToNull(form.getLeaveReason()));
            student.setPurgeAfter(null);
        }
    }

    /**
     * 빈 입력칸은 "" 로 들어온다. 그대로 두면 상세 화면의 ?: '—' 를 통과해
     * 값이 없다는 표시 대신 빈칸이 보인다.
     */
    private String blankToNull(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }

    /**
     * S + 등록연도 + 3자리 일련번호. 예: S2026001
     * 같은 해에 이미 발급된 마지막 번호 다음을 쓴다.
     *
     * 번호는 숫자로 뽑는다. 예전처럼 code 를 문자열로 정렬해 마지막 것을 파싱하면
     * 두 가지가 깨졌다.
     *   - 999 를 넘기면 S2026999 가 S20261000 보다 크다고 나와 번호가 멈춘다
     *   - code 에 형식에 맞지 않는 값이 하나라도 섞이면 parseInt 가 터지면서
     *     학생 등록 자체가 전면 불능이 된다
     */
    private String nextCode(LocalDate enrolledOn) {
        int year = (enrolledOn == null ? LocalDate.now() : enrolledOn).getYear();
        String prefix = "S" + year;
        Integer max = studentRepository.maxCodeSequence(prefix, prefix.length() + 1);
        return prefix + String.format("%03d", (max == null ? 0 : max) + 1);
    }
}
