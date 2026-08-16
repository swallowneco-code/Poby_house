package io.poby_house.controller;

import io.poby_house.domain.Assessment;
import io.poby_house.domain.AssessmentType;
import io.poby_house.domain.ErrorTagCode;
import io.poby_house.domain.Student;
import io.poby_house.domain.UnitStatus;
import io.poby_house.dto.AssessmentForm;
import io.poby_house.security.TeacherPrincipal;
import io.poby_house.service.AssessmentService;
import io.poby_house.service.StudentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Map;

/**
 * 평가와 오답 유형 태그.
 *
 * 클래스에 @RequestMapping 을 두지 않는다. 목록·작성은 학생 밑(/students/{id}/assessments),
 * 상세·수정은 평가 자체의 주소(/assessments/{aid}) 이기 때문이다.
 * 상세를 학생 밑에 두면 주소에 학생 id 와 평가 id 가 둘 다 들어가고,
 * 둘이 어긋난 주소(다른 학생의 평가 id)를 매번 검사해야 한다.
 */
@Controller
@RequiredArgsConstructor
public class AssessmentController {

    private final AssessmentService assessmentService;
    private final StudentService studentService;

    @GetMapping("/students/{id}/assessments")
    public String list(@PathVariable Long id, Model model) {
        List<Assessment> assessments = assessmentService.history(id);
        Map<ErrorTagCode, Long> distribution = assessmentService.tagDistribution(assessments);

        model.addAttribute("student", studentService.get(id));
        model.addAttribute("assessments", assessments);
        model.addAttribute("tagDist", distribution);
        // 막대 길이의 기준. 가장 많은 유형을 100% 로 잡아 서로 견주게 한다
        model.addAttribute("tagMax", distribution.values().stream().mapToLong(Long::longValue).max().orElse(0L));
        model.addAttribute("tagTotal", distribution.values().stream().mapToLong(Long::longValue).sum());
        model.addAttribute("latestBlank", assessmentService.latestBlank(assessments).orElse(null));
        if (!assessments.isEmpty()) {
            // 이력은 최근 순이다. 마지막 것이 첫 평가다
            model.addAttribute("periodFrom", assessments.get(assessments.size() - 1).getAssessedOn());
            model.addAttribute("periodTo", assessments.get(0).getAssessedOn());
        }
        return "assessment/list";
    }

    @GetMapping("/students/{id}/assessments/new")
    public String newForm(@PathVariable Long id, Model model) {
        model.addAttribute("form", new AssessmentForm());
        prepareForm(model, studentService.get(id), null);
        return "assessment/form";
    }

    @PostMapping("/students/{id}/assessments")
    public String create(@PathVariable Long id,
                         @AuthenticationPrincipal TeacherPrincipal principal,
                         @Valid @ModelAttribute("form") AssessmentForm form,
                         BindingResult bindingResult,
                         Model model,
                         RedirectAttributes redirect) {
        if (bindingResult.hasErrors()) {
            prepareForm(model, studentService.get(id), null);
            return "assessment/form";
        }
        Assessment saved = assessmentService.create(id, form, principal.getId());
        redirect.addFlashAttribute("message", saved.getType().getLabel() + " 평가를 기록했습니다.");
        return "redirect:/assessments/" + saved.getId();
    }

    @GetMapping("/assessments/{aid}")
    public String detail(@PathVariable Long aid, Model model) {
        Assessment assessment = assessmentService.get(aid);
        model.addAttribute("assessment", assessment);
        model.addAttribute("student", assessment.getStudent());
        return "assessment/detail";
    }

    @GetMapping("/assessments/{aid}/edit")
    public String editForm(@PathVariable Long aid, Model model) {
        Assessment assessment = assessmentService.get(aid);
        model.addAttribute("form", AssessmentForm.from(assessment));
        prepareForm(model, assessment.getStudent(), assessment);
        return "assessment/form";
    }

    @PostMapping("/assessments/{aid}")
    public String update(@PathVariable Long aid,
                         @Valid @ModelAttribute("form") AssessmentForm form,
                         BindingResult bindingResult,
                         Model model,
                         RedirectAttributes redirect) {
        if (bindingResult.hasErrors()) {
            Assessment assessment = assessmentService.get(aid);
            prepareForm(model, assessment.getStudent(), assessment);
            return "assessment/form";
        }
        assessmentService.update(aid, form);
        redirect.addFlashAttribute("message", "평가 기록을 고쳤습니다.");
        return "redirect:/assessments/" + aid;
    }

    /**
     * 폼이 늘 필요한 값. 검증 오류로 폼을 되돌릴 때도 같은 값을 다시 넣어야 하므로 한곳에 모은다.
     * assessment 가 null 이면 새로 적는 것이다.
     */
    private void prepareForm(Model model, Student student, Assessment assessment) {
        model.addAttribute("student", student);
        model.addAttribute("assessment", assessment);
        model.addAttribute("editing", assessment != null);
        model.addAttribute("types", AssessmentType.values());
        model.addAttribute("unitStatuses", UnitStatus.values());
    }
}
