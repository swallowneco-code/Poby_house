package io.poby_house.dto;

import io.poby_house.domain.ErrorTagCode;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 오답 유형 태그 한 줄.
 *
 * 폼은 7종 전체를 늘 보내 온다. 개수 0 인 것은 서버에서 버려 행을 만들지 않는다.
 * (assessmentId, tagCode) 가 유니크라 "0개짜리 행" 을 남겨 두면
 * 나중에 태그별 합계에 0 이 섞이고, 안 틀린 유형까지 처방 목록에 올라온다.
 *
 * code 를 hidden 으로 함께 받는 이유: 자리(index)로 코드를 짐작하지 않는다.
 * ErrorTagCode 상수 순서를 나중에 바꾸면 화면의 3번째 칸이 다른 유형에 조용히 저장된다.
 */
@Getter
@Setter
@NoArgsConstructor
public class ErrorTagInput {

    private ErrorTagCode code;

    /**
     * 이 유형의 오답 개수. 0 은 "이 유형은 아니었다" 이고 저장하지 않는다.
     * 스테퍼가 0 아래로 내려가지 않지만 숫자 칸은 직접 고칠 수 있으므로 여기서도 막는다.
     */
    @Min(value = 0, message = "오답 개수는 0보다 작을 수 없습니다")
    @Max(value = 99, message = "한 시험에서 같은 유형은 99개까지 셉니다")
    private Integer cnt = 0;

    public static ErrorTagInput of(ErrorTagCode code, int cnt) {
        ErrorTagInput input = new ErrorTagInput();
        input.code = code;
        input.cnt = cnt;
        return input;
    }
}
