package io.poby_house.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

/**
 * 퇴원 처리. 확인 화면 전용 폼이다.
 *
 * 퇴원일이 보관 만료일(퇴원일 + 3년)과 반 배정 종료일을 함께 정한다.
 * 그래서 기본값을 오늘로 두되 반드시 눈으로 확인하게 한다.
 */
@Getter
@Setter
@NoArgsConstructor
public class LeaveForm {

    @NotNull(message = "퇴원일을 선택하세요")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate leftOn = LocalDate.now();

    /** DB 가 VARCHAR(255) 다. 막지 않으면 저장 시점에 MySQL 오류로 튄다 */
    @Size(max = 255, message = "퇴원 사유는 255자까지 입력할 수 있습니다")
    private String leaveReason;

    /** 확인 화면에 "보관 만료 = 퇴원일 + 3년" 을 실제 날짜로 보여 주기 위한 값 */
    public LocalDate getPurgeAfterPreview() {
        return leftOn == null ? null : leftOn.plusYears(3);
    }
}
