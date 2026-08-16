package io.poby_house.service;

import io.poby_house.domain.Teacher;
import io.poby_house.domain.TeacherRole;
import io.poby_house.dto.SetupForm;
import io.poby_house.dto.TeacherEditForm;
import io.poby_house.dto.TeacherForm;
import io.poby_house.repository.TeacherRepository;
import io.poby_house.support.BusinessRuleException;
import io.poby_house.support.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

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
            throw new BusinessRuleException("이미 계정이 있습니다.");
        }
        Teacher teacher = Teacher.of(
                form.getLoginId().trim(),
                passwordEncoder.encode(form.getPassword()),
                form.getName().trim(),
                TeacherRole.ADMIN);
        return teacherRepository.save(teacher);
    }

    public List<Teacher> findAll() {
        return teacherRepository.findAllByOrderByActiveDescNameAsc();
    }

    public Teacher get(Long id) {
        return teacherRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("계정을 찾을 수 없습니다. id=" + id));
    }

    public boolean isLoginIdTaken(String loginId) {
        return teacherRepository.existsByLoginId(loginId.trim());
    }

    /** 강사 계정 추가. 최초 원장 생성과 같은 방식이고 권한만 고를 수 있다 */
    @Transactional
    public Teacher create(TeacherForm form) {
        Teacher teacher = Teacher.of(
                form.getLoginId().trim(),
                passwordEncoder.encode(form.getPassword()),
                form.getName().trim(),
                form.getRole());
        return teacherRepository.save(teacher);
    }

    /**
     * 이름·권한·사용 여부만 바꾼다.
     * actorId 는 지금 조작하는 사람. 자기 발등을 찍는 것을 막는 데 쓴다.
     */
    @Transactional
    public void update(Long id, TeacherEditForm form, Long actorId) {
        Teacher teacher = get(id);

        boolean changingRole = teacher.getRole() != form.getRole();
        boolean changingActive = teacher.isActive() != form.isActive();
        if (id.equals(actorId) && (changingRole || changingActive)) {
            throw new BusinessRuleException("자기 계정의 권한과 사용 여부는 바꿀 수 없습니다.");
        }
        guardLastAdmin(teacher, form.getRole(), form.isActive());

        teacher.setName(form.getName().trim());
        teacher.setRole(form.getRole());
        teacher.setActive(form.isActive());
    }

    /**
     * 원장이 한 명도 없는 상태를 막는다.
     * 계정 관리 화면 자체가 /admin/** 이라, 마지막 원장을 강등하거나 중지하면
     * 그 화면에 아무도 못 들어가고 되돌릴 방법이 사라진다.
     */
    private void guardLastAdmin(Teacher target, TeacherRole newRole, boolean newActive) {
        boolean wasActiveAdmin = target.getRole() == TeacherRole.ADMIN && target.isActive();
        boolean staysActiveAdmin = newRole == TeacherRole.ADMIN && newActive;
        if (wasActiveAdmin && !staysActiveAdmin
                && teacherRepository.countByRoleAndActiveTrue(TeacherRole.ADMIN) <= 1) {
            throw new BusinessRuleException("마지막 원장 계정입니다. 다른 원장 계정을 먼저 만들어 주세요.");
        }
    }

    public boolean matchesPassword(Long teacherId, String rawPassword) {
        Teacher teacher = get(teacherId);
        return passwordEncoder.matches(rawPassword, teacher.getPassword());
    }

    @Transactional
    public void changePassword(Long teacherId, String newRawPassword) {
        get(teacherId).setPassword(passwordEncoder.encode(newRawPassword));
    }

    /** 이름만 바꾼다. 아이디는 바꾸지 않고, 비밀번호는 별도 화면에서 다룬다 */
    @Transactional
    public void rename(Long teacherId, String name) {
        get(teacherId).setName(name.trim());
    }
}
