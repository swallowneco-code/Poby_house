package io.poby_house.service;

import io.poby_house.domain.Scene;
import io.poby_house.domain.Student;
import io.poby_house.dto.SceneForm;
import io.poby_house.repository.SceneRepository;
import io.poby_house.repository.StudentRepository;
import io.poby_house.repository.TeacherRepository;
import io.poby_house.support.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.util.List;

/**
 * 장면. 리포트 4-1 "편지" 의 재료다.
 *
 * 분기에 1개면 충분한 기록이라, 이 서비스의 절반은 "이번 분기에 아직 없다" 를 알려 주는 일이다.
 * 알려 주지 않으면 1년이 지나도 0개고, 그러면 편지를 한 줄도 쓸 수 없다.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SceneService {

    private final SceneRepository sceneRepository;
    private final StudentRepository studentRepository;
    private final TeacherRepository teacherRepository;

    /** 학생의 장면 이력. 최근 순. 리포트와 상세 화면이 함께 쓴다 */
    public List<Scene> history(Long studentId) {
        return sceneRepository.findByStudentIdOrderByOccurredOnDesc(studentId);
    }

    public Scene get(Long id) {
        return sceneRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("장면 기록을 찾을 수 없습니다. id=" + id));
    }

    @Transactional
    public Scene create(Long studentId, SceneForm form, Long teacherId) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new NotFoundException("학생을 찾을 수 없습니다. id=" + studentId));

        Scene scene = Scene.of(student, form.getOccurredOn(), form.getContent().trim());
        scene.setSituation(blankToNull(form.getSituation()));
        if (teacherId != null) {
            teacherRepository.findById(teacherId).ifPresent(scene::setCreatedBy);
        }
        return sceneRepository.save(scene);
    }

    /**
     * 고칠 수는 있게 두되 createdBy 는 건드리지 않는다.
     * 처음 본 사람이 누구였는지가 몇 년 뒤 리포트를 쓸 때 물어볼 사람을 정한다.
     */
    @Transactional
    public Scene update(Long id, SceneForm form) {
        Scene scene = get(id);
        scene.setOccurredOn(form.getOccurredOn());
        scene.setSituation(blankToNull(form.getSituation()));
        scene.setContent(form.getContent().trim());
        return scene;
    }

    /** 이번 분기 시작일. 1/1 · 4/1 · 7/1 · 10/1 */
    public LocalDate currentQuarterStart() {
        LocalDate today = LocalDate.now();
        int firstMonthOfQuarter = (today.getMonthValue() - 1) / 3 * 3 + 1;
        return LocalDate.of(today.getYear(), firstMonthOfQuarter, 1);
    }

    /** "2026년 3분기". 화면 문구에 쓴다. 어느 분기를 말하는지 밝히지 않으면 안내가 잔소리로만 읽힌다 */
    public String currentQuarterLabel() {
        LocalDate start = currentQuarterStart();
        return start.getYear() + "년 " + ((start.getMonthValue() - 1) / 3 + 1) + "분기";
    }

    /**
     * 이번 분기에 있었던 장면 수.
     *
     * 이미 조회한 목록을 받는다. studentId 를 받아 다시 세면 화면 한 장에 같은 테이블을 두 번 읽는다.
     * 기준은 occurredOn(있었던 날)이다. 적은 날로 세면 지난 분기 일을 이번 분기에 몰아 적은 것이
     * 이번 분기 기록으로 잡힌다.
     */
    public long countInCurrentQuarter(List<Scene> scenes) {
        LocalDate from = currentQuarterStart();
        return scenes.stream()
                .filter(scene -> scene.getOccurredOn() != null && !scene.getOccurredOn().isBefore(from))
                .count();
    }

    /** 빈 입력칸은 "" 로 들어온다. 그대로 두면 화면의 ?: '—' 를 통과해 빈칸이 보인다 */
    private String blankToNull(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }
}
