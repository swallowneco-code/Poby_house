package io.poby_house.repository;

import io.poby_house.domain.Assessment;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AssessmentRepository extends JpaRepository<Assessment, Long> {

    /**
     * 학생의 평가 이력.
     * 목록에서 오답 태그 칩을 함께 그리므로 태그까지 한 번에 가져온다.
     * 이것을 빼면 평가 건수만큼 태그 조회가 다시 나간다.
     */
    @EntityGraph(attributePaths = {"createdBy", "errorTags"})
    List<Assessment> findByStudentIdOrderByAssessedOnDesc(Long studentId);

    /** 평가 한 건 + 태그. 수정 폼이 태그를 그대로 다시 보여 줘야 하므로 함께 가져온다 */
    @EntityGraph(attributePaths = "errorTags")
    Optional<Assessment> findWithTagsById(Long id);
}
