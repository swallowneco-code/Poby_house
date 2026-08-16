package io.poby_house.dto.dashboard;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.LocalDate;
import java.util.List;

/**
 * 홈 화면 한 장. "오늘 무엇을 해야 하나" 에 대한 답이다.
 *
 * 통계 화면이 아니다. 모든 줄은 눌러서 그 기록 화면으로 가는 것이 목적이고,
 * 숫자는 그 줄을 누를 이유를 알려 주는 데까지만 쓴다.
 */
@Getter
@RequiredArgsConstructor
public class Dashboard {

    private final LocalDate today;

    /** "8월 18일 (화)" */
    private final String todayLabel;

    /** "2026년 3분기" */
    private final String quarterLabel;

    /** 오늘 수업이 있는 반. 하루에 몇 개뿐이라 자르지 않는다 */
    private final List<TodayClass> todayClasses;

    private final DashCard<MissingAttendance> missingAttendance;
    private final DashCard<ObservationDue> observationDue;
    private final DashCard<OpenIntervention> openInterventions;

    /** 원장이 아니면 항상 비어 있다. 화면에서 감추는 것이 아니라 아예 담지 않는다 */
    private final DashCard<PurgeDue> purgeDue;
    private final boolean purgeVisible;

    /** 이번 분기에 장면 기록이 없는 재원생 수 */
    private final long sceneMissingCount;

    private final long activeCount;

    /** 밀린 것 총 건수. 장면은 분기 단위라 성격이 달라 여기 넣지 않는다 */
    public int getPendingTotal() {
        int total = missingAttendance.getTotal() + observationDue.getTotal() + openInterventions.getTotal();
        if (purgeVisible) {
            total += purgeDue.getTotal();
        }
        return total;
    }

    /** 오늘 수업 중 아직 손대지 않은 것 */
    public long getTodayTodoCount() {
        return todayClasses.stream().filter(c -> !c.isDone()).count();
    }

    /** 밀린 것이 하나도 없다. 좋은 소식이므로 화면이 그렇게 보여야 한다 */
    public boolean isAllClear() {
        return getPendingTotal() == 0 && sceneMissingCount == 0 && getTodayTodoCount() == 0;
    }
}
