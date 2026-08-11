package io.poby_house.service;

import io.poby_house.domain.ClassGroup;
import io.poby_house.domain.Enrollment;
import io.poby_house.domain.Student;
import io.poby_house.domain.StudentStatus;
import io.poby_house.domain.Teacher;
import io.poby_house.dto.ClassGroupForm;
import io.poby_house.repository.ClassGroupRepository;
import io.poby_house.repository.EnrollmentRepository;
import io.poby_house.repository.StudentRepository;
import io.poby_house.repository.TeacherRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ClassGroupService {

    private final ClassGroupRepository classGroupRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final StudentRepository studentRepository;
    private final TeacherRepository teacherRepository;

    public List<ClassGroup> findAll() {
        return classGroupRepository.findAllByOrderByActiveDescNameAsc();
    }

    public List<ClassGroup> findActive() {
        return classGroupRepository.findByActiveTrueOrderByNameAsc();
    }

    public ClassGroup get(Long id) {
        return classGroupRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("반을 찾을 수 없습니다. id=" + id));
    }

    public long countCurrentStudents(Long classGroupId) {
        return enrollmentRepository.countByClassGroupIdAndEndedOnIsNull(classGroupId);
    }

    /** 현재 이 반에 소속된 배정 */
    public List<Enrollment> currentEnrollments(Long classGroupId) {
        return enrollmentRepository.findByClassGroupIdAndEndedOnIsNullOrderByStudentNameAsc(classGroupId);
    }

    /** 아직 이 반에 없는 재원생. 배정 후보 목록 */
    public List<Student> assignableStudents(Long classGroupId) {
        List<Long> already = currentEnrollments(classGroupId).stream()
                .map(e -> e.getStudent().getId())
                .toList();
        return studentRepository.findByStatusOrderByNameAsc(StudentStatus.ACTIVE).stream()
                .filter(s -> !already.contains(s.getId()))
                .toList();
    }

    @Transactional
    public ClassGroup create(ClassGroupForm form, Long teacherId) {
        ClassGroup group = new ClassGroup();
        apply(group, form);
        if (teacherId != null) {
            teacherRepository.findById(teacherId).ifPresent(group::setTeacher);
        }
        return classGroupRepository.save(group);
    }

    @Transactional
    public ClassGroup update(Long id, ClassGroupForm form) {
        ClassGroup group = get(id);
        apply(group, form);
        return group;
    }

    private void apply(ClassGroup group, ClassGroupForm form) {
        group.setName(form.getName().trim());
        group.setLevel(form.getLevel());
        group.setDayOfWeek(form.getDayOfWeek());
        group.setStartTime(form.getStartTime());
        group.setEndTime(form.getEndTime());
        group.setActive(form.isActive());
    }

    @Transactional
    public void assignStudent(Long classGroupId, Long studentId, LocalDate startedOn) {
        if (enrollmentRepository.existsByStudentIdAndClassGroupIdAndEndedOnIsNull(studentId, classGroupId)) {
            return;
        }
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("학생을 찾을 수 없습니다. id=" + studentId));
        ClassGroup group = get(classGroupId);
        enrollmentRepository.save(Enrollment.of(student, group, startedOn == null ? LocalDate.now() : startedOn));
    }

    /** 반에서 빼되 기록은 남긴다. 지우지 않고 종료일만 찍는다 */
    @Transactional
    public void releaseStudent(Long enrollmentId, LocalDate endedOn) {
        Enrollment enrollment = enrollmentRepository.findById(enrollmentId)
                .orElseThrow(() -> new IllegalArgumentException("배정 기록을 찾을 수 없습니다. id=" + enrollmentId));
        enrollment.setEndedOn(endedOn == null ? LocalDate.now() : endedOn);
    }
}
