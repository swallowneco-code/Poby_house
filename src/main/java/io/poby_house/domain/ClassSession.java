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
 * 수업 회차 = 반 × 날짜. 그날 수업 한 칸이다.
 * dailyNote 는 화면에서 "수업 메모"로 부른다. 반 전체에 있었던 일을 적는다.
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

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "classGroupId", nullable = false)
    private ClassGroup classGroup;

    @Column(nullable = false)
    private LocalDate sessionDate;

    private LocalTime startTime;

    private LocalTime endTime;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "teacherId")
    private Teacher teacher;

    /** 수업 메모 - 그날 반 전체에 있었던 일 */
    private String dailyNote;

    /* ---- 진도는 반 공통이다. 한 반이 같은 진도를 나가므로 학생별로 쪼개지 않는다 ---- */

    @Column(length = 100)
    private String textbook;

    @Column(length = 100)
    private String rangeText;

    @Column(length = 500)
    private String content;

    @Column(length = 500)
    private String homework;

    /**
     * 숨긴 시각. 지우지 않고 목록에서만 뺀다.
     * 같은 날짜로 다시 열면 되살아나므로 되돌릴 길이 항상 있다.
     */
    private LocalDateTime hiddenAt;

    public boolean isHidden() {
        return hiddenAt != null;
    }

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    public static ClassSession of(ClassGroup group, LocalDate date, Teacher teacher) {
        ClassSession s = new ClassSession();
        s.classGroup = group;
        s.sessionDate = date;
        s.startTime = group.getStartTime();
        s.endTime = group.getEndTime();
        s.teacher = teacher;
        return s;
    }
}
