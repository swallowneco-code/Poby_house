package io.poby_house.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.access.hierarchicalroles.RoleHierarchyImpl;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * 로그인하지 않으면 아무 화면도 볼 수 없다.
 * 예외는 로그인 화면, 최초 계정 설정 화면, 에러 화면, 정적 자원뿐이다.
 */
@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
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
                        .defaultSuccessUrl("/students", true)
                        .failureUrl("/login?error")
                        .permitAll())
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login?logout")
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID")
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
