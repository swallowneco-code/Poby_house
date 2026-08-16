package io.poby_house.repository;

import io.poby_house.domain.Enrollment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {

    /** 현재 이 반에 소속된 배정 */
    List<Enrollment> findByClassGroupIdAndEndedOnIsNullOrderByStudentNameAsc(Long classGroupId);

    /** 이 학생이 거쳐 온 반 */
    List<Enrollment> findByStudentIdOrderByStartedOnDesc(Long studentId);

    /** 이 학생이 지금 어느 반이든 소속돼 있는가 */
    Optional<Enrollment> findByStudentIdAndEndedOnIsNull(Long studentId);

    /**
     * 반별 현재 인원을 한 번에 센다.
     * 반 목록에서 반 개수만큼 count 쿼리를 돌리던 것을 이 하나로 대신한다.
     */
    @Query("""
           select e.classGroup.id, count(e)
           from Enrollment e
           where e.endedOn is null
           group by e.classGroup.id
           """)
    List<Object[]> countCurrentByClassGroup();
}
