package io.poby_house.repository;

import io.poby_house.domain.AssessmentErrorTag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface AssessmentErrorTagRepository extends JpaRepository<AssessmentErrorTag, Long> {

    /** 평가 한 건에 붙은 태그. 태그만 필요할 때 평가를 다시 읽지 않는다 */
    List<AssessmentErrorTag> findByAssessmentId(Long assessmentId);

    /**
     * 기간 안의 태그별 오답 합계. 태그 분포가 곧 처방이고, 리포트 2-2 가 이 표를 쓴다.
     * 평가를 전부 끌어와 메모리에서 더하면 태그가 붙은 평가만큼 조회가 늘어난다.
     *
     * cnt 합계이므로 개수가 아니라 sum 이다. count 로 세면 "계산 오답 5개" 가 1로 뭉개진다.
     *
     * @return [ErrorTagCode, Long] 행 목록. 0인 태그는 아예 행이 없다
     */
    @Query("""
           select t.tagCode, sum(t.cnt)
           from AssessmentErrorTag t
           where t.assessment.student.id = :studentId
             and t.assessment.assessedOn between :from and :to
           group by t.tagCode
           """)
    List<Object[]> sumTagsByStudent(@Param("studentId") Long studentId,
                                   @Param("from") LocalDate from,
                                   @Param("to") LocalDate to);
}
