package io.poby_house.controller;

import io.poby_house.domain.Student;
import io.poby_house.dto.PurgeForm;
import io.poby_house.security.TeacherPrincipal;
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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * 보관 만료 파기. 원장 전용이다(/admin/** 는 SecurityConfig 가 ADMIN 으로 막아 둔다).
 *
 * 이 화면이 있는 이유: 퇴원 + 3년이 지난 개인 정보를 계속 들고 있으면 방침 위반이다.
 * 그런데 "지운다" 를 학생 행 삭제로 구현하면 지난 회차 기록과 반 통계까지 함께 사라진다.
 * 그래서 지우는 대상은 개인을 특정하는 정보뿐이고, 기록의 뼈대는 남긴다.
 */
@Controller
@RequestMapping("/admin/purge")
@RequiredArgsConstructor
public class PurgeController {

    private final StudentService studentService;

    @GetMapping
    public String list(Model model) {
        model.addAttribute("targets", studentService.purgeTargets());
        return "admin/purge/list";
    }

    @GetMapping("/{id}")
    public String confirm(@PathVariable Long id, Model model) {
        model.addAttribute("student", studentService.get(id));
        model.addAttribute("counts", studentService.textRecordCounts(id));
        model.addAttribute("form", new PurgeForm());
        return "admin/purge/confirm";
    }

    @PostMapping("/{id}")
    public String purge(@PathVariable Long id,
                        @AuthenticationPrincipal TeacherPrincipal principal,
                        @Valid @ModelAttribute("form") PurgeForm form,
                        BindingResult bindingResult,
                        Model model,
                        RedirectAttributes redirect) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("student", studentService.get(id));
            model.addAttribute("counts", studentService.textRecordCounts(id));
            return "admin/purge/confirm";
        }
        // 이름이 틀리면 서비스가 BusinessRuleException 을 던지고,
        // GlobalExceptionHandler 가 이 화면으로 되돌려 이유만 알려 준다
        Student purged = studentService.anonymize(id, form, principal.getId());
        // 안내에 이름을 쓰지 않는다. 방금 지운 이름을 flash 로 다시 띄우는 것은 앞뒤가 안 맞는다
        redirect.addFlashAttribute("message",
                purged.getCode() + " 학생의 개인 정보를 지웠습니다. 출결·진도·평가 기록은 그대로 남습니다.");
        return "redirect:/admin/purge";
    }
}
