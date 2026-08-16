package io.poby_house.dto;

import io.poby_house.domain.ClassSession;
import lombok.Getter;

import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.Locale;

/**
 * 회차 목록의 한 줄.
 *
 * 회차마다 "출결을 몇 명 찍었나 / 진도기록이 몇 명 남았나" 를 함께 들고 다닌다.
 * 이 숫자가 없으면 목록에서 어느 날이 덜 채워졌는지 알 수 없어서, 회차를 하나하나 열어 봐야 한다.
 * 세는 것은 목록 쿼리 한 방에 끝낸다 (ClassSessionService).
 */
@Getter
public class SessionListRow {

    private final ClassSession session;
    private final long attendanceCount;
    private final long progressCount;

    public SessionListRow(ClassSession session, long attendanceCount, long progressCount) {
        this.session = session;
        this.attendanceCount = attendanceCount;
        this.progressCount = progressCount;
    }

    /** 월별 탭에 쓰는 키. "2026-08" */
    public String getMonth() {
        return YearMonth.from(session.getSessionDate()).toString();
    }

    /** 요일. 날짜만 있으면 수업일이 맞는지 눈으로 확인할 수 없다 */
    public String getWeekday() {
        return session.getSessionDate().getDayOfWeek().getDisplayName(TextStyle.SHORT, Locale.KOREAN);
    }

    /** 목록에 한 줄로 보일 요약. 진도 -> 오늘 한 것 -> 수업 메모 순으로 있는 것을 쓴다 */
    public String getSummary() {
        String progress = session.getProgressText();
        if (progress != null) {
            return progress;
        }
        if (notBlank(session.getContent())) {
            return session.getContent();
        }
        return notBlank(session.getDailyNote()) ? session.getDailyNote() : null;
    }

    /** 출결을 한 명도 안 찍은 회차. 목록에서 표시해 주지 않으면 빠진 날을 못 찾는다 */
    public boolean isAttendanceMissing() {
        return attendanceCount == 0;
    }

    private boolean notBlank(String s) {
        return s != null && !s.isBlank();
    }
}
