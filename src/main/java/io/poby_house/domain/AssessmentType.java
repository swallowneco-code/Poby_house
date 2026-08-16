package io.poby_house.domain;

/**
 * 평가 종류.
 * BLANK(백지테스트)일 때만 4축 점수(용어·조건·예외·인과)를 채운다.
 */
public enum AssessmentType {

    BLANK("백지"),
    CUMULATIVE("누적"),
    UNIT("단원평가"),
    COMPREHENSIVE("총괄");

    private final String label;

    AssessmentType(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    /** 4축 입력 칸을 열어 줄지 화면이 이걸로 판단한다 */
    public boolean usesBlankAxes() {
        return this == BLANK;
    }
}
