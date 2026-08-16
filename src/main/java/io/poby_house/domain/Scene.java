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

/**
 * 장면. 분기 1개면 충분하다.
 *
 * 시험지에 남지 않는 데이터다. 리포트 4-1 편지는 이것 없이 못 쓴다.
 * content 에는 성격("성실하다")이 아니라 행동과 말("남아서 3번을 다시 풀었다")을 적는다.
 * 성격을 적으면 몇 년 뒤에 읽을 때 근거가 남지 않는다.
 */
@Entity
@Table(name = "Scene")
@Getter
@Setter
@NoArgsConstructor
public class Scene {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "studentId", nullable = false)
    private Student student;

    @Column(nullable = false)
    private LocalDate occurredOn;

    /** 어떤 상황이었나 */
    @Column(length = 500)
    private String situation;

    /** 무엇을 했고 무슨 말을 했나 */
    @Column(nullable = false)
    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "createdBy")
    private Teacher createdBy;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    public static Scene of(Student student, LocalDate occurredOn, String content) {
        Scene s = new Scene();
        s.student = student;
        s.occurredOn = occurredOn;
        s.content = content;
        return s;
    }
}
