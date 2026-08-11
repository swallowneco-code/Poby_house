package io.poby_house.controller;

import io.poby_house.security.TeacherPrincipal;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

/**
 * 모든 화면에서 로그인한 사람 이름을 쓸 수 있게 한다.
 * Thymeleaf 보안 방언에 의존하지 않으려고 모델에 직접 넣는다.
 */
@ControllerAdvice
public class CurrentUserAdvice {

    @ModelAttribute("currentUserName")
    public String currentUserName(@AuthenticationPrincipal TeacherPrincipal principal) {
        return principal == null ? null : principal.getName();
    }
}
