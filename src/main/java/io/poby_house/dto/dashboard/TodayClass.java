package io.poby_house.dto.dashboard;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 오늘 수업이 있는 반 한 줄.
 *
 * 회차가 열렸는지, 출결을 몇 명 찍었는지까지 한 줄에 담는다.
 * "회차는 열었는데 출결을 안 찍었다" 가 가장 흔한 누락이라, 그것이 눈에 보여야 한다.
 */
@Getter
@RequiredArgsConstructor
public class TodayClass {

    private final Long classGroupId;
    private final String name;
    private final String scheduleText;

    /** null 이면 오늘 회차가 아직 없다 */
    private final Long sessionId;

    private final long attendanceCount;

    /** 현재 이 반의 인원. 0 이면 아직 배정된 학생이 없다 */
    private final long headcount;

    public boolean isSessionOpen() {
        return sessionId != null;
    }

    /**
     * 회차만 열고 덮어 둔 상태.
     * 학생이 아직 배정되지 않은 반(headcount 0)은 여기서 뺀다.
     * 찍을 사람이 없는 반을 매일 재촉하면 나머지 재촉이 함께 무시당한다.
     */
    public boolean isAttendanceMissing() {
        return sessionId != null && headcount > 0 && attendanceCount == 0;
    }

    /** 인원보다 출결이 적다 = 아직 덜 찍었다 */
    public boolean isAttendancePartial() {
        return sessionId != null && attendanceCount > 0 && headcount > attendanceCount;
    }

    /** 인원이 0 인 반은 회차를 연 것만으로 끝이다(0 >= 0) */
    public boolean isDone() {
        return sessionId != null && attendanceCount >= headcount;
    }

    public String getAttendanceText() {
        return attendanceCount + " / " + headcount;
    }
}
