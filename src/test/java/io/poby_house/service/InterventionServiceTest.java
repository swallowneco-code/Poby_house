package io.poby_house.service;

import io.poby_house.domain.Intervention;
import io.poby_house.domain.InterventionResult;
import io.poby_house.domain.Student;
import io.poby_house.dto.InterventionReviewForm;
import io.poby_house.repository.InterventionRepository;
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
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class InterventionServiceTest {

    @Mock
    InterventionRepository interventionRepository;

    @Mock
    StudentRepository studentRepository;

    @Mock
    TeacherRepository teacherRepository;

    @InjectMocks
    InterventionService interventionService;

    private Intervention tried(long id, LocalDate triedOn, String problem) {
        Intervention intervention = Intervention.of(new Student(), triedOn, problem, "무엇을 해 봤다");
        intervention.setId(id);
        return intervention;
    }

    private InterventionReviewForm reviewForm(InterventionResult result, String resultNote, LocalDate reviewedOn) {
        InterventionReviewForm form = new InterventionReviewForm();
        form.setResult(result);
        form.setResultNote(resultNote);
        form.setReviewedOn(reviewedOn);
        return form;
    }

    @Test
    @DisplayName("근거 없이 통함으로 적으려 하면 거절한다. 리포트에 못 쓰는 기록이다")
    void review_workedWithoutEvidenceRejected() {
        assertThatThrownBy(() -> interventionService.review(9L, reviewForm(InterventionResult.WORKED, "  ", null)))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("근거");

        // 못 쓸 기록을 위해 행을 읽지도 않는다. 저장 시도 자체가 없어야 한다
        verifyNoInteractions(interventionRepository);
    }

    @Test
    @DisplayName("근거를 함께 적으면 통함으로 저장된다")
    void review_workedWithEvidence() {
        Intervention intervention = tried(9L, LocalDate.of(2026, 8, 1), "문제를 끝까지 안 읽는다");
        given(interventionRepository.findById(9L)).willReturn(Optional.of(intervention));

        interventionService.review(9L,
                reviewForm(InterventionResult.WORKED, "다음 수업 오답 5개 중 4개를 스스로 고쳤다", LocalDate.of(2026, 8, 20)));

        assertThat(intervention.getResult()).isEqualTo(InterventionResult.WORKED);
        assertThat(intervention.getReviewedOn()).isEqualTo(LocalDate.of(2026, 8, 20));
    }

    @Test
    @DisplayName("근거가 없어도 안 통함·일부·진행중은 적을 수 있다. 실패는 근거가 없어도 사실이다")
    void review_failedWithoutEvidenceAllowed() {
        Intervention intervention = tried(9L, LocalDate.of(2026, 8, 1), "문제를 끝까지 안 읽는다");
        given(interventionRepository.findById(9L)).willReturn(Optional.of(intervention));

        interventionService.review(9L, reviewForm(InterventionResult.FAILED, null, LocalDate.of(2026, 8, 20)));

        assertThat(intervention.getResult()).isEqualTo(InterventionResult.FAILED);
    }

    @Test
    @DisplayName("확인한 날을 비우면 오늘로 찍힌다. 결과만 있고 언제 확인했는지 없는 기록을 만들지 않는다")
    void review_stampsReviewedOn() {
        Intervention intervention = tried(9L, LocalDate.of(2026, 8, 1), "문제를 끝까지 안 읽는다");
        given(interventionRepository.findById(9L)).willReturn(Optional.of(intervention));

        interventionService.review(9L, reviewForm(InterventionResult.ONGOING, null, null));

        assertThat(intervention.getReviewedOn()).isEqualTo(LocalDate.now());
    }

    @Test
    @DisplayName("빈 근거는 null 로 저장한다. 화면에서 — 로 보여야 한다")
    void review_blankNoteBecomesNull() {
        Intervention intervention = tried(9L, LocalDate.of(2026, 8, 1), "문제를 끝까지 안 읽는다");
        given(interventionRepository.findById(9L)).willReturn(Optional.of(intervention));

        interventionService.review(9L, reviewForm(InterventionResult.PARTIAL, "   ", null));

        assertThat(intervention.getResultNote()).isNull();
    }

    @Test
    @DisplayName("결과 미확인이 목록 위로 온다. 그중에서도 오래 방치된 것이 가장 위")
    void historyOpenFirst_unreviewedOnTop() {
        Intervention recentOpen = tried(1L, LocalDate.of(2026, 8, 1), "최근 미확인");
        Intervention reviewed = tried(2L, LocalDate.of(2026, 6, 1), "확인 끝");
        reviewed.review(InterventionResult.WORKED, "오답이 줄었다", LocalDate.of(2026, 6, 10));
        Intervention oldOpen = tried(3L, LocalDate.of(2026, 5, 1), "오래된 미확인");

        // 리포지토리는 시도한 날 내림차순으로 준다
        given(interventionRepository.findByStudentIdOrderByTriedOnDesc(7L))
                .willReturn(List.of(recentOpen, reviewed, oldOpen));

        assertThat(interventionService.historyOpenFirst(7L))
                .containsExactly(oldOpen, recentOpen, reviewed);
    }

    @Test
    @DisplayName("진행중은 결과 미확인 목록에 넣지 않는다. 확인해 본 결과는 방치가 아니다")
    void unreviewed_excludesOngoing() {
        Intervention open = tried(1L, LocalDate.of(2026, 5, 1), "미확인");
        Intervention ongoing = tried(2L, LocalDate.of(2026, 6, 1), "진행중");
        ongoing.review(InterventionResult.ONGOING, "아직 지켜보는 중", LocalDate.of(2026, 6, 5));

        given(interventionRepository.findByResultOrResultIsNullOrderByTriedOnAsc(InterventionResult.ONGOING))
                .willReturn(List.of(open, ongoing));

        assertThat(interventionService.unreviewed()).containsExactly(open);
    }

    @Test
    @DisplayName("리포트용 결과별 묶음에는 미확인이 들어가지 않는다. 통했는지 모르는 것을 통한 것 옆에 두지 않는다")
    void groupedByResult_excludesUnreviewed() {
        Intervention worked = tried(1L, LocalDate.of(2026, 8, 1), "통한 것");
        worked.review(InterventionResult.WORKED, "오답이 줄었다", LocalDate.of(2026, 8, 10));
        Intervention failed = tried(2L, LocalDate.of(2026, 7, 1), "안 통한 것");
        failed.review(InterventionResult.FAILED, null, LocalDate.of(2026, 7, 10));
        Intervention open = tried(3L, LocalDate.of(2026, 6, 1), "미확인");

        given(interventionRepository.findByStudentIdOrderByTriedOnDesc(7L))
                .willReturn(List.of(worked, failed, open));

        Map<InterventionResult, List<Intervention>> grouped = interventionService.groupedByResult(7L);

        assertThat(grouped).containsOnlyKeys(InterventionResult.WORKED, InterventionResult.FAILED);
        assertThat(grouped.get(InterventionResult.WORKED)).containsExactly(worked);
    }

    @Test
    @DisplayName("기다린 날수는 미확인만 센다. 결과를 적은 기록에 경고 배지가 붙으면 안 된다")
    void waitingDays_onlyUnreviewed() {
        Intervention open = tried(1L, LocalDate.now().minusDays(31), "미확인");
        Intervention reviewed = tried(2L, LocalDate.now().minusDays(40), "확인 끝");
        reviewed.review(InterventionResult.PARTIAL, "일부 나아졌다", LocalDate.now().minusDays(10));

        Map<Long, Long> days = interventionService.waitingDays(List.of(open, reviewed));

        assertThat(days).containsOnlyKeys(1L);
        assertThat(days.get(1L)).isEqualTo(31L);
    }
}
