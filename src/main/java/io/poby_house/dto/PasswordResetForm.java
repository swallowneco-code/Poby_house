package io.poby_house.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 원장이 남의 비밀번호를 새로 정해 준다.
 * 현재 비밀번호를 묻지 않는다. 본인이 잊어버려서 하는 일이기 때문이다.
 */
@Getter
@Setter
@NoArgsConstructor
public class PasswordResetForm {

    @NotBlank(message = "새 비밀번호를 입력하세요")
    @Size(min = 8, message = "비밀번호는 8자 이상이어야 합니다")
    private String newPassword;

    @NotBlank(message = "새 비밀번호를 한 번 더 입력하세요")
    private String newPasswordConfirm;

    public boolean isNewPasswordMatched() {
        return newPassword != null && newPassword.equals(newPasswordConfirm);
    }
}
