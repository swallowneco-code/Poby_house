package io.poby_house.domain;

public enum TeacherRole {

    ADMIN("원장"),
    TEACHER("강사");

    private final String label;

    TeacherRole(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
