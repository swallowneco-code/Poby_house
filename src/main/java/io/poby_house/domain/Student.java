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

    /**
     * 상태는 수정 폼으로 바꾸지 않는다.
     * 퇴원·휴원·복귀는 되돌리기 어려운 조작이라 각각 전용 화면과 아래 메서드를 지난다.
     * setter 를 남겨 두는 이유는 JPA 와 예외적인 데이터 손질뿐이다.
     */
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
     * 마지막으로 이 학생을 고친 강사.
     * 강사가 여러 명이라 같은 학생을 서로 만진다. 누가 고쳤는지가 남지 않으면
     * 값이 이상해졌을 때 물어볼 사람을 찾지 못한다.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "updatedBy")
    private Teacher updatedBy;

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

    /**
     * 휴원. 퇴원 정보는 건드리지 않는다.
     * 반 배정도 그대로 둔다 — 휴원은 돌아올 자리를 비워 두는 상태다.
     * 여기서 배정까지 끊으면 복귀할 때 어느 반이었는지를 사람이 기억해서 다시 넣어야 한다.
     */
    public void pause() {
        this.status = StudentStatus.PAUSED;
    }

    /**
     * 재원으로 되돌린다. 퇴원 취소도 이 길로 온다.
     *
     * 그래서 퇴원일·사유·보관 만료일을 함께 지운다.
     * 남겨 두면 재원생인데 보관 만료일이 박혀 있어, 파기 목록에 다니는 학생이 올라온다.
     */
    public void resume() {
        this.status = StudentStatus.ACTIVE;
        this.leftOn = null;
        this.leaveReason = null;
        this.purgeAfter = null;
    }

    /**
     * 보관 기간이 끝난 퇴원생에게서 개인을 특정하는 정보만 지운다. 학생 행은 남긴다.
     *
     * 행을 삭제하면 Enrollment · Attendance · ProgressLog 가 CASCADE 로 함께 사라진다.
     * 그러면 지난 회차의 인원과 반 단위 통계가 소급해서 바뀌어,
     * 이미 내보낸 리포트와 지금 화면이 어긋난다. 그래서 지우는 것이 아니라 익명화한다.
     * 남는 것은 식별번호(code)와 날짜·상태뿐이라 개인을 특정할 수 없고,
     * 그래서 보관 기간 방침과도 어긋나지 않는다.
     *
     * 이름을 code 로 덮는 이유: 화면과 목록이 name 을 그대로 그리기 때문이다.
     * null 로 두면 상세·목록이 빈 줄로 뜨고, DB 도 name 을 NOT NULL 로 잡고 있다.
     *
     * purgeAfter 를 비우는 것이 "처리했다"는 표시다. 남겨 두면 파기 목록에 계속 뜬다.
     * status 는 LEFT 로 유지한다. 퇴원했다는 사실 자체는 지울 대상이 아니다.
     */
    public void anonymize() {
        this.name = this.code;
        this.school = null;
        this.memo = null;
        this.leaveReason = null;
        this.purgeAfter = null;
    }
}
