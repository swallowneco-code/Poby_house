package io.poby_house.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * 수업 회차 = 반 x 날짜. 그날 수업 한 칸이다.
 *
 * 진도(textbook/rangeText/content/homework)를 회차에 두는 이유:
 * 한 반은 같은 진도를 나간다. 학생 수만큼 같은 문장을 다시 입력하게 만들면 아무도 안 쓴다.
 * 반 단위로 한 번 입력받아 학생별 ProgressLog 로 복사 기록하고, 반 단위 원본은 여기 남긴다.
 * 학생 개인에 대한 것은 ProgressLog.note("학생 메모")에 적는다.
 */
@Entity
@Table(name = "ClassSession")
@Getter
@Setter
@NoArgsConstructor
public class ClassSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "classGroupId", nullable = false)
    private ClassGroup classGroup;

    @Column(nullable = false)
    private LocalDate sessionDate;

    private LocalTime startTime;

    private LocalTime endTime;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "teacherId")
    private Teacher teacher;

    /** 수업 메모 - 그날 반 전체에 있었던 일 */
    private String dailyNote;

    /* ---- 아래 네 칸은 반 공통 진도다. 학생별로 쪼개 입력받지 않는다 ---- */

    @Column(length = 100)
    private String textbook;

    /** p.42~55 형태 */
    @Column(length = 100)
    private String rangeText;

    /** 오늘 한 것 */
    @Column(length = 500)
    private String content;

    /** 오늘 내준 과제 */
    @Column(length = 500)
    private String homework;

    /**
     * 숨긴 시각. 잘못 연 회차를 지우지 않고 목록에서만 뺀다.
     * 행을 지우면 딸린 출결과 진도기록이 CASCADE 로 함께 사라져 되돌릴 수 없다.
     * 같은 날짜로 다시 열면 되살아나므로 되돌릴 길이 항상 있다.
     */
    private LocalDateTime hiddenAt;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    /** 회차는 반의 수업 시간을 물려받는다. 매번 시간을 다시 찍게 하면 타수가 늘어난다 */
    public static ClassSession of(ClassGroup group, LocalDate date, Teacher teacher) {
        ClassSession s = new ClassSession();
        s.classGroup = group;
        s.sessionDate = date;
        s.startTime = group.getStartTime();
        s.endTime = group.getEndTime();
        s.teacher = teacher;
        return s;
    }

    public boolean isHidden() {
        return hiddenAt != null;
    }

    public void hide() {
        this.hiddenAt = LocalDateTime.now();
    }

    /** 같은 날짜로 다시 열면 숨김이 풀린다 */
    public void unhide() {
        this.hiddenAt = null;
    }

    /**
     * 교재와 범위를 한 줄로. 둘 다 비었으면 null 을 준다.
     * 빈 문자열을 주면 화면의 ?: 표현이 걸리지 않아 빈칸이 그대로 남는다.
     */
    public String getProgressText() {
        StringBuilder sb = new StringBuilder();
        if (textbook != null && !textbook.isBlank()) {
            sb.append(textbook);
        }
        if (rangeText != null && !rangeText.isBlank()) {
            if (sb.length() > 0) {
                sb.append(' ');
            }
            sb.append(rangeText);
        }
        return sb.length() == 0 ? null : sb.toString();
    }
}
