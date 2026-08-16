package io.poby_house.dto.report;

import java.time.LocalDate;
import java.util.List;

/**
 * 리포트 7. 상담에서 한 약속.
 *
 * 약속만 모은다. 사실·걱정은 여기 쓰지 않는다(걱정은 학부모의 말이고 약속은 내 말이다).
 * withoutPromise 를 함께 넘기는 이유: 상담은 했는데 약속 칸이 빈 건수가 보여야
 * "다음 상담에서는 약속을 적어야 한다" 는 것이 드러난다.
 */
public record Promises(long consultationCount, long withoutPromise, List<Item> items) {

    public record Item(LocalDate consultedOn, String typeLabel, String promiseNote,
                       String worryNote, String authorName) {
    }
}
