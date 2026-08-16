package io.poby_house.domain;

import jakarta.persistence.CascadeType;
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
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 평가 1건 (백지·누적·단원·총괄).
 *
 * type=BLANK 일 때만 4축(용어/조건/예외/인과)을 채운다. 1~5, 5가 최상이다.
 * sourceLocation 에는 원본 시험지의 "위치"만 적는다. 학원 자산인 시험지 자체는 반출하지 않는다.
 */
@Entity
@Table(name = "Assessment")
@Getter
@Setter
@NoArgsConstructor
public class Assessment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "studentId", nullable = false)
    private Student student;

    @Column(nullable = false)
    private LocalDate assessedOn;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AssessmentType type;

    @Column(length = 100)
    private String unitName;

    /** 득점. DECIMAL(5,1) 이라 0.5점 단위가 들어간다 */
    @Column(precision = 5, scale = 1)
    private BigDecimal score;

    @Column(precision = 5, scale = 1)
    private BigDecimal maxScore;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private UnitStatus unitStatus;

    /* ---- 백지 4축. BLANK 일 때만 채운다 ---- */

    private Integer blankTerm;

    private Integer blankCondition;

    private Integer blankException;

    private Integer blankCausality;

    /** 아이가 쓴 문장 한 줄 그대로. 다듬으면 리포트에서 인용할 수 없다 */
    @Column(length = 500)
    private String studentQuote;

    /** 원본 시험지 위치만 적는다 */
    @Column(length = 255)
    private String sourceLocation;

    private String note;

    /**
     * 오답 유형 태그. 평가와 생사가 같다.
     * 평가를 지웠는데 태그가 남으면 태그별 합계가 없는 시험의 오답까지 세게 된다.
     * 그래서 cascade=ALL + orphanRemoval 로 붙여 둔다.
     */
    @Setter(AccessLevel.NONE)
    @OneToMany(mappedBy = "assessment", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AssessmentErrorTag> errorTags = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "createdBy")
    private Teacher createdBy;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    public static Assessment of(Student student, LocalDate assessedOn, AssessmentType type) {
        Assessment a = new Assessment();
        a.student = student;
        a.assessedOn = assessedOn;
        a.type = type;
        return a;
    }

    /**
     * 태그를 붙인다. 개수가 0 이하면 붙이지 않는다.
     * (assessmentId, tagCode) 가 유니크라 같은 코드를 두 번 넣으면 저장 시점에 터진다.
     * 그래서 이미 있는 코드는 개수만 바꾼다.
     */
    public void putTag(ErrorTagCode code, int cnt) {
        if (code == null) {
            return;
        }
        for (AssessmentErrorTag tag : errorTags) {
            if (tag.getTagCode() == code) {
                if (cnt <= 0) {
                    errorTags.remove(tag);
                } else {
                    tag.setCnt(cnt);
                }
                return;
            }
        }
        if (cnt > 0) {
            errorTags.add(AssessmentErrorTag.of(this, code, cnt));
        }
    }

    /** 태그를 새 묶음으로 갈아 끼운다. 수정 폼이 매번 전체를 보내 오므로 이 방식이 맞다 */
    public void clearTags() {
        errorTags.clear();
    }

    /**
     * 사람이 문장으로 쓴 칸만 비운다. 보관 만료 파기가 부른다.
     *
     * 행을 지우지 않는 이유: 점수·오답 태그·단원 상태는 반 단위 통계와 지난 리포트의 근거라
     * 지우면 이미 내보낸 문서와 지금 화면이 어긋난다.
     * 그런데 studentQuote 는 아이가 쓴 문장 그대로이고 note 도 자유 입력이라
     * 그 안에 실명·학교가 섞여 들어간다. 익명화가 학생 칸만 지우면 이 문장들이 그대로 남는다.
     * unitName 은 교과 단원 이름이라 개인을 특정하지 않고, 리포트의 단원 상태 표가 그 값을 쓴다.
     */
    public void scrubText() {
        this.studentQuote = null;
        this.note = null;
    }

    /** 비울 자유 텍스트가 있는가. 파기 확인 화면이 "몇 건이 비워지는가" 를 세는 데 쓴다 */
    public boolean hasFreeText() {
        return notBlank(studentQuote) || notBlank(note);
    }

    private boolean notBlank(String s) {
        return s != null && !s.isBlank();
    }

    /** 만점이 있을 때만 백분율. 없으면 null 이라 화면이 '—' 를 쓴다 */
    public Integer getPercent() {
        if (score == null || maxScore == null || maxScore.signum() == 0) {
            return null;
        }
        return score.multiply(BigDecimal.valueOf(100))
                .divide(maxScore, 0, RoundingMode.HALF_UP)
                .intValue();
    }

    /** "18 / 20" 형태. 둘 중 하나라도 없으면 null */
    public String getScoreText() {
        if (score == null) {
            return null;
        }
        return maxScore == null
                ? score.stripTrailingZeros().toPlainString()
                : score.stripTrailingZeros().toPlainString() + " / " + maxScore.stripTrailingZeros().toPlainString();
    }
}
