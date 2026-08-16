package io.poby_house.dto;

import io.poby_house.domain.Teacher;
import io.poby_house.domain.TeacherRole;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 강사 계정 수정.
 * 아이디와 비밀번호는 여기서 바꾸지 않는다.
 * 아이디를 바꾸면 세션에 남아 있는 로그인 정보와 자동 로그인 토큰이 함께 어긋난다.
 */
@Getter
@Setter
@NoArgsConstructor
public class TeacherEditForm {

    @NotBlank(message = "이름을 입력하세요")
    private String name;

    @NotNull(message = "권한을 고르세요")
    private TeacherRole role = TeacherRole.TEACHER;

    private boolean active = true;

    public static TeacherEditForm from(Teacher teacher) {
        TeacherEditForm form = new TeacherEditForm();
        form.setName(teacher.getName());
        form.setRole(teacher.getRole());
        form.setActive(teacher.isActive());
        return form;
    }
}
