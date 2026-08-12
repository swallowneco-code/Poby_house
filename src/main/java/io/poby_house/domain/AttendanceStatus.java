package io.poby_house.domain;

public enum AttendanceStatus {

    PRESENT("출석"),
    LATE("지각"),
    ABSENT("결석"),
    MAKEUP("보강"),
    /** 휴원. 나중에 기간을 등록하면 그 기간의 기본값으로 자동으로 잡힌다 */
    PAUSED("휴원");

    private final String label;

    AttendanceStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
