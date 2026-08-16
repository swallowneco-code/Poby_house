package io.poby_house.controller;

import io.poby_house.domain.Teacher;
import io.poby_house.dto.PasswordForm;
import io.poby_house.dto.ProfileForm;
import io.poby_house.security.TeacherPrincipal;
import io.poby_house.service.TeacherService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
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

    /** 세션에 보관된 인증 정보를 직접 갈아 끼울 때 쓴다 */
    private final SecurityContextRepository securityContextRepository = new HttpSessionSecurityContextRepository();

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
                                 HttpServletRequest request,
                                 HttpServletResponse response,
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
        refreshAuthentication(principal.getId(), request, response);
        redirect.addFlashAttribute("message", "비밀번호를 변경했습니다.");
        return "redirect:/students";
    }

    /**
     * 세션에 든 로그인 정보를 새 비밀번호 기준으로 다시 만들고 세션 id 를 갈아 끼운다.
     *
     * 이걸 안 하면 세션의 TeacherPrincipal 이 바뀌기 전 해시를 그대로 들고 있다.
     * 지금 당장 보이는 문제는 없지만, 다음 커밋에서 붙일 자동 로그인 토큰이
     * 그 해시로 서명되므로 낡은 값을 들고 있으면 토큰을 다시 발급할 수 없다.
     *
     * 다만 이것만으로 다른 기기에 열려 있는 세션까지 끊기지는 않는다.
     * 그건 세션 레지스트리를 따로 두어야 하는 일이라 여기서는 하지 않는다.
     * (자동 로그인 쿠키는 비밀번호 해시로 서명되므로 비밀번호를 바꾸면 저절로 무효가 된다)
     *
     * 강제 로그아웃 대신 세션을 새로 채우는 쪽을 골랐다.
     * 수업 중 기록하다 비밀번호를 바꾸면 하던 작업이 날아가기 때문이다.
     */
    private void refreshAuthentication(Long teacherId, HttpServletRequest request, HttpServletResponse response) {
        TeacherPrincipal refreshed = new TeacherPrincipal(teacherService.get(teacherId));
        Authentication authentication = UsernamePasswordAuthenticationToken
                .authenticated(refreshed, null, refreshed.getAuthorities());

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);

        request.changeSessionId();
        securityContextRepository.saveContext(context, request, response);
    }
}
