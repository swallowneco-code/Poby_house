package io.poby_house.repository;

import io.poby_house.domain.Teacher;
import io.poby_house.domain.TeacherRole;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TeacherRepository extends JpaRepository<Teacher, Long> {

    Optional<Teacher> findByLoginId(String loginId);

    boolean existsByLoginId(String loginId);

    /** 계정 목록. 쓰는 계정을 위로, 중지한 계정을 아래로 */
    List<Teacher> findAllByOrderByActiveDescNameAsc();

    /** 마지막 원장이 사라지는 것을 막을 때 쓴다 */
    long countByRoleAndActiveTrue(TeacherRole role);
}
