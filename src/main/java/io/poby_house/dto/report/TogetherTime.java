package io.poby_house.dto.report;

import java.time.LocalDate;
import java.util.List;

/**
 * 리포트 1. 함께한 시간.
 *
 * 근거는 Attendance 집계 하나다. 여기 숫자를 눈대중으로 바꾸면 리포트 전체의 신뢰가 무너진다.
 *
 * heldSessions(반이 열었던 회차)와 recordedSessions(출결을 표시한 회차)를 따로 둔다.
 * 둘을 하나로 합치면 "출결을 안 적은 회차" 가 조용히 사라져서
 * 출석률이 실제보다 좋아 보인다. 차이를 그대로 보여 주고, 그것이 곧 "출결을 적어라" 는 안내가 된다.
 */
public record TogetherTime(
        LocalDate from,
        LocalDate to,
        /** "1년 5개월" 처럼 사람이 읽는 기간 */
        String spanText,
        /** 이 학생이 소속돼 있던 반이 그 기간에 열었던 회차 (숨긴 회차 제외) */
        long heldSessions,
        /** 그중 출결을 표시한 회차 */
        long recordedSessions,
        /** 출결을 표시하지 않은 회차 */
        long unrecordedSessions,
        /** 상태별 개수. 0건인 상태도 행을 남긴다. "결석 0회" 는 리포트에서 의미 있는 정보다 */
        List<StatusCount> counts,
        /** 출석률 분자: 출석 + 지각 + 보강 */
        long attended,
        /** 출석률 분모: 출석 + 지각 + 결석 + 보강. 휴원은 빠진다 */
        long attendable,
        /** 분모가 0이면 null. 0으로 나누지 않고 "셀 수 없음" 으로 표시한다 */
        Integer attendanceRate,
        List<String> classNames) {

    public record StatusCount(String code, String label, long count) {
    }

    /** 출석률을 낼 수 없는 상태인가. 화면과 텍스트가 같은 판단을 쓰도록 여기에 둔다 */
    public boolean rateUnknown() {
        return attendanceRate == null;
    }
}
