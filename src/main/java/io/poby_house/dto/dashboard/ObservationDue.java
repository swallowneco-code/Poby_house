package io.poby_house.dto.dashboard;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.LocalDate;

/**
 * 관찰이 밀린 재원생 한 줄.
 *
 * baselineMissing 이 다른 어떤 지연보다 앞선다.
 * 입회 기준선은 리포트의 before 다. 그것이 없으면 after 를 아무리 잘 적어도 성장을 쓸 수 없고,
 * 시간이 지나면 기준선 자체를 만들 수 없다(입회 시점이 이미 지나갔으므로).
 */
@Getter
@RequiredArgsConstructor
public class ObservationDue {

    private final Long studentId;
    private final String studentName;
    private final String studentCode;

    /** 입회 기준선(BASELINE)이 아예 없다 */
    private final boolean baselineMissing;

    /** 마지막 관찰 종료일. null 이면 관찰 기록이 하나도 없다 */
    private final LocalDate lastObservedOn;

    /** 기준선이 없으면 등록 후 며칠, 있으면 마지막 관찰 후 며칠 */
    private final long daysSince;
}
