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
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 관찰기록. 1~4주에 한 번, 5분.
 *
 * kind=BASELINE 이면 입회 시점 스냅샷이다. 리포트에서 이것이 before 가 되고
 * 마지막 관찰이 after 가 된다. 그래서 기준선은 학생마다 가장 이른 것 하나만 의미가 있다.
 * understanding/homeworkRate 는 1~5, 5가 최상이다. 방향을 뒤집으면 지난 기록과 비교가 어긋난다.
 */
@Entity
@Table(name = "Observation")
@Getter
@Setter
@NoArgsConstructor
public class Observation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "studentId", nullable = false)
    private Student student;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ObservationKind kind = ObservationKind.PERIODIC;

    @Column(nullable = false)
    private LocalDate periodStart;

    /** 관찰 구간의 끝. 목록 정렬과 기준선/최신 판단이 전부 이 날짜를 쓴다 */
    @Column(nullable = false)
    private LocalDate periodEnd;

    /** 이해도 1~5 / 5 최상. 확인하지 않았으면 null 로 둔다 */
    private Integer understanding;

    /** 과제 수행도 1~5 / 5 최상 */
    private Integer homeworkRate;

    /** 태도와 특이사항 */
    private String attitudeNote;

    /** 잘하는 점. 근거 없는 칭찬은 리포트에 못 쓴다 */
    private String strength;

    /** 보완할 점. 처방 없는 지적은 리포트에 못 쓴다 */
    private String weakness;

    /** 학생 자기평가 한 줄. 아이 말 그대로 적는다. 요약하면 리포트에서 인용할 수 없다 */
    @Column(length = 500)
    private String selfAssessment;

    private String teacherComment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "createdBy")
    private Teacher createdBy;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    public static Observation of(Student student, ObservationKind kind,
                                LocalDate periodStart, LocalDate periodEnd) {
        Observation o = new Observation();
        o.student = student;
        o.kind = kind;
        o.periodStart = periodStart;
        o.periodEnd = periodEnd;
        return o;
    }

    public boolean isBaseline() {
        return kind == ObservationKind.BASELINE;
    }
}
