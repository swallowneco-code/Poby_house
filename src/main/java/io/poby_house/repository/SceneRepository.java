package io.poby_house.repository;

import io.poby_house.domain.Scene;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SceneRepository extends JpaRepository<Scene, Long> {

    /** 학생의 장면 목록. 카드마다 작성자를 쓰므로 함께 가져온다 */
    @EntityGraph(attributePaths = "createdBy")
    List<Scene> findByStudentIdOrderByOccurredOnDesc(Long studentId);
}
