package io.poby_house.service;

import io.poby_house.domain.Assessment;
import io.poby_house.domain.AssessmentErrorTag;
import io.poby_house.domain.AssessmentType;
import io.poby_house.domain.ErrorTagCode;
import io.poby_house.domain.Student;
import io.poby_house.dto.AssessmentForm;
import io.poby_house.dto.ErrorTagInput;
import io.poby_house.repository.AssessmentErrorTagRepository;
import io.poby_house.repository.AssessmentRepository;
import io.poby_house.repository.StudentRepository;
import io.poby_house.repository.TeacherRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class AssessmentServiceTest {

    @Mock
    AssessmentRepository assessmentRepository;

    @Mock
    AssessmentErrorTagRepository assessmentErrorTagRepository;

    @Mock
    StudentRepository studentRepository;

    @Mock
    TeacherRepository teacherRepository;

    @InjectMocks
    AssessmentService assessmentService;

    private static final LocalDate DAY = LocalDate.of(2026, 8, 18);

    private final Student student = new Student();

    private AssessmentForm form(AssessmentType type) {
        AssessmentForm form = new AssessmentForm();
        form.setAssessedOn(DAY);
        form.setType(type);
        return form;
    }

    private void tag(AssessmentForm form, ErrorTagCode code, int cnt) {
        for (ErrorTagInput input : form.getTags()) {
            if (input.getCode() == code) {
                input.setCnt(cnt);
                return;
            }
        }
    }

    private int cntOf(Assessment assessment, ErrorTagCode code) {
        for (AssessmentErrorTag saved : assessment.getErrorTags()) {
            if (saved.getTagCode() == code) {
                return saved.getCnt();
            }
        }
        return 0;
    }

    private Assessment create(AssessmentForm form) {
        given(studentRepository.findById(1L)).willReturn(Optional.of(student));
        given(assessmentRepository.save(any(Assessment.class))).willAnswer(inv -> inv.getArgument(0));
        return assessmentService.create(1L, form, null);
    }

    @Test
    @DisplayName("백지면 4축이 저장된다")
    void blankAxes_savedForBlank() {
        AssessmentForm form = form(AssessmentType.BLANK);
        form.setBlankTerm(4);
        form.setBlankCondition(3);
        form.setBlankException(1);
        form.setBlankCausality(null);

        Assessment saved = create(form);

        assertThat(saved.getBlankTerm()).isEqualTo(4);
        assertThat(saved.getBlankCondition()).isEqualTo(3);
        assertThat(saved.getBlankException()).isEqualTo(1);
        // 미표시(null)는 1점이 아니다. 확인하지 않았다는 정보 그대로 남는다
        assertThat(saved.getBlankCausality()).isNull();
    }

    @Test
    @DisplayName("백지가 아니면 4축을 저장하지 않는다")
    void blankAxes_droppedForOtherTypes() {
        // 백지로 적다가 종류만 바꿔 저장한 상황. 감춘 입력도 그대로 제출된다
        AssessmentForm form = form(AssessmentType.UNIT);
        form.setBlankTerm(5);
        form.setBlankCondition(5);
        form.setBlankException(5);
        form.setBlankCausality(5);

        Assessment saved = create(form);

        assertThat(saved.getBlankTerm()).isNull();
        assertThat(saved.getBlankCondition()).isNull();
        assertThat(saved.getBlankException()).isNull();
        assertThat(saved.getBlankCausality()).isNull();
    }

    @Test
    @DisplayName("개수가 0인 오답 유형은 행을 만들지 않는다")
    void tags_zeroMakesNoRow() {
        AssessmentForm form = form(AssessmentType.UNIT);
        tag(form, ErrorTagCode.CALC, 3);
        tag(form, ErrorTagCode.COND, 0);

        Assessment saved = create(form);

        // 폼은 7종 전체를 보내 오지만 남는 것은 개수가 있는 하나뿐이다.
        // 0 을 행으로 남기면 안 틀린 유형까지 처방 목록에 올라온다
        assertThat(saved.getErrorTags()).hasSize(1);
        assertThat(cntOf(saved, ErrorTagCode.CALC)).isEqualTo(3);
    }

    @Test
    @DisplayName("수정에서 개수를 0으로 내린 유형은 지워진다")
    void tags_removedOnUpdate() {
        Assessment existing = Assessment.of(student, DAY, AssessmentType.UNIT);
        existing.putTag(ErrorTagCode.CALC, 3);
        existing.putTag(ErrorTagCode.COND, 2);
        given(assessmentRepository.findWithTagsById(9L)).willReturn(Optional.of(existing));

        AssessmentForm form = form(AssessmentType.UNIT);
        tag(form, ErrorTagCode.CALC, 4);
        tag(form, ErrorTagCode.COND, 0);

        Assessment updated = assessmentService.update(9L, form);

        // 컬렉션에서 실제로 빼야 orphanRemoval 이 지운다. 개수만 0으로 두면 행이 남는다
        assertThat(updated.getErrorTags()).hasSize(1);
        assertThat(cntOf(updated, ErrorTagCode.CALC)).isEqualTo(4);
        assertThat(cntOf(updated, ErrorTagCode.COND)).isZero();
    }

    @Test
    @DisplayName("같은 유형을 다시 저장해도 행이 늘지 않고 개수만 바뀐다")
    void tags_sameCodeKeepsOneRow() {
        Assessment existing = Assessment.of(student, DAY, AssessmentType.UNIT);
        existing.putTag(ErrorTagCode.CALC, 1);
        given(assessmentRepository.findWithTagsById(9L)).willReturn(Optional.of(existing));

        AssessmentForm form = form(AssessmentType.UNIT);
        tag(form, ErrorTagCode.CALC, 5);

        Assessment updated = assessmentService.update(9L, form);

        // (assessmentId, tagCode) 가 유니크다. 행이 둘이 되면 저장이 통째로 실패한다
        assertThat(updated.getErrorTags()).hasSize(1);
        assertThat(cntOf(updated, ErrorTagCode.CALC)).isEqualTo(5);
    }

    @Test
    @DisplayName("만점이 없거나 0이어도 백분율 계산이 터지지 않는다")
    void percent_neverDividesByZero() {
        AssessmentForm noMax = form(AssessmentType.UNIT);
        noMax.setScore(new BigDecimal("18"));
        assertThat(create(noMax).getPercent()).isNull();

        AssessmentForm zeroMax = form(AssessmentType.UNIT);
        zeroMax.setScore(new BigDecimal("18"));
        zeroMax.setMaxScore(BigDecimal.ZERO);
        assertThat(create(zeroMax).getPercent()).isNull();

        AssessmentForm both = form(AssessmentType.UNIT);
        both.setScore(new BigDecimal("18.0"));
        both.setMaxScore(new BigDecimal("20.0"));
        assertThat(create(both).getPercent()).isEqualTo(90);
    }

    @Test
    @DisplayName("태그 분포는 많이 틀린 유형이 앞이다")
    void tagDistribution_orderedByCountDesc() {
        given(assessmentErrorTagRepository.sumTagsByStudent(eq(1L), any(LocalDate.class), any(LocalDate.class)))
                .willReturn(List.of(
                        new Object[]{ErrorTagCode.CALC, 2L},
                        new Object[]{ErrorTagCode.CONCEPT, 5L},
                        new Object[]{ErrorTagCode.TIME, 3L}));

        Map<ErrorTagCode, Long> distribution =
                assessmentService.tagDistribution(1L, DAY.minusMonths(6), DAY);

        // 순서 자체가 정보다. 처방은 가장 많이 반복된 유형부터 손댄다
        assertThat(distribution.keySet())
                .containsExactly(ErrorTagCode.CONCEPT, ErrorTagCode.TIME, ErrorTagCode.CALC);
        assertThat(distribution.get(ErrorTagCode.CONCEPT)).isEqualTo(5L);
    }

    @Test
    @DisplayName("이력으로 접은 분포는 평가를 가로질러 개수를 더한다")
    void tagDistribution_foldsHistory() {
        Assessment first = Assessment.of(student, DAY.minusDays(7), AssessmentType.UNIT);
        first.putTag(ErrorTagCode.CALC, 2);
        first.putTag(ErrorTagCode.CONCEPT, 1);
        Assessment second = Assessment.of(student, DAY, AssessmentType.CUMULATIVE);
        second.putTag(ErrorTagCode.CALC, 3);

        Map<ErrorTagCode, Long> distribution = assessmentService.tagDistribution(List.of(second, first));

        assertThat(distribution).containsEntry(ErrorTagCode.CALC, 5L);
        assertThat(distribution).containsEntry(ErrorTagCode.CONCEPT, 1L);
        assertThat(distribution.keySet()).containsExactly(ErrorTagCode.CALC, ErrorTagCode.CONCEPT);
    }

    @Test
    @DisplayName("가장 최근 백지만 4축 기준으로 잡는다")
    void latestBlank_picksNewestBlankOnly() {
        Assessment oldBlank = Assessment.of(student, DAY.minusMonths(5), AssessmentType.BLANK);
        Assessment newBlank = Assessment.of(student, DAY.minusDays(3), AssessmentType.BLANK);
        Assessment unitToday = Assessment.of(student, DAY, AssessmentType.UNIT);

        // 이력은 최근 순으로 온다. 가장 위에 있는 백지가 기준이다
        Optional<Assessment> found =
                assessmentService.latestBlank(List.of(unitToday, newBlank, oldBlank));

        assertThat(found).containsSame(newBlank);
    }

    @Test
    @DisplayName("백지 기록이 없으면 4축 기준도 없다")
    void latestBlank_emptyWhenNoBlank() {
        Assessment unitToday = Assessment.of(student, DAY, AssessmentType.UNIT);

        assertThat(assessmentService.latestBlank(List.of(unitToday))).isEmpty();
    }

    @Test
    @DisplayName("빈 입력칸은 null 로 저장한다. 화면에서 대시로 보여야 한다")
    void apply_blankBecomesNull() {
        AssessmentForm form = form(AssessmentType.UNIT);
        form.setUnitName("   ");
        form.setStudentQuote("");
        form.setSourceLocation("");
        form.setNote("  ");

        Assessment saved = create(form);

        assertThat(saved.getUnitName()).isNull();
        assertThat(saved.getStudentQuote()).isNull();
        assertThat(saved.getSourceLocation()).isNull();
        assertThat(saved.getNote()).isNull();
    }
}
