package io.poby_house.controller;

import io.poby_house.domain.Scene;
import io.poby_house.domain.Student;
import io.poby_house.dto.SceneForm;
import io.poby_house.security.TeacherPrincipal;
import io.poby_house.service.SceneService;
import io.poby_house.service.StudentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

/**
 * 장면.
 *
 * 목록과 등록 폼이 한 화면이다. 3분 기록인데 "추가" 를 눌러 화면을 옮기게 만들면
 * 수업 끝나고 폰으로는 아무도 안 적는다. 그래서 /scenes/new 는 없다.
 * 클래스 단위 @RequestMapping 을 두지 않는 이유: 목록은 학생 밑(/students/{id}/scenes),
 * 수정은 기록 밑(/scenes/{sid})이라 접두사가 하나로 모이지 않는다.
 */
@Controller
@RequiredArgsConstructor
public class SceneController {

    private final SceneService sceneService;
    private final StudentService studentService;

    @GetMapping("/students/{id}/scenes")
    public String list(@PathVariable Long id, Model model) {
        model.addAttribute("form", new SceneForm());
        return listView(id, model);
    }

    @PostMapping("/students/{id}/scenes")
    public String create(@PathVariable Long id,
                         @AuthenticationPrincipal TeacherPrincipal principal,
                         @Valid @ModelAttribute("form") SceneForm form,
                         BindingResult bindingResult,
                         Model model,
                         RedirectAttributes redirect) {
        if (bindingResult.hasErrors()) {
            // 적던 글을 살려서 같은 화면으로 돌려준다. 목록도 다시 채워야 화면이 반쪽이 되지 않는다
            return listView(id, model);
        }
        sceneService.create(id, form, principal.getId());
        redirect.addFlashAttribute("message", "장면을 기록했습니다.");
        return "redirect:/students/" + id + "/scenes";
    }

    @GetMapping("/scenes/{sid}/edit")
    public String editForm(@PathVariable Long sid, Model model) {
        Scene scene = sceneService.get(sid);
        model.addAttribute("form", SceneForm.from(scene));
        model.addAttribute("scene", scene);
        model.addAttribute("student", scene.getStudent());
        return "scene/form";
    }

    @PostMapping("/scenes/{sid}")
    public String update(@PathVariable Long sid,
                         @Valid @ModelAttribute("form") SceneForm form,
                         BindingResult bindingResult,
                         Model model,
                         RedirectAttributes redirect) {
        Scene scene = sceneService.get(sid);
        if (bindingResult.hasErrors()) {
            model.addAttribute("scene", scene);
            model.addAttribute("student", scene.getStudent());
            return "scene/form";
        }
        sceneService.update(sid, form);
        redirect.addFlashAttribute("message", "장면을 고쳤습니다.");
        return "redirect:/students/" + scene.getStudent().getId() + "/scenes";
    }

    /** 목록 화면이 필요한 모델. 등록 실패로 되돌아올 때도 같은 것을 채워야 한다 */
    private String listView(Long studentId, Model model) {
        Student student = studentService.get(studentId);
        List<Scene> scenes = sceneService.history(studentId);

        model.addAttribute("student", student);
        model.addAttribute("scenes", scenes);
        model.addAttribute("quarterLabel", sceneService.currentQuarterLabel());
        model.addAttribute("quarterCount", sceneService.countInCurrentQuarter(scenes));
        return "scene/list";
    }
}
