package io.poby_house.service;

import io.poby_house.domain.AttendanceStatus;
import io.poby_house.domain.ClassSession;
import io.poby_house.domain.Intervention;
import io.poby_house.domain.InterventionResult;
import io.poby_house.domain.Observation;
import io.poby_house.domain.ObservationKind;
import io.poby_house.domain.ProgressLog;
import io.poby_house.domain.Student;
import io.poby_house.dto.report.InterventionGroups;
import io.poby_house.dto.report.ReportView;
import io.poby_house.dto.report.WhatWeDid;
import io.poby_house.repository.AssessmentErrorTagRepository;
import io.poby_house.repository.AssessmentRepository;
import io.poby_house.repository.AttendanceRepository;
import io.poby_house.repository.ConsultationRepository;
import io.poby_house.repository.EnrollmentRepository;
import io.poby_house.repository.InterventionRepository;
import io.poby_house.repository.ObservationRepository;
import io.poby_house.repository.SceneRepository;
import io.poby_house.repository.StudentRepository;
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
import static org.mockito.BDDMockito.given;

/**
 * 리포트가 지켜야 하는 판단만 잠근다.
 *
 * 여기 있는 네 가지는 전부 "틀리면 리포트가 거짓말을 하는" 규칙이다.
 * 기준선 없이 변화를 주장하지 않는다 / 반출 모드에서 실명이 새지 않는다 /
 * 0 회차로 나누지 않는다 / 진도 구간이 교재 경계에서 끊긴다.
 */
@ExtendWith(MockitoExtension.class)
class ReportServiceTest {

    private static final Long ID = 1L;
    private static final LocalDate FROM = LocalDate.of(2026, 3, 2);
    private static final LocalDate TO = LocalDate.of(2026, 8, 18);

    @Mock
    StudentRepository studentRepository;

    @Mock
    EnrollmentRepository enrollmentRepository;

    @Mock
    AttendanceRepository attendanceRepository;

    @Mock
    ObservationRepository observationRepository;

    @Mock
    InterventionRepository interventionRepository;

    @Mock
    SceneRepository sceneRepository;

    @Mock
    AssessmentRepository assessmentRepository;

    @Mock
    AssessmentErrorTagRepository assessmentErrorTagRepository;

    @Mock
    ConsultationRepository consultationRepository;

    @Mock
    ReportQueryRepository reportQueryRepository;

    @InjectMocks
    ReportService reportService;

    private final Student student = student();

    /* ================================================================
       3. 무엇이 달라졌나 — 기준선
       ================================================================ */

    @Test
    @DisplayName("입회 기준선이 없으면 변화 섹션이 경고를 낸다. 가장 이른 관찰로 대신하지 않는다")
    void baselineMissing_warns() {
        given(observationRepository.findByStudentIdOrderByPeriodEndDesc(ID))
                .willReturn(List.of(observation(ObservationKind.PERIODIC, LocalDate.of(2026, 7, 1), 4, 3)));

        ReportView view = draft(false);

        assertThat(view.whatChanged().baselineMissing()).isTrue();
        assertThat(view.whatChanged().warning()).contains("기준선").contains("증명할 수 없습니다");
        assertThat(view.whatChanged().hint()).isNotBlank();
        // 가장 이른 관찰은 참고로만 붙는다. 이것을 before 로 쓰면 성장 폭이 실제보다 작게 나온다
        assertThat(view.whatChanged().earliest()).isNotNull();
        assertThat(view.whatChanged().understanding().comparable()).isFalse();
        assertThat(view.whatChanged().understanding().before()).isNull();
        assertThat(view.plainText()).contains("증명할 수 없습니다");
    }

    @Test
    @DisplayName("기준선과 최신 관찰이 있으면 점수 변화가 화살표로 계산된다")
    void baselineAndLatest_compare() {
        given(observationRepository.findByStudentIdOrderByPeriodEndDesc(ID)).willReturn(List.of(
                observation(ObservationKind.PERIODIC, LocalDate.of(2026, 7, 1), 4, 3),
                observation(ObservationKind.BASELINE, LocalDate.of(2026, 3, 30), 2, 2)));

        ReportView view = draft(false);

        assertThat(view.whatChanged().baselineMissing()).isFalse();
        assertThat(view.whatChanged().warning()).isNull();
        assertThat(view.whatChanged().understanding().comparable()).isTrue();
        assertThat(view.whatChanged().understanding().text()).isEqualTo("2 → 4 (↑2)");
        assertThat(view.whatChanged().understanding().up()).isTrue();
        assertThat(view.whatChanged().homeworkRate().diff()).isEqualTo(1);
        // 기준선이 없을 때만 붙는 참고 항목이다. 기준선이 있으면 나오지 않아야 한다
        assertThat(view.whatChanged().earliest()).isNull();
    }

    @Test
    @DisplayName("관찰이 기준선 한 건뿐이면 자기 자신과 비교하지 않고 경고를 낸다")
    void onlyBaseline_notComparedWithItself() {
        given(observationRepository.findByStudentIdOrderByPeriodEndDesc(ID))
                .willReturn(List.of(observation(ObservationKind.BASELINE, LocalDate.of(2026, 3, 30), 2, 2)));

        ReportView view = draft(false);

        assertThat(view.whatChanged().onlyOneObservation()).isTrue();
        assertThat(view.whatChanged().understanding().comparable()).isFalse();
        assertThat(view.whatChanged().warning()).contains("한 건뿐");
    }

    /* ================================================================
       외부 반출 모드
       ================================================================ */

    @Test
    @DisplayName("반출 모드에서는 실명과 학교가 결과에 담기지 않는다. 복사용 평문에도 없다")
    void anon_hasNoNameOrSchool() {
        ReportView view = draft(true);

        assertThat(view.header().anon()).isTrue();
        assertThat(view.header().displayName()).isEqualTo("S2026001");
        assertThat(view.header().school()).isNull();
        assertThat(view.header().leaveReason()).isNull();
        // 평문은 DTO 에서만 만들어진다. 그래서 감춘 값이 여기 들어갈 길이 없다
        assertThat(view.plainText()).doesNotContain("김민준");
        assertThat(view.plainText()).doesNotContain("대치초");
        assertThat(view.plainText()).contains("S2026001");
    }

    @Test
    @DisplayName("반출 모드 평문에는 '본문에 실명이 있을 수 있다'는 단서가 함께 들어간다")
    void anon_plainTextCarriesCaution() {
        ReportView view = draft(true);

        // 실제로 외부로 나가는 물건은 '텍스트 복사'가 만든 이 평문이다.
        // 여기에 "실명과 학교를 감췄습니다" 만 있으면 검토·발송하는 사람이 본문을 읽지 않는다
        assertThat(view.plainText()).contains(view.header().modeCaution());
    }

    @Test
    @DisplayName("내부 모드에는 단서를 붙이지 않는다. 실명이 보이는 것이 이미 화면에 적혀 있다")
    void named_hasNoCaution() {
        assertThat(draft(false).header().modeCaution()).isNull();
    }

    @Test
    @DisplayName("내부 모드에서는 실명과 학교가 그대로 나온다")
    void named_showsNameAndSchool() {
        ReportView view = draft(false);

        assertThat(view.header().displayName()).isEqualTo("김민준");
        assertThat(view.header().school()).isEqualTo("대치초");
        assertThat(view.plainText()).contains("김민준");
    }

    /* ================================================================
       1. 함께한 시간 — 출석률
       ================================================================ */

    @Test
    @DisplayName("출결을 한 건도 표시하지 않았으면 출석률은 0% 가 아니라 '셀 수 없음' 이다")
    void attendanceRate_neverDividesByZero() {
        ReportView view = draft(false);

        assertThat(view.togetherTime().attendable()).isZero();
        assertThat(view.togetherTime().attendanceRate()).isNull();
        assertThat(view.togetherTime().rateUnknown()).isTrue();
        assertThat(view.plainText()).contains("셀 수 없음");
    }

    @Test
    @DisplayName("휴원은 출석률 분모에서 뺀다. 결석과 섞으면 쉬었던 기간 때문에 출석률이 깎인다")
    void attendanceRate_excludesPaused() {
        given(attendanceRepository.countByStatusForStudent(ID, FROM, TO)).willReturn(List.of(
                new Object[]{AttendanceStatus.PRESENT, 9L},
                new Object[]{AttendanceStatus.LATE, 1L},
                new Object[]{AttendanceStatus.ABSENT, 2L},
                new Object[]{AttendanceStatus.PAUSED, 8L}));

        ReportView view = draft(false);

        assertThat(view.togetherTime().attended()).isEqualTo(10);
        assertThat(view.togetherTime().attendable()).isEqualTo(12);
        assertThat(view.togetherTime().attendanceRate()).isEqualTo(83);
        // 표시한 회차 자체는 휴원까지 20건이다. 분모에서만 빠진다
        assertThat(view.togetherTime().recordedSessions()).isEqualTo(20);
    }

    /* ================================================================
       2. 무엇을 했나 — 진도 구간 압축
       ================================================================ */

    @Test
    @DisplayName("진도 구간은 교재가 바뀌는 지점에서 끊긴다")
    void spans_breakOnTextbookChange() {
        given(reportQueryRepository.findProgressInPeriod(ID, FROM, TO)).willReturn(List.of(
                progress("쎈수학 5-2", "p.1~10", LocalDate.of(2026, 3, 2)),
                progress("쎈수학 5-2", "p.11~20", LocalDate.of(2026, 3, 9)),
                progress("일품 5-2", "p.1~5", LocalDate.of(2026, 3, 16))));

        List<WhatWeDid.ProgressSpan> spans = draft(false).whatWeDid().spans();

        assertThat(spans).hasSize(2);
        assertThat(spans.get(0).textbook()).isEqualTo("쎈수학 5-2");
        assertThat(spans.get(0).sessions()).isEqualTo(2);
        assertThat(spans.get(0).from()).isEqualTo(LocalDate.of(2026, 3, 2));
        assertThat(spans.get(0).to()).isEqualTo(LocalDate.of(2026, 3, 9));
        assertThat(spans.get(0).rangeText()).isEqualTo("p.1~10 → p.11~20");
        assertThat(spans.get(1).textbook()).isEqualTo("일품 5-2");
        assertThat(spans.get(1).sessions()).isEqualTo(1);
        // 한 회차뿐이면 화살표 없이 그 범위만 쓴다
        assertThat(spans.get(1).rangeText()).isEqualTo("p.1~5");
    }

    @Test
    @DisplayName("같은 교재가 나중에 다시 나오면 새 구간이다. 하나로 합치면 중간에 다른 교재를 한 사실이 사라진다")
    void spans_sameTextbookAgainIsNewSpan() {
        given(reportQueryRepository.findProgressInPeriod(ID, FROM, TO)).willReturn(List.of(
                progress("쎈수학 5-2", "p.1~10", LocalDate.of(2026, 3, 2)),
                progress("일품 5-2", "p.1~5", LocalDate.of(2026, 3, 9)),
                progress("쎈수학 5-2", "p.11~20", LocalDate.of(2026, 3, 16))));

        List<WhatWeDid.ProgressSpan> spans = draft(false).whatWeDid().spans();

        assertThat(spans).hasSize(3);
        assertThat(spans).extracting(WhatWeDid.ProgressSpan::textbook)
                .containsExactly("쎈수학 5-2", "일품 5-2", "쎈수학 5-2");
    }

    /* ================================================================
       4. 통한 것 / 안 통한 것
       ================================================================ */

    @Test
    @DisplayName("결과 미확인과 진행중을 같은 묶음에 넣지 않는다")
    void interventions_unreviewedIsNotOngoing() {
        Intervention ongoing = Intervention.of(student, LocalDate.of(2026, 5, 1), "과제 미제출", "수업 끝에 5분 함께 시작");
        ongoing.review(InterventionResult.ONGOING, "두 주 지켜보는 중", LocalDate.of(2026, 5, 15));
        Intervention unreviewed = Intervention.of(student, LocalDate.of(2026, 5, 2), "지각", "10분 일찍 오기로 약속");
        given(interventionRepository.findByStudentIdOrderByTriedOnDesc(ID))
                .willReturn(List.of(unreviewed, ongoing));

        InterventionGroups groups = draft(false).interventions();

        assertThat(groups.total()).isEqualTo(2);
        assertThat(group(groups, "ONGOING").count()).isEqualTo(1);
        assertThat(group(groups, "UNREVIEWED").count()).isEqualTo(1);
        assertThat(group(groups, "ONGOING").items().get(0).resultNote()).isEqualTo("두 주 지켜보는 중");
        // 근거가 없는 미확인 항목은 "결과 없음" 이 아니라 결과 미확인으로 남아야 한다
        assertThat(group(groups, "UNREVIEWED").items().get(0).resultLabel()).isEqualTo("결과 미확인");
        // 결과를 적은 항목만 근거 없음으로 센다. 미확인은 애초에 근거를 적을 수 없다
        assertThat(groups.evidenceMissing()).isZero();
    }

    /* ================================================================
       도우미
       ================================================================ */

    private ReportView draft(boolean anon) {
        given(studentRepository.findById(ID)).willReturn(Optional.of(student));
        return reportService.draft(ID, FROM, TO, anon);
    }

    private InterventionGroups.Group group(InterventionGroups groups, String key) {
        return groups.groups().stream()
                .filter(g -> g.key().equals(key))
                .findFirst()
                .orElseThrow(() -> new AssertionError("묶음이 없습니다. key=" + key));
    }

    private static Student student() {
        Student student = new Student();
        student.assignCode("S2026001");
        student.setName("김민준");
        student.setSchool("대치초");
        student.setGrade("초6");
        student.setEnrolledOn(FROM);
        return student;
    }

    private Observation observation(ObservationKind kind, LocalDate periodEnd,
                                   Integer understanding, Integer homeworkRate) {
        Observation observation = Observation.of(student, kind, periodEnd.minusDays(28), periodEnd);
        observation.setUnderstanding(understanding);
        observation.setHomeworkRate(homeworkRate);
        return observation;
    }

    private ProgressLog progress(String textbook, String rangeText, LocalDate on) {
        ClassSession session = new ClassSession();
        session.setSessionDate(on);
        ProgressLog log = ProgressLog.of(session, student);
        log.setTextbook(textbook);
        log.setRangeText(rangeText);
        return log;
    }
}
