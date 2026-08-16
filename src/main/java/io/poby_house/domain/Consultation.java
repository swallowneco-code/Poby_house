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

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 학부모 상담일지.
 *
 * 요약을 사실 / 걱정 / 약속 세 칸으로 나눈다. 한 칸에 몰아 적으면
 * 나중에 "내가 무엇을 하기로 했는지" 를 찾을 수 없고, 리포트의 약속 이행 항목이 빈다.
 * 가정 사정·형제 비교·경제 상황은 적지 않는다. 리포트에 쓸 수 없는 정보다.
 */
@Entity
@Table(name = "Consultation")
@Getter
@Setter
@NoArgsConstructor
public class Consultation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "studentId", nullable = false)
    private Student student;

    @Column(nullable = false)
    private LocalDate consultedOn;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private ConsultationType type;

    /** 사실 - 오간 내용 */
    private String factNote;

    /** 걱정 - 학부모가 우려한 것 */
    private String worryNote;

    /** 약속 - 내가 하기로 한 것 */
    private String promiseNote;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "createdBy")
    private Teacher createdBy;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    public static Consultation of(Student student, LocalDate consultedOn, ConsultationType type) {
        Consultation c = new Consultation();
        c.student = student;
        c.consultedOn = consultedOn;
        c.type = type;
        return c;
    }
}
