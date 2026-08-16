package io.poby_house.dto.dashboard;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.LocalDate;

/**
 * 결과를 아직 확인하지 않은 개입.
 *
 * result 가 null 인 것만 담는다. ONGOING(진행중)은 확인해 본 결과이므로 여기 넣지 않는다.
 * 둘을 섞으면 이 목록이 "언젠가 볼 것" 더미가 되어 아무 재촉도 하지 못한다.
 */
@Getter
@RequiredArgsConstructor
public class OpenIntervention {

    private final Long interventionId;
    private final Long studentId;
    private final String studentName;
    private final String problem;
    private final LocalDate triedOn;
    private final long daysOpen;
}
