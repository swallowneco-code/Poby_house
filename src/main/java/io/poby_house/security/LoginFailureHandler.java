package io.poby_house.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * 로그인 실패 사유를 구분해서 알려 준다.
 *
 * 지금까지는 전부 "아이디 또는 비밀번호가 맞지 않습니다"로 뭉뚱그렸다.
 * 그래서 원장이 중지시킨 계정의 강사는 비밀번호가 틀린 줄 알고 계속 다시 쳤다.
 *
 * 아이디가 없는 경우와 비밀번호가 틀린 경우는 <b>일부러</b> 같은 메시지로 둔다.
 * 둘을 나누면 어떤 아이디가 존재하는지 알려 주는 셈이 된다.
 * (Spring Security 도 기본으로 UsernameNotFoundException 을 BadCredentialsException 으로 바꾼다)
 *
 * 반대로 "중지된 계정"은 계정이 있다는 사실을 드러낸다.
 * 학원 안에서 쓰는 도구라 알려 주는 쪽이 낫다고 판단했다.
 * 외부에 열게 되면 이 분기를 되돌려야 한다.
 */
@Component
public class LoginFailureHandler extends SimpleUrlAuthenticationFailureHandler {

    @Override
    public void onAuthenticationFailure(HttpServletRequest request,
                                        HttpServletResponse response,
                                        AuthenticationException exception) throws IOException, ServletException {
        // setDefaultFailureUrl 을 쓰면 싱글턴의 필드를 매 요청마다 바꾸게 된다.
        // 두 사람이 동시에 로그인에 실패하면 서로의 메시지를 덮어쓴다.
        String url = request.getContextPath() + "/login?error=" + reasonCode(exception);
        getRedirectStrategy().sendRedirect(request, response, url);
    }

    private String reasonCode(AuthenticationException exception) {
        if (exception instanceof DisabledException) {
            return "disabled";
        }
        if (exception instanceof LockedException) {
            return "locked";
        }
        return "bad";
    }
}
