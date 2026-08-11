package io.poby_house.domain;

public enum StudentStatus {

    ACTIVE("재원"),
    PAUSED("휴원"),
    LEFT("퇴원");

    private final String label;

    StudentStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
