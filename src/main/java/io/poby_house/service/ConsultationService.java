package io.poby_house.service;

import io.poby_house.domain.Consultation;
import io.poby_house.domain.Student;
import io.poby_house.dto.ConsultationForm;
import io.poby_house.repository.ConsultationRepository;
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

/**
 * 학부모 상담일지.
 *
 * 이 서비스가 지키는 것은 하나다: 사실 / 걱정 / 약속 중 최소 한 칸은 채워져야 한다.
 * 그리고 약속만 따로 뽑아 줄 수 있어야 한다. 상담의 신뢰는 "지난번에 하기로 한 것"을
 * 다음 통화에서 먼저 꺼내는 데서 나오고, 그 문장을 찾아 주는 것이 이 화면의 존재 이유다.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ConsultationService {

    private final ConsultationRepository consultationRepository;
    private final StudentRepository studentRepository;
    private final TeacherRepository teacherRepository;

    /** 이 학생의 상담 이력. 최근 상담이 위로 */
    public List<Consultation> history(Long studentId) {
        return consultationRepository.findByStudentIdOrderByConsultedOnDesc(studentId);
    }

    /**
     * 최근 약속만. 약속 칸이 빈 상담은 빼고 낸다.
     * 통화 중에 훑는 목록이므로 전체를 주지 않고 최근 것만 잘라 준다.
     */
    public List<Consultation> recentPromises(Long studentId, int limit) {
        return promisesOf(history(studentId), limit);
    }

    /**
     * 이미 이력을 불러 온 화면(상담일지 한 화면)에서 같은 쿼리를 두 번 돌리지 않도록 열어 둔다.
     * history() 가 consultedOn 내림차순이므로 여기서 다시 정렬하지 않는다.
     */
    public List<Consultation> promisesOf(List<Consultation> history, int limit) {
        if (history == null || limit <= 0) {
            return List.of();
        }
        return history.stream()
                .filter(c -> StringUtils.hasText(c.getPromiseNote()))
                .limit(limit)
                .toList();
    }

    public Consultation get(Long id) {
        return consultationRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("상담 기록을 찾을 수 없습니다. id=" + id));
    }

    @Transactional
    public Consultation create(Long studentId, ConsultationForm form, Long teacherId) {
        requireAnyNote(form);
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new NotFoundException("학생을 찾을 수 없습니다. id=" + studentId));

        Consultation consultation = Consultation.of(student, consultedOn(form), form.getType());
        apply(consultation, form);
        if (teacherId != null) {
            teacherRepository.findById(teacherId).ifPresent(consultation::setCreatedBy);
        }
        return consultationRepository.save(consultation);
    }

    /**
     * createdBy 는 건드리지 않는다. 처음 상담한 사람이 누구였는지가 남아야 하고,
     * 스키마에 updatedBy 가 없으므로 여기서 덮어쓰면 그 정보만 사라진다.
     */
    @Transactional
    public Consultation update(Long id, ConsultationForm form) {
        requireAnyNote(form);
        Consultation consultation = get(id);
        consultation.setConsultedOn(consultedOn(form));
        consultation.setType(form.getType());
        apply(consultation, form);
        return consultation;
    }

    private void apply(Consultation consultation, ConsultationForm form) {
        consultation.setFactNote(blankToNull(form.getFactNote()));
        consultation.setWorryNote(blankToNull(form.getWorryNote()));
        consultation.setPromiseNote(blankToNull(form.getPromiseNote()));
    }

    /**
     * 세 칸이 모두 비면 거절한다.
     * 날짜와 경로만 남은 행은 리포트에서 쓸 수 없는데 이력 목록은 차지한다.
     * 폼에도 같은 검사가 있지만(화면에서 적던 내용을 지키려고), 규칙은 여기가 원본이다.
     */
    private void requireAnyNote(ConsultationForm form) {
        if (StringUtils.hasText(form.getFactNote())
                || StringUtils.hasText(form.getWorryNote())
                || StringUtils.hasText(form.getPromiseNote())) {
            return;
        }
        throw new BusinessRuleException("사실 · 걱정 · 약속 중 최소 한 칸은 적어야 합니다.");
    }

    /** 날짜를 비워 보낸 경우는 오늘 상담한 것으로 본다. 상담 직후에 적는 것이 기본이다 */
    private LocalDate consultedOn(ConsultationForm form) {
        return form.getConsultedOn() == null ? LocalDate.now() : form.getConsultedOn();
    }

    /**
     * 빈 칸은 "" 로 들어온다. 그대로 저장하면 약속 모아보기가 빈 문자열을
     * "약속이 있다" 로 세고, 화면의 ?: '—' 도 통과한다.
     */
    private String blankToNull(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }
}
