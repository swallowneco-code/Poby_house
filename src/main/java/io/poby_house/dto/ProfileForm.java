package io.poby_house.dto;

import io.poby_house.domain.Teacher;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** 내 계정. 스스로 바꿀 수 있는 것은 이름과 비밀번호뿐이다 */
@Getter
@Setter
@NoArgsConstructor
public class ProfileForm {

    @NotBlank(message = "이름을 입력하세요")
    private String name;

    public static ProfileForm from(Teacher teacher) {
        ProfileForm form = new ProfileForm();
        form.setName(teacher.getName());
        return form;
    }
}
