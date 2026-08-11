package io.poby_house.service;

import io.poby_house.domain.Student;
import io.poby_house.domain.StudentStatus;
import io.poby_house.dto.StudentForm;
import io.poby_house.repository.StudentRepository;
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

    public List<Student> findAll(StudentStatus status, String keyword) {
        if (StringUtils.hasText(keyword)) {
            return studentRepository.findByNameContainingOrderByNameAsc(keyword.trim());
        }
        if (status != null) {
            return studentRepository.findByStatusOrderByNameAsc(status);
        }
        return studentRepository.findAllByOrderByStatusAscNameAsc();
    }

    public Student get(Long id) {
        return studentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("학생을 찾을 수 없습니다. id=" + id));
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
        student.setSchool(form.getSchool());
        student.setGrade(form.getGrade());
        student.setEnrolledOn(form.getEnrolledOn());
        student.setMemo(form.getMemo());

        StudentStatus status = form.getStatus() == null ? StudentStatus.ACTIVE : form.getStatus();
        if (status == StudentStatus.LEFT) {
            LocalDate leftOn = form.getLeftOn() == null ? LocalDate.now() : form.getLeftOn();
            student.markLeft(leftOn, form.getLeaveReason());
        } else {
            student.setStatus(status);
            student.setLeftOn(null);
            student.setLeaveReason(null);
            student.setPurgeAfter(null);
        }
    }

    /**
     * S + 등록연도 + 3자리 일련번호. 예: S2026001
     * 같은 해에 이미 발급된 마지막 번호 다음을 쓴다.
     */
    private String nextCode(LocalDate enrolledOn) {
        int year = (enrolledOn == null ? LocalDate.now() : enrolledOn).getYear();
        String prefix = "S" + year;
        int next = studentRepository.findTopByCodeStartingWithOrderByCodeDesc(prefix)
                .map(s -> Integer.parseInt(s.getCode().substring(prefix.length())) + 1)
                .orElse(1);
        return String.format("%s%03d", prefix, next);
    }
}
