package io.poby_house.security;

import io.poby_house.domain.Teacher;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

/**
 * 로그인한 강사. 화면에 이름을 띄우려고 UserDetails 에 이름과 id 를 함께 담는다.
 */
@Getter
public class TeacherPrincipal implements UserDetails {

    private final Long id;
    private final String loginId;
    private final String password;
    private final String name;
    private final boolean active;
    private final Collection<? extends GrantedAuthority> authorities;

    public TeacherPrincipal(Teacher teacher) {
        this.id = teacher.getId();
        this.loginId = teacher.getLoginId();
        this.password = teacher.getPassword();
        this.name = teacher.getName();
        this.active = teacher.isActive();
        this.authorities = List.of(new SimpleGrantedAuthority("ROLE_" + teacher.getRole().name()));
    }

    @Override
    public String getUsername() {
        return loginId;
    }

    @Override
    public boolean isEnabled() {
        return active;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }
}
