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

import java.time.LocalDate;

/**
 * 학생이 어느 반에 언제부터 언제까지 있었는지.
 * 반을 옮겨도 과거 기록이 남도록 배정을 이력으로 관리한다.
 */
@Entity
@Table(name = "Enrollment")
@Getter
@Setter
@NoArgsConstructor
public class Enrollment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "studentId", nullable = false)
    private Student student;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "classGroupId", nullable = false)
    private ClassGroup classGroup;

    @Column(nullable = false)
    private LocalDate startedOn;

    /** null 이면 현재 소속중 */
    private LocalDate endedOn;

    public static Enrollment of(Student student, ClassGroup classGroup, LocalDate startedOn) {
        Enrollment e = new Enrollment();
        e.student = student;
        e.classGroup = classGroup;
        e.startedOn = startedOn;
        return e;
    }

    public boolean isCurrent() {
        return endedOn == null;
    }
}
