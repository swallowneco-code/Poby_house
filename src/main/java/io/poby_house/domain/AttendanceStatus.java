package io.poby_house.domain;

/**
 * 출결.
 * 기본값을 두지 않는다. 확인하지 않은 것과 출석은 다른 정보다.
 * 미표시면 Attendance 행을 아예 만들지 않는다. 그래야 출석률이 거짓말을 하지 않는다.
 */
public enum AttendanceStatus {

    PRESENT("출석"),
    LATE("지각"),
    ABSENT("결석"),
    MAKEUP("보강"),
    /** 휴원. 결석과 섞으면 리포트의 "함께한 시간"이 부당하게 깎인다 */
    PAUSED("휴원");

    private final String label;

    AttendanceStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
