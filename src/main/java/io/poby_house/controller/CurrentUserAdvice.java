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
     *
     * getRequestURI() 는 context-path 를 포함한다. 배포는 context-path=/poby 로 뜨므로
     * 그대로 비교하면 URI 가 "/poby/classes" 라서 startsWith("/classes") 가 전부 거짓이 되고,
     * 마지막 return 때문에 어느 화면에서든 '학생' 메뉴만 켜졌다. 비교 전에 떼어낸다.
     */
    @ModelAttribute("activeNav")
    public String activeNav(HttpServletRequest request) {
        String uri = request.getRequestURI().substring(request.getContextPath().length());

        // context-path 를 떼면 홈은 "/" 또는 빈 문자열이다. 둘 다 홈이다
        if (uri.isEmpty() || "/".equals(uri)) {
            return "home";
        }
        // 반 아래에 있는 화면이다. 회차 기록을 열면 '반' 이 켜져 있어야 어디서 왔는지가 보인다
        if (uri.startsWith("/classes") || uri.startsWith("/sessions")) {
            return "classes";
        }
        // /admin/purge 를 /admin 보다 먼저 본다. 순서를 바꾸면 파기 화면에서 '계정' 이 켜진다
        if (uri.startsWith("/admin/purge")) {
            return "purge";
        }
        if (uri.startsWith("/admin")) {
            return "teachers";
        }
        return "students";
    }
}
