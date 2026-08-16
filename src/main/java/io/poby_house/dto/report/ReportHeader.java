package io.poby_house.dto.report;

import java.time.LocalDate;

/**
 * 리포트 머리말.
 *
 * anon(외부 반출 모드)에서 실명과 학교는 이 레코드에 <b>담기지 않는다</b>.
 * 화면에서 감추는 방식이 아니라 애초에 값을 넣지 않는 방식이어야 한다.
 * 화면에서만 감추면 텍스트 복사·인쇄·나중에 붙는 화면 중 하나만 빠뜨려도 실명이 그대로 나간다.
 * 실수로 실명을 외부로 내보내는 것이 이 앱에서 가장 큰 사고다.
 *
 * leaveReason 도 반출 모드에서는 뺀다. "OO중학교 앞 학원으로 옮김" 처럼
 * 학교나 사람 이름이 적혀 있는 칸이라 실명과 같은 취급을 해야 한다.
 */
public record ReportHeader(
        /** 반출 모드면 code, 아니면 실명 */
        String displayName,
        String code,
        /** 반출 모드면 null */
        String school,
        String grade,
        String statusLabel,
        LocalDate enrolledOn,
        LocalDate leftOn,
        /** 반출 모드면 null */
        String leaveReason,
        LocalDate from,
        LocalDate to,
        boolean anon,
        LocalDate generatedOn) {

    /** 지금 어느 모드인지 눈에 보여야 한다. 화면·인쇄물·복사한 텍스트가 같은 문장을 쓴다 */
    public String modeLabel() {
        return anon ? "외부 반출 모드 · 실명과 학교를 감췄습니다" : "내부 모드 · 실명이 그대로 보입니다";
    }

    /**
     * 반출 모드에서 함께 나가야 하는 단서. 반출 모드가 아니면 null 이다.
     *
     * modeLabel 만으로는 거짓이 된다. 머리말의 실명·학교는 이 레코드가 담지 않아 안전하지만,
     * 장면·관찰·상담·개입의 본문은 강사가 쓴 문장을 한 글자도 걸러내지 않고 그대로 옮긴다.
     * 그 문장에 "민수가 형 이름을 대며" 처럼 실명이 들어 있으면 반출 모드에서도 그대로 나간다.
     *
     * 문장을 여기 한 곳에 두는 이유: 화면에만 있고 '텍스트 복사'가 만든 평문에는 없어서
     * 실제로 메일로 나가는 물건에는 안전하다는 단정만 남는 사고가 있었다.
     * 리포트를 만든 사람과 검토·발송하는 사람이 다르면 두 번째 사람은 그 단정만 읽는다.
     */
    public String modeCaution() {
        return anon
                ? "본문(장면·관찰·상담·개입)에 실명이 적혀 있을 수 있습니다. 내보내기 전에 반드시 읽어 보세요."
                : null;
    }
}
