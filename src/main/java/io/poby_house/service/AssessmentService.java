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
import io.poby_house.support.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 평가와 오답 유형 태그.
 *
 * 태그 분포가 곧 처방이다. 그래서 오답 유형은 자유 텍스트가 아니라 코드로만 저장하고,
 * 분포는 화면에서 세지 않고 여기서 접어 준다. 리포트도 같은 메서드를 쓴다.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AssessmentService {

    private final AssessmentRepository assessmentRepository;
    private final AssessmentErrorTagRepository assessmentErrorTagRepository;
    private final StudentRepository studentRepository;
    private final TeacherRepository teacherRepository;

    /** 학생의 평가 이력. 최근 것이 위. 목록에서 태그 칩을 그리므로 태그까지 함께 온다 */
    public List<Assessment> history(Long studentId) {
        return assessmentRepository.findByStudentIdOrderByAssessedOnDesc(studentId);
    }

    /** 태그까지 함께 읽는다. 수정 폼이 태그 개수를 그대로 다시 보여 줘야 한다 */
    public Assessment get(Long id) {
        return assessmentRepository.findWithTagsById(id)
                .orElseThrow(() -> new NotFoundException("평가 기록을 찾을 수 없습니다. id=" + id));
    }

    /**
     * 기간 안의 오답 유형 분포. 리포트가 쓰는 계약 메서드다.
     *
     * 평가를 전부 끌어와 메모리에서 더하지 않고 group by 한 방으로 센다.
     * count 가 아니라 sum(cnt) 다. count 로 세면 "계산 오답 5개" 가 1로 뭉개진다.
     *
     * @return 개수가 많은 유형이 앞. 0건인 유형은 아예 들어 있지 않다
     */
    public Map<ErrorTagCode, Long> tagDistribution(Long studentId, LocalDate from, LocalDate to) {
        Map<ErrorTagCode, Long> counts = new EnumMap<>(ErrorTagCode.class);
        for (Object[] row : assessmentErrorTagRepository.sumTagsByStudent(studentId, from, to)) {
            counts.put((ErrorTagCode) row[0], (Long) row[1]);
        }
        return byCountDesc(counts);
    }

    /**
     * 전체 기간 분포. 이미 이력을 읽은 화면은 그 목록을 그대로 넘긴다.
     *
     * 이력 조회가 errorTags 를 함께 읽어 오므로 합계 쿼리를 한 번 더 던질 이유가 없다.
     * 기간을 LocalDate.MIN~MAX 로 넘겨 위 메서드를 재사용하지 않는 이유:
     * MySQL DATE 가 담을 수 있는 범위를 벗어나 쿼리가 터진다.
     */
    public Map<ErrorTagCode, Long> tagDistribution(List<Assessment> history) {
        Map<ErrorTagCode, Long> counts = new EnumMap<>(ErrorTagCode.class);
        for (Assessment assessment : history) {
            for (AssessmentErrorTag tag : assessment.getErrorTags()) {
                counts.merge(tag.getTagCode(), (long) tag.getCnt(), Long::sum);
            }
        }
        return byCountDesc(counts);
    }

    /**
     * 4축 추이의 기준이 되는 가장 최근 백지 테스트.
     * 리포트가 before/after 를 잡을 때 이것과 가장 오래된 백지를 견준다.
     */
    public Optional<Assessment> latestBlank(Long studentId) {
        return latestBlank(history(studentId));
    }

    /** 이력을 이미 읽은 화면용. 같은 조회를 두 번 하지 않는다. 이력은 최근 순이라 첫 백지가 최신이다 */
    public Optional<Assessment> latestBlank(List<Assessment> history) {
        return history.stream()
                .filter(a -> a.getType() == AssessmentType.BLANK)
                .findFirst();
    }

    @Transactional
    public Assessment create(Long studentId, AssessmentForm form, Long teacherId) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new NotFoundException("학생을 찾을 수 없습니다. id=" + studentId));

        Assessment assessment = Assessment.of(student, form.getAssessedOn(), form.getType());
        if (teacherId != null) {
            teacherRepository.findById(teacherId).ifPresent(assessment::setCreatedBy);
        }
        apply(assessment, form);
        return assessmentRepository.save(assessment);
    }

    @Transactional
    public Assessment update(Long id, AssessmentForm form) {
        Assessment assessment = get(id);
        assessment.setAssessedOn(form.getAssessedOn());
        assessment.setType(form.getType());
        apply(assessment, form);
        return assessment;
    }

    private void apply(Assessment assessment, AssessmentForm form) {
        assessment.setUnitName(blankToNull(form.getUnitName()));
        assessment.setScore(form.getScore());
        assessment.setMaxScore(form.getMaxScore());
        assessment.setUnitStatus(form.getUnitStatus());
        assessment.setStudentQuote(blankToNull(form.getStudentQuote()));
        assessment.setSourceLocation(blankToNull(form.getSourceLocation()));
        assessment.setNote(blankToNull(form.getNote()));
        applyBlankAxes(assessment, form);
        applyTags(assessment, form);
    }

    /**
     * 4축은 백지 테스트에서만 뜻이 있다. BLANK 가 아니면 지운다.
     *
     * 화면이 JS 로 칸을 감추지만 감춘 입력도 그대로 제출된다.
     * 백지로 적다가 종류를 단원평가로 바꿔 저장하면 4축이 따라붙고,
     * 그러면 4축 추이 그래프에 백지가 아닌 점이 섞인다.
     */
    private void applyBlankAxes(Assessment assessment, AssessmentForm form) {
        boolean blank = assessment.getType() != null && assessment.getType().usesBlankAxes();
        assessment.setBlankTerm(blank ? form.getBlankTerm() : null);
        assessment.setBlankCondition(blank ? form.getBlankCondition() : null);
        assessment.setBlankException(blank ? form.getBlankException() : null);
        assessment.setBlankCausality(blank ? form.getBlankCausality() : null);
    }

    /**
     * 폼이 보내 온 7종 전체를 putTag 에 넘긴다.
     *
     * clearTags 로 비우고 다시 넣지 않는 이유: 같은 트랜잭션 안에서 delete 보다 insert 가
     * 먼저 나가면 (assessmentId, tagCode) 유니크를 위반해 저장이 통째로 실패한다.
     * putTag 는 있는 행의 개수만 바꾸고, 0 이하면 컬렉션에서 빼 orphanRemoval 이 지우게 한다.
     */
    private void applyTags(Assessment assessment, AssessmentForm form) {
        if (form.getTags() == null) {
            return;
        }
        for (ErrorTagInput input : form.getTags()) {
            if (input == null || input.getCode() == null) {
                continue;
            }
            assessment.putTag(input.getCode(), input.getCnt() == null ? 0 : input.getCnt());
        }
    }

    /**
     * 많이 틀린 유형이 앞. 순서 자체가 정보다.
     * 처방은 가장 많이 반복된 유형부터 손대야 하고, 화면과 리포트가 같은 순서를 봐야 한다.
     * 개수가 같으면 enum 선언 순서를 지킨다(같은 데이터로 순서가 흔들리지 않게).
     */
    private Map<ErrorTagCode, Long> byCountDesc(Map<ErrorTagCode, Long> counts) {
        List<Map.Entry<ErrorTagCode, Long>> rows = new ArrayList<>(counts.entrySet());
        rows.sort(Comparator.<Map.Entry<ErrorTagCode, Long>, Long>comparing(Map.Entry::getValue).reversed()
                .thenComparing(e -> e.getKey().ordinal()));

        Map<ErrorTagCode, Long> sorted = new LinkedHashMap<>();
        for (Map.Entry<ErrorTagCode, Long> row : rows) {
            sorted.put(row.getKey(), row.getValue());
        }
        return sorted;
    }

    /** 빈 칸은 "" 로 들어온다. 그대로 두면 상세 화면의 ?: '—' 를 통과해 빈칸이 보인다 */
    private String blankToNull(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }
}
