package io.poby_house.controller;

import io.poby_house.domain.Consultation;
import io.poby_house.domain.ConsultationType;
import io.poby_house.dto.ConsultationForm;
import io.poby_house.security.TeacherPrincipal;
import io.poby_house.service.ConsultationService;
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
 * 학부모 상담일지.
 *
 * 클래스 단위 @RequestMapping 을 두지 않는다. 목록·등록은 학생 아래(/students/{id}/consultations),
 * 수정은 기록 자체(/consultations/{cid}) 라 접두어가 하나로 모이지 않는다.
 * 억지로 한쪽에 맞추면 나머지 URL 이 학생 id 를 쓸데없이 끌고 다닌다.
 *
 * 목록과 등록 폼은 한 화면이다. "상담 기록하기" 를 누르러 한 번 더 들어가게 하면
 * 통화 끝나고 폰으로 적는 30초 안에 화면 이동이 끼어든다.
 */
@Controller
@RequiredArgsConstructor
public class ConsultationController {

    /** 통화 중에 훑는 용도이므로 전체가 아니라 최근 것만 낸다 */
    private static final int PROMISE_PREVIEW = 3;

    private final ConsultationService consultationService;
    private final StudentService studentService;

    @GetMapping("/students/{studentId}/consultations")
    public String list(@PathVariable Long studentId, Model model) {
        model.addAttribute("form", new ConsultationForm());
        fillList(studentId, model);
        return "consultation/list";
    }

    @PostMapping("/students/{studentId}/consultations")
    public String create(@PathVariable Long studentId,
                         @AuthenticationPrincipal TeacherPrincipal principal,
                         @Valid @ModelAttribute("form") ConsultationForm form,
                         BindingResult bindingResult,
                         Model model,
                         RedirectAttributes redirect) {
        if (bindingResult.hasErrors()) {
            fillList(studentId, model);
            return "consultation/list";
        }
        consultationService.create(studentId, form, principal == null ? null : principal.getId());
        redirect.addFlashAttribute("message", "상담을 기록했습니다.");
        return "redirect:/students/" + studentId + "/consultations";
    }

    @GetMapping("/consultations/{cid}/edit")
    public String editForm(@PathVariable Long cid, Model model) {
        Consultation consultation = consultationService.get(cid);
        model.addAttribute("form", ConsultationForm.from(consultation));
        fillEdit(consultation, model);
        return "consultation/form";
    }

    @PostMapping("/consultations/{cid}")
    public String update(@PathVariable Long cid,
                         @Valid @ModelAttribute("form") ConsultationForm form,
                         BindingResult bindingResult,
                         Model model,
                         RedirectAttributes redirect) {
        Consultation consultation = consultationService.get(cid);
        if (bindingResult.hasErrors()) {
            fillEdit(consultation, model);
            return "consultation/form";
        }
        consultationService.update(cid, form);
        redirect.addFlashAttribute("message", "상담 기록을 수정했습니다.");
        return "redirect:/students/" + consultation.getStudent().getId() + "/consultations";
    }

    /**
     * 이력을 한 번만 조회하고 약속 미리보기는 그 목록에서 골라낸다.
     * recentPromises(studentId, n) 를 따로 부르면 같은 쿼리가 한 화면에서 두 번 나간다.
     */
    private void fillList(Long studentId, Model model) {
        List<Consultation> history = consultationService.history(studentId);
        model.addAttribute("student", studentService.get(studentId));
        model.addAttribute("consultations", history);
        model.addAttribute("promises", consultationService.promisesOf(history, PROMISE_PREVIEW));
        model.addAttribute("types", ConsultationType.values());
    }

    private void fillEdit(Consultation consultation, Model model) {
        model.addAttribute("consultation", consultation);
        model.addAttribute("student", consultation.getStudent());
        model.addAttribute("types", ConsultationType.values());
    }
}
