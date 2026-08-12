package io.poby_house.domain;

/** 지난 시간에 내준 과제를 해 왔는가 */
public enum HomeworkStatus {

    DONE("완료"),
    PARTIAL("일부"),
    NONE("미제출");

    private final String label;

    HomeworkStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
