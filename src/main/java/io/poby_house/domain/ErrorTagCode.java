package io.poby_house.domain;

/**
 * 오답 유형. 태그 분포가 곧 처방이다.
 * 자유 텍스트로 받으면 집계가 안 되므로 반드시 이 코드로만 저장한다.
 */
public enum ErrorTagCode {

    CALC("계산"),
    COND("조건"),
    CONCEPT("개념"),
    APPLY("적용"),
    TIME("시간"),
    NOTRY("무시도"),
    WRITE("서술");

    private final String label;

    ErrorTagCode(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
