package io.poby_house.dto.dashboard;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.LocalDate;

/**
 * 열렸지만 출결이 한 건도 없는 회차.
 *
 * 출결은 리포트 1-2 "함께한 시간" 의 근거다. 그날이 지나면 기억으로 복원할 수 없으므로
 * 오래된 것이 위로 온다.
 */
@Getter
@RequiredArgsConstructor
public class MissingAttendance {

    private final Long sessionId;
    private final Long classGroupId;
    private final String className;
    private final LocalDate sessionDate;
    private final long daysAgo;
}
