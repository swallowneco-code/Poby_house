package io.poby_house.dto;

import io.poby_house.domain.TeacherRole;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** 강사 계정 생성. 아이디는 만들 때만 정하고 이후로는 바꾸지 않는다 */
@Getter
@Setter
@NoArgsConstructor
public class TeacherForm {

    @NotBlank(message = "이름을 입력하세요")
    private String name;

    @NotBlank(message = "아이디를 입력하세요")
    @Pattern(regexp = "^[a-zA-Z0-9_]{4,20}$", message = "아이디는 영문·숫자·밑줄 4~20자입니다")
    private String loginId;

    @NotBlank(message = "비밀번호를 입력하세요")
    @Size(min = 8, message = "비밀번호는 8자 이상이어야 합니다")
    private String password;

    @NotBlank(message = "비밀번호를 한 번 더 입력하세요")
    private String passwordConfirm;

    @NotNull(message = "권한을 고르세요")
    private TeacherRole role = TeacherRole.TEACHER;

    public boolean isPasswordMatched() {
        return password != null && password.equals(passwordConfirm);
    }
}
