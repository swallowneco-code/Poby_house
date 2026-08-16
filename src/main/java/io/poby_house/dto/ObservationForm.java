package io.poby_house.dto;

import io.poby_house.domain.Observation;
import io.poby_house.domain.ObservationKind;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

/**
 * 관찰기록 입력 폼.
 *
 * 점수 두 칸에 @Min/@Max 를 반드시 남긴다.
 * DB 컬럼이 TINYINT 라 6 이나 0 을 넣어도 저장은 조용히 성공하고,
 * 리포트의 before/after 비교만 나중에 이상해진다. 그때는 원인을 찾을 단서가 없다.
 */
@Getter
@Setter
@NoArgsConstructor
public class ObservationForm {

    @NotNull(message = "관찰 종류를 선택하세요")
    private ObservationKind kind = ObservationKind.PERIODIC;

    @NotNull(message = "관찰 시작일을 선택하세요")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate periodStart;

    @NotNull(message = "관찰 종료일을 선택하세요")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate periodEnd;

    /** 1~5, 5가 최상. 확인하지 않았으면 null 이다. 1점과 다른 정보라 0 으로 눌러 담지 않는다 */
    @Min(value = 1, message = "이해도는 1~5 사이입니다")
    @Max(value = 5, message = "이해도는 1~5 사이입니다")
    private Integer understanding;

    @Min(value = 1, message = "과제 수행도는 1~5 사이입니다")
    @Max(value = 5, message = "과제 수행도는 1~5 사이입니다")
    private Integer homeworkRate;

    private String attitudeNote;

    private String strength;

    private String weakness;

    /** DB 가 VARCHAR(500) 이다. 막지 않으면 긴 인용문에서 저장 순간 MySQL 오류로 튄다 */
    @Size(max = 500, message = "아이 말은 500자까지 적을 수 있습니다")
    private String selfAssessment;

    private String teacherComment;

    /**
     * 기간이 뒤집힌 것은 폼에서 잡는다.
     * 서비스에서 BusinessRuleException 으로 던지면 리다이렉트되면서
     * 방금 적은 태도·강점·약점·코멘트가 전부 날아간다. 5분 걸려 적은 것을 그렇게 버릴 수 없다.
     */
    @AssertTrue(message = "관찰 종료일이 시작일보다 앞설 수 없습니다")
    public boolean isPeriodInOrder() {
        return periodStart == null || periodEnd == null || !periodEnd.isBefore(periodStart);
    }

    public static ObservationForm from(Observation observation) {
        ObservationForm form = new ObservationForm();
        form.setKind(observation.getKind());
        form.setPeriodStart(observation.getPeriodStart());
        form.setPeriodEnd(observation.getPeriodEnd());
        form.setUnderstanding(observation.getUnderstanding());
        form.setHomeworkRate(observation.getHomeworkRate());
        form.setAttitudeNote(observation.getAttitudeNote());
        form.setStrength(observation.getStrength());
        form.setWeakness(observation.getWeakness());
        form.setSelfAssessment(observation.getSelfAssessment());
        form.setTeacherComment(observation.getTeacherComment());
        return form;
    }
}
