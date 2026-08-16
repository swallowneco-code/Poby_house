package io.poby_house.dto;

import io.poby_house.domain.Consultation;
import io.poby_house.domain.ConsultationType;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.util.StringUtils;

import java.time.LocalDate;

/**
 * 학부모 상담 한 건.
 *
 * 요약을 사실 / 걱정 / 약속 세 칸으로 나눠 받는다. 한 칸에 몰아 적게 하면
 * 몇 달 뒤 "내가 무엇을 하기로 했더라" 를 문장 속에서 찾아내야 하고, 결국 못 찾는다.
 */
@Getter
@Setter
@NoArgsConstructor
public class ConsultationForm {

    @NotNull(message = "상담한 날을 선택하세요")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate consultedOn = LocalDate.now();

    /**
     * 대부분 전화다. 기본값을 두지 않으면 세 칸을 적기 전에 경로부터 고르는 탭이 매번 붙는다.
     * (관찰기록의 점수와 달리 여기서는 "확인하지 않았다" 라는 상태가 없다. 상담을 한 경로는 항상 안다)
     */
    private ConsultationType type = ConsultationType.PHONE;

    /** 사실 - 오간 내용 */
    private String factNote;

    /** 걱정 - 학부모가 우려한 것 */
    private String worryNote;

    /** 약속 - 내가 하기로 한 것 */
    private String promiseNote;

    /**
     * 세 칸이 모두 비면 남길 것이 없는 기록이다. 날짜와 경로만 남은 행은
     * 나중에 리포트를 쓸 때 아무 근거가 되지 못하면서 이력 목록만 길게 만든다.
     *
     * 규칙 자체는 서비스가 지킨다(어느 경로로 들어와도 막혀야 하므로).
     * 여기 한 번 더 두는 이유는 화면에서 적던 내용을 잃지 않고 그 자리에서 안내를 받게 하려는 것이다.
     * 예외로 튕기면 폼이 비워진 채 오류 화면이 뜨고, 상담 직후의 기억이 날아간다.
     */
    @AssertTrue(message = "사실 · 걱정 · 약속 중 최소 한 칸은 적어야 합니다")
    public boolean isAnyNoteFilled() {
        return StringUtils.hasText(factNote)
                || StringUtils.hasText(worryNote)
                || StringUtils.hasText(promiseNote);
    }

    public static ConsultationForm from(Consultation consultation) {
        ConsultationForm form = new ConsultationForm();
        form.setConsultedOn(consultation.getConsultedOn());
        form.setType(consultation.getType());
        form.setFactNote(consultation.getFactNote());
        form.setWorryNote(consultation.getWorryNote());
        form.setPromiseNote(consultation.getPromiseNote());
        return form;
    }
}
