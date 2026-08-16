package io.poby_house.dto.report;

import java.time.LocalDate;

/**
 * 리포트 3. 무엇이 달라졌나. 입회 기준선(BASELINE) 대 최신 관찰.
 *
 * 기준선이 없으면 <b>변화를 증명할 수 없다</b>. 그때 가장 이른 정기 관찰을 기준선인 척 쓰면
 * 이미 몇 달 자란 상태가 before 가 되어 성장 폭이 실제보다 작게 나온다.
 * 그래서 대신 쓰지 않고, 없다고 분명히 말한다. 이것이 이 리포트에서 가장 중요한 경고다.
 *
 * 기준선은 기간 필터로 자르지 않는다. 입회 시점 스냅샷이라 조회 기간보다 앞서는 것이 정상이다.
 * 자르면 기간을 좁힐 때마다 before 가 사라져 화면이 거짓 경고를 낸다.
 */
public record WhatChanged(
        int observationCount,
        /** 입회 기준선. 없으면 null */
        ObservationPoint baseline,
        /** 기준선이 없을 때만 참고로 보여 주는 가장 이른 관찰. 기준선이 아니라고 이름을 붙여 내보낸다 */
        ObservationPoint earliest,
        /** 기간 안의 마지막 관찰. 없으면 null */
        ObservationPoint latest,
        ScoreDelta understanding,
        ScoreDelta homeworkRate,
        boolean baselineMissing,
        /** 기준선이 조회 기간보다 앞선 기록인가 */
        boolean baselineBeforePeriod,
        /** 관찰이 기준선 한 건뿐인가. 자기 자신과 비교할 수는 없다 */
        boolean onlyOneObservation,
        /** 증명할 수 없을 때의 경고. 없으면 null */
        String warning,
        /** 무엇을 적으면 이 칸이 채워지는가 */
        String hint) {

    public record ObservationPoint(String kindLabel, LocalDate periodStart, LocalDate periodEnd,
                                   Integer understanding, Integer homeworkRate,
                                   String strength, String weakness, String attitudeNote,
                                   String selfAssessment, String teacherComment,
                                   String authorName) {
    }

    /**
     * 점수 변화. 1~5 이고 5가 최상이다.
     *
     * comparable=false 는 "변화가 없다" 가 아니라 "비교할 수 없다" 다.
     * 한쪽 점수가 미확인(null)인데 0 변화로 그리면 리포트가 없는 사실을 주장하게 된다.
     */
    public record ScoreDelta(String label, Integer before, Integer after,
                             boolean comparable, int diff, String arrow, String text) {

        public boolean up() {
            return comparable && diff > 0;
        }

        public boolean down() {
            return comparable && diff < 0;
        }

        /**
         * 화면에 붙일 클래스 이름.
         *
         * 템플릿에서 삼항연산자를 두 번 겹쳐 쓰는 대신 여기서 정한다.
         * 색을 정하는 판단(오른 것 · 내린 것 · 색을 주지 않을 것)은 화면 사정이 아니라 이 데이터의 성질이다.
         * 비교 불가와 변화 없음은 둘 다 flat 이다. 색을 주면 좋음/나쁨으로 읽힌다.
         */
        public String direction() {
            if (up()) {
                return "up";
            }
            return down() ? "down" : "flat";
        }
    }
}
