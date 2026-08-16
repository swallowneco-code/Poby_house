package io.poby_house.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 오답 유형 태그. 태그 분포가 곧 처방이다.
 *
 * 평가 1건에 코드별로 한 행이다. (assessmentId, tagCode) 가 유니크라
 * 같은 코드를 두 행으로 넣을 수 없다. 개수는 cnt 로 올린다.
 * 생성은 Assessment.putTag 를 거친다. 여기서 직접 new 로 만들면 그 유니크 제약을 넘기 쉽다.
 */
@Entity
@Table(name = "AssessmentErrorTag")
@Getter
@Setter
@NoArgsConstructor
public class AssessmentErrorTag {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assessmentId", nullable = false)
    private Assessment assessment;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ErrorTagCode tagCode;

    /** 해당 유형 오답 개수 */
    @Column(nullable = false)
    private int cnt = 1;

    static AssessmentErrorTag of(Assessment assessment, ErrorTagCode tagCode, int cnt) {
        AssessmentErrorTag tag = new AssessmentErrorTag();
        tag.assessment = assessment;
        tag.tagCode = tagCode;
        tag.cnt = cnt;
        return tag;
    }
}
