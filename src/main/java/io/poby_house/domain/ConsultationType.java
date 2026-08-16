package io.poby_house.domain;

/** 상담 경로 */
public enum ConsultationType {

    PHONE("전화"),
    VISIT("방문"),
    MESSAGE("메시지");

    private final String label;

    ConsultationType(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
