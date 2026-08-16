package io.poby_house.service;

import io.poby_house.domain.Observation;
import io.poby_house.domain.ObservationKind;
import io.poby_house.domain.Student;
import io.poby_house.dto.ObservationForm;
import io.poby_house.repository.ObservationRepository;
import io.poby_house.repository.StudentRepository;
import io.poby_house.repository.TeacherRepository;
import io.poby_house.support.BusinessRuleException;
import io.poby_house.support.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * 관찰기록.
 *
 * 이 서비스가 지키는 것은 하나다: <b>기준선(BASELINE)은 학생마다 하나</b>.
 * 리포트의 before 가 두 개면 "이만큼 달라졌다"의 출발점이 사람 손에 따라 달라진다.
 * 그래서 두 번째 기준선은 만들게 두지 않고 있는 것을 고치도록 되돌린다.
 *
 * baseline/latest/history/hasBaseline 은 리포트와 대시보드가 그대로 쓴다.
 * 이름을 바꾸면 그쪽이 함께 깨진다.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ObservationService {

    private static final String DUPLICATE_BASELINE = "입회 기준선은 학생마다 하나입니다. 새로 만들지 말고 이미 있는 기준선을 수정하세요.";

    private final ObservationRepository observationRepository;
    private final StudentRepository studentRepository;
    private final TeacherRepository teacherRepository;

    public Observation get(Long id) {
        return observationRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("관찰기록을 찾을 수 없습니다. id=" + id));
    }

    /** 관찰 이력. 최근이 위로 */
    public List<Observation> history(Long studentId) {
        return observationRepository.findByStudentIdOrderByPeriodEndDesc(studentId);
    }

    /**
     * 리포트의 before.
     * 실수로 기준선이 둘이 되어도 가장 이른 것을 쓴다. 최신을 잡으면 성장 폭이 줄어든다.
     */
    public Optional<Observation> baseline(Long studentId) {
        return observationRepository
                .findFirstByStudentIdAndKindOrderByPeriodEndAsc(studentId, ObservationKind.BASELINE);
    }

    public boolean hasBaseline(Long studentId) {
        return baseline(studentId).isPresent();
    }

    /** 리포트의 after. 종류를 가리지 않고 가장 최근 관찰이다 */
    public Optional<Observation> latest(Long studentId) {
        return observationRepository.findFirstByStudentIdOrderByPeriodEndDesc(studentId);
    }

    /**
     * 새 관찰 폼의 기본값.
     *
     * 기간을 사람이 매번 고르게 하면 폰에서 날짜 두 개를 굴려야 한다.
     * 지난 관찰이 끝난 다음날부터 오늘까지를 미리 채워 두면 대개 그대로 두고 저장한다.
     *
     * 기준선만 다르다. 기준선은 "들어왔을 때의 모습"을 적는 칸이라
     * 뒤늦게 만들어도 시작이 등록일이어야 한다. 지난 관찰 다음날부터로 채우면
     * 입회 스냅샷인데 기간이 몇 달 뒤부터 시작하는 앞뒤 안 맞는 기록이 남는다.
     */
    public ObservationForm newForm(Long studentId, ObservationKind kind) {
        ObservationKind resolved = kind == null ? ObservationKind.PERIODIC : kind;

        ObservationForm form = new ObservationForm();
        form.setKind(resolved);

        LocalDate start = resolved == ObservationKind.BASELINE
                ? enrolledOn(studentId)
                : lastPeriodEnd(studentId).map(end -> end.plusDays(1)).orElseGet(() -> enrolledOn(studentId));

        // 지난 관찰이 오늘까지 잡혀 있으면 시작이 내일이 되어 기간이 뒤집힌다.
        // 그 상태로 폼을 열면 아무것도 안 했는데 검증 오류가 먼저 뜬다.
        LocalDate end = LocalDate.now();
        if (start != null && start.isAfter(end)) {
            end = start;
        }
        form.setPeriodStart(start);
        form.setPeriodEnd(end);
        return form;
    }

    @Transactional
    public Observation create(Long studentId, ObservationForm form, Long teacherId) {
        ObservationKind kind = form.getKind() == null ? ObservationKind.PERIODIC : form.getKind();
        if (kind == ObservationKind.BASELINE && hasBaseline(studentId)) {
            throw new BusinessRuleException(DUPLICATE_BASELINE);
        }

        Observation observation = Observation.of(
                student(studentId), kind, form.getPeriodStart(), form.getPeriodEnd());
        apply(observation, form);
        if (teacherId != null) {
            teacherRepository.findById(teacherId).ifPresent(observation::setCreatedBy);
        }
        return observationRepository.save(observation);
    }

    /**
     * 수정.
     *
     * 종류도 바꿀 수 있게 둔다. 가장 이른 정기 관찰이 사실은 기준선이었다는 것을
     * 나중에 깨닫는 일이 실제로 있고, 그때 기록을 지우고 다시 적게 하면 작성자와 작성일이 사라진다.
     * 대신 기준선이 이미 따로 있으면 여기서도 막는다.
     */
    @Transactional
    public Observation update(Long id, ObservationForm form) {
        Observation observation = get(id);
        ObservationKind kind = form.getKind() == null ? observation.getKind() : form.getKind();

        if (kind == ObservationKind.BASELINE) {
            baseline(observation.getStudent().getId())
                    .filter(existing -> !existing.getId().equals(observation.getId()))
                    .ifPresent(existing -> {
                        throw new BusinessRuleException(DUPLICATE_BASELINE);
                    });
        }

        observation.setKind(kind);
        apply(observation, form);
        return observation;
    }

    private void apply(Observation observation, ObservationForm form) {
        observation.setPeriodStart(form.getPeriodStart());
        observation.setPeriodEnd(form.getPeriodEnd());
        observation.setUnderstanding(form.getUnderstanding());
        observation.setHomeworkRate(form.getHomeworkRate());
        observation.setAttitudeNote(blankToNull(form.getAttitudeNote()));
        observation.setStrength(blankToNull(form.getStrength()));
        observation.setWeakness(blankToNull(form.getWeakness()));
        observation.setSelfAssessment(blankToNull(form.getSelfAssessment()));
        observation.setTeacherComment(blankToNull(form.getTeacherComment()));
    }

    /**
     * 안 적은 칸은 "" 로 들어온다. 그대로 저장하면 화면이 그 칸을 적힌 것으로 보고
     * 빈 제목만 그리고, 리포트도 내용이 있다고 판단해 빈 줄을 인용한다.
     */
    private String blankToNull(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }

    private Optional<LocalDate> lastPeriodEnd(Long studentId) {
        return latest(studentId).map(Observation::getPeriodEnd);
    }

    private LocalDate enrolledOn(Long studentId) {
        return student(studentId).getEnrolledOn();
    }

    private Student student(Long studentId) {
        return studentRepository.findById(studentId)
                .orElseThrow(() -> new NotFoundException("학생을 찾을 수 없습니다. id=" + studentId));
    }
}
