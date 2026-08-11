package io.poby_house.security;

import io.poby_house.repository.TeacherRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TeacherDetailsService implements UserDetailsService {

    private final TeacherRepository teacherRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String loginId) throws UsernameNotFoundException {
        return teacherRepository.findByLoginId(loginId)
                .map(TeacherPrincipal::new)
                .orElseThrow(() -> new UsernameNotFoundException("계정을 찾을 수 없습니다: " + loginId));
    }
}
