package io.poby_house.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * @Size 는 DB 의 VARCHAR 길이와 맞춘다. 이걸 빼면 긴 이름이 저장 시점에 MySQL 오류(1406)로 튀어서
 * 한국어 필드 오류 대신 500 화면이 뜬다. 최초 설정 화면에서 그러면 계정을 만들 길이 없어 보인다.
 * loginId 는 @Pattern 이 이미 4~20자로 묶어 두어 길이 제약을 따로 걸지 않는다.
 */
@Getter
@Setter
@NoArgsConstructor
public class SetupForm {

    @NotBlank(message = "이름을 입력하세요")
    @Size(max = 50, message = "이름은 50자까지 입력할 수 있습니다")
    private String name;

    @NotBlank(message = "아이디를 입력하세요")
    @Pattern(regexp = "^[a-zA-Z0-9_]{4,20}$", message = "아이디는 영문·숫자·밑줄 4~20자입니다")
    private String loginId;

    @NotBlank(message = "비밀번호를 입력하세요")
    @Size(min = 8, message = "비밀번호는 8자 이상이어야 합니다")
    private String password;

    @NotBlank(message = "비밀번호를 한 번 더 입력하세요")
    private String passwordConfirm;

    public boolean isPasswordMatched() {
        return password != null && password.equals(passwordConfirm);
    }
}
