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

import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * 반. 학년이 아니라 진도(level) 기준으로 묶는다.
 * 자유학년제라 한 반에 여러 학년이 섞이는 것이 정상이다.
 */
@Entity
@Table(name = "ClassGroup")
@Getter
@Setter
@NoArgsConstructor
public class ClassGroup {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String name;

    /** 진도 기준 레벨. 예: 분수심화, 중등대수1 */
    @Column(length = 50)
    private String level;

    /** 월수금, 화목 등 자유 입력 */
    @Column(length = 20)
    private String dayOfWeek;

    private LocalTime startTime;

    private LocalTime endTime;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "teacherId")
    private Teacher teacher;

    @Column(nullable = false)
    private boolean active = true;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    /** "화목 15:00~16:30" 형태로 화면에 뿌린다 */
    public String getScheduleText() {
        StringBuilder sb = new StringBuilder();
        if (dayOfWeek != null && !dayOfWeek.isBlank()) {
            sb.append(dayOfWeek);
        }
        if (startTime != null) {
            if (sb.length() > 0) {
                sb.append(' ');
            }
            sb.append(startTime);
            if (endTime != null) {
                sb.append('~').append(endTime);
            }
        }
        return sb.length() == 0 ? "시간 미정" : sb.toString();
    }
}
