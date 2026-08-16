package io.poby_house.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 보관 만료 파기 확인.
 *
 * 이름을 직접 입력받는다. 버튼 하나로 끝나면 목록에서 옆줄을 눌러 엉뚱한 학생을 지운다.
 * 화면에서 JS 로 버튼을 잠그지만 검증은 서버에서도 한다.
 * JS 를 신뢰하면 크롬 개발자도구도, 뒤로 가서 다시 제출하는 것도 다 통과한다.
 */
@Getter
@Setter
@NoArgsConstructor
public class PurgeForm {

    @NotBlank(message = "확인을 위해 학생 이름을 입력하세요")
    private String confirmName;

    /**
     * 기록에 적힌 자유 텍스트까지 함께 없앨지.
     *
     * 범위는 사람이 문장으로 쓴 칸 전부다. 관찰·장면·상담은 문장이 기록의 본체라서 행째로 지우고,
     * 평가(아이가 쓴 문장·메모)·개입(문제·시도·근거)·진도(학생 메모)는 점수와 코드를 남겨야 하므로
     * 그 칸만 비운다. 일부만 처리하면 화면은 "실명을 지웠다" 고 알리면서 실명이 남는다.
     *
     * 기본은 false 다. 이 기록들은 퇴원생 리포트의 재료라서 없애면 되돌릴 수 없다.
     * 다만 자유 텍스트에는 실명이 섞여 들어갈 수 있어서(형·동생 이름, 학교 이름 등)
     * 그것까지 없애야 하는 경우를 선택으로 남긴다.
     */
    private boolean purgeTexts;
}
