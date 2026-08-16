package io.poby_house.controller;

import io.poby_house.domain.Assessment;
import io.poby_house.domain.Consultation;
import io.poby_house.domain.Intervention;
import io.poby_house.domain.Observation;
import io.poby_house.domain.ProgressLog;
import io.poby_house.domain.Scene;
import io.poby_house.domain.Student;
import io.poby_house.domain.StudentStatus;
import io.poby_house.dto.LeaveForm;
import io.poby_house.dto.StudentForm;
import io.poby_house.repository.ProgressLogRepository;
import io.poby_house.security.TeacherPrincipal;
import io.poby_house.service.AssessmentService;
import io.poby_house.service.ConsultationService;
import io.poby_house.service.InterventionService;
import io.poby_house.service.ObservationService;
import io.poby_house.service.SceneService;
import io.poby_house.service.StudentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

@Controller
@RequestMapping("/students")
@RequiredArgsConstructor
public class StudentController {

    private final StudentService studentService;
    private final ObservationService observationService;
    private final SceneService sceneService;
    private final InterventionService interventionService;
    private final AssessmentService assessmentService;
    private final ConsultationService consultationService;
    /**
     * 진도만 리포지토리를 직접 쓴다. 다른 다섯 기록에는 전용 서비스가 있지만
     * 진도는 회차 화면이 반 단위로 써 넣는 기록이라 학생 단위 서비스가 아직 없다.
     * 생기면 그쪽으로 옮긴다.
     */
    private final ProgressLogRepository progressLogRepository;

    @GetMapping
    public String list(@RequestParam(required = false) StudentStatus status,
                       @RequestParam(required = false) String keyword,
                       @RequestParam(defaultValue = "0") int page,
                       Model model) {
        // 화면에 넘기기 전에 검색어를 서비스와 같은 규칙으로 정규화한다.
        // 빈 문자열을 그대로 넘기면 화면의 "아무 조건도 안 걸렸다" 판정(status == null and keyword == null)이
        // 거짓이 되어, 학생이 한 명도 없는데 '조건에 맞는 학생이 없습니다' 가 뜨고 등록 버튼이 사라진다.
        // 상태 칩 링크에 빈 keyword 파라미터가 붙는 것도 함께 사라진다
        String query = StringUtils.hasText(keyword) ? keyword.trim() : null;
        Page<Student> result = studentService.search(status, query, page);
        // 목록은 List 로 넘긴다. 화면의 th:each 와 #lists.isEmpty 가 그대로 쓰인다.
        // 총원은 목록 크기가 아니라 pageInfo.totalElements 다. 한 페이지만 올라오기 때문이다
        model.addAttribute("students", result.getContent());
        model.addAttribute("pageInfo", result);
        model.addAttribute("status", status);
        model.addAttribute("keyword", query);
        model.addAttribute("statuses", StudentStatus.values());
        model.addAttribute("primaryActionHref", "/students/new");
        model.addAttribute("primaryActionLabel", "+ 학생 등록");
        return "student/list";
    }

    @GetMapping("/new")
    public String newForm(Model model) {
        model.addAttribute("form", new StudentForm());
        model.addAttribute("editing", false);
        return "student/form";
    }

    @PostMapping
    public String create(@AuthenticationPrincipal TeacherPrincipal principal,
                         @Valid @ModelAttribute("form") StudentForm form,
                         BindingResult bindingResult,
                         Model model,
                         RedirectAttributes redirect) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("editing", false);
            return "student/form";
        }
        Student saved = studentService.create(form, principal.getId());
        redirect.addFlashAttribute("message", saved.getName() + " 학생을 등록했습니다. ("
                + saved.getCode() + ") 입회 기준선을 남겨 주세요. 지금 안 남기면 나중에 변화를 증명할 수 없습니다.");
        // 등록 직후가 기준선을 남길 수 있는 유일한 시점이다.
        // 상세 화면으로 보내면 "나중에" 가 되고, 그 나중은 오지 않는다.
        // 그러면 퇴원 리포트의 before 칸이 영구히 빈다
        return "redirect:/students/" + saved.getId() + "/observations/new?kind=BASELINE";
    }

    /**
     * 학생 한 명의 기록이 모두 모이는 화면. 여섯 종류의 기록 목록을 각각 한 번씩 읽는다.
     *
     * 개수만 필요하면 count 쿼리가 싸지만, 이 화면은 개수와 함께 최근 몇 건을 그리고
     * 섹션마다 "전체 보기" 를 낼지도 개수로 정한다. count 를 따로 두면 같은 것을 두 번 읽는다.
     * 대신 반복문 안에서 조회하는 곳이 없어야 한다. 여섯 조회 모두 @EntityGraph 로
     * 화면이 쓰는 연관(작성자·회차·반·오답 태그)까지 한 번에 가져온다.
     */
    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        model.addAttribute("student", studentService.get(id));
        model.addAttribute("enrollments", studentService.enrollmentHistory(id));
        // 진도·출결은 학생 화면에서 적을 수 없다. 지금 속한 반의 회차 화면으로 보내려고 함께 읽는다
        model.addAttribute("currentEnrollment", studentService.currentEnrollment(id));

        List<ProgressLog> progressLogs = progressLogRepository.findByStudentIdOrderBySessionSessionDateDesc(id);
        List<Observation> observations = observationService.history(id);
        List<Scene> scenes = sceneService.history(id);
        // history() 가 아니라 historyOpenFirst() 다. 결과를 안 적은 시도가 위로 와야
        // 이 화면에 그리는 두 건이 "적어야 할 것" 이 된다
        List<Intervention> interventions = interventionService.historyOpenFirst(id);
        List<Assessment> assessments = assessmentService.history(id);
        List<Consultation> consultations = consultationService.history(id);

        model.addAttribute("progressLogs", progressLogs);
        model.addAttribute("observations", observations);
        model.addAttribute("scenes", scenes);
        model.addAttribute("interventions", interventions);
        model.addAttribute("assessments", assessments);
        model.addAttribute("consultations", consultations);

        // 기준선이 없으면 리포트의 before 칸이 영구히 빈다. 가장 중요한 누락이라 화면 위에서 알린다
        model.addAttribute("observationBaseline", observationService.baseline(id).orElse(null));
        // 약속은 이미 읽은 상담 목록에서 고른다. recentPromises(id, 3) 를 부르면 같은 쿼리가 두 번 나간다
        model.addAttribute("promises", consultationService.promisesOf(consultations, 3));

        // 마지막 기록일. 목록이 전부 최근 순이라 첫 건이 마지막 기록이다.
        // 개수만 보이면 석 달 전에 멈춘 기록과 어제 적은 기록이 같아 보인다
        model.addAttribute("lastProgressOn",
                progressLogs.isEmpty() ? null : progressLogs.get(0).getSession().getSessionDate());
        model.addAttribute("lastObservationOn",
                observations.isEmpty() ? null : observations.get(0).getPeriodEnd());
        model.addAttribute("lastSceneOn", scenes.isEmpty() ? null : scenes.get(0).getOccurredOn());
        model.addAttribute("lastAssessmentOn",
                assessments.isEmpty() ? null : assessments.get(0).getAssessedOn());
        model.addAttribute("lastConsultationOn",
                consultations.isEmpty() ? null : consultations.get(0).getConsultedOn());
        // 개입만 예외다. 목록이 '결과 미확인 먼저' 로 재정렬돼 있어 첫 건이 가장 최근 시도가 아니다
        model.addAttribute("lastInterventionOn", interventions.stream()
                .map(Intervention::getTriedOn)
                .filter(Objects::nonNull)
                .max(LocalDate::compareTo)
                .orElse(null));

        return "student/detail";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        Student student = studentService.get(id);
        model.addAttribute("form", StudentForm.from(student));
        model.addAttribute("student", student);
        model.addAttribute("editing", true);
        return "student/form";
    }

    @PostMapping("/{id}")
    public String update(@PathVariable Long id,
                         @AuthenticationPrincipal TeacherPrincipal principal,
                         @Valid @ModelAttribute("form") StudentForm form,
                         BindingResult bindingResult,
                         Model model,
                         RedirectAttributes redirect) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("student", studentService.get(id));
            model.addAttribute("editing", true);
            return "student/form";
        }
        studentService.update(id, form, principal.getId());
        redirect.addFlashAttribute("message", "수정했습니다.");
        return "redirect:/students/" + id;
    }

    /**
     * 퇴원 확인 화면.
     *
     * 퇴원은 되돌리기 어렵고(보관 만료일이 세워지고 반 배정이 끊긴다) 그 결과가 눈에 안 보인다.
     * 그래서 수정 폼의 드롭다운에서 꺼내 전용 화면으로 두고, 무엇이 함께 일어나는지 먼저 보여 준다.
     */
    @GetMapping("/{id}/leave")
    public String leaveForm(@PathVariable Long id, Model model) {
        model.addAttribute("student", studentService.get(id));
        model.addAttribute("currentEnrollment", studentService.currentEnrollment(id));
        model.addAttribute("form", new LeaveForm());
        return "student/leave";
    }

    @PostMapping("/{id}/leave")
    public String leave(@PathVariable Long id,
                        @AuthenticationPrincipal TeacherPrincipal principal,
                        @Valid @ModelAttribute("form") LeaveForm form,
                        BindingResult bindingResult,
                        Model model,
                        RedirectAttributes redirect) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("student", studentService.get(id));
            model.addAttribute("currentEnrollment", studentService.currentEnrollment(id));
            return "student/leave";
        }
        Student student = studentService.leave(id, form, principal.getId());
        redirect.addFlashAttribute("message",
                "퇴원 처리했습니다. 반 배정도 " + student.getLeftOn() + " 로 끝냈습니다. 보관 만료는 "
                        + student.getPurgeAfter() + " 입니다.");
        return "redirect:/students/" + id;
    }

    /** 휴원. 확인 화면을 두지 않는다. 반 배정이 그대로 남고 복귀 버튼 한 번으로 되돌아간다 */
    @PostMapping("/{id}/pause")
    public String pause(@PathVariable Long id,
                        @AuthenticationPrincipal TeacherPrincipal principal,
                        RedirectAttributes redirect) {
        studentService.pause(id, principal.getId());
        redirect.addFlashAttribute("message", "휴원으로 바꿨습니다. 반 배정은 그대로 두었습니다. 돌아올 자리입니다.");
        return "redirect:/students/" + id;
    }

    @PostMapping("/{id}/resume")
    public String resume(@PathVariable Long id,
                         @AuthenticationPrincipal TeacherPrincipal principal,
                         RedirectAttributes redirect) {
        // 되돌린 뒤에는 퇴원이었는지 휴원이었는지 알 수 없다. 안내 문구가 달라야 하므로 먼저 본다
        boolean wasLeft = studentService.get(id).getStatus() == StudentStatus.LEFT;
        studentService.resume(id, principal.getId());
        redirect.addFlashAttribute("message", wasLeft
                ? "퇴원을 취소하고 재원으로 되돌렸습니다. 퇴원일·사유·보관 만료일을 지웠습니다. 반 배정은 다시 해 주세요."
                : "재원으로 되돌렸습니다.");
        return "redirect:/students/" + id;
    }
}
