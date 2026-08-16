package io.poby_house.repository;

import io.poby_house.domain.Intervention;
import io.poby_house.domain.InterventionResult;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InterventionRepository extends JpaRepository<Intervention, Long> {

    /** 학생의 개입 이력. 카드마다 작성자를 쓰므로 함께 가져온다 */
    @EntityGraph(attributePaths = "createdBy")
    List<Intervention> findByStudentIdOrderByTriedOnDesc(Long studentId);

    /**
     * 결과 미확인 추적. result 가 null(아직 확인 안 함)인 것과
     * 넘긴 상태(보통 ONGOING 진행중)인 것을 함께 뽑는다.
     *
     * 오름차순인 것이 중요하다. 오래 방치된 시도가 위로 와야 추적 목록 구실을 한다.
     * 개입은 결과를 적지 않으면 리포트 3-1 에 한 줄도 못 나가므로 이 목록이 그것을 막는다.
     */
    @EntityGraph(attributePaths = {"student", "createdBy"})
    List<Intervention> findByResultOrResultIsNullOrderByTriedOnAsc(InterventionResult result);
}
