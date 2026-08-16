package io.poby_house.domain;

/**
 * 단원 상태. 리포트의 단원별 표에 기호로 그대로 나간다.
 * label 에 기호를 붙여 둔다. 화면마다 기호를 다시 붙이면 리포트와 어긋난다.
 */
public enum UnitStatus {

    EXCELLENT("◎ 아주 좋음"),
    OK("○ 괜찮음"),
    WEAK("△ 약함");

    private final String label;

    UnitStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
