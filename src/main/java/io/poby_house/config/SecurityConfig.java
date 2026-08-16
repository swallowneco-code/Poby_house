package io.poby_house.config;

import io.poby_house.security.LoginFailureHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.access.hierarchicalroles.RoleHierarchyImpl;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.RememberMeServices;
import org.springframework.security.web.authentication.rememberme.TokenBasedRememberMeServices;

/**
 * 로그인하지 않으면 아무 화면도 볼 수 없다.
 * 예외는 로그인 화면, 최초 계정 설정 화면, 에러 화면, 정적 자원뿐이다.
 */
@Configuration
public class SecurityConfig {

    /**
     * 자동 로그인.
     *
     * 토큰 방식이라 테이블이 필요 없다. 쿠키에 서명만 담는다.
     * 서명에 비밀번호 해시가 들어가므로, 비밀번호를 바꾸면 다른 기기의 자동 로그인이 저절로 끊긴다.
     * 오히려 그게 바라는 동작이다.
     *
     * 빈으로 꺼내 두는 이유는 키를 한 곳에만 두기 위해서다.
     */
    @Bean
    public RememberMeServices rememberMeServices(UserDetailsService userDetailsService,
                                                 @Value("${poby.remember-me.key}") String key) {
        TokenBasedRememberMeServices services = new TokenBasedRememberMeServices(
                key, userDetailsService, TokenBasedRememberMeServices.RememberMeTokenAlgorithm.SHA256);
        services.setParameter("rememberMe");
        services.setCookieName("POBY_REMEMBER");
        services.setTokenValiditySeconds(60 * 60 * 24 * 30);
        return services;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
                                                   LoginFailureHandler loginFailureHandler,
                                                   RememberMeServices rememberMeServices) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/css/**", "/js/**", "/favicon.ico").permitAll()
                        // Security 6+ 는 ERROR 디스패치도 필터를 탄다.
                        // 이걸 빼면 로그인 전에 오류가 났을 때 에러 화면 대신 /login 으로 튕기고,
                        // 그 /login 이 또 실패하면 무한 리다이렉트가 되면서 아무 메시지도 안 뜬다.
                        .requestMatchers("/error", "/error/**").permitAll()
                        .requestMatchers("/login", "/setup").permitAll()
                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        .anyRequest().authenticated())
                .formLogin(form -> form
                        .loginPage("/login")
                        .loginProcessingUrl("/login")
                        .usernameParameter("loginId")
                        .passwordParameter("password")
                        // alwaysUse 를 켜지 않는다.
                        // 켜 두면 세션이 끊겨 다시 로그인했을 때 보던 화면으로 돌아가지 못하고
                        // 늘 이 주소로 튕긴다. 인자 없는 형태는 원래 가려던 곳을 기억한다.
                        // 착지 화면은 홈 대시보드다. 학생 목록은 누가 있는지만 알려 주고
                        // 오늘 무엇이 밀렸는지는 말해 주지 않는다
                        .defaultSuccessUrl("/")
                        .failureHandler(loginFailureHandler)
                        .permitAll())
                .rememberMe(remember -> remember.rememberMeServices(rememberMeServices))
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login?logout")
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID", "POBY_REMEMBER")
                        .permitAll());
        return http.build();
    }

    /**
     * 원장은 강사가 할 수 있는 것을 전부 할 수 있다.
     * 이 한 줄이 없으면 화면 규칙마다 hasAnyRole("ADMIN","TEACHER") 을 반복해야 하고,
     * 언젠가 한 곳을 빠뜨려 원장이 자기 화면에서 막힌다.
     */
    @Bean
    public RoleHierarchy roleHierarchy() {
        return RoleHierarchyImpl.fromHierarchy("ROLE_ADMIN > ROLE_TEACHER");
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
