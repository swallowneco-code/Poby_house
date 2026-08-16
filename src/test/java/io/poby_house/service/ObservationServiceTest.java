package io.poby_house.service;

import io.poby_house.domain.Observation;
import io.poby_house.domain.ObservationKind;
import io.poby_house.domain.Student;
import io.poby_house.dto.ObservationForm;
import io.poby_house.repository.ObservationRepository;
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
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class ObservationServiceTest {

    private static final Long STUDENT_ID = 1L;

    @Mock
    ObservationRepository observationRepository;

    @Mock
    StudentRepository studentRepository;

    @Mock
    TeacherRepository teacherRepository;

    @InjectMocks
    ObservationService observationService;

    // ------------------------------------------------------------------
    // 기준선은 학생마다 하나
    // ------------------------------------------------------------------

    @Test
    @DisplayName("기준선이 이미 있으면 두 번째 기준선을 만들지 못한다")
    void create_rejectsSecondBaseline() {
        givenBaseline(Optional.of(observation(3L, ObservationKind.BASELINE, LocalDate.now().minusMonths(5))));

        assertThatThrownBy(() -> observationService.create(STUDENT_ID, form(ObservationKind.BASELINE), null))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("하나");
    }

    @Test
    @DisplayName("기준선이 없으면 기준선을 만들 수 있다")
    void create_allowsFirstBaseline() {
        givenBaseline(Optional.empty());
        givenStudent(LocalDate.now().minusMonths(1));
        givenSaveReturnsArgument();

        Observation saved = observationService.create(STUDENT_ID, form(ObservationKind.BASELINE), null);

        assertThat(saved.getKind()).isEqualTo(ObservationKind.BASELINE);
        assertThat(saved.isBaseline()).isTrue();
    }

    @Test
    @DisplayName("정기 관찰을 기준선으로 올릴 때도 기준선이 이미 있으면 거절한다")
    void update_rejectsSecondBaseline() {
        Observation periodic = observation(9L, ObservationKind.PERIODIC, LocalDate.now().minusDays(3));
        given(observationRepository.findById(9L)).willReturn(Optional.of(periodic));
        givenBaseline(Optional.of(observation(3L, ObservationKind.BASELINE, LocalDate.now().minusMonths(5))));

        assertThatThrownBy(() -> observationService.update(9L, form(ObservationKind.BASELINE)))
                .isInstanceOf(BusinessRuleException.class);

        // 거절했으면 종류가 바뀌어 있어서는 안 된다. 예외를 던지기 전에 setKind 를 하면
        // 같은 트랜잭션에서 더티 체킹으로 저장돼 기준선이 둘이 된다
        assertThat(periodic.getKind()).isEqualTo(ObservationKind.PERIODIC);
    }

    @Test
    @DisplayName("기준선이 없으면 정기 관찰을 기준선으로 올릴 수 있다")
    void update_promotesToBaseline() {
        Observation periodic = observation(9L, ObservationKind.PERIODIC, LocalDate.now().minusDays(3));
        given(observationRepository.findById(9L)).willReturn(Optional.of(periodic));
        givenBaseline(Optional.empty());

        observationService.update(9L, form(ObservationKind.BASELINE));

        assertThat(periodic.getKind()).isEqualTo(ObservationKind.BASELINE);
    }

    // ------------------------------------------------------------------
    // 기간 기본값 — 폰에서 날짜를 굴리지 않게 하는 것이 목적이다
    // ------------------------------------------------------------------

    @Test
    @DisplayName("기간 기본값은 지난 관찰이 끝난 다음날부터 오늘까지다")
    void newForm_startsAfterLastObservation() {
        givenLatest(Optional.of(observation(5L, ObservationKind.PERIODIC, LocalDate.now().minusDays(7))));

        ObservationForm form = observationService.newForm(STUDENT_ID, ObservationKind.PERIODIC);

        assertThat(form.getPeriodStart()).isEqualTo(LocalDate.now().minusDays(6));
        assertThat(form.getPeriodEnd()).isEqualTo(LocalDate.now());
    }

    @Test
    @DisplayName("관찰이 하나도 없으면 기간은 학생 등록일부터다")
    void newForm_startsAtEnrolledOn() {
        LocalDate enrolledOn = LocalDate.now().minusMonths(2);
        givenLatest(Optional.empty());
        givenStudent(enrolledOn);

        ObservationForm form = observationService.newForm(STUDENT_ID, ObservationKind.PERIODIC);

        assertThat(form.getPeriodStart()).isEqualTo(enrolledOn);
        assertThat(form.getPeriodEnd()).isEqualTo(LocalDate.now());
    }

    @Test
    @DisplayName("지난 관찰이 오늘까지 잡혀 있어도 기간이 뒤집히지 않는다")
    void newForm_neverInverted() {
        givenLatest(Optional.of(observation(5L, ObservationKind.PERIODIC, LocalDate.now())));

        ObservationForm form = observationService.newForm(STUDENT_ID, ObservationKind.PERIODIC);

        // 시작이 내일이 되면 폼을 열자마자 검증 오류가 뜬다. 끝을 시작까지 밀어 준다
        assertThat(form.getPeriodStart()).isEqualTo(LocalDate.now().plusDays(1));
        assertThat(form.getPeriodEnd()).isEqualTo(form.getPeriodStart());
    }

    @Test
    @DisplayName("기준선은 지난 관찰과 무관하게 등록일부터 시작한다")
    void newForm_baselineStartsAtEnrolledOn() {
        LocalDate enrolledOn = LocalDate.now().minusMonths(4);
        givenStudent(enrolledOn);

        ObservationForm form = observationService.newForm(STUDENT_ID, ObservationKind.BASELINE);

        // 기준선은 "들어왔을 때의 모습" 이다. 지난 관찰 다음날부터로 채우면
        // 입회 스냅샷인데 기간이 몇 달 뒤부터 시작하는 앞뒤 안 맞는 기록이 남는다
        assertThat(form.getPeriodStart()).isEqualTo(enrolledOn);
    }

    // ------------------------------------------------------------------
    // 빈 칸
    // ------------------------------------------------------------------

    @Test
    @DisplayName("안 적은 칸은 null 로 저장한다. 미표시 점수도 null 그대로 남는다")
    void create_blankBecomesNull() {
        givenStudent(LocalDate.now().minusMonths(1));
        givenSaveReturnsArgument();

        ObservationForm form = form(ObservationKind.PERIODIC);
        form.setAttitudeNote("");
        form.setStrength("   ");
        form.setWeakness("");
        form.setSelfAssessment("  ");
        form.setTeacherComment("");

        Observation saved = observationService.create(STUDENT_ID, form, null);

        assertThat(saved.getAttitudeNote()).isNull();
        assertThat(saved.getStrength()).isNull();
        assertThat(saved.getWeakness()).isNull();
        assertThat(saved.getSelfAssessment()).isNull();
        assertThat(saved.getTeacherComment()).isNull();
        // 확인하지 않은 것과 1점은 다른 정보다. 0 이나 1 로 눌러 담지 않는다
        assertThat(saved.getUnderstanding()).isNull();
        assertThat(saved.getHomeworkRate()).isNull();
    }

    @Test
    @DisplayName("적은 칸은 앞뒤 공백만 떼고 그대로 남는다")
    void create_trimsButKeepsContent() {
        givenStudent(LocalDate.now().minusMonths(1));
        givenSaveReturnsArgument();

        ObservationForm form = form(ObservationKind.PERIODIC);
        form.setSelfAssessment("  문제 읽는 게 어려워요  ");
        form.setUnderstanding(4);

        Observation saved = observationService.create(STUDENT_ID, form, null);

        assertThat(saved.getSelfAssessment()).isEqualTo("문제 읽는 게 어려워요");
        assertThat(saved.getUnderstanding()).isEqualTo(4);
    }

    // ------------------------------------------------------------------
    // 도우미
    // ------------------------------------------------------------------

    private ObservationForm form(ObservationKind kind) {
        ObservationForm form = new ObservationForm();
        form.setKind(kind);
        form.setPeriodStart(LocalDate.now().minusDays(14));
        form.setPeriodEnd(LocalDate.now());
        return form;
    }

    private Student student(LocalDate enrolledOn) {
        Student student = new Student();
        student.setId(STUDENT_ID);
        student.setEnrolledOn(enrolledOn);
        return student;
    }

    private Observation observation(Long id, ObservationKind kind, LocalDate periodEnd) {
        Observation observation = Observation.of(
                student(LocalDate.now().minusMonths(6)), kind, periodEnd.minusDays(7), periodEnd);
        observation.setId(id);
        return observation;
    }

    private void givenBaseline(Optional<Observation> result) {
        given(observationRepository.findFirstByStudentIdAndKindOrderByPeriodEndAsc(
                STUDENT_ID, ObservationKind.BASELINE)).willReturn(result);
    }

    private void givenLatest(Optional<Observation> result) {
        given(observationRepository.findFirstByStudentIdOrderByPeriodEndDesc(STUDENT_ID)).willReturn(result);
    }

    private void givenStudent(LocalDate enrolledOn) {
        given(studentRepository.findById(STUDENT_ID)).willReturn(Optional.of(student(enrolledOn)));
    }

    private void givenSaveReturnsArgument() {
        given(observationRepository.save(any(Observation.class))).willAnswer(inv -> inv.getArgument(0));
    }
}
