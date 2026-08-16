package io.poby_house.service;

import io.poby_house.domain.Teacher;
import io.poby_house.domain.TeacherRole;
import io.poby_house.dto.TeacherEditForm;
import io.poby_house.dto.TeacherForm;
import io.poby_house.repository.TeacherRepository;
import io.poby_house.support.BusinessRuleException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.lang.reflect.Field;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

/**
 * 계정 관리는 잘못되면 아무도 못 들어가는 상태가 된다.
 * 손으로 발견했을 때는 이미 늦으므로 여기만은 자동으로 지킨다.
 */
@ExtendWith(MockitoExtension.class)
class TeacherServiceTest {

    @Mock
    TeacherRepository teacherRepository;

    @org.mockito.Spy
    PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @InjectMocks
    TeacherService teacherService;

    private Teacher teacher(Long id, TeacherRole role, boolean active) {
        Teacher t = Teacher.of("poby", "encoded", "포비", role);
        t.changeActive(active);
        setId(t, id);
        return t;
    }

    private void setId(Teacher teacher, Long id) {
        try {
            Field field = Teacher.class.getDeclaredField("id");
            field.setAccessible(true);
            field.set(teacher, id);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException(e);
        }
    }

    @Test
    @DisplayName("계정을 만들 때 비밀번호를 평문으로 저장하지 않는다")
    void create_encodesPassword() {
        TeacherForm form = new TeacherForm();
        form.setName("김강사");
        form.setLoginId("kim");
        form.setPassword("plain-password");
        form.setPasswordConfirm("plain-password");
        form.setRole(TeacherRole.TEACHER);
        given(teacherRepository.save(any(Teacher.class))).willAnswer(inv -> inv.getArgument(0));

        Teacher saved = teacherService.create(form);

        assertThat(saved.getPassword()).isNotEqualTo("plain-password");
        assertThat(passwordEncoder.matches("plain-password", saved.getPassword())).isTrue();
    }

    @Test
    @DisplayName("마지막 남은 원장을 강사로 내리면 거절한다")
    void update_rejectsDemotingLastAdmin() {
        Teacher target = teacher(1L, TeacherRole.ADMIN, true);
        given(teacherRepository.findById(1L)).willReturn(Optional.of(target));
        given(teacherRepository.countByRoleAndActiveTrue(TeacherRole.ADMIN)).willReturn(1L);

        TeacherEditForm form = new TeacherEditForm();
        form.setName("포비");
        form.setRole(TeacherRole.TEACHER);
        form.setActive(true);

        // 조작하는 사람은 다른 계정(2L)이라 자기 자신 방어에는 걸리지 않는다
        assertThatThrownBy(() -> teacherService.update(1L, form, 2L))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("마지막 원장");

        assertThat(target.getRole()).isEqualTo(TeacherRole.ADMIN);
    }

    @Test
    @DisplayName("마지막 남은 원장을 중지시켜도 거절한다")
    void update_rejectsDeactivatingLastAdmin() {
        Teacher target = teacher(1L, TeacherRole.ADMIN, true);
        given(teacherRepository.findById(1L)).willReturn(Optional.of(target));
        given(teacherRepository.countByRoleAndActiveTrue(TeacherRole.ADMIN)).willReturn(1L);

        TeacherEditForm form = new TeacherEditForm();
        form.setName("포비");
        form.setRole(TeacherRole.ADMIN);
        form.setActive(false);

        assertThatThrownBy(() -> teacherService.update(1L, form, 2L))
                .isInstanceOf(BusinessRuleException.class);

        assertThat(target.isActive()).isTrue();
    }

    @Test
    @DisplayName("원장이 둘이면 한 명을 내릴 수 있다")
    void update_allowsDemotionWhenAnotherAdminRemains() {
        Teacher target = teacher(1L, TeacherRole.ADMIN, true);
        given(teacherRepository.findById(1L)).willReturn(Optional.of(target));
        given(teacherRepository.countByRoleAndActiveTrue(TeacherRole.ADMIN)).willReturn(2L);

        TeacherEditForm form = new TeacherEditForm();
        form.setName("포비");
        form.setRole(TeacherRole.TEACHER);
        form.setActive(true);

        teacherService.update(1L, form, 2L);

        assertThat(target.getRole()).isEqualTo(TeacherRole.TEACHER);
    }

    @Test
    @DisplayName("자기 계정의 권한은 스스로 바꿀 수 없다")
    void update_rejectsChangingOwnRole() {
        Teacher me = teacher(1L, TeacherRole.ADMIN, true);
        given(teacherRepository.findById(1L)).willReturn(Optional.of(me));

        TeacherEditForm form = new TeacherEditForm();
        form.setName("포비");
        form.setRole(TeacherRole.TEACHER);
        form.setActive(true);

        assertThatThrownBy(() -> teacherService.update(1L, form, 1L))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("자기 계정");

        // 마지막 원장 검사까지 가기 전에 막혀야 한다
        verify(teacherRepository, never()).countByRoleAndActiveTrue(any());
    }

    @Test
    @DisplayName("자기 이름은 스스로 바꿀 수 있다")
    void update_allowsChangingOwnName() {
        Teacher me = teacher(1L, TeacherRole.ADMIN, true);
        given(teacherRepository.findById(1L)).willReturn(Optional.of(me));

        TeacherEditForm form = new TeacherEditForm();
        form.setName("새이름");
        form.setRole(TeacherRole.ADMIN);
        form.setActive(true);

        teacherService.update(1L, form, 1L);

        assertThat(me.getName()).isEqualTo("새이름");
    }

    @Test
    @DisplayName("계정이 이미 있으면 최초 설정을 다시 하지 못한다")
    void createFirstAdmin_rejectedWhenAccountExists() {
        given(teacherRepository.count()).willReturn(1L);

        assertThatThrownBy(() -> teacherService.createFirstAdmin(new io.poby_house.dto.SetupForm()))
                .isInstanceOf(BusinessRuleException.class);
    }
}
