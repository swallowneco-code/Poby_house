package io.poby_house.service;

import io.poby_house.domain.Consultation;
import io.poby_house.domain.ConsultationType;
import io.poby_house.domain.Student;
import io.poby_house.dto.ConsultationForm;
import io.poby_house.repository.ConsultationRepository;
import io.poby_house.repository.StudentRepository;
import io.poby_house.repository.TeacherRepository;
import io.poby_house.support.BusinessRuleException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class ConsultationServiceTest {

    private static final Long STUDENT_ID = 1L;

    @Mock
    ConsultationRepository consultationRepository;

    @Mock
    StudentRepository studentRepository;

    @Mock
    TeacherRepository teacherRepository;

    @InjectMocks
    ConsultationService consultationService;

    private ConsultationForm form() {
        ConsultationForm form = new ConsultationForm();
        form.setConsultedOn(LocalDate.of(2026, 8, 18));
        form.setType(ConsultationType.PHONE);
        return form;
    }

    private Consultation saved(ConsultationForm form) {
        given(studentRepository.findById(STUDENT_ID)).willReturn(Optional.of(new Student()));
        given(consultationRepository.save(any(Consultation.class))).willAnswer(inv -> inv.getArgument(0));
        return consultationService.create(STUDENT_ID, form, null);
    }

    private Consultation withPromise(LocalDate consultedOn, String promiseNote) {
        Consultation consultation = Consultation.of(new Student(), consultedOn, ConsultationType.PHONE);
        consultation.setFactNote("통화했다");
        consultation.setPromiseNote(promiseNote);
        return consultation;
    }

    @Test
    @DisplayName("사실·걱정·약속이 모두 비면 저장하지 않는다")
    void create_rejectsEmptyNotes() {
        assertThatThrownBy(() -> consultationService.create(STUDENT_ID, form(), null))
                .isInstanceOf(BusinessRuleException.class);

        // 학생을 찾기도 전에 막는다. 날짜와 경로만 남은 행은 리포트에서 쓸 수 없으면서 이력만 길게 만든다
        verifyNoInteractions(studentRepository);
        verify(consultationRepository, never()).save(any());
    }

    @Test
    @DisplayName("공백만 채운 것은 적은 것이 아니다")
    void create_rejectsWhitespaceOnlyNotes() {
        ConsultationForm form = form();
        form.setFactNote("   ");
        form.setWorryNote("\n");
        form.setPromiseNote("");

        assertThatThrownBy(() -> consultationService.create(STUDENT_ID, form, null))
                .isInstanceOf(BusinessRuleException.class);
    }

    @Test
    @DisplayName("한 칸만 적어도 저장한다. 걱정만 들은 통화도 기록이다")
    void create_acceptsSingleNote() {
        ConsultationForm form = form();
        form.setWorryNote("서술형에서 점수가 깎이는 것을 걱정하셨다");

        Consultation consultation = saved(form);

        assertThat(consultation.getWorryNote()).isEqualTo("서술형에서 점수가 깎이는 것을 걱정하셨다");
        assertThat(consultation.getFactNote()).isNull();
        assertThat(consultation.getPromiseNote()).isNull();
    }

    @Test
    @DisplayName("빈 칸은 null 로 저장한다. 빈 문자열이 남으면 약속 모아보기가 그것을 약속으로 센다")
    void create_blankBecomesNull() {
        ConsultationForm form = form();
        form.setFactNote("진도와 오답 유형을 설명했다");
        form.setPromiseNote("   ");

        assertThat(saved(form).getPromiseNote()).isNull();
    }

    @Test
    @DisplayName("수정할 때도 세 칸을 모두 비우면 거절한다")
    void update_rejectsEmptyNotes() {
        assertThatThrownBy(() -> consultationService.update(9L, form()))
                .isInstanceOf(BusinessRuleException.class);

        // 기록을 불러오기 전에 막는다. 불러온 뒤 막으면 이미 setter 가 돌아 세 칸이 지워질 수 있다
        verifyNoInteractions(consultationRepository);
    }

    @Test
    @DisplayName("약속 모아보기는 약속 칸이 있는 것만 낸다")
    void recentPromises_onlyWithPromise() {
        given(consultationRepository.findByStudentIdOrderByConsultedOnDesc(STUDENT_ID)).willReturn(List.of(
                withPromise(LocalDate.of(2026, 8, 18), null),
                withPromise(LocalDate.of(2026, 7, 10), "오답노트를 매주 확인하기로 했다"),
                withPromise(LocalDate.of(2026, 6, 1), "   ")
        ));

        List<Consultation> promises = consultationService.recentPromises(STUDENT_ID, 5);

        assertThat(promises).hasSize(1);
        assertThat(promises.get(0).getPromiseNote()).isEqualTo("오답노트를 매주 확인하기로 했다");
    }

    @Test
    @DisplayName("약속 모아보기는 최근 것부터 limit 개만 낸다")
    void recentPromises_keepsOrderAndLimit() {
        given(consultationRepository.findByStudentIdOrderByConsultedOnDesc(STUDENT_ID)).willReturn(List.of(
                withPromise(LocalDate.of(2026, 8, 18), "3주 뒤에 결과를 알려 드리기로 했다"),
                withPromise(LocalDate.of(2026, 7, 10), "오답노트를 매주 확인하기로 했다"),
                withPromise(LocalDate.of(2026, 6, 1), "숙제량을 절반으로 줄여 보기로 했다")
        ));

        List<Consultation> promises = consultationService.recentPromises(STUDENT_ID, 2);

        assertThat(promises).hasSize(2);
        assertThat(promises.get(0).getConsultedOn()).isEqualTo(LocalDate.of(2026, 8, 18));
        assertThat(promises.get(1).getConsultedOn()).isEqualTo(LocalDate.of(2026, 7, 10));
    }

    @Test
    @DisplayName("limit 이 0 이면 쿼리를 돌리지 않고 빈 목록을 낸다")
    void promisesOf_zeroLimit() {
        assertThat(consultationService.promisesOf(
                List.of(withPromise(LocalDate.of(2026, 8, 18), "약속")), 0)).isEmpty();
    }
}
