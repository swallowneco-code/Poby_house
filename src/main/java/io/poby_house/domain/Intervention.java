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
 * 개입 로그. 시도 -> 결과.
 *
 * 리포트 3-1 "통한 것 / 안 통한 것" 은 전적으로 이 테이블에서 나온다.
 * result 가 null 인 것은 "아직 확인하지 않았다" 다. ONGOING(진행중)은 확인해 본 결과이므로 다른 정보다.
 * 둘을 섞으면 결과 미확인 추적 목록이 무의미해진다.
 */
@Entity
@Table(name = "Intervention")
@Getter
@Setter
@NoArgsConstructor
public class Intervention {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "studentId", nullable = false)
    private Student student;

    @Column(nullable = false)
    private LocalDate triedOn;

    /** 무엇을 해결하려 했나 */
    @Column(nullable = false, length = 500)
    private String problem;

    /** 무엇을 시도했나 */
    @Column(nullable = false)
    private String action;

    /** null 이면 아직 결과를 확인하지 않았다 */
    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private InterventionResult result;

    /** 어떻게 알았나. 근거 없는 결과는 리포트에 쓸 수 없다 */
    private String resultNote;

    /** 결과를 확인한 날 */
    private LocalDate reviewedOn;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "createdBy")
    private Teacher createdBy;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    public static Intervention of(Student student, LocalDate triedOn, String problem, String action) {
        Intervention i = new Intervention();
        i.student = student;
        i.triedOn = triedOn;
        i.problem = problem;
        i.action = action;
        return i;
    }

    /**
     * 결과를 적는다. 확인한 날을 함께 남긴다.
     * reviewedOn 을 따로 두는 이유: 시도한 날과 결과를 안 날이 다르고,
     * 그 간격이 "며칠 지켜봤나" 라는 근거가 된다.
     */
    public void review(InterventionResult result, String resultNote, LocalDate reviewedOn) {
        this.result = result;
        this.resultNote = resultNote;
        this.reviewedOn = reviewedOn == null ? LocalDate.now() : reviewedOn;
    }

    /**
     * 파기된 자유 텍스트 자리에 남기는 표시.
     *
     * problem·action 은 DB 가 NOT NULL 이라 비울 수 없다. 빈 문자열로 두면
     * 화면의 ?: '—' 를 통과해 "안 적은 것" 과 "지운 것" 이 같아 보인다.
     */
    public static final String PURGED_TEXT = "(파기됨)";

    /**
     * 사람이 문장으로 쓴 칸만 비운다. 보관 만료 파기가 부른다.
     *
     * 행과 result(통함/일부/안통함)는 남긴다. 반 단위 통계와 지난 리포트가 그 코드를 세기 때문이다.
     * 반면 problem·action·resultNote 는 강사가 자유롭게 쓰는 칸이라
     * "형 이름을 대며 비교했다" 처럼 실명이 그대로 들어간다.
     */
    public void scrubText() {
        this.problem = PURGED_TEXT;
        this.action = PURGED_TEXT;
        this.resultNote = null;
    }

    /** 비울 자유 텍스트가 남아 있는가. 파기 확인 화면이 건수를 세는 데 쓴다 */
    public boolean hasFreeText() {
        return !PURGED_TEXT.equals(problem) || !PURGED_TEXT.equals(action)
                || (resultNote != null && !resultNote.isBlank());
    }

    /** 아직 결과를 확인하지 않았거나 진행중인가. 추적 목록이 이걸 본다 */
    public boolean isOpen() {
        return result == null || result == InterventionResult.ONGOING;
    }
}
