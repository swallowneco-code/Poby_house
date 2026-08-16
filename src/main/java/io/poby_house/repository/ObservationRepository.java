package io.poby_house.repository;

import io.poby_house.domain.Observation;
import io.poby_house.domain.ObservationKind;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ObservationRepository extends JpaRepository<Observation, Long> {

    /** 학생의 관찰 이력. 카드마다 작성자를 쓰므로 함께 가져온다 */
    @EntityGraph(attributePaths = "createdBy")
    List<Observation> findByStudentIdOrderByPeriodEndDesc(Long studentId);

    /**
     * 기준선. BASELINE 을 넘겨 쓴다.
     * 오름차순으로 첫 건을 잡는다. 기준선을 실수로 두 번 만들었어도
     * 리포트의 before 는 가장 이른 것이어야 한다. 최신을 잡으면 성장 폭이 줄어든다.
     */
    Optional<Observation> findFirstByStudentIdAndKindOrderByPeriodEndAsc(Long studentId, ObservationKind kind);

    /** 가장 최근 관찰. 리포트의 after 이자 학생 상세의 요약이다 */
    Optional<Observation> findFirstByStudentIdOrderByPeriodEndDesc(Long studentId);

    /**
     * 학생별 마지막 관찰일. 대시보드에서 "관찰이 밀린 학생" 을 찾는 데 쓴다.
     * 학생 수만큼 조회를 돌리면 재원생이 늘 때마다 대시보드가 느려진다.
     *
     * @return [studentId(Long), periodEnd(LocalDate)] 행 목록. 관찰이 하나도 없는 학생은 행이 없다
     */
    @Query("""
           select o.student.id, max(o.periodEnd)
           from Observation o
           group by o.student.id
           """)
    List<Object[]> latestPeriodEndByStudent();
}
