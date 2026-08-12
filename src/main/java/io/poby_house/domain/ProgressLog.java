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

import java.time.LocalDateTime;

/**
 * 학생 × 회차 기록. 매 수업 30초 안에 채우는 것이 목표다.
 * note 는 화면에서 "학생 메모"로 부른다. 눈에 띈 것만 적고 없으면 비워 둔다.
 */
@Entity
@Table(name = "ProgressLog")
@Getter
@Setter
@NoArgsConstructor
public class ProgressLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "sessionId", nullable = false)
    private ClassSession session;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "studentId", nullable = false)
    private Student student;

    /** 교재명 */
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

    /** 지난 시간 과제를 해 왔는가 */
    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private HomeworkStatus homeworkStatus;

    /** 학생 메모 - 그날 이 학생에게 눈에 띈 것 */
    @Column(length = 500)
    private String note;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "createdBy")
    private Teacher createdBy;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    public static ProgressLog of(ClassSession session, Student student) {
        ProgressLog p = new ProgressLog();
        p.session = session;
        p.student = student;
        return p;
    }

    /** 아무것도 안 적힌 기록인가. 빈 줄까지 저장할 필요는 없다 */
    public boolean isBlank() {
        return isEmpty(textbook) && isEmpty(rangeText) && isEmpty(content)
                && isEmpty(homework) && isEmpty(note) && homeworkStatus == null;
    }

    private boolean isEmpty(String s) {
        return s == null || s.isBlank();
    }
}
