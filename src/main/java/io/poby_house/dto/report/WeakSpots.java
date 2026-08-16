package io.poby_house.dto.report;

import java.time.LocalDate;
import java.util.List;

/**
 * 리포트 5. 약한 지점과 처방.
 *
 * 오답 유형 태그 분포가 곧 처방이다. "계산 12 · 조건 9" 는 "더 열심히 하자" 와 다른 문서를 만든다.
 * 0인 태그는 담지 않는다. 일곱 줄 중 다섯 줄이 0 이면 많은 두 줄이 눈에 안 들어온다.
 */
public record WeakSpots(
        long totalErrors,
        /** 개수 많은 순. 앞의 두 개가 top=true */
        List<TagBar> tags,
        /** 강조된 태그 이름. 문장으로 쓸 때 쓴다 */
        List<String> topLabels,
        /** 백지 4축 점수가 있는 평가만 */
        List<BlankAxes> blanks,
        /** 단원 상태가 '약함' 인 평가 */
        List<WeakUnit> weakUnits,
        /** 아이가 쓴 문장. 다듬지 않고 그대로 낸다 */
        List<Quote> quotes,
        long assessmentCount) {

    /**
     * @param share 전체 오답 중 비율. 문장으로 인용할 수 있는 숫자다
     * @param width 막대 길이(%). 최다 태그를 100 으로 잡는다.
     *              전체 비율로 그리면 태그가 일곱 개로 흩어졌을 때 막대가 전부 짧아 차이가 안 보인다
     */
    public record TagBar(String code, String label, long count, int share, int width, boolean top) {
    }

    public record BlankAxes(LocalDate assessedOn, String unitName,
                            Integer term, Integer condition, Integer exception, Integer causality,
                            String quote) {
    }

    public record WeakUnit(LocalDate assessedOn, String unitName, String statusLabel,
                           String scoreText, Integer percent, String note) {
    }

    public record Quote(LocalDate assessedOn, String unitName, String quote) {
    }
}
