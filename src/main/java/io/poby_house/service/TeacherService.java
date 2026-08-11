package io.poby_house.service;

import io.poby_house.domain.Teacher;
import io.poby_house.domain.TeacherRole;
import io.poby_house.dto.SetupForm;
import io.poby_house.repository.TeacherRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TeacherService {

    private final TeacherRepository teacherRepository;
    private final PasswordEncoder passwordEncoder;

    /** 계정이 하나도 없으면 최초 설정 화면으로 보내야 한다 */
    public boolean noAccountYet() {
        return teacherRepository.count() == 0;
    }

    @Transactional
    public Teacher createFirstAdmin(SetupForm form) {
        if (!noAccountYet()) {
            throw new IllegalStateException("이미 계정이 있습니다.");
        }
        Teacher teacher = Teacher.of(
                form.getLoginId().trim(),
                passwordEncoder.encode(form.getPassword()),
                form.getName().trim(),
                TeacherRole.ADMIN);
        return teacherRepository.save(teacher);
    }

    public boolean matchesPassword(Long teacherId, String rawPassword) {
        Teacher teacher = get(teacherId);
        return passwordEncoder.matches(rawPassword, teacher.getPassword());
    }

    @Transactional
    public void changePassword(Long teacherId, String newRawPassword) {
        get(teacherId).setPassword(passwordEncoder.encode(newRawPassword));
    }

    private Teacher get(Long id) {
        return teacherRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("계정을 찾을 수 없습니다. id=" + id));
    }
}
