package io.poby_house.repository;

import io.poby_house.domain.Enrollment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {

    /** 현재 이 반에 소속된 배정 */
    List<Enrollment> findByClassGroupIdAndEndedOnIsNullOrderByStudentNameAsc(Long classGroupId);

    /** 이 반의 전체 이력 (나간 학생 포함) */
    List<Enrollment> findByClassGroupIdOrderByEndedOnAscStudentNameAsc(Long classGroupId);

    /** 이 학생이 거쳐 온 반 */
    List<Enrollment> findByStudentIdOrderByStartedOnDesc(Long studentId);

    Optional<Enrollment> findByStudentIdAndClassGroupIdAndEndedOnIsNull(Long studentId, Long classGroupId);

    boolean existsByStudentIdAndClassGroupIdAndEndedOnIsNull(Long studentId, Long classGroupId);

    long countByClassGroupIdAndEndedOnIsNull(Long classGroupId);
}
