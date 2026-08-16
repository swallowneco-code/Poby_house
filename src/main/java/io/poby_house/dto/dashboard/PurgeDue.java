package io.poby_house.dto.dashboard;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.LocalDate;

/**
 * 보관 만료가 임박했거나 이미 지난 퇴원생.
 *
 * 원장만 본다. 파기는 되돌릴 수 없는 조작이라, 이 화면은 알리는 데까지만 하고
 * 실제 파기는 확인 단계를 가진 별도 화면에서 한다.
 */
@Getter
@RequiredArgsConstructor
public class PurgeDue {

    private final Long studentId;
    private final String studentName;
    private final String studentCode;
    private final LocalDate purgeAfter;

    /** 남은 날. 음수면 이미 지났다 */
    private final long daysLeft;

    public boolean isOverdue() {
        return daysLeft < 0;
    }
}
