package io.poby_house.repository;

import io.poby_house.domain.ClassGroup;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ClassGroupRepository extends JpaRepository<ClassGroup, Long> {

    List<ClassGroup> findAllByOrderByActiveDescNameAsc();

    List<ClassGroup> findByActiveTrueOrderByNameAsc();
}
