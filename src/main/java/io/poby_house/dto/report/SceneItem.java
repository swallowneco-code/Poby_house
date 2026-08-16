package io.poby_house.dto.report;

import java.time.LocalDate;

/**
 * 리포트 6. 장면. 편지의 재료다.
 *
 * 여기는 요약하지 않는다. 적힌 문장을 그대로 낸다.
 * "성실했다" 로 줄이면 편지에 쓸 것이 남지 않는다. 원문의 "남아서 3번을 다시 풀었다" 가 재료다.
 */
public record SceneItem(LocalDate occurredOn, String situation, String content, String authorName) {
}
