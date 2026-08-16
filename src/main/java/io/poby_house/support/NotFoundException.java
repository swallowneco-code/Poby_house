package io.poby_house.support;

/**
 * 요청한 것이 없다. 주소를 잘못 눌렀거나 이미 지워진 것이다.
 * IllegalArgumentException 을 쓰면 의미상 404 인 상황이 500 으로 나간다.
 */
public class NotFoundException extends RuntimeException {

    public NotFoundException(String message) {
        super(message);
    }
}
