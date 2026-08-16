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
import io.poby_house.support.BusinessRuleException;
import io.poby_house.support.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    public ClassGroup get(Long id) {
        return classGroupRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("반을 찾을 수 없습니다. id=" + id));
    }

    /**
     * 반별 현재 인원. 목록 화면에서 한 번에 쓴다.
     * 반마다 count 를 돌리면 반 개수만큼 쿼리가 나간다.
     */
    public Map<Long, Long> currentHeadcounts() {
        Map<Long, Long> counts = new HashMap<>();
        for (Object[] row : enrollmentRepository.countCurrentByClassGroup()) {
            counts.put((Long) row[0], (Long) row[1]);
        }
        return counts;
    }

    /** 현재 이 반에 소속된 배정 */
    public List<Enrollment> currentEnrollments(Long classGroupId) {
        return enrollmentRepository.findByClassGroupIdAndEndedOnIsNullOrderByStudentNameAsc(classGroupId);
    }

    /**
     * 배정 후보. 한 학생은 한 반에만 속하므로
     * <b>어느 반이든</b> 이미 소속된 학생은 제외한다.
     */
    public List<Student> assignableStudents() {
        return studentRepository.findAssignable();
    }

    /** 아직 어느 반에도 안 들어간 재원생 수. 목록을 만들지 않고 센다 */
    public long unassignedCount() {
        return studentRepository.countAssignable();
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

    /**
     * 반에 학생을 넣는다. 이미 다른 반에 있으면 거절한다.
     * 화면에서 후보를 걸러 주지만, 창을 두 개 띄워 두는 경우를 막기 위해 여기서도 확인한다.
     */
    @Transactional
    public void assignStudent(Long classGroupId, Long studentId, LocalDate startedOn) {
        enrollmentRepository.findByStudentIdAndEndedOnIsNull(studentId).ifPresent(existing -> {
            if (existing.getClassGroup().getId().equals(classGroupId)) {
                throw new BusinessRuleException("이미 이 반에 있는 학생입니다.");
            }
            throw new BusinessRuleException(
                    existing.getStudent().getName() + " 학생은 이미 "
                            + existing.getClassGroup().getName() + "에 있습니다. 그 반에서 먼저 빼 주세요.");
        });
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new NotFoundException("학생을 찾을 수 없습니다. id=" + studentId));
        ClassGroup group = get(classGroupId);
        enrollmentRepository.save(Enrollment.of(student, group, startedOn == null ? LocalDate.now() : startedOn));
    }

    /** 반에서 빼되 기록은 남긴다. 지우지 않고 종료일만 찍는다 */
    @Transactional
    public void releaseStudent(Long enrollmentId, LocalDate endedOn) {
        Enrollment enrollment = enrollmentRepository.findById(enrollmentId)
                .orElseThrow(() -> new NotFoundException("배정 기록을 찾을 수 없습니다. id=" + enrollmentId));
        enrollment.setEndedOn(endedOn == null ? LocalDate.now() : endedOn);
    }
}
