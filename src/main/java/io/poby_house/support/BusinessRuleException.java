package io.poby_house.support;

/**
 * 규칙에 어긋나는 요청. 사용자가 고치면 되는 일이다.
 * 에러 페이지로 보내지 않고, 하던 화면으로 되돌려 이유만 알려 준다.
 */
public class BusinessRuleException extends RuntimeException {

    public BusinessRuleException(String message) {
        super(message);
    }
}
