package io.poby_house.controller;

import io.poby_house.dto.SetupForm;
import io.poby_house.service.TeacherService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
public class AuthController {

    private final TeacherService teacherService;

    @GetMapping("/login")
    public String login(Model model) {
        if (teacherService.noAccountYet()) {
            return "redirect:/setup";
        }
        return "login";
    }

    /** 계정이 하나도 없을 때만 열리는 최초 설정 화면 */
    @GetMapping("/setup")
    public String setupForm(Model model) {
        if (!teacherService.noAccountYet()) {
            return "redirect:/login";
        }
        model.addAttribute("form", new SetupForm());
        return "setup";
    }

    @PostMapping("/setup")
    public String setup(@Valid @ModelAttribute("form") SetupForm form,
                        BindingResult bindingResult,
                        RedirectAttributes redirect) {
        if (!teacherService.noAccountYet()) {
            return "redirect:/login";
        }
        if (!form.isPasswordMatched()) {
            bindingResult.rejectValue("passwordConfirm", "mismatch", "비밀번호가 서로 다릅니다");
        }
        if (bindingResult.hasErrors()) {
            return "setup";
        }
        teacherService.createFirstAdmin(form);
        redirect.addFlashAttribute("message", "계정을 만들었습니다. 로그인해 주세요.");
        return "redirect:/login";
    }
}
