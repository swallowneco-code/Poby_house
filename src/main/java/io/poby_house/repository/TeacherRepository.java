package io.poby_house.repository;

import io.poby_house.domain.Teacher;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TeacherRepository extends JpaRepository<Teacher, Long> {

    Optional<Teacher> findByLoginId(String loginId);

    boolean existsByLoginId(String loginId);
}
