package io.poby_house.controller;

import io.poby_house.domain.Teacher;
import io.poby_house.dto.PasswordForm;
import io.poby_house.dto.ProfileForm;
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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/account")
@RequiredArgsConstructor
public class AccountController {

    private final TeacherService teacherService;

    @GetMapping
    public String profile(@AuthenticationPrincipal TeacherPrincipal principal, Model model) {
        Teacher me = teacherService.get(principal.getId());
        model.addAttribute("form", ProfileForm.from(me));
        model.addAttribute("me", me);
        return "account/profile";
    }

    @PostMapping
    public String updateProfile(@AuthenticationPrincipal TeacherPrincipal principal,
                                @Valid @ModelAttribute("form") ProfileForm form,
                                BindingResult bindingResult,
                                Model model,
                                RedirectAttributes redirect) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("me", teacherService.get(principal.getId()));
            return "account/profile";
        }
        teacherService.rename(principal.getId(), form.getName());
        redirect.addFlashAttribute("message", "이름을 바꿨습니다. 다음 로그인부터 화면에 반영됩니다.");
        return "redirect:/account";
    }

    @GetMapping("/password")
    public String passwordForm(Model model) {
        model.addAttribute("form", new PasswordForm());
        return "account/password";
    }

    @PostMapping("/password")
    public String changePassword(@AuthenticationPrincipal TeacherPrincipal principal,
                                 @Valid @ModelAttribute("form") PasswordForm form,
                                 BindingResult bindingResult,
                                 RedirectAttributes redirect) {
        if (!teacherService.matchesPassword(principal.getId(), form.getCurrentPassword())) {
            bindingResult.rejectValue("currentPassword", "wrong", "현재 비밀번호가 맞지 않습니다");
        }
        if (!form.isNewPasswordMatched()) {
            bindingResult.rejectValue("newPasswordConfirm", "mismatch", "새 비밀번호가 서로 다릅니다");
        }
        if (bindingResult.hasErrors()) {
            return "account/password";
        }
        teacherService.changePassword(principal.getId(), form.getNewPassword());
        redirect.addFlashAttribute("message", "비밀번호를 변경했습니다.");
        return "redirect:/students";
    }
}
