package io.poby_house.service;

import io.poby_house.domain.Intervention;
import io.poby_house.domain.InterventionResult;
import io.poby_house.domain.Student;
import io.poby_house.dto.InterventionForm;
import io.poby_house.dto.InterventionReviewForm;
import io.poby_house.repository.InterventionRepository;
import io.poby_house.repository.StudentRepository;
import io.poby_house.repository.TeacherRepository;
import io.poby_house.support.BusinessRuleException;
import io.poby_house.support.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 개입 로그. 시도 -> 결과 두 단계다.
 *
 * 리포트 3-1 "통한 것 / 안 통한 것" 은 전적으로 이 테이블에서 나온다.
 * 결과가 안 적힌 개입은 리포트에 한 줄도 못 나가므로, 이 서비스는 미확인을 계속 위로 올린다.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class InterventionService {

    /** 이 날수를 넘겨 방치된 미확인은 화면에서 더 눈에 띄게 그린다 */
    public static final int STALE_DAYS = 30;

    private final InterventionRepository interventionRepository;
    private final StudentRepository studentRepository;
    private final TeacherRepository teacherRepository;

    /** 학생의 개입 이력. 최근 시도 순. 리포트가 이 순서를 쓴다 */
    public List<Intervention> history(Long studentId) {
        return interventionRepository.findByStudentIdOrderByTriedOnDesc(studentId);
    }

    public Intervention get(Long id) {
        return interventionRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("개입 기록을 찾을 수 없습니다. id=" + id));
    }

    /**
     * 화면용 목록. 결과 미확인이 위로 온다.
     *
     * 미확인끼리는 <b>오래된 것이 위</b>다. 최근 순으로 두면 가장 오래 방치된 시도가
     * 목록 밑으로 밀려나 영원히 안 적힌다. 결과를 적은 것은 그 아래에 최근 순으로 붙는다.
     */
    public List<Intervention> historyOpenFirst(Long studentId) {
        List<Intervention> all = history(studentId);

        List<Intervention> unreviewed = new ArrayList<>();
        List<Intervention> reviewed = new ArrayList<>();
        for (Intervention intervention : all) {
            // ONGOING(진행중)은 확인해 본 결과다. 미확인과 섞으면 추적 목록의 뜻이 사라진다
            if (intervention.getResult() == null) {
                unreviewed.add(intervention);
            } else {
                reviewed.add(intervention);
            }
        }
        // all 이 triedOn 내림차순이므로 뒤집으면 오래된 것이 위로 온다
        Collections.reverse(unreviewed);

        List<Intervention> ordered = new ArrayList<>(unreviewed);
        ordered.addAll(reviewed);
        return ordered;
    }

    /**
     * 결과 미확인 전체. 대시보드가 쓴다. 오래 방치된 것이 위.
     *
     * 리포지토리 조회는 (넘긴 상태 or result is null) 을 함께 뽑는다. ONGOING 을 넘겨
     * 두 종류를 받은 뒤 미확인만 남긴다. result 자리에 null 을 넘겨도 같은 답이 나오지만
     * (SQL 에서 result = null 이 참이 되지 않아서) 왜 되는지 알 수 없는 코드가 된다.
     */
    public List<Intervention> unreviewed() {
        List<Intervention> open = interventionRepository
                .findByResultOrResultIsNullOrderByTriedOnAsc(InterventionResult.ONGOING);
        List<Intervention> result = new ArrayList<>();
        for (Intervention intervention : open) {
            if (intervention.getResult() == null) {
                result.add(intervention);
            }
        }
        return result;
    }

    /**
     * 리포트 3-1 의 재료. 결과별로 묶는다.
     *
     * 미확인(result == null)은 담지 않는다. 통했는지 모르는 시도를 "통한 것" 옆에 두면
     * 리포트가 추측을 사실처럼 적게 된다.
     * EnumMap 이라 WORKED · PARTIAL · FAILED · ONGOING 순서가 리포트에서도 그대로 유지된다.
     */
    public Map<InterventionResult, List<Intervention>> groupedByResult(Long studentId) {
        Map<InterventionResult, List<Intervention>> grouped = new EnumMap<>(InterventionResult.class);
        for (Intervention intervention : history(studentId)) {
            if (intervention.getResult() == null) {
                continue;
            }
            grouped.computeIfAbsent(intervention.getResult(), key -> new ArrayList<>()).add(intervention);
        }
        return grouped;
    }

    /**
     * 미확인인 것만 id -> 결과를 기다린 날수.
     *
     * 화면에서 계산할 수 없어 서비스가 만들어 준다(Thymeleaf 에 날짜 차이를 세는 도구가 없다).
     * 결과를 적은 것은 담지 않는다. 담으면 화면에서 다 지난 일에 경고 배지가 붙는다.
     */
    public Map<Long, Long> waitingDays(List<Intervention> interventions) {
        LocalDate today = LocalDate.now();
        Map<Long, Long> days = new HashMap<>();
        for (Intervention intervention : interventions) {
            if (intervention.getResult() == null && intervention.getTriedOn() != null) {
                days.put(intervention.getId(), ChronoUnit.DAYS.between(intervention.getTriedOn(), today));
            }
        }
        return days;
    }

    @Transactional
    public Intervention create(Long studentId, InterventionForm form, Long teacherId) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new NotFoundException("학생을 찾을 수 없습니다. id=" + studentId));

        // result 는 비워 둔다. 시도한 날에는 통했는지 알 수 없다
        Intervention intervention = Intervention.of(
                student, form.getTriedOn(), form.getProblem().trim(), form.getAction().trim());
        if (teacherId != null) {
            teacherRepository.findById(teacherId).ifPresent(intervention::setCreatedBy);
        }
        return interventionRepository.save(intervention);
    }

    /**
     * 2단계: 결과를 적는다. 확인한 날이 함께 찍힌다(비워 두면 오늘).
     *
     * 근거 없는 "통함" 은 거절한다. 리포트 3-1 에 "이것이 통했습니다" 를 쓸 때
     * 어떻게 알았는지 못 대면 그 줄은 쓸 수 없다. 저장을 막는 편이 낫다.
     * 확인하기 전에 던지는 이유: 못 쓸 기록을 위해 굳이 행을 읽지 않는다.
     */
    @Transactional
    public Intervention review(Long id, InterventionReviewForm form) {
        if (form.getResult() == InterventionResult.WORKED && !StringUtils.hasText(form.getResultNote())) {
            throw new BusinessRuleException(
                    "통했다고 적으려면 근거가 필요합니다. 어떻게 알았는지(무엇이 달라졌는지)를 함께 적어 주세요.");
        }
        Intervention intervention = get(id);
        intervention.review(form.getResult(), blankToNull(form.getResultNote()), form.getReviewedOn());
        return intervention;
    }

    /** 빈 입력칸은 "" 로 들어온다. 그대로 두면 화면의 ?: '—' 를 통과해 빈칸이 보인다 */
    private String blankToNull(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }
}
