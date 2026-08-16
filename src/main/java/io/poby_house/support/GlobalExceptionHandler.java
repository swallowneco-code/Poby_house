package io.poby_house.support;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.net.URI;

/**
 * 예외를 사람이 읽을 수 있는 화면으로 바꾼다.
 * 이것이 없으면 없는 학생 번호를 눌렀을 때 영문 Whitelabel 500 이 뜬다.
 */
@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

    private static final String FALLBACK = "/students";

    @ExceptionHandler(NotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String notFound(NotFoundException e, Model model) {
        model.addAttribute("errorTitle", "찾을 수 없습니다");
        model.addAttribute("errorDetail", e.getMessage());
        return "error/notfound";
    }

    /** 하던 화면으로 되돌리고 이유만 알려 준다. 여기서 에러 페이지를 띄우면 입력하던 것이 날아간다 */
    @ExceptionHandler(BusinessRuleException.class)
    public String businessRule(BusinessRuleException e,
                               HttpServletRequest request,
                               RedirectAttributes redirect) {
        redirect.addFlashAttribute("errorMessage", e.getMessage());
        return "redirect:" + safeReferer(request);
    }

    /**
     * Referer 를 그대로 리다이렉트에 쓰면 오픈 리다이렉트가 된다.
     * 호스트를 버리고 경로만 쓴다.
     *
     * 컨텍스트 경로도 떼어 낸다. "redirect:" 로 나가는 주소는 컨텍스트 기준이라
     * /poby 아래에 배포했을 때 그대로 두면 /poby/poby/classes/1 이 된다.
     */
    private String safeReferer(HttpServletRequest request) {
        String referer = request.getHeader("Referer");
        if (referer == null) {
            return FALLBACK;
        }
        String path;
        try {
            path = URI.create(referer).getPath();
        } catch (IllegalArgumentException e) {
            return FALLBACK;
        }
        if (path == null || path.isBlank()) {
            return FALLBACK;
        }
        String contextPath = request.getContextPath();
        if (!contextPath.isEmpty() && path.startsWith(contextPath)) {
            path = path.substring(contextPath.length());
        }
        return path.isBlank() ? FALLBACK : path;
    }
}
