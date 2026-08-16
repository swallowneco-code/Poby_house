package io.poby_house.controller;

import io.poby_house.security.TeacherPrincipal;
import io.poby_house.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * 홈. 로그인 직후 착지 화면이다.
 *
 * 예전에는 학생 목록으로 리다이렉트만 했다. 목록은 누가 있는지는 보여 주지만
 * 오늘 무엇이 밀렸는지는 아무도 알려 주지 않았다. 그 자리에 대시보드를 놓는다.
 */
@Controller
@RequiredArgsConstructor
public class HomeController {

    private final DashboardService dashboardService;

    /**
     * @param more 펼칠 카드 이름(sessions / observations / interventions / purge).
     *             카드마다 5줄까지만 보이므로, 나머지를 보는 길을 같은 화면 안에 둔다.
     *             밀린 목록만 보여 주는 별도 화면을 만들면 화면 수만 늘고 볼 이유가 없다.
     */
    @GetMapping("/")
    public String dashboard(@RequestParam(required = false) String more,
                            @AuthenticationPrincipal TeacherPrincipal principal,
                            Model model) {
        model.addAttribute("dashboard", dashboardService.load(isAdmin(principal), more));
        return "home/dashboard";
    }

    /**
     * 원장인가. CurrentUserAdvice 도 isAdmin 을 모델에 넣지만 그것은 화면 감추기용이다.
     * 여기서는 조회할지 말지를 정해야 해서, 모델 속성이 채워지는 순서에 기대지 않고 직접 본다.
     */
    private boolean isAdmin(TeacherPrincipal principal) {
        return principal != null && principal.getAuthorities().stream()
                .anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()));
    }
}
