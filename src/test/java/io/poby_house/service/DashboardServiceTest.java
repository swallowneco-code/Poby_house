package io.poby_house.service;

import io.poby_house.domain.ClassGroup;
import io.poby_house.domain.Student;
import io.poby_house.domain.StudentStatus;
import io.poby_house.dto.dashboard.Dashboard;
import io.poby_house.dto.dashboard.MissingAttendance;
import io.poby_house.dto.dashboard.ObservationDue;
import io.poby_house.dto.dashboard.TodayClass;
import io.poby_house.repository.ClassGroupRepository;
import io.poby_house.repository.EnrollmentRepository;
import io.poby_house.repository.ObservationRepository;
import io.poby_house.repository.StudentRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class DashboardServiceTest {

    /** 2026-08-19 은 수요일이다. 요일 판정 테스트가 이 날짜에 기대고 있다 */
    private static final LocalDate WEDNESDAY = LocalDate.of(2026, 8, 19);

    @Mock
    ClassGroupRepository classGroupRepository;

    @Mock
    EnrollmentRepository enrollmentRepository;

    @Mock
    StudentRepository studentRepository;

    @Mock
    ObservationRepository observationRepository;

    @Mock
    DashboardQueryRepository dashboardQueryRepository;

    @InjectMocks
    DashboardService dashboardService;

    private ClassGroup group(Long id, String name, String dayOfWeek) {
        ClassGroup group = new ClassGroup();
        group.setId(id);
        group.setName(name);
        group.setDayOfWeek(dayOfWeek);
        return group;
    }

    private Student student(Long id, String name, LocalDate enrolledOn) {
        Student student = new Student();
        student.setId(id);
        student.setName(name);
        student.assignCode("S2026" + String.format("%03d", id));
        student.setEnrolledOn(enrolledOn);
        student.setStatus(StudentStatus.ACTIVE);
        return student;
    }

    @Test
    @DisplayName("요일이 자유 입력이어도 월수금 반은 수요일에 오늘 수업으로 잡힌다")
    void todayClasses_matchesFreeTextDayOfWeek() {
        given(classGroupRepository.findByActiveTrueOrderByNameAsc()).willReturn(List.of(
                group(1L, "월수금 A반", "월수금"),
                group(2L, "화목 B반", "화목"),
                group(3L, "요일 미정반", null)));

        Dashboard dashboard = dashboardService.load(WEDNESDAY, false, null);

        assertThat(dashboard.getTodayClasses())
                .extracting(TodayClass::getName)
                .containsExactly("월수금 A반");
    }

    @Test
    @DisplayName("오늘 회차가 이미 열려 있으면 그 회차의 출결 개수가 붙는다")
    void todayClasses_attachesTodaySession() {
        given(classGroupRepository.findByActiveTrueOrderByNameAsc())
                .willReturn(List.of(group(1L, "월수금 A반", "월수금"), group(2L, "수요 B반", "수")));
        // 1번 반만 오늘 회차가 있고, 출결은 3명 찍혀 있다
        given(dashboardQueryRepository.sessionsOn(any(), any()))
                .willReturn(List.<Object[]>of(new Object[]{1L, 77L, 3L}));
        given(enrollmentRepository.countCurrentByClassGroup())
                .willReturn(List.<Object[]>of(new Object[]{1L, 5L}, new Object[]{2L, 4L}));

        Dashboard dashboard = dashboardService.load(WEDNESDAY, false, null);

        TodayClass opened = dashboard.getTodayClasses().get(0);
        assertThat(opened.getSessionId()).isEqualTo(77L);
        assertThat(opened.getAttendanceText()).isEqualTo("3 / 5");
        // 5명 중 3명이므로 아직 덜 찍었다. 이 상태를 "다 적음"으로 보이면 두 명이 영구히 빈다
        assertThat(opened.isAttendancePartial()).isTrue();
        assertThat(opened.isDone()).isFalse();

        TodayClass notOpened = dashboard.getTodayClasses().get(1);
        assertThat(notOpened.isSessionOpen()).isFalse();
    }

    @Test
    @DisplayName("기준선이 없는 학생이 관찰 기한 맨 위에 온다")
    void observationDue_baselineMissingFirst() {
        Student overdue = student(1L, "김민준", WEDNESDAY.minusDays(300));
        Student noBaseline = student(2L, "이서연", WEDNESDAY.minusDays(10));
        given(studentRepository.findByStatusOrderByNameAsc(StudentStatus.ACTIVE))
                .willReturn(List.of(overdue, noBaseline));
        // 1번은 기준선이 있지만 마지막 관찰이 60일 전, 2번은 기준선이 아예 없다
        given(dashboardQueryRepository.studentIdsWithBaseline()).willReturn(List.of(1L));
        given(observationRepository.latestPeriodEndByStudent())
                .willReturn(List.<Object[]>of(new Object[]{1L, WEDNESDAY.minusDays(60)}));

        Dashboard dashboard = dashboardService.load(WEDNESDAY, false, null);

        // 60일 밀린 것보다 기준선 없는 것이 급하다. 기준선은 입회 시점이 지나면 못 만든다
        assertThat(dashboard.getObservationDue().getRows())
                .extracting(ObservationDue::getStudentId)
                .containsExactly(2L, 1L);
        assertThat(dashboard.getObservationDue().getRows().get(0).isBaselineMissing()).isTrue();
    }

    @Test
    @DisplayName("마지막 관찰이 4주를 넘지 않은 학생은 관찰 기한에 오르지 않는다")
    void observationDue_skipsRecentlyObserved() {
        Student recent = student(1L, "김민준", WEDNESDAY.minusDays(300));
        given(studentRepository.findByStatusOrderByNameAsc(StudentStatus.ACTIVE)).willReturn(List.of(recent));
        given(dashboardQueryRepository.studentIdsWithBaseline()).willReturn(List.of(1L));
        given(observationRepository.latestPeriodEndByStudent())
                .willReturn(List.<Object[]>of(new Object[]{1L, WEDNESDAY.minusDays(28)}));

        Dashboard dashboard = dashboardService.load(WEDNESDAY, false, null);

        assertThat(dashboard.getObservationDue().getTotal()).isZero();
    }

    @Test
    @DisplayName("보관 만료는 원장이 아니면 조회조차 하지 않는다")
    void purgeDue_hiddenFromNonAdmin() {
        Dashboard dashboard = dashboardService.load(WEDNESDAY, false, null);

        assertThat(dashboard.isPurgeVisible()).isFalse();
        assertThat(dashboard.getPurgeDue().getTotal()).isZero();
        // 화면에서 감추는 것으로 끝내면, 감추는 것을 한 곳에서 빠뜨릴 때 퇴원생 실명이 그대로 새 나간다
        then(dashboardQueryRepository).should(never()).purgeDueStudents(any());
    }

    @Test
    @DisplayName("원장에게는 보관 만료가 보인다")
    void purgeDue_visibleToAdmin() {
        given(dashboardQueryRepository.purgeDueStudents(any())).willReturn(List.<Object[]>of(
                new Object[]{9L, "박도윤", "S2026009", WEDNESDAY.minusDays(3)}));

        Dashboard dashboard = dashboardService.load(WEDNESDAY, true, null);

        assertThat(dashboard.isPurgeVisible()).isTrue();
        assertThat(dashboard.getPurgeDue().getTotal()).isEqualTo(1);
        assertThat(dashboard.getPurgeDue().getRows().get(0).isOverdue()).isTrue();
    }

    @Test
    @DisplayName("카드 하나는 5줄까지만 보이고 남은 개수를 알려 준다")
    void card_capsRowsButKeepsTotal() {
        List<Object[]> rows = new ArrayList<>();
        for (int i = 1; i <= 7; i++) {
            rows.add(new Object[]{(long) i, 1L, "월수금 A반", WEDNESDAY.minusDays(i)});
        }
        given(dashboardQueryRepository.sessionsWithoutAttendance(any(), any())).willReturn(rows);

        Dashboard dashboard = dashboardService.load(WEDNESDAY, false, null);

        assertThat(dashboard.getMissingAttendance().getRows()).hasSize(5);
        assertThat(dashboard.getMissingAttendance().getTotal()).isEqualTo(7);
        assertThat(dashboard.getMissingAttendance().isTruncated()).isTrue();
        assertThat(dashboard.getMissingAttendance().getHiddenCount()).isEqualTo(2);
    }

    @Test
    @DisplayName("더 보기로 펼친 카드만 전체가 나온다")
    void card_expandsOnlyRequested() {
        List<Object[]> rows = new ArrayList<>();
        for (int i = 1; i <= 7; i++) {
            rows.add(new Object[]{(long) i, 1L, "월수금 A반", WEDNESDAY.minusDays(i)});
        }
        given(dashboardQueryRepository.sessionsWithoutAttendance(any(), any())).willReturn(rows);

        Dashboard dashboard = dashboardService.load(WEDNESDAY, false, "sessions");

        assertThat(dashboard.getMissingAttendance().getRows()).hasSize(7);
        assertThat(dashboard.getMissingAttendance().isTruncated()).isFalse();
        assertThat(dashboard.getMissingAttendance().getRows())
                .extracting(MissingAttendance::getDaysAgo)
                .startsWith(1L);
    }
}
