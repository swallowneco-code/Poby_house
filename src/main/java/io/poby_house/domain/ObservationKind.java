package io.poby_house.domain;

/**
 * 관찰기록의 종류.
 * BASELINE 은 입회 시점 스냅샷이다. 리포트에서 이것이 before 가 되므로,
 * 학생마다 가장 이른 BASELINE 하나가 끝까지 기준선 노릇을 한다.
 */
public enum ObservationKind {

    BASELINE("입회 기준선"),
    PERIODIC("정기"),
    QUARTERLY("분기 점검");

    private final String label;

    ObservationKind(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
