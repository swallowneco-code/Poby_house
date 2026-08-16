package io.poby_house.domain;

/**
 * 개입의 결과. 리포트 3-1 "통한 것 / 안 통한 것" 이 전적으로 이 값에서 나온다.
 * null 은 "아직 확인하지 않았다" 다. ONGOING(진행중)과 다른 정보이므로 섞지 않는다.
 */
public enum InterventionResult {

    WORKED("통함"),
    PARTIAL("일부"),
    FAILED("안 통함"),
    ONGOING("진행중");

    private final String label;

    InterventionResult(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
