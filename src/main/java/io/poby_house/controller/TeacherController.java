package io.poby_house.controller;

import io.poby_house.domain.Teacher;
import io.poby_house.domain.TeacherRole;
import io.poby_house.dto.TeacherEditForm;
import io.poby_house.dto.TeacherForm;
import io.poby_house.security.TeacherPrincipal;
import io.poby_house.service.TeacherService;
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
 * 강사 계정 관리. 원장만 들어온다.
 * 접근 제한은 SecurityConfig 의 /admin/** 규칙이 담당하므로 여기에 어노테이션을 더 붙이지 않는다.
 */
@Controller
@RequestMapping("/admin/teachers")
@RequiredArgsConstructor
public class TeacherController {

    private final TeacherService teacherService;

    @GetMapping
    public String list(Model model) {
        model.addAttribute("teachers", teacherService.findAll());
        model.addAttribute("primaryActionHref", "/admin/teachers/new");
        model.addAttribute("primaryActionLabel", "+ 계정 추가");
        return "admin/teacher/list";
    }

    @GetMapping("/new")
    public String newForm(Model model) {
        model.addAttribute("form", new TeacherForm());
        model.addAttribute("roles", TeacherRole.values());
        model.addAttribute("editing", false);
        return "admin/teacher/form";
    }

    @PostMapping
    public String create(@Valid @ModelAttribute("form") TeacherForm form,
                         BindingResult bindingResult,
                         Model model,
                         RedirectAttributes redirect) {
        if (form.getLoginId() != null && teacherService.isLoginIdTaken(form.getLoginId())) {
            bindingResult.rejectValue("loginId", "duplicate", "이미 사용 중인 아이디입니다");
        }
        if (!form.isPasswordMatched()) {
            bindingResult.rejectValue("passwordConfirm", "mismatch", "비밀번호가 서로 다릅니다");
        }
        if (bindingResult.hasErrors()) {
            model.addAttribute("roles", TeacherRole.values());
            model.addAttribute("editing", false);
            return "admin/teacher/form";
        }
        Teacher saved = teacherService.create(form);
        redirect.addFlashAttribute("message", saved.getName() + " 계정을 만들었습니다.");
        return "redirect:/admin/teachers";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        Teacher teacher = teacherService.get(id);
        model.addAttribute("form", TeacherEditForm.from(teacher));
        model.addAttribute("teacher", teacher);
        model.addAttribute("roles", TeacherRole.values());
        model.addAttribute("editing", true);
        return "admin/teacher/form";
    }

    @PostMapping("/{id}")
    public String update(@PathVariable Long id,
                         @Valid @ModelAttribute("form") TeacherEditForm form,
                         BindingResult bindingResult,
                         Model model,
                         @AuthenticationPrincipal TeacherPrincipal principal,
                         RedirectAttributes redirect) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("teacher", teacherService.get(id));
            model.addAttribute("roles", TeacherRole.values());
            model.addAttribute("editing", true);
            return "admin/teacher/form";
        }
        teacherService.update(id, form, principal.getId());
        redirect.addFlashAttribute("message", "수정했습니다.");
        return "redirect:/admin/teachers";
    }
}
