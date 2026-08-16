package io.poby_house.dto;

import io.poby_house.domain.Assessment;
import io.poby_house.domain.AssessmentErrorTag;
import io.poby_house.domain.AssessmentType;
import io.poby_house.domain.ErrorTagCode;
import io.poby_house.domain.UnitStatus;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * 평가 입력 폼.
 *
 * 길이 제한을 여기서 거는 이유: DB 가 VARCHAR 로 잘라 주지 않고 저장 시점에 오류로 튄다.
 * 500자를 넘긴 인용문 하나 때문에 다 적은 평가가 통째로 날아가는 것을 화면에서 막는다.
 */
@Getter
@Setter
@NoArgsConstructor
public class AssessmentForm {

    @NotNull(message = "평가한 날을 선택하세요")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate assessedOn = LocalDate.now();

    /** 기본값을 두지 않는다. 종류에 따라 4축 칸이 열리므로 넘겨짚으면 엉뚱한 칸을 채우게 된다 */
    @NotNull(message = "평가 종류를 고르세요")
    private AssessmentType type;

    @Size(max = 100, message = "단원명은 100자까지입니다")
    private String unitName;

    /**
     * 득점. DECIMAL(5,1) 이라 정수부 4자리 + 소수 첫째 자리까지다.
     * double 로 받지 않는다. 0.1 오차가 리포트 백분율에 그대로 실린다.
     */
    @DecimalMin(value = "0", message = "득점은 0점부터입니다")
    @Digits(integer = 4, fraction = 1, message = "득점은 소수 첫째 자리까지 적습니다")
    private BigDecimal score;

    @DecimalMin(value = "0", message = "만점은 0점부터입니다")
    @Digits(integer = 4, fraction = 1, message = "만점은 소수 첫째 자리까지 적습니다")
    private BigDecimal maxScore;

    private UnitStatus unitStatus;

    /* ---- 백지 4축. BLANK 일 때만 저장된다 (AssessmentService 가 나머지 종류에서는 버린다) ----
       DB 가 TINYINT 라 6 을 넣어도 조용히 저장된다. 범위는 여기서만 막힌다. */

    @Min(value = 1, message = "용어 점수는 1~5 입니다")
    @Max(value = 5, message = "용어 점수는 1~5 입니다")
    private Integer blankTerm;

    @Min(value = 1, message = "조건 점수는 1~5 입니다")
    @Max(value = 5, message = "조건 점수는 1~5 입니다")
    private Integer blankCondition;

    @Min(value = 1, message = "예외 점수는 1~5 입니다")
    @Max(value = 5, message = "예외 점수는 1~5 입니다")
    private Integer blankException;

    @Min(value = 1, message = "인과 점수는 1~5 입니다")
    @Max(value = 5, message = "인과 점수는 1~5 입니다")
    private Integer blankCausality;

    /** 아이가 쓴 문장 한 줄 그대로 */
    @Size(max = 500, message = "학생 문장은 500자까지입니다")
    private String studentQuote;

    /** 원본 시험지의 위치만 적는다. 시험지 자체는 학원 자산이라 반출하지 않는다 */
    @Size(max = 255, message = "원본 위치는 255자까지입니다")
    private String sourceLocation;

    /** TEXT 컬럼이라 길이를 막지 않는다 */
    private String note;

    /**
     * 오답 유형 7종 전체. 개수 0 으로 미리 채워 두고 화면이 스테퍼로 올린다.
     *
     * 미리 채우지 않으면 th:field="*{tags[0].cnt}" 가 그릴 대상이 없다.
     * 또 자리가 고정돼 있어야 검증 오류로 폼을 되돌릴 때 입력한 개수가 그대로 남는다.
     */
    @Valid
    private List<ErrorTagInput> tags = zeroTags();

    private static List<ErrorTagInput> zeroTags() {
        List<ErrorTagInput> list = new ArrayList<>();
        for (ErrorTagCode code : ErrorTagCode.values()) {
            list.add(ErrorTagInput.of(code, 0));
        }
        return list;
    }

    public static AssessmentForm from(Assessment assessment) {
        AssessmentForm form = new AssessmentForm();
        form.setAssessedOn(assessment.getAssessedOn());
        form.setType(assessment.getType());
        form.setUnitName(assessment.getUnitName());
        form.setScore(assessment.getScore());
        form.setMaxScore(assessment.getMaxScore());
        form.setUnitStatus(assessment.getUnitStatus());
        form.setBlankTerm(assessment.getBlankTerm());
        form.setBlankCondition(assessment.getBlankCondition());
        form.setBlankException(assessment.getBlankException());
        form.setBlankCausality(assessment.getBlankCausality());
        form.setStudentQuote(assessment.getStudentQuote());
        form.setSourceLocation(assessment.getSourceLocation());
        form.setNote(assessment.getNote());

        // 저장된 태그는 7종 중 일부뿐이다. 없는 유형은 0 으로 남겨 스테퍼 자리를 지킨다
        for (AssessmentErrorTag tag : assessment.getErrorTags()) {
            for (ErrorTagInput input : form.getTags()) {
                if (input.getCode() == tag.getTagCode()) {
                    input.setCnt(tag.getCnt());
                    break;
                }
            }
        }
        return form;
    }

    /** 4축 칸을 처음 그릴 때 열어 둘지. 그 뒤로는 화면의 JS 가 종류에 맞춰 여닫는다 */
    public boolean isBlankType() {
        return type != null && type.usesBlankAxes();
    }
}
