package io.poby_house.controller;

import io.poby_house.domain.Intervention;
import io.poby_house.domain.InterventionResult;
import io.poby_house.dto.InterventionForm;
import io.poby_house.dto.InterventionReviewForm;
import io.poby_house.security.TeacherPrincipal;
import io.poby_house.service.InterventionService;
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

/**
 * 개입 로그. 시도(등록)와 결과(review)가 다른 화면이다.
 *
 * 등록은 목록과 한 화면에 둔다(3분 기록). 결과는 따로 두는 편이 낫다.
 * 결과를 적는 일은 며칠 뒤에 하는 다른 판단이고, 그때 시도 내용을 다시 읽어야 한다.
 */
@Controller
@RequiredArgsConstructor
public class InterventionController {

    private final InterventionService interventionService;
    private final StudentService studentService;

    @GetMapping("/students/{id}/interventions")
    public String list(@PathVariable Long id, Model model) {
        model.addAttribute("form", new InterventionForm());
        return listView(id, model);
    }

    @PostMapping("/students/{id}/interventions")
    public String create(@PathVariable Long id,
                         @AuthenticationPrincipal TeacherPrincipal principal,
                         @Valid @ModelAttribute("form") InterventionForm form,
                         BindingResult bindingResult,
                         Model model,
                         RedirectAttributes redirect) {
        if (bindingResult.hasErrors()) {
            return listView(id, model);
        }
        interventionService.create(id, form, principal.getId());
        redirect.addFlashAttribute("message", "시도를 기록했습니다. 결과는 며칠 뒤에 확인해서 적으세요.");
        return "redirect:/students/" + id + "/interventions";
    }

    @GetMapping("/interventions/{iid}/review")
    public String reviewForm(@PathVariable Long iid, Model model) {
        Intervention intervention = interventionService.get(iid);
        model.addAttribute("form", InterventionReviewForm.from(intervention));
        return reviewView(intervention, model);
    }

    @PostMapping("/interventions/{iid}/review")
    public String review(@PathVariable Long iid,
                         @Valid @ModelAttribute("form") InterventionReviewForm form,
                         BindingResult bindingResult,
                         Model model,
                         RedirectAttributes redirect) {
        Intervention intervention = interventionService.get(iid);

        // 근거 없는 "통함" 은 서비스가 최종적으로 막는다. 여기서 먼저 걸러 내는 이유는
        // 서비스 예외로 튀면 리다이렉트되면서 방금 적던 글이 사라지기 때문이다.
        if (form.lacksEvidence()) {
            bindingResult.rejectValue("resultNote", "evidence.required",
                    "통했다고 적으려면 근거가 필요합니다. 무엇이 달라졌는지 한 줄이라도 적어 주세요.");
        }
        if (bindingResult.hasErrors()) {
            return reviewView(intervention, model);
        }
        interventionService.review(iid, form);
        redirect.addFlashAttribute("message", "결과를 적었습니다. 리포트에 쓸 수 있는 기록이 되었습니다.");
        return "redirect:/students/" + intervention.getStudent().getId() + "/interventions";
    }

    /** 목록 화면이 필요한 모델. 등록 실패로 되돌아올 때도 같은 것을 채워야 한다 */
    private String listView(Long studentId, Model model) {
        List<Intervention> interventions = interventionService.historyOpenFirst(studentId);
        List<Intervention> open = interventions.stream()
                .filter(intervention -> intervention.getResult() == null)
                .toList();

        model.addAttribute("student", studentService.get(studentId));
        model.addAttribute("interventions", interventions);
        model.addAttribute("waitingDays", interventionService.waitingDays(interventions));
        model.addAttribute("openCount", open.size());
        // 맨 위 안내에서 한 번에 갈 수 있게 가장 오래 방치된 것을 넘긴다.
        // 목록이 미확인-오래된 순으로 시작하므로 첫 번째가 그것이다
        model.addAttribute("oldestOpen", open.isEmpty() ? null : open.get(0));
        model.addAttribute("staleAfter", InterventionService.STALE_DAYS);
        return "intervention/list";
    }

    private String reviewView(Intervention intervention, Model model) {
        model.addAttribute("intervention", intervention);
        model.addAttribute("student", intervention.getStudent());
        model.addAttribute("results", InterventionResult.values());
        return "intervention/review";
    }
}
