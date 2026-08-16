package io.poby_house.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 학생. 컬럼명은 DB와 1:1로 맞춘다(camelCase).
 * ddl-auto=none 이라 이 클래스가 테이블을 만들지 않는다. db/schema.sql 이 원본이다.
 */
@Entity
@Table(name = "Student")
@Getter
@Setter
@NoArgsConstructor
public class Student {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 외부로 나가는 문서에 실명 대신 쓰는 식별번호. S2026001 형태.
     * setter 를 만들지 않는다. 발급은 assignCode 로만 한다.
     */
    @Setter(AccessLevel.NONE)
    @Column(nullable = false, unique = true, length = 20)
    private String code;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(length = 50)
    private String school;

    /** 초4, 중1(자유학년) 등. 반 편성과 무관하다 */
    @Column(length = 20)
    private String grade;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StudentStatus status = StudentStatus.ACTIVE;

    @Column(nullable = false)
    private LocalDate enrolledOn;

    private LocalDate leftOn;

    @Column(length = 255)
    private String leaveReason;

    /** 보관 만료일. 퇴원일 + 3년 */
    private LocalDate purgeAfter;

    private String memo;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    /**
     * 식별번호는 발급할 때 한 번만 정한다.
     * 외부 문서에 실명 대신 나가는 번호라 나중에 바뀌면 지난 문서와 어긋난다.
     * 그래서 setter 대신 이 메서드를 두고, 이미 있으면 거절한다.
     */
    public void assignCode(String code) {
        if (this.code != null) {
            throw new IllegalStateException("식별번호는 다시 발급하지 않습니다. code=" + this.code);
        }
        this.code = code;
    }

    /** 퇴원 처리 시 보관 만료일을 함께 세운다 */
    public void markLeft(LocalDate leftOn, String reason) {
        this.status = StudentStatus.LEFT;
        this.leftOn = leftOn;
        this.leaveReason = reason;
        this.purgeAfter = leftOn == null ? null : leftOn.plusYears(3);
    }
}
