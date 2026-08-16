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
 * 학생 x 회차 기록. 매 수업 30초 안에 채우는 것이 목표다.
 *
 * textbook/rangeText/content/homework 는 회차의 반 공통 진도를 복사해 둔 값이다.
 * 참조로 두지 않고 복사하는 이유: 나중에 반 진도를 고쳐도 그날 이 학생이 무엇을 했는지는
 * 그대로 남아 있어야 한다. 퇴원생 리포트는 몇 년 전 기록을 그대로 인용한다.
 * note("학생 메모")만 이 학생 고유의 정보다. 눈에 띈 것만 적고 없으면 비워 둔다.
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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sessionId", nullable = false)
    private ClassSession session;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "studentId", nullable = false)
    private Student student;

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

    /** 지난 시간에 내준 과제를 해 왔는가 */
    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private HomeworkStatus homeworkStatus;

    /**
     * 학생 메모 - 그날 이 학생에게 눈에 띈 것.
     * content 는 반 공통 진도라서 개인 관찰을 담을 칸이 없다. 그 칸이 이것이다.
     */
    @Column(length = 500)
    private String note;

    @ManyToOne(fetch = FetchType.LAZY)
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

    /** 회차의 반 공통 진도를 이 학생 기록으로 옮긴다 */
    public void copyProgressFrom(ClassSession source) {
        this.textbook = source.getTextbook();
        this.rangeText = source.getRangeText();
        this.content = source.getContent();
        this.homework = source.getHomework();
    }

    /**
     * 학생 메모만 비운다. 보관 만료 파기가 부른다.
     *
     * note 는 "그날 이 학생에게 눈에 띈 것" 이라 실명·학교가 섞여 들어간다.
     * 나머지 네 칸(교재·범위·오늘 한 것·과제)은 반 공통 진도를 복사한 값이라
     * 이 학생만의 정보가 아니고, 비우면 그날 반이 무엇을 했는지가 이 학생에게서만 사라진다.
     * 행 자체도 남긴다. 지우면 출석한 회차 수와 진도 흐름이 소급해서 바뀐다.
     */
    public void scrubText() {
        this.note = null;
    }

    /** 아무것도 안 적힌 기록인가. 빈 줄까지 저장하면 학생 기록 목록이 빈 행으로 뒤덮인다 */
    public boolean isBlank() {
        return isEmpty(textbook) && isEmpty(rangeText) && isEmpty(content)
                && isEmpty(homework) && isEmpty(note) && homeworkStatus == null;
    }

    private boolean isEmpty(String s) {
        return s == null || s.isBlank();
    }
}
