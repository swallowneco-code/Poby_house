package io.poby_house.repository;

import io.poby_house.domain.Student;
import io.poby_house.domain.StudentStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface StudentRepository extends JpaRepository<Student, Long> {

    List<Student> findAllByOrderByStatusAscNameAsc();

    List<Student> findByStatusOrderByNameAsc(StudentStatus status);

    List<Student> findByNameContainingOrderByNameAsc(String keyword);

    /** 올해 발급된 마지막 식별번호. 다음 번호를 뽑을 때 쓴다 */
    Optional<Student> findTopByCodeStartingWithOrderByCodeDesc(String prefix);
}
