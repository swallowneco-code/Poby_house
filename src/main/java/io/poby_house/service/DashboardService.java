package io.poby_house.service;

import io.poby_house.domain.ClassGroup;
import io.poby_house.domain.Student;
import io.poby_house.domain.StudentStatus;
import io.poby_house.dto.dashboard.DashCard;
import io.poby_house.dto.dashboard.Dashboard;
import io.poby_house.dto.dashboard.MissingAttendance;
import io.poby_house.dto.dashboard.ObservationDue;
import io.poby_house.dto.dashboard.OpenIntervention;
import io.poby_house.dto.dashboard.PurgeDue;
import io.poby_house.dto.dashboard.TodayClass;
import io.poby_house.repository.ClassGroupRepository;
import io.poby_house.repository.EnrollmentRepository;
import io.poby_house.repository.ObservationRepository;
import io.poby_house.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 홈 대시보드.
 *
 * 목적은 예쁜 통계가 아니라 <b>밀린 기록을 잡아내는 것</b>이다.
 * 그래서 모든 항목은 왜 밀렸는지 알려 주는 숫자와, 바로 그 기록 화면으로 가는 링크를 함께 가진다.
 *
 * 로그인 직후 착지 화면이므로 무거우면 안 된다.
 * 학생·반이 늘어도 쿼리 수가 늘지 않게, 학생별 반복 조회는 전부 집계 한 방으로 대신한다.
 * 이 클래스가 도는 쿼리는 항상 열 번 안쪽이다(원장이 아니면 아홉 번).
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DashboardService {

    /** 카드 하나에 보여 줄 최대 줄 수. 30줄이 쏟아지면 아무것도 안 읽는다 */
    private static final int CARD_LIMIT = 5;

    /** 관찰 주기가 1~4주다. 4주를 넘기면 밀린 것으로 본다 */
    private static final int OBSERVATION_DUE_DAYS = 28;

    /** 결과를 확인하지 않은 개입을 언제부터 재촉할지 */
    private static final int INTERVENTION_OPEN_DAYS = 30;

    /** 못 적은 회차를 며칠까지 되돌아볼지. 그보다 오래되면 기억으로 복원할 수 없다 */
    private static final int MISSING_ATTENDANCE_DAYS = 14;

    /** 보관 만료 예고 기간 */
    private static final int PURGE_NOTICE_DAYS = 30;

    /** 월요일부터. DayOfWeek.getValue() 가 월요일 1 이라 그대로 인덱스로 쓴다 */
    private static final String DAY_CHARS = "월화수목금토일";

    private final ClassGroupRepository classGroupRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final StudentRepository studentRepository;
    private final ObservationRepository observationRepository;
    private final DashboardQueryRepository dashboardQueryRepository;

    /**
     * @param admin    원장인가. 보관 만료 항목은 원장만 본다
     * @param expanded 더 보기로 펼친 카드 이름(sessions / observations / interventions / purge). null 이면 전부 접힘
     */
    public Dashboard load(boolean admin, String expanded) {
        return load(LocalDate.now(), admin, expanded);
    }

    /**
     * 날짜를 인자로 받는 쪽. 테스트가 수요일을 지정할 수 있어야 한다.
     * LocalDate.now() 를 안에서 부르면 요일 판정 규칙을 화요일에만 통과하는 테스트로밖에 못 잠근다.
     */
    Dashboard load(LocalDate today, boolean admin, String expanded) {
        List<Student> activeStudents = studentRepository.findByStatusOrderByNameAsc(StudentStatus.ACTIVE);
        LocalDate quarterStart = quarterStart(today);
        LocalDate quarterEnd = quarterStart.plusMonths(3).minusDays(1);

        return new Dashboard(
                today,
                todayLabel(today),
                quarterLabel(quarterStart),
                todayClasses(today),
                DashCard.of(missingAttendance(today), CARD_LIMIT, "sessions".equals(expanded)),
                DashCard.of(observationDue(today, activeStudents), CARD_LIMIT, "observations".equals(expanded)),
                DashCard.of(openInterventions(today), CARD_LIMIT, "interventions".equals(expanded)),
                DashCard.of(purgeDue(today, admin), CARD_LIMIT, "purge".equals(expanded)),
                admin,
                dashboardQueryRepository.countActiveStudentsWithoutScene(quarterStart, quarterEnd),
                activeStudents.size());
    }

    /**
     * 오늘 수업이 있는 반. 반마다 오늘 회차가 열렸는지, 출결을 몇 명 찍었는지까지 붙인다.
     * 반 하나씩 회차를 찾으면 반 개수만큼 쿼리가 나가므로 한 번에 받아 맵으로 맞춘다.
     */
    private List<TodayClass> todayClasses(LocalDate today) {
        List<ClassGroup> groups = new ArrayList<>();
        for (ClassGroup group : classGroupRepository.findByActiveTrueOrderByNameAsc()) {
            if (meetsOn(group.getDayOfWeek(), today)) {
                groups.add(group);
            }
        }
        if (groups.isEmpty()) {
            return List.of();
        }

        List<Long> ids = new ArrayList<>();
        for (ClassGroup group : groups) {
            ids.add(group.getId());
        }

        Map<Long, Long> sessionIds = new HashMap<>();
        Map<Long, Long> attendanceCounts = new HashMap<>();
        for (Object[] row : dashboardQueryRepository.sessionsOn(today, ids)) {
            sessionIds.put((Long) row[0], (Long) row[1]);
            attendanceCounts.put((Long) row[0], (Long) row[2]);
        }

        Map<Long, Long> headcounts = new HashMap<>();
        for (Object[] row : enrollmentRepository.countCurrentByClassGroup()) {
            headcounts.put((Long) row[0], (Long) row[1]);
        }

        List<TodayClass> rows = new ArrayList<>();
        for (ClassGroup group : groups) {
            rows.add(new TodayClass(
                    group.getId(),
                    group.getName(),
                    group.getScheduleText(),
                    sessionIds.get(group.getId()),
                    attendanceCounts.getOrDefault(group.getId(), 0L),
                    headcounts.getOrDefault(group.getId(), 0L)));
        }
        return rows;
    }

    /**
     * 오늘 수업이 있는 반인가.
     *
     * ClassGroup.dayOfWeek 는 월수금, 화목 같은 <b>자유 입력 문자열</b>이다.
     * 그래서 오늘 요일 한 글자가 그 안에 들어 있는지로 판단한다.
     * 쉼표를 넣은 표기나 "매주 화" 는 걸리지만 "매일" 이나 "격주 화" 는 못 잡는다.
     * 요일을 구조화하면(요일 비트마스크 컬럼 등) 바뀌는 곳이 여기다.
     */
    static boolean meetsOn(String dayOfWeek, LocalDate date) {
        return dayOfWeek != null && dayOfWeek.indexOf(dayChar(date)) >= 0;
    }

    static char dayChar(LocalDate date) {
        return DAY_CHARS.charAt(date.getDayOfWeek().getValue() - 1);
    }

    /**
     * 최근 2주 안에 열렸지만 출결이 하나도 없는 회차.
     * 오늘은 뺀다. 오늘 것은 오늘 수업 카드가 이미 다루고 있어서
     * 여기 또 나오면 같은 회차가 화면에 두 번 앉는다.
     */
    private List<MissingAttendance> missingAttendance(LocalDate today) {
        List<MissingAttendance> rows = new ArrayList<>();
        for (Object[] row : dashboardQueryRepository.sessionsWithoutAttendance(
                today.minusDays(MISSING_ATTENDANCE_DAYS), today.minusDays(1))) {
            LocalDate sessionDate = (LocalDate) row[3];
            rows.add(new MissingAttendance(
                    (Long) row[0], (Long) row[1], (String) row[2], sessionDate,
                    ChronoUnit.DAYS.between(sessionDate, today)));
        }
        return rows;
    }

    /**
     * 관찰이 밀린 재원생.
     *
     * 기준선(BASELINE)이 없는 학생을 맨 위에 둔다. 유예 기간을 두지 않는다.
     * 기준선은 리포트의 before 인데, 입회 시점이 지나 버리면 그 스냅샷은 두 번 다시 만들 수 없다.
     * 며칠 있다 적자를 허용하면 그 학생의 성장은 영원히 쓸 수 없게 된다.
     */
    private List<ObservationDue> observationDue(LocalDate today, List<Student> activeStudents) {
        if (activeStudents.isEmpty()) {
            return List.of();
        }
        Set<Long> withBaseline = new HashSet<>(dashboardQueryRepository.studentIdsWithBaseline());
        Map<Long, LocalDate> lastObserved = new HashMap<>();
        for (Object[] row : observationRepository.latestPeriodEndByStudent()) {
            lastObserved.put((Long) row[0], (LocalDate) row[1]);
        }

        List<ObservationDue> rows = new ArrayList<>();
        for (Student student : activeStudents) {
            LocalDate last = lastObserved.get(student.getId());
            if (!withBaseline.contains(student.getId())) {
                rows.add(new ObservationDue(student.getId(), student.getName(), student.getCode(),
                        true, last, daysBetween(student.getEnrolledOn(), today)));
                continue;
            }
            if (last == null) {
                continue;
            }
            long days = ChronoUnit.DAYS.between(last, today);
            if (days > OBSERVATION_DUE_DAYS) {
                rows.add(new ObservationDue(student.getId(), student.getName(), student.getCode(),
                        false, last, days));
            }
        }

        // 기준선 없는 학생이 항상 위, 그 다음이 오래 밀린 순서.
        // Comparator 조합으로 접지 않은 이유: 기준선이 무엇보다 앞선다는 것이 이 화면의 규칙이라 눈에 보여야 한다.
        rows.sort((a, b) -> {
            if (a.isBaselineMissing() != b.isBaselineMissing()) {
                return a.isBaselineMissing() ? -1 : 1;
            }
            return Long.compare(b.getDaysSince(), a.getDaysSince());
        });
        return rows;
    }

    private List<OpenIntervention> openInterventions(LocalDate today) {
        List<OpenIntervention> rows = new ArrayList<>();
        for (Object[] row : dashboardQueryRepository.unreviewedInterventions(
                today.minusDays(INTERVENTION_OPEN_DAYS))) {
            LocalDate triedOn = (LocalDate) row[4];
            rows.add(new OpenIntervention(
                    (Long) row[0], (Long) row[1], (String) row[2], (String) row[3], triedOn,
                    ChronoUnit.DAYS.between(triedOn, today)));
        }
        return rows;
    }

    /**
     * 보관 만료. 원장만 본다.
     *
     * 원장이 아니면 조회 자체를 하지 않는다. 목록을 만들어 두고 화면에서 감추는 방식은 쓰지 않는다.
     * 감추는 것을 한 곳에서 빠뜨리면 그대로 새 나가고, 여기 실려 있는 것은 퇴원생 실명이다.
     */
    private List<PurgeDue> purgeDue(LocalDate today, boolean admin) {
        if (!admin) {
            return List.of();
        }
        List<PurgeDue> rows = new ArrayList<>();
        for (Object[] row : dashboardQueryRepository.purgeDueStudents(today.plusDays(PURGE_NOTICE_DAYS))) {
            LocalDate purgeAfter = (LocalDate) row[3];
            rows.add(new PurgeDue(
                    (Long) row[0], (String) row[1], (String) row[2], purgeAfter,
                    ChronoUnit.DAYS.between(today, purgeAfter)));
        }
        return rows;
    }

    /** 등록일이 비어 있는 데이터가 섞여도 홈 화면이 죽지 않아야 한다 */
    private static long daysBetween(LocalDate from, LocalDate to) {
        return from == null ? 0 : ChronoUnit.DAYS.between(from, to);
    }

    private static LocalDate quarterStart(LocalDate date) {
        return LocalDate.of(date.getYear(), ((date.getMonthValue() - 1) / 3) * 3 + 1, 1);
    }

    private static String quarterLabel(LocalDate quarterStart) {
        return quarterStart.getYear() + "년 " + ((quarterStart.getMonthValue() - 1) / 3 + 1) + "분기";
    }

    private static String todayLabel(LocalDate date) {
        return date.getMonthValue() + "월 " + date.getDayOfMonth() + "일 (" + dayChar(date) + ")";
    }
}
