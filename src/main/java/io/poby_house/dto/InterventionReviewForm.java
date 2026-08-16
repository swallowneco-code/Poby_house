package io.poby_house.dto;

import io.poby_house.domain.Intervention;
import io.poby_house.domain.InterventionResult;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.util.StringUtils;

import java.time.LocalDate;

/**
 * 개입의 2단계(결과).
 *
 * result 를 필수로 받는다. 이 화면은 결과를 적으러 들어온 곳이므로
 * 아무것도 고르지 않고 저장하면 "미확인" 상태 그대로 남아 아무 일도 일어나지 않는다.
 * 아직 판단이 안 서면 ONGOING(진행중)을 고르게 한다. 그것도 확인해 본 결과다.
 */
@Getter
@Setter
@NoArgsConstructor
public class InterventionReviewForm {

    @NotNull(message = "결과를 고르세요. 아직 판단이 안 서면 진행중을 고릅니다")
    private InterventionResult result;

    /** 어떻게 알았나 */
    private String resultNote;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate reviewedOn = LocalDate.now();

    /**
     * 근거 없이 "통함" 을 고른 상태인가.
     *
     * 규칙 자체는 InterventionService 가 최종적으로 막는다(서비스를 직접 부르는 곳도 있으므로).
     * 화면은 저장 전에 이걸로 먼저 걸러 낸다. 서비스 예외로 튀면 리다이렉트되면서
     * 방금 적던 글이 사라진다.
     */
    public boolean lacksEvidence() {
        return result == InterventionResult.WORKED && !StringUtils.hasText(resultNote);
    }

    public static InterventionReviewForm from(Intervention intervention) {
        InterventionReviewForm form = new InterventionReviewForm();
        form.setResult(intervention.getResult());
        form.setResultNote(intervention.getResultNote());
        form.setReviewedOn(intervention.getReviewedOn() == null ? LocalDate.now() : intervention.getReviewedOn());
        return form;
    }
}
