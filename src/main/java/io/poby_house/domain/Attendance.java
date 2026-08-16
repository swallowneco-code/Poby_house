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
 * 출결. 리포트 1-2 "함께한 시간"의 근거다.
 *
 * "아직 확인하지 않았다" 는 행을 만들지 않는 것으로 표현한다.
 * 그래서 status 에 기본값을 두지 않는다. 미표시를 출석으로 채우면 출석률이 거짓말을 한다.
 */
@Entity
@Table(name = "Attendance")
@Getter
@Setter
@NoArgsConstructor
public class Attendance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sessionId", nullable = false)
    private ClassSession session;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "studentId", nullable = false)
    private Student student;

    /** 기본값을 두지 않는다. 확인하지 않은 것과 출석은 다른 정보다 */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AttendanceStatus status;

    @Column(length = 255)
    private String note;

    public static Attendance of(ClassSession session, Student student, AttendanceStatus status) {
        Attendance a = new Attendance();
        a.session = session;
        a.student = student;
        a.status = status;
        return a;
    }
}
