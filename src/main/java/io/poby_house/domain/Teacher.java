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
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "Teacher")
@Getter
@Setter
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
}
