package io.poby_house.service;

import io.poby_house.domain.Assessment;
import io.poby_house.domain.AssessmentType;
import io.poby_house.domain.AttendanceStatus;
import io.poby_house.domain.Consultation;
import io.poby_house.domain.Enrollment;
import io.poby_house.domain.ErrorTagCode;
import io.poby_house.domain.HomeworkStatus;
import io.poby_house.domain.Intervention;
import io.poby_house.domain.InterventionResult;
import io.poby_house.domain.Observation;
import io.poby_house.domain.ProgressLog;
import io.poby_house.domain.Scene;
import io.poby_house.domain.Student;
import io.poby_house.domain.Teacher;
import io.poby_house.domain.UnitStatus;
import io.poby_house.dto.report.InterventionGroups;
import io.poby_house.dto.report.Promises;
import io.poby_house.dto.report.ReportHeader;
import io.poby_house.dto.report.ReportView;
import io.poby_house.dto.report.SceneItem;
import io.poby_house.dto.report.TogetherTime;
import io.poby_house.dto.report.WeakSpots;
import io.poby_house.dto.report.WhatChanged;
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
import io.poby_house.support.BusinessRuleException;
import io.poby_house.support.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 퇴원생 리포트 초안.
 *
 * 이 앱이 존재하는 이유이고, 모든 기록이 여기로 모인다. 규칙은 하나다.
 * <b>문장을 지어내지 않는다.</b> 기록된 사실과 그 근거를 배치하고, 없는 것은 없다고 말한다.
 * 그리고 "무엇을 적으면 이 칸이 채워지는가" 를 함께 알려 준다. 그 안내가 곧 사용 유도다.
 *
 * 쿼리는 학생 한 명당 열한 번이다(학생 · 반배정 · 출결집계 · 회차수 · 진도 · 관찰 ·
 * 개입 · 장면 · 평가 · 태그합계 · 상담). 집계 셋은 group by 한 방으로 끝내고,
 * 목록은 필요한 연관만 @EntityGraph / join fetch 로 동반한다.
 * <b>반복문 안에서 조회하지 않는다.</b> 여기서 한 번 미끄러지면 학생마다 수십 쿼리가 나간다.
 *
 * 다른 기록 서비스(ObservationService 등)를 부르지 않고 리포지토리를 직접 쓴다.
 * 리포트는 읽기 전용이고, 저장 쪽 규칙이 바뀌어도 지난 기록을 그대로 인용해야 한다.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReportService {

    private final StudentRepository studentRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final AttendanceRepository attendanceRepository;
    private final ObservationRepository observationRepository;
    private final InterventionRepository interventionRepository;
    private final SceneRepository sceneRepository;
    private final AssessmentRepository assessmentRepository;
    private final AssessmentErrorTagRepository assessmentErrorTagRepository;
    private final ConsultationRepository consultationRepository;
    private final ReportQueryRepository reportQueryRepository;

    /**
     * 리포트 초안 한 장.
     *
     * @param anon 외부 반출 모드. 실명과 학교를 결과에 담지 않는다
     */
    public ReportView draft(Long studentId, LocalDate fromParam, LocalDate toParam, boolean anon) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new NotFoundException("학생을 찾을 수 없습니다. id=" + studentId));

        // 기본 기간은 학생의 전체 기간이다. 퇴원생이면 퇴원일까지, 아니면 오늘까지.
        // 기간을 매번 고르게 만들면 아무도 이 화면을 열지 않는다
        LocalDate to = toParam != null ? toParam
                : (student.getLeftOn() != null ? student.getLeftOn() : LocalDate.now());
        LocalDate begin = fromParam != null ? fromParam
                : (student.getEnrolledOn() != null ? student.getEnrolledOn() : to);
        if (begin.isAfter(to)) {
            if (fromParam != null || toParam != null) {
                throw new BusinessRuleException("기간이 거꾸로입니다. 시작일이 종료일보다 늦습니다.");
            }
            // 등록일이 퇴원일보다 늦은 어긋난 데이터. 그것 때문에 리포트를 아예 못 열게 만들지는 않는다
            to = begin;
        }
        final LocalDate from = begin;
        final LocalDate until = to;

        Map<AttendanceStatus, Long> attendance = attendanceCounts(studentId, from, until);
        Map<ErrorTagCode, Long> tagSums = tagSums(studentId, from, until);
        long heldSessions = reportQueryRepository.countHeldSessions(studentId, from, until);
        List<ProgressLog> logs = reportQueryRepository.findProgressInPeriod(studentId, from, until);

        // 관찰은 기간으로 자르지 않고 전부 읽는다. 기준선(입회 시점)이 조회 기간보다 앞서기 때문이다.
        // 기간으로 자르면 기간을 좁힐 때마다 before 가 사라져 "기준선이 없다" 는 거짓 경고가 뜬다
        List<Observation> observations = observationRepository.findByStudentIdOrderByPeriodEndDesc(studentId);

        ReportHeader header = header(student, from, until, anon);
        TogetherTime together = togetherTime(from, until, attendance, heldSessions,
                enrollmentRepository.findByStudentIdOrderByStartedOnDesc(studentId));
        WhatWeDid whatWeDid = whatWeDid(logs);
        WhatChanged whatChanged = whatChanged(observations, from, until);
        InterventionGroups interventions = interventions(
                interventionRepository.findByStudentIdOrderByTriedOnDesc(studentId), from, until);
        WeakSpots weakSpots = weakSpots(tagSums,
                assessmentRepository.findByStudentIdOrderByAssessedOnDesc(studentId), from, until);
        List<SceneItem> scenes = scenes(
                sceneRepository.findByStudentIdOrderByOccurredOnDesc(studentId), from, until);
        Promises promises = promises(
                consultationRepository.findByStudentIdOrderByConsultedOnDesc(studentId), from, until);

        String plainText = plainText(header, together, whatWeDid, whatChanged,
                interventions, weakSpots, scenes, promises);

        return new ReportView(header, together, whatWeDid, whatChanged,
                interventions, weakSpots, scenes, promises, plainText);
    }

    /* =================================================================
       머리말
       ================================================================= */

    private ReportHeader header(Student student, LocalDate from, LocalDate to, boolean anon) {
        // 반출 모드에서는 실명·학교·퇴원사유를 아예 담지 않는다.
        // 화면에서만 감추면 인쇄 · 텍스트 복사 · 나중에 붙는 화면 중 하나만 빠뜨려도 실명이 나간다
        return new ReportHeader(
                anon ? student.getCode() : student.getName(),
                student.getCode(),
                anon ? null : student.getSchool(),
                student.getGrade(),
                student.getStatus() == null ? null : student.getStatus().getLabel(),
                student.getEnrolledOn(),
                student.getLeftOn(),
                anon ? null : student.getLeaveReason(),
                from, to, anon, LocalDate.now());
    }

    /* =================================================================
       1. 함께한 시간
       ================================================================= */

    private TogetherTime togetherTime(LocalDate from, LocalDate to,
                                      Map<AttendanceStatus, Long> counts,
                                      long heldSessions,
                                      List<Enrollment> enrollments) {
        List<TogetherTime.StatusCount> rows = new ArrayList<>();
        long recorded = 0;
        for (AttendanceStatus status : AttendanceStatus.values()) {
            long n = counts.getOrDefault(status, 0L);
            recorded += n;
            // 0건인 상태도 남긴다. "결석 0회" 는 리포트에서 가장 하고 싶은 말일 수 있다
            rows.add(new TogetherTime.StatusCount(status.name(), status.getLabel(), n));
        }

        long attended = counts.getOrDefault(AttendanceStatus.PRESENT, 0L)
                + counts.getOrDefault(AttendanceStatus.LATE, 0L)
                + counts.getOrDefault(AttendanceStatus.MAKEUP, 0L);
        // 휴원은 분모에서 뺀다. 결석과 섞으면 쉬었던 달 때문에 출석률이 부당하게 깎인다
        long attendable = attended + counts.getOrDefault(AttendanceStatus.ABSENT, 0L);
        // 출결을 한 건도 표시하지 않았으면 출석률은 0% 가 아니라 "셀 수 없음" 이다.
        // 0 으로 나누지도 않고, 0% 라고 거짓말하지도 않는다
        Integer rate = attendable == 0 ? null : (int) Math.round(attended * 100.0 / attendable);

        List<String> classNames = enrollments.stream()
                .filter(e -> overlaps(e, from, to))
                .map(e -> e.getClassGroup() == null ? null : e.getClassGroup().getName())
                .filter(Objects::nonNull)
                .distinct()
                .toList();

        return new TogetherTime(from, to, spanText(from, to),
                heldSessions, recorded, Math.max(0, heldSessions - recorded),
                rows, attended, attendable, rate, classNames);
    }

    /** 소속 기간이 조회 기간과 겹치는가. 겹치지 않는 반은 이 리포트가 다루는 반이 아니다 */
    private boolean overlaps(Enrollment e, LocalDate from, LocalDate to) {
        if (e.getStartedOn() != null && e.getStartedOn().isAfter(to)) {
            return false;
        }
        return e.getEndedOn() == null || !e.getEndedOn().isBefore(from);
    }

    private String spanText(LocalDate from, LocalDate to) {
        long months = ChronoUnit.MONTHS.between(from, to);
        if (months <= 0) {
            // 하루짜리 기간도 "0개월" 이 아니라 "1일" 로 읽혀야 한다
            return (ChronoUnit.DAYS.between(from, to) + 1) + "일";
        }
        long years = months / 12;
        long rest = months % 12;
        if (years == 0) {
            return months + "개월";
        }
        return rest == 0 ? years + "년" : years + "년 " + rest + "개월";
    }

    /* =================================================================
       2. 무엇을 했나
       ================================================================= */

    private WhatWeDid whatWeDid(List<ProgressLog> logsAsc) {
        Map<HomeworkStatus, Long> homework = new EnumMap<>(HomeworkStatus.class);
        List<WhatWeDid.NoteItem> notes = new ArrayList<>();
        long recorded = 0;
        for (ProgressLog log : logsAsc) {
            if (log.getHomeworkStatus() != null) {
                homework.merge(log.getHomeworkStatus(), 1L, Long::sum);
                recorded++;
            }
            String note = text(log.getNote());
            if (note != null) {
                notes.add(new WhatWeDid.NoteItem(sessionDate(log), note));
            }
        }

        List<WhatWeDid.HomeworkCount> homeworkRows = new ArrayList<>();
        for (HomeworkStatus status : HomeworkStatus.values()) {
            long n = homework.getOrDefault(status, 0L);
            int percent = recorded == 0 ? 0 : (int) Math.round(n * 100.0 / recorded);
            homeworkRows.add(new WhatWeDid.HomeworkCount(status.name(), status.getLabel(), n, percent));
        }

        return new WhatWeDid(logsAsc.size(), compress(logsAsc), homeworkRows,
                recorded, logsAsc.size() - recorded, notes);
    }

    /**
     * 진도 구간 압축. <b>교재가 바뀌는 지점에서만 끊는다.</b>
     *
     * 회차를 전부 나열하면 3년 다닌 학생은 300줄이 되고 아무도 읽지 않는다.
     * 같은 교재가 나중에 다시 나오면 새 구간이다(A → B → A 는 세 구간).
     * 하나로 합치면 중간에 다른 교재를 했다는 사실이 사라진다.
     *
     * 교재·범위·내용이 모두 빈 기록은 구간을 만들지 않는다. 학생 메모만 남긴 회차이기 때문이다.
     */
    private List<WhatWeDid.ProgressSpan> compress(List<ProgressLog> logsAsc) {
        List<Span> spans = new ArrayList<>();
        Span current = null;
        for (ProgressLog log : logsAsc) {
            String textbook = text(log.getTextbook());
            String range = text(log.getRangeText());
            if (textbook == null && range == null && text(log.getContent()) == null) {
                continue;
            }
            if (current == null || !Objects.equals(current.textbook, textbook)) {
                current = new Span(textbook);
                spans.add(current);
            }
            current.add(sessionDate(log), range);
        }
        return spans.stream().map(Span::toRecord).toList();
    }

    /** 압축 중인 구간. 결과 레코드는 불변이라 쌓는 동안만 쓰는 가변 상자다 */
    private static final class Span {

        private final String textbook;
        private LocalDate from;
        private LocalDate to;
        private String firstRange;
        private String lastRange;
        private int sessions;

        private Span(String textbook) {
            this.textbook = textbook;
        }

        /** 날짜 오름차순으로 들어온다는 전제다. 그래서 처음 것이 시작, 마지막 것이 끝이다 */
        private void add(LocalDate on, String range) {
            if (on != null) {
                if (from == null) {
                    from = on;
                }
                to = on;
            }
            if (range != null) {
                if (firstRange == null) {
                    firstRange = range;
                }
                lastRange = range;
            }
            sessions++;
        }

        private WhatWeDid.ProgressSpan toRecord() {
            String rangeText;
            if (firstRange == null) {
                rangeText = null;
            } else if (lastRange == null || lastRange.equals(firstRange)) {
                rangeText = firstRange;
            } else {
                rangeText = firstRange + " → " + lastRange;
            }
            return new WhatWeDid.ProgressSpan(textbook, from, to, rangeText, sessions);
        }
    }

    /* =================================================================
       3. 무엇이 달라졌나
       ================================================================= */

    private WhatChanged whatChanged(List<Observation> all, LocalDate from, LocalDate to) {
        List<Observation> dated = all.stream().filter(o -> o.getPeriodEnd() != null).toList();

        // 기준선은 가장 이른 BASELINE 하나다. 실수로 두 번 만들었어도 before 는 가장 이른 것이어야 한다.
        // 최신 것을 잡으면 성장 폭이 줄어든다
        Observation baseline = dated.stream()
                .filter(Observation::isBaseline)
                .min(Comparator.comparing(Observation::getPeriodEnd))
                .orElse(null);
        List<Observation> inPeriod = dated.stream()
                .filter(o -> inPeriod(o.getPeriodEnd(), from, to))
                .toList();
        Observation latest = inPeriod.stream()
                .max(Comparator.comparing(Observation::getPeriodEnd))
                .orElse(null);

        // 같은 객체인지로 비교한다. 저장 전 객체는 id 가 null 이라 id 로 비교하면 여기서 터진다
        boolean onlyOne = latest != null && latest == baseline;
        Observation after = onlyOne ? null : latest;

        WhatChanged.ScoreDelta understanding = delta("이해도",
                baseline == null ? null : baseline.getUnderstanding(),
                after == null ? null : after.getUnderstanding());
        WhatChanged.ScoreDelta homeworkRate = delta("과제 수행도",
                baseline == null ? null : baseline.getHomeworkRate(),
                after == null ? null : after.getHomeworkRate());

        String warning = null;
        String hint = null;
        if (baseline == null) {
            // 이 리포트에서 가장 중요한 경고다. 가장 이른 정기 관찰을 기준선인 척 쓰지 않는다.
            // 이미 몇 달 자란 상태가 before 가 되면 성장 폭이 실제보다 작게 나온다
            warning = "입회 기준선이 없어 변화를 증명할 수 없습니다.";
            hint = "관찰기록을 남길 때 종류를 '입회 기준선' 으로 고르면 이 칸이 채워집니다. "
                    + "남아 있는 관찰 중 가장 이른 것으로 대신하지 않습니다. "
                    + "입회 시점이 아니어서 성장 폭이 실제보다 작게 나오기 때문입니다.";
        } else if (latest == null) {
            warning = "조회 기간 안에 관찰기록이 없어 변화를 낼 수 없습니다.";
            hint = "기간을 넓히거나, 관찰기록을 한 건 남기면 기준선과 비교됩니다.";
        } else if (onlyOne) {
            warning = "관찰이 입회 기준선 한 건뿐입니다. 비교할 두 번째 기록이 없습니다.";
            hint = "정기 관찰을 한 번 더 남기면 이해도·과제 수행도의 변화가 화살표로 계산됩니다.";
        } else if (!understanding.comparable() && !homeworkRate.comparable()) {
            warning = "기준선과 최신 관찰에 점수가 비어 있어 변화를 숫자로 낼 수 없습니다.";
            hint = "관찰기록의 이해도·과제 수행도 칩을 눌러 두면 다음 리포트에 화살표가 나옵니다.";
        }

        // 기준선이 없을 때만 "가장 이른 관찰" 을 참고로 붙인다. 기준선이 아니라는 이름을 달아 내보낸다
        Observation earliest = baseline != null ? null
                : dated.stream().min(Comparator.comparing(Observation::getPeriodEnd)).orElse(null);

        return new WhatChanged(inPeriod.size(), point(baseline), point(earliest), point(latest),
                understanding, homeworkRate,
                baseline == null,
                baseline != null && baseline.getPeriodEnd().isBefore(from),
                onlyOne, warning, hint);
    }

    private WhatChanged.ObservationPoint point(Observation o) {
        if (o == null) {
            return null;
        }
        return new WhatChanged.ObservationPoint(
                o.getKind() == null ? null : o.getKind().getLabel(),
                o.getPeriodStart(), o.getPeriodEnd(),
                o.getUnderstanding(), o.getHomeworkRate(),
                text(o.getStrength()), text(o.getWeakness()), text(o.getAttitudeNote()),
                text(o.getSelfAssessment()), text(o.getTeacherComment()),
                author(o.getCreatedBy()));
    }

    /**
     * 점수 변화 한 줄.
     *
     * 한쪽이 미확인(null)이면 비교 불가다. 0 변화로 그리면
     * "확인하지 않았다" 가 "변하지 않았다" 로 바뀌어 없는 사실을 주장하게 된다.
     */
    private WhatChanged.ScoreDelta delta(String label, Integer before, Integer after) {
        if (before == null || after == null) {
            String text = before == null && after == null
                    ? "양쪽 모두 점수가 없어 비교할 수 없습니다"
                    : "한쪽 점수가 없어 비교할 수 없습니다";
            return new WhatChanged.ScoreDelta(label, before, after, false, 0, "—", text);
        }
        int diff = after - before;
        String arrow = diff > 0 ? "↑" : diff < 0 ? "↓" : "→";
        String text = before + " → " + after
                + (diff == 0 ? " (변화 없음)" : " (" + arrow + Math.abs(diff) + ")");
        return new WhatChanged.ScoreDelta(label, before, after, true, diff, arrow, text);
    }

    /* =================================================================
       4. 통한 것 / 안 통한 것
       ================================================================= */

    private InterventionGroups interventions(List<Intervention> all, LocalDate from, LocalDate to) {
        List<Intervention> inPeriod = all.stream()
                .filter(i -> inPeriod(i.getTriedOn(), from, to))
                .sorted(Comparator.comparing(Intervention::getTriedOn))
                .toList();

        List<InterventionGroups.Group> groups = List.of(
                group("WORKED", "통한 것", "근거와 함께 리포트에 그대로 쓸 수 있는 항목",
                        inPeriod, InterventionResult.WORKED),
                group("PARTIAL", "일부 통한 것", "조건이 맞을 때만 통했다. 그 조건이 다음 사람에게 넘길 정보다",
                        inPeriod, InterventionResult.PARTIAL),
                group("FAILED", "안 통한 것", "숨기지 않는다. 무엇을 그만두었는지가 이 리포트의 신뢰다",
                        inPeriod, InterventionResult.FAILED),
                group("ONGOING", "진행중", "결과를 확인했고 아직 지켜보는 중이다",
                        inPeriod, InterventionResult.ONGOING),
                // 미확인을 진행중과 합치지 않는다. 합치면 "결과를 적어야 리포트에 나간다" 는 안내가 사라진다
                group("UNREVIEWED", "결과 미확인", "결과를 적지 않으면 리포트에 한 줄도 못 나간다",
                        inPeriod, null));

        long evidenceMissing = inPeriod.stream()
                .filter(i -> i.getResult() != null && text(i.getResultNote()) == null)
                .count();

        StringBuilder countLine = new StringBuilder();
        for (InterventionGroups.Group group : groups) {
            if (countLine.length() > 0) {
                countLine.append(" · ");
            }
            countLine.append(group.label()).append(' ').append(group.count());
        }

        return new InterventionGroups(inPeriod.size(), evidenceMissing, groups, countLine.toString());
    }

    private InterventionGroups.Group group(String key, String label, String meaning,
                                           List<Intervention> inPeriod, InterventionResult result) {
        List<InterventionGroups.Item> items = inPeriod.stream()
                .filter(i -> i.getResult() == result)
                .map(this::item)
                .toList();
        return new InterventionGroups.Group(key, label, meaning, items);
    }

    private InterventionGroups.Item item(Intervention i) {
        // 시도한 날과 결과를 안 날의 간격이 "며칠 지켜봤나" 라는 근거가 된다.
        // 확인일이 시도일보다 앞서면(잘못 입력) 계산하지 않는다. "-14일 지켜봄" 은 리포트에 나갈 문장이 아니다
        boolean bothDates = i.getTriedOn() != null && i.getReviewedOn() != null;
        Long watched = bothDates && !i.getReviewedOn().isBefore(i.getTriedOn())
                ? ChronoUnit.DAYS.between(i.getTriedOn(), i.getReviewedOn())
                : null;
        return new InterventionGroups.Item(
                i.getTriedOn(), i.getReviewedOn(), text(i.getProblem()), text(i.getAction()),
                i.getResult() == null ? "결과 미확인" : i.getResult().getLabel(),
                text(i.getResultNote()),
                i.getResult() != null && text(i.getResultNote()) == null,
                watched, author(i.getCreatedBy()));
    }

    /* =================================================================
       5. 약한 지점과 처방
       ================================================================= */

    private WeakSpots weakSpots(Map<ErrorTagCode, Long> sums, List<Assessment> all,
                                LocalDate from, LocalDate to) {
        long total = 0;
        long max = 0;
        for (Long n : sums.values()) {
            long v = n == null ? 0 : n;
            total += v;
            max = Math.max(max, v);
        }

        // 키만 꺼내 정렬한다. EnumMap 의 entrySet 은 순회하는 동안 Entry 객체 하나를 돌려쓰기 때문에
        // new ArrayList<>(sums.entrySet()) 로 담으면 전부 마지막 항목이 되어 순서가 엉킨다
        List<ErrorTagCode> sorted = new ArrayList<>(sums.keySet());
        // 개수 많은 순. 같으면 enum 선언 순서로 고정한다. 열 때마다 순서가 바뀌면 리포트를 못 믿는다
        sorted.sort(Comparator.comparingLong((ErrorTagCode code) -> -sums.get(code))
                .thenComparing(ErrorTagCode::ordinal));

        List<WeakSpots.TagBar> tags = new ArrayList<>();
        List<String> topLabels = new ArrayList<>();
        for (int i = 0; i < sorted.size(); i++) {
            ErrorTagCode code = sorted.get(i);
            long count = sums.get(code) == null ? 0 : sums.get(code);
            if (count <= 0) {
                continue;
            }
            int share = total == 0 ? 0 : (int) Math.round(count * 100.0 / total);
            // 막대는 최다 태그를 100 으로 잡는다. 전체 비율로 그리면 태그가 일곱 개로 흩어졌을 때
            // 막대가 전부 짧아져 차이가 보이지 않는다. 1개짜리도 보이도록 최소 폭을 준다
            int width = max == 0 ? 0 : Math.max(6, (int) Math.round(count * 100.0 / max));
            boolean top = i < 2;
            tags.add(new WeakSpots.TagBar(code.name(), code.getLabel(), count, share, width, top));
            if (top) {
                topLabels.add(code.getLabel());
            }
        }

        List<Assessment> inPeriod = all.stream()
                .filter(a -> inPeriod(a.getAssessedOn(), from, to))
                .sorted(Comparator.comparing(Assessment::getAssessedOn))
                .toList();

        List<WeakSpots.BlankAxes> blanks = inPeriod.stream()
                .filter(a -> a.getType() == AssessmentType.BLANK && hasAnyAxis(a))
                .map(a -> new WeakSpots.BlankAxes(a.getAssessedOn(), text(a.getUnitName()),
                        a.getBlankTerm(), a.getBlankCondition(), a.getBlankException(), a.getBlankCausality(),
                        text(a.getStudentQuote())))
                .toList();

        List<WeakSpots.WeakUnit> weakUnits = inPeriod.stream()
                .filter(a -> a.getUnitStatus() == UnitStatus.WEAK)
                .map(a -> new WeakSpots.WeakUnit(a.getAssessedOn(), text(a.getUnitName()),
                        a.getUnitStatus().getLabel(), a.getScoreText(), a.getPercent(), text(a.getNote())))
                .toList();

        List<WeakSpots.Quote> quotes = inPeriod.stream()
                .filter(a -> text(a.getStudentQuote()) != null)
                .map(a -> new WeakSpots.Quote(a.getAssessedOn(), text(a.getUnitName()),
                        text(a.getStudentQuote())))
                .toList();

        return new WeakSpots(total, tags, topLabels, blanks, weakUnits, quotes, inPeriod.size());
    }

    private boolean hasAnyAxis(Assessment a) {
        return a.getBlankTerm() != null || a.getBlankCondition() != null
                || a.getBlankException() != null || a.getBlankCausality() != null;
    }

    /* =================================================================
       6. 장면 / 7. 약속
       ================================================================= */

    /** 장면은 요약하지 않는다. 적힌 문장을 시간순으로 그대로 낸다 */
    private List<SceneItem> scenes(List<Scene> all, LocalDate from, LocalDate to) {
        return all.stream()
                .filter(s -> inPeriod(s.getOccurredOn(), from, to))
                .sorted(Comparator.comparing(Scene::getOccurredOn))
                .map(s -> new SceneItem(s.getOccurredOn(), text(s.getSituation()), text(s.getContent()),
                        author(s.getCreatedBy())))
                .toList();
    }

    private Promises promises(List<Consultation> all, LocalDate from, LocalDate to) {
        List<Consultation> inPeriod = all.stream()
                .filter(c -> inPeriod(c.getConsultedOn(), from, to))
                .sorted(Comparator.comparing(Consultation::getConsultedOn))
                .toList();
        List<Promises.Item> items = inPeriod.stream()
                .filter(c -> text(c.getPromiseNote()) != null)
                .map(c -> new Promises.Item(c.getConsultedOn(),
                        c.getType() == null ? null : c.getType().getLabel(),
                        text(c.getPromiseNote()), text(c.getWorryNote()), author(c.getCreatedBy())))
                .toList();
        return new Promises(inPeriod.size(), inPeriod.size() - items.size(), items);
    }

    /* =================================================================
       평문 초안
       ================================================================= */

    /**
     * 문서 편집기로 옮길 평문.
     *
     * <b>인자로 받은 섹션에서만 만든다.</b> 엔티티를 다시 읽지 않는다.
     * 그래야 반출 모드에서 감춘 값이 복사한 텍스트로 새어 나가는 일이 구조적으로 불가능해진다.
     */
    private String plainText(ReportHeader header, TogetherTime together, WhatWeDid did,
                             WhatChanged changed, InterventionGroups interventions,
                             WeakSpots weak, List<SceneItem> scenes, Promises promises) {
        StringBuilder sb = new StringBuilder();

        sb.append("[퇴원생 리포트 초안]\n");
        sb.append(header.displayName());
        if (StringUtils.hasText(header.school())) {
            sb.append(" · ").append(header.school());
        }
        if (StringUtils.hasText(header.grade())) {
            sb.append(" · ").append(header.grade());
        }
        sb.append('\n');
        sb.append("식별번호 ").append(header.code());
        if (StringUtils.hasText(header.statusLabel())) {
            sb.append(" · ").append(header.statusLabel());
        }
        sb.append('\n');
        sb.append("기간 ").append(d(header.from())).append(" ~ ").append(d(header.to()))
                .append(" (").append(together.spanText()).append(")\n");
        sb.append("작성 ").append(d(header.generatedOn())).append(" · ").append(header.modeLabel()).append('\n');
        // 반출 모드의 단서를 화면과 같은 문장으로 여기에도 남긴다.
        // 실제로 외부로 나가는 물건은 '텍스트 복사' 버튼이 만든 이 평문인데,
        // 여기에 modeLabel("실명과 학교를 감췄습니다")만 있으면 안전하다는 단정만 나간다
        if (header.modeCaution() != null) {
            sb.append(header.modeCaution()).append('\n');
        }
        sb.append("이 문서는 초안입니다. 기록된 사실만 배치했습니다. 사람이 읽고 다듬어 내보내세요.\n");

        head(sb, "1. 함께한 시간");
        if (!together.classNames().isEmpty()) {
            sb.append("- 반: ").append(String.join(", ", together.classNames())).append('\n');
        }
        sb.append("- 회차: 반이 열었던 ").append(together.heldSessions()).append("회 중 출결을 표시한 ")
                .append(together.recordedSessions()).append("회");
        if (together.unrecordedSessions() > 0) {
            sb.append(" (표시하지 않은 ").append(together.unrecordedSessions()).append("회는 셈에서 빠졌습니다)");
        }
        sb.append('\n');
        StringBuilder statusLine = new StringBuilder();
        for (TogetherTime.StatusCount c : together.counts()) {
            if (statusLine.length() > 0) {
                statusLine.append(" · ");
            }
            statusLine.append(c.label()).append(' ').append(c.count()).append("회");
        }
        sb.append("- 출결: ").append(statusLine).append('\n');
        if (together.rateUnknown()) {
            sb.append("- 출석률: 셀 수 없음 (출결을 표시한 회차가 없습니다)\n");
        } else {
            sb.append("- 출석률: ").append(together.attendanceRate()).append("% (출석+지각+보강 ")
                    .append(together.attended()).append(" / ").append(together.attendable())
                    .append("회, 휴원은 셈에서 뺐습니다)\n");
        }

        head(sb, "2. 무엇을 했나");
        if (did.spans().isEmpty()) {
            sb.append("- (기록 없음) 회차마다 진도기록을 남기면 교재별 진도 흐름이 여기 채워집니다.\n");
        } else {
            for (WhatWeDid.ProgressSpan span : did.spans()) {
                sb.append("- ").append(span.textbook() == null ? "교재 미기재" : span.textbook());
                if (span.rangeText() != null) {
                    sb.append(' ').append(span.rangeText());
                }
                sb.append(" · ").append(d(span.from())).append(" ~ ").append(d(span.to()))
                        .append(" · ").append(span.sessions()).append("회\n");
            }
        }
        StringBuilder homeworkLine = new StringBuilder();
        for (WhatWeDid.HomeworkCount c : did.homework()) {
            if (homeworkLine.length() > 0) {
                homeworkLine.append(" · ");
            }
            homeworkLine.append(c.label()).append(' ').append(c.count()).append("회");
        }
        sb.append("- 과제: ").append(did.homeworkRecorded() == 0
                ? "기록 없음 (진도기록에서 지난 과제 상태를 표시하면 채워집니다)"
                : homeworkLine.toString()).append('\n');
        if (did.homeworkUnrecorded() > 0) {
            sb.append("  과제 상태를 적지 않은 회차 ").append(did.homeworkUnrecorded()).append("회\n");
        }
        for (WhatWeDid.NoteItem note : did.notes()) {
            sb.append("- 메모 ").append(d(note.on())).append(": ").append(block(note.note())).append('\n');
        }

        head(sb, "3. 무엇이 달라졌나");
        if (changed.warning() != null) {
            sb.append("! ").append(changed.warning()).append('\n');
            sb.append("  ").append(changed.hint()).append('\n');
        }
        if (changed.baseline() != null) {
            sb.append("- 입회 기준선 ").append(d(changed.baseline().periodEnd())).append('\n');
            appendPoint(sb, changed.baseline());
        }
        if (changed.earliest() != null) {
            sb.append("- 가장 이른 관찰 (기준선 아님) ").append(d(changed.earliest().periodEnd()))
                    .append(" · ").append(nz(changed.earliest().kindLabel())).append('\n');
            appendPoint(sb, changed.earliest());
        }
        if (changed.latest() != null && !changed.onlyOneObservation()) {
            sb.append("- 최신 관찰 ").append(d(changed.latest().periodEnd())).append('\n');
            appendPoint(sb, changed.latest());
        }
        sb.append("- 이해도: ").append(changed.understanding().text()).append('\n');
        sb.append("- 과제 수행도: ").append(changed.homeworkRate().text()).append('\n');

        head(sb, "4. 통한 것 / 안 통한 것");
        sb.append("- ").append(interventions.countLine()).append('\n');
        if (interventions.total() == 0) {
            sb.append("- (기록 없음) 무엇을 해결하려 무엇을 시도했는지 개입 로그로 남기면 이 칸이 채워집니다.\n");
        }
        for (InterventionGroups.Group group : interventions.groups()) {
            if (group.empty()) {
                continue;
            }
            sb.append("\n[").append(group.label()).append(' ').append(group.count()).append("건]\n");
            for (InterventionGroups.Item item : group.items()) {
                sb.append("- ").append(d(item.triedOn())).append(" 문제: ").append(nz(item.problem())).append('\n');
                sb.append("  시도: ").append(block(nz(item.action()))).append('\n');
                sb.append("  결과: ").append(item.resultLabel());
                if (item.reviewedOn() != null) {
                    sb.append(" (").append(d(item.reviewedOn())).append(" 확인");
                    if (item.watchedDays() != null) {
                        sb.append(", ").append(item.watchedDays()).append("일 지켜봄");
                    }
                    sb.append(')');
                }
                sb.append('\n');
                sb.append("  근거: ").append(item.resultNote() == null
                        ? "없음 — 근거를 적지 않으면 리포트에 쓸 수 없습니다" : block(item.resultNote())).append('\n');
            }
        }

        head(sb, "5. 약한 지점과 처방");
        if (weak.tags().isEmpty()) {
            sb.append("- (기록 없음) 평가에 오답 유형 태그를 붙이면 무엇이 약한지가 숫자로 나옵니다.\n");
        } else {
            if (!weak.topLabels().isEmpty()) {
                sb.append("- 가장 많은 유형: ").append(String.join(", ", weak.topLabels())).append('\n');
            }
            for (WeakSpots.TagBar tag : weak.tags()) {
                sb.append("- ").append(tag.label()).append(' ').append(tag.count())
                        .append("개 (").append(tag.share()).append("%)\n");
            }
            sb.append("- 오답 합계 ").append(weak.totalErrors()).append("개 / 평가 ")
                    .append(weak.assessmentCount()).append("건\n");
        }
        for (WeakSpots.BlankAxes axes : weak.blanks()) {
            sb.append("- 백지 ").append(d(axes.assessedOn())).append(' ').append(nz(axes.unitName()))
                    .append(" · 용어 ").append(nz(axes.term())).append(" / 조건 ").append(nz(axes.condition()))
                    .append(" / 예외 ").append(nz(axes.exception())).append(" / 인과 ").append(nz(axes.causality()))
                    .append('\n');
        }
        for (WeakSpots.WeakUnit unit : weak.weakUnits()) {
            sb.append("- 약한 단원 ").append(d(unit.assessedOn())).append(' ').append(nz(unit.unitName()));
            if (unit.scoreText() != null) {
                sb.append(" · ").append(unit.scoreText());
            }
            sb.append('\n');
        }
        for (WeakSpots.Quote quote : weak.quotes()) {
            sb.append("- 아이가 쓴 문장 ").append(d(quote.assessedOn())).append(": \"")
                    .append(quote.quote()).append("\"\n");
        }

        head(sb, "6. 장면");
        if (scenes.isEmpty()) {
            sb.append("- (기록 없음) 수업 중 한 장면을 적어 두면 편지에 쓸 재료가 생깁니다.\n");
        }
        for (SceneItem scene : scenes) {
            sb.append("- ").append(d(scene.occurredOn()));
            if (scene.situation() != null) {
                sb.append(" (").append(scene.situation()).append(')');
            }
            sb.append('\n').append("  ").append(block(nz(scene.content()))).append('\n');
        }

        head(sb, "7. 상담에서 한 약속");
        if (promises.items().isEmpty()) {
            sb.append("- (기록 없음) 상담일지의 '약속' 칸을 적으면 무엇을 하기로 했는지가 남습니다.\n");
        }
        for (Promises.Item item : promises.items()) {
            sb.append("- ").append(d(item.consultedOn()));
            if (item.typeLabel() != null) {
                sb.append(" (").append(item.typeLabel()).append(')');
            }
            sb.append(": ").append(block(item.promiseNote())).append('\n');
        }
        if (promises.withoutPromise() > 0) {
            sb.append("  약속을 적지 않은 상담 ").append(promises.withoutPromise()).append("건\n");
        }

        return sb.toString();
    }

    private void appendPoint(StringBuilder sb, WhatChanged.ObservationPoint p) {
        sb.append("  이해도 ").append(nz(p.understanding()))
                .append(" / 과제 수행도 ").append(nz(p.homeworkRate())).append('\n');
        if (p.strength() != null) {
            sb.append("  잘하는 점: ").append(block(p.strength())).append('\n');
        }
        if (p.weakness() != null) {
            sb.append("  보완할 점: ").append(block(p.weakness())).append('\n');
        }
        if (p.selfAssessment() != null) {
            sb.append("  아이 말: \"").append(block(p.selfAssessment())).append("\"\n");
        }
    }

    private void head(StringBuilder sb, String title) {
        sb.append("\n\n").append(title).append('\n');
    }

    /* =================================================================
       공용
       ================================================================= */

    private Map<AttendanceStatus, Long> attendanceCounts(Long studentId, LocalDate from, LocalDate to) {
        Map<AttendanceStatus, Long> counts = new EnumMap<>(AttendanceStatus.class);
        for (Object[] row : attendanceRepository.countByStatusForStudent(studentId, from, to)) {
            counts.put((AttendanceStatus) row[0], ((Number) row[1]).longValue());
        }
        return counts;
    }

    private Map<ErrorTagCode, Long> tagSums(Long studentId, LocalDate from, LocalDate to) {
        Map<ErrorTagCode, Long> sums = new EnumMap<>(ErrorTagCode.class);
        for (Object[] row : assessmentErrorTagRepository.sumTagsByStudent(studentId, from, to)) {
            sums.put((ErrorTagCode) row[0], ((Number) row[1]).longValue());
        }
        return sums;
    }

    private boolean inPeriod(LocalDate date, LocalDate from, LocalDate to) {
        return date != null && !date.isBefore(from) && !date.isAfter(to);
    }

    private LocalDate sessionDate(ProgressLog log) {
        return log.getSession() == null ? null : log.getSession().getSessionDate();
    }

    private String author(Teacher teacher) {
        return teacher == null ? null : teacher.getName();
    }

    /** 빈 문자열을 null 로 바꾼다. 화면의 ?: '—' 와 평문의 '없음' 이 같은 판단을 쓰도록 여기서 한 번만 정리한다 */
    private String text(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }

    private String d(LocalDate date) {
        return date == null ? "—" : date.toString();
    }

    /**
     * 여러 줄 입력을 들여쓰기에 맞춘다.
     *
     * 장면·근거·상담 약속은 줄바꿈을 넣어 적는 칸이다. 그대로 이어 붙이면
     * 둘째 줄부터 왼쪽 끝에 붙어 어느 항목에 딸린 문장인지 알 수 없게 된다.
     */
    private String block(String value) {
        return value == null ? null : value.replace("\n", "\n  ");
    }

    private String nz(Object value) {
        return value == null ? "—" : value.toString();
    }
}
