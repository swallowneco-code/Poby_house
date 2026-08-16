package io.poby_house.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * 강사 계정.
 *
 * @Setter 를 열어 두지 않는다. 특히 password 가 그렇다.
 * setPassword 가 public 이면 BCrypt 를 거치지 않은 평문 저장이 그냥 컴파일된다.
 * schema.sql 이 "암호화 저장 (평문 금지)" 라고 못박아 둔 것과 정면으로 어긋난다.
 */
@Entity
@Table(name = "Teacher")
@Getter
@NoArgsConstructor
public class Teacher {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String loginId;

    /** BCrypt 해시. 평문을 넣지 않는다 */
    @Column(nullable = false, length = 255)
    private String password;

    @Column(nullable = false, length = 50)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TeacherRole role = TeacherRole.TEACHER;

    @Column(nullable = false)
    private boolean active = true;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    public static Teacher of(String loginId, String encodedPassword, String name, TeacherRole role) {
        Teacher teacher = new Teacher();
        teacher.loginId = loginId;
        teacher.password = encodedPassword;
        teacher.name = name;
        teacher.role = role;
        teacher.active = true;
        return teacher;
    }

    /** 반드시 인코딩된 값을 넣는다. 이름으로 그 사실을 남긴다 */
    public void changePassword(String encodedPassword) {
        this.password = encodedPassword;
    }

    public void rename(String name) {
        this.name = name;
    }

    public void changeRole(TeacherRole role) {
        this.role = role;
    }

    /** 계정을 지우지 않고 로그인만 막는다. 지난 기록의 작성자가 남아 있어야 한다 */
    public void changeActive(boolean active) {
        this.active = active;
    }
}
