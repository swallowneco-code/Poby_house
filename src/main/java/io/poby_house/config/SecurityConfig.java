package io.poby_house.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

/**
 * 1단계: 로그인 없이 전부 열어 둔다. 로컬에서 화면을 먼저 완성하기 위한 임시 설정이다.
 * CSRF는 켜 둔다 - Thymeleaf 폼이 토큰을 자동으로 넣어 주므로 지금 꺼 둘 이유가 없다.
 *
 * TODO 화면이 자리 잡으면 Teacher 테이블 기반 로그인으로 교체하고 anyRequest().authenticated() 로 바꾼다.
 */
@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
                .formLogin(form -> form.disable())
                .httpBasic(basic -> basic.disable());
        return http.build();
    }
}
