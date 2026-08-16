package io.poby_house.dto;

import io.poby_house.domain.Intervention;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

/**
 * 개입의 1단계(시도)만 받는다. 결과는 InterventionReviewForm 이 따로 받는다.
 * 한 폼에 결과까지 넣으면 시도한 날 결과를 짐작해서 적게 되고, 그러면 리포트 3-1 이 추측이 된다.
 */
@Getter
@Setter
@NoArgsConstructor
public class InterventionForm {

    @NotNull(message = "시도한 날을 선택하세요")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate triedOn = LocalDate.now();

    /** DB 가 VARCHAR(500). 화면에서 막지 않으면 저장 시점에 MySQL 오류가 난다 */
    @NotBlank(message = "무엇을 해결하려 했는지 적으세요")
    @Size(max = 500, message = "해결하려 한 것은 500자까지 적을 수 있습니다")
    private String problem;

    @NotBlank(message = "무엇을 시도했는지 적으세요")
    private String action;

    public static InterventionForm from(Intervention intervention) {
        InterventionForm form = new InterventionForm();
        form.setTriedOn(intervention.getTriedOn());
        form.setProblem(intervention.getProblem());
        form.setAction(intervention.getAction());
        return form;
    }
}
