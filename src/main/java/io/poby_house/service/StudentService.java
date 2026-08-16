package io.poby_house.service;

import io.poby_house.domain.Assessment;
import io.poby_house.domain.Enrollment;
import io.poby_house.domain.Intervention;
import io.poby_house.domain.ProgressLog;
import io.poby_house.domain.Student;
import io.poby_house.domain.StudentStatus;
import io.poby_house.dto.LeaveForm;
import io.poby_house.dto.PurgeForm;
import io.poby_house.dto.StudentForm;
import io.poby_house.repository.AssessmentRepository;
import io.poby_house.repository.ConsultationRepository;
import io.poby_house.repository.EnrollmentRepository;
import io.poby_house.repository.InterventionRepository;
import io.poby_house.repository.ObservationRepository;
import io.poby_house.repository.ProgressLogRepository;
import io.poby_house.repository.SceneRepository;
import io.poby_house.repository.StudentRepository;
import io.poby_house.repository.TeacherRepository;
import io.poby_house.support.BusinessRuleException;
import io.poby_house.support.NotFoundException;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StudentService {

    /**
     * 한 페이지에 50명.
     * 폰에서 보는 목록이라 더 늘리면 스크롤이 끝나지 않고, 더 줄이면 페이지 이동이 잦아진다.
     */
    public static final int PAGE_SIZE = 50;

    private final StudentRepository studentRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final TeacherRepository teacherRepository;
    private final ObservationRepository observationRepository;
    private final SceneRepository sceneRepository;
    private final ConsultationRepository consultationRepository;
    /**
     * 평가·개입·진도기록은 파기 때만 쓴다.
     * 이 셋은 행을 지우지 않고 문장이 적힌 칸만 비우므로, 세 종류와 다른 처리를 한다.
     */
    private final AssessmentRepository assessmentRepository;
    private final InterventionRepository interventionRepository;
    private final ProgressLogRepository progressLogRepository;

    /**
     * 상태와 검색어는 함께 걸린다. 검색한다고 상태 필터가 풀리지 않는다.
     * 페이지 번호도 함께 받는다. 화면이 이동할 때 status·keyword 를 다시 실어 보내야
     * 2페이지에서 필터가 풀리지 않는다.
     */
    public Page<Student> search(StudentStatus status, String keyword, int page) {
        // 주소창의 page 는 사람이 손으로 고칠 수 있다. 음수를 그대로 넘기면 PageRequest 가 예외를 던진다
        int safePage = Math.max(page, 0);
        return studentRepository.search(
                status,
                StringUtils.hasText(keyword) ? keyword.trim() : null,
                PageRequest.of(safePage, PAGE_SIZE));
    }

    public Student get(Long id) {
        return studentRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("학생을 찾을 수 없습니다. id=" + id));
    }

    /** 이 학생이 거쳐 온 반. 최근 배정이 위로 */
    public List<Enrollment> enrollmentHistory(Long studentId) {
        return enrollmentRepository.findByStudentIdOrderByStartedOnDesc(studentId);
    }

    /** 지금 소속된 반. 없으면 null. 퇴원 확인 화면이 "어느 반에서 빠지는가" 를 알려 주려고 쓴다 */
    public Enrollment currentEnrollment(Long studentId) {
        return enrollmentRepository.findByStudentIdAndEndedOnIsNull(studentId).orElse(null);
    }

    @Transactional
    public Student create(StudentForm form, Long teacherId) {
        Student student = new Student();
        student.assignCode(nextCode(form.getEnrolledOn()));
        apply(student, form);
        touch(student, teacherId);
        return studentRepository.save(student);
    }

    @Transactional
    public Student update(Long id, StudentForm form, Long teacherId) {
        Student student = get(id);
        apply(student, form);
        touch(student, teacherId);
        return student;
    }

    /**
     * 퇴원. 반 배정도 같은 날짜로 함께 끝낸다.
     *
     * 이 두 번째 일이 빠져 있어서 퇴원생이 반 목록에 그대로 남아 있었다.
     * Enrollment.endedOn 이 null 인 동안은 "현재 소속중" 이라,
     * 반 인원과 배정 후보 계산이 전부 그 유령을 세고 있었다.
     */
    @Transactional
    public Student leave(Long id, LeaveForm form, Long teacherId) {
        Student student = get(id);
        LocalDate leftOn = form.getLeftOn() == null ? LocalDate.now() : form.getLeftOn();
        student.markLeft(leftOn, blankToNull(form.getLeaveReason()));

        enrollmentRepository.findByStudentIdAndEndedOnIsNull(id).ifPresent(enrollment -> {
            // 배정 시작보다 앞선 퇴원일을 그대로 찍으면 끝난 날이 시작보다 이른 구간이 생긴다.
            // 함께한 기간을 세는 리포트가 음수 일수를 받는다
            LocalDate endedOn = leftOn.isBefore(enrollment.getStartedOn())
                    ? enrollment.getStartedOn()
                    : leftOn;
            enrollment.setEndedOn(endedOn);
        });

        touch(student, teacherId);
        return student;
    }

    /**
     * 휴원. 반 배정은 유지한다.
     *
     * 휴원은 돌아올 자리를 비워 두는 상태다. 배정을 끊으면 복귀할 때
     * 어느 반이었는지를 사람이 기억해서 다시 넣어야 하고, 그 사이 반 인원이 실제와 어긋난다.
     * 그래서 퇴원과 달리 Enrollment 를 건드리지 않는다.
     */
    @Transactional
    public Student pause(Long id, Long teacherId) {
        Student student = get(id);
        student.pause();
        touch(student, teacherId);
        return student;
    }

    /**
     * 재원 복귀. 퇴원 취소도 이 길로 온다.
     *
     * 퇴원 때 끊은 반 배정은 되살리지 않는다. 어느 반으로 돌아갈지는 사람이 정할 일이고,
     * 근거 없이 지난 배정을 이어 붙이면 함께한 기간이 실제보다 늘어난다.
     * 그래서 화면이 "반 배정을 다시 해 주세요" 를 알린다.
     */
    @Transactional
    public Student resume(Long id, Long teacherId) {
        Student student = get(id);
        student.resume();
        touch(student, teacherId);
        return student;
    }

    /** 보관 만료일이 지난 퇴원생 */
    public List<Student> purgeTargets() {
        return studentRepository.findPurgeTargets(LocalDate.now());
    }

    /**
     * 파기 확인 화면이 "무엇이 함께 지워지는가" 를 항목별로 보여 주려고 쓴다.
     *
     * 목록을 세 번 읽어 세는 것은 파기가 학생 한 명 단위로 아주 드물게 도는 화면이기 때문이다.
     * 이것 하나 때문에 다른 기능의 리포지토리에 count 쿼리를 새로 심지 않는다.
     */
    public TextRecordCounts textRecordCounts(Long studentId) {
        return new TextRecordCounts(
                observationRepository.findByStudentIdOrderByPeriodEndDesc(studentId).size(),
                sceneRepository.findByStudentIdOrderByOccurredOnDesc(studentId).size(),
                consultationRepository.findByStudentIdOrderByConsultedOnDesc(studentId).size(),
                // 평가·개입·진도는 문장이 적힌 것만 센다.
                // 전체 건수를 보여 주면 점수만 있는 평가까지 "비워진다" 고 읽힌다
                (int) assessmentRepository.findByStudentIdOrderByAssessedOnDesc(studentId).stream()
                        .filter(Assessment::hasFreeText).count(),
                (int) interventionRepository.findByStudentIdOrderByTriedOnDesc(studentId).stream()
                        .filter(Intervention::hasFreeText).count(),
                (int) progressLogRepository.findByStudentIdOrderBySessionSessionDateDesc(studentId).stream()
                        .filter(log -> log.getNote() != null && !log.getNote().isBlank()).count());
    }

    /**
     * 보관 만료 파기. 학생 행을 지우지 않고 익명화한다.
     * 왜 지우지 않는지는 Student.anonymize 주석에 적어 두었다.
     *
     * 이름 확인을 서버에서 다시 한다. 화면의 JS 는 오조작을 줄이는 장치일 뿐이고,
     * 뒤로 가서 다시 제출하는 것도, 주소를 직접 두드리는 것도 그 JS 를 지나지 않는다.
     */
    @Transactional
    public Student anonymize(Long id, PurgeForm form, Long teacherId) {
        Student student = get(id);

        if (student.getStatus() != StudentStatus.LEFT) {
            throw new BusinessRuleException("퇴원 처리된 학생만 파기할 수 있습니다.");
        }
        if (student.getPurgeAfter() == null || student.getPurgeAfter().isAfter(LocalDate.now())) {
            throw new BusinessRuleException("보관 기간이 아직 남아 있습니다. 보관 만료일 이후에만 파기할 수 있습니다.");
        }
        if (!student.getName().equals(trimmed(form.getConfirmName()))) {
            throw new BusinessRuleException("입력한 이름이 이 학생의 이름과 다릅니다. 다시 확인해 주세요.");
        }

        if (form.isPurgeTexts()) {
            // 자유 텍스트에는 실명이 섞여 들어간다. 익명화는 학생 칸만 지우므로 문장 속 이름이 남는다.
            //
            // 처리를 두 갈래로 나눈다.
            // 관찰·장면·상담은 자유 텍스트가 기록의 본체라서 비우면 껍데기만 남는다. 그래서 행째로 지운다.
            // 평가·개입·진도는 점수·결과 코드·출석한 회차가 반 단위 통계와 지난 리포트의 근거이므로
            // 행을 지우면 이미 내보낸 문서와 지금 화면이 어긋난다. 그래서 문장이 적힌 칸만 비운다.
            //
            // 어느 쪽이든 빠뜨리면 안 된다. 원장은 실명 제거를 목적으로 이 체크박스를 켜고,
            // 화면이 "처리했다" 고 알린 뒤에는 purgeAfter 가 비어 파기 목록에 다시 오르지 않는다.
            observationRepository.deleteAll(observationRepository.findByStudentIdOrderByPeriodEndDesc(id));
            sceneRepository.deleteAll(sceneRepository.findByStudentIdOrderByOccurredOnDesc(id));
            consultationRepository.deleteAll(consultationRepository.findByStudentIdOrderByConsultedOnDesc(id));

            for (Assessment assessment : assessmentRepository.findByStudentIdOrderByAssessedOnDesc(id)) {
                assessment.scrubText();
            }
            for (Intervention intervention : interventionRepository.findByStudentIdOrderByTriedOnDesc(id)) {
                intervention.scrubText();
            }
            for (ProgressLog log : progressLogRepository.findByStudentIdOrderBySessionSessionDateDesc(id)) {
                log.scrubText();
            }
        }

        student.anonymize();
        touch(student, teacherId);
        return student;
    }

    private void apply(Student student, StudentForm form) {
        student.setName(form.getName().trim());
        student.setSchool(blankToNull(form.getSchool()));
        student.setGrade(blankToNull(form.getGrade()));
        student.setEnrolledOn(form.getEnrolledOn());
        student.setMemo(blankToNull(form.getMemo()));
    }

    /**
     * 마지막 수정자를 남긴다.
     *
     * 없는 강사 id 면 조용히 비워 둔다. 여기서 예외를 던지면
     * 누가 고쳤는지 남기려다가 학생 저장 자체가 막힌다. 그쪽이 더 큰 손해다.
     */
    private void touch(Student student, Long teacherId) {
        if (teacherId == null) {
            return;
        }
        teacherRepository.findById(teacherId).ifPresent(student::setUpdatedBy);
    }

    /**
     * 빈 입력칸은 "" 로 들어온다. 그대로 두면 상세 화면의 ?: '—' 를 통과해
     * 값이 없다는 표시 대신 빈칸이 보인다.
     */
    private String blankToNull(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }

    private String trimmed(String value) {
        return value == null ? "" : value.trim();
    }

    /**
     * S + 등록연도 + 3자리 일련번호. 예: S2026001
     * 같은 해에 이미 발급된 마지막 번호 다음을 쓴다.
     *
     * 번호는 숫자로 뽑는다. 예전처럼 code 를 문자열로 정렬해 마지막 것을 파싱하면
     * 두 가지가 깨졌다.
     *   - 999 를 넘기면 S2026999 가 S20261000 보다 크다고 나와 번호가 멈춘다
     *   - code 에 형식에 맞지 않는 값이 하나라도 섞이면 parseInt 가 터지면서
     *     학생 등록 자체가 전면 불능이 된다
     */
    private String nextCode(LocalDate enrolledOn) {
        int year = (enrolledOn == null ? LocalDate.now() : enrolledOn).getYear();
        String prefix = "S" + year;
        Integer max = studentRepository.maxCodeSequence(prefix, prefix.length() + 1);
        return prefix + String.format("%03d", (max == null ? 0 : max) + 1);
    }

    /**
     * 파기 확인 화면에 뿌리는 개수. 자유 텍스트가 들어 있는 기록만 센다.
     * record 로 두지 않는 이유: 화면(Thymeleaf)이 getXxx 규약으로 값을 읽는다.
     */
    @Getter
    public static class TextRecordCounts {

        /* 행째로 지워지는 기록 */
        private final int observations;
        private final int scenes;
        private final int consultations;

        /* 행은 남기고 문장이 적힌 칸만 비우는 기록 */
        private final int assessments;
        private final int interventions;
        private final int progressNotes;

        public TextRecordCounts(int observations, int scenes, int consultations,
                                int assessments, int interventions, int progressNotes) {
            this.observations = observations;
            this.scenes = scenes;
            this.consultations = consultations;
            this.assessments = assessments;
            this.interventions = interventions;
            this.progressNotes = progressNotes;
        }

        /** 행째로 지워지는 건수 */
        public int getDeletedRows() {
            return observations + scenes + consultations;
        }

        /**
         * 문장 칸만 비워지는 건수.
         * 삭제 건수와 합쳐 한 숫자로 보여 주지 않는다. 지우는 것과 비우는 것은 되돌릴 수 없는 정도가 다르고,
         * 원장이 판단해야 하는 것은 "무엇이 사라지는가" 다.
         */
        public int getScrubbedRows() {
            return assessments + interventions + progressNotes;
        }
    }
}
