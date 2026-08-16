package io.poby_house.controller;

import io.poby_house.security.TeacherPrincipal;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

/**
 * 모든 화면이 공통 레이아웃을 그리는 데 필요한 값을 넣어 준다.
 * Thymeleaf 보안 방언에 의존하지 않으려고 모델에 직접 넣는다.
 * 여기에 두면 컨트롤러를 하나도 고치지 않고 레이아웃이 값을 쓸 수 있다.
 */
@ControllerAdvice
public class CurrentUserAdvice {

    @ModelAttribute("currentUserName")
    public String currentUserName(@AuthenticationPrincipal TeacherPrincipal principal) {
        return principal == null ? null : principal.getName();
    }

    /** 원장에게만 보여야 하는 메뉴를 감출 때 쓴다. TeacherPrincipal 이 ROLE_ 접두사를 이미 붙여 준다 */
    @ModelAttribute("isAdmin")
    public boolean isAdmin(@AuthenticationPrincipal TeacherPrincipal principal) {
        return principal != null && principal.getAuthorities().stream()
                .anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()));
    }

    /**
     * 상단 메뉴에서 어느 항목을 켤지.
     * Thymeleaf 3.1 에서 #httpServletRequest 가 제거돼 템플릿이 URI 를 직접 읽을 수 없다.
     * 그래서 여기서 넘긴다.
     */
    @ModelAttribute("activeNav")
    public String activeNav(HttpServletRequest request) {
        String uri = request.getRequestURI();
        if (uri.startsWith("/classes")) {
            return "classes";
        }
        if (uri.startsWith("/admin")) {
            return "teachers";
        }
        return "students";
    }
}
