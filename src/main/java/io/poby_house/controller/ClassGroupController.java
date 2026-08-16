package io.poby_house.controller;

import io.poby_house.domain.ClassGroup;
import io.poby_house.dto.ClassGroupForm;
import io.poby_house.security.TeacherPrincipal;
import io.poby_house.service.ClassGroupService;
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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;

@Controller
@RequestMapping("/classes")
@RequiredArgsConstructor
public class ClassGroupController {

    private final ClassGroupService classGroupService;

    @GetMapping
    public String list(Model model) {
        model.addAttribute("groups", classGroupService.findAll());
        model.addAttribute("counts", classGroupService.currentHeadcounts());
        model.addAttribute("unassigned", classGroupService.unassignedCount());
        model.addAttribute("primaryActionHref", "/classes/new");
        model.addAttribute("primaryActionLabel", "+ 반 만들기");
        return "classgroup/list";
    }

    @GetMapping("/new")
    public String newForm(Model model) {
        model.addAttribute("form", new ClassGroupForm());
        model.addAttribute("editing", false);
        return "classgroup/form";
    }

    @PostMapping
    public String create(@AuthenticationPrincipal TeacherPrincipal principal,
                         @Valid @ModelAttribute("form") ClassGroupForm form,
                         BindingResult bindingResult,
                         Model model,
                         RedirectAttributes redirect) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("editing", false);
            return "classgroup/form";
        }
        ClassGroup saved = classGroupService.create(form, principal.getId());
        redirect.addFlashAttribute("message", saved.getName() + " 반을 만들었습니다.");
        return "redirect:/classes/" + saved.getId();
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        model.addAttribute("group", classGroupService.get(id));
        model.addAttribute("enrollments", classGroupService.currentEnrollments(id));
        model.addAttribute("assignable", classGroupService.assignableStudents());
        return "classgroup/detail";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        ClassGroup group = classGroupService.get(id);
        model.addAttribute("form", ClassGroupForm.from(group));
        model.addAttribute("group", group);
        model.addAttribute("editing", true);
        return "classgroup/form";
    }

    @PostMapping("/{id}")
    public String update(@PathVariable Long id,
                         @AuthenticationPrincipal TeacherPrincipal principal,
                         @Valid @ModelAttribute("form") ClassGroupForm form,
                         BindingResult bindingResult,
                         Model model,
                         RedirectAttributes redirect) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("group", classGroupService.get(id));
            model.addAttribute("editing", true);
            return "classgroup/form";
        }
        classGroupService.update(id, form, principal.getId());
        redirect.addFlashAttribute("message", "수정했습니다.");
        return "redirect:/classes/" + id;
    }

    @PostMapping("/{id}/students")
    public String assign(@PathVariable Long id,
                         @RequestParam Long studentId,
                         RedirectAttributes redirect) {
        // 규칙 위반과 없는 id 는 GlobalExceptionHandler 가 받는다.
        // 여기서 한 종류만 잡으면 나머지가 500 으로 새어 나간다.
        classGroupService.assignStudent(id, studentId, LocalDate.now());
        redirect.addFlashAttribute("message", "학생을 반에 넣었습니다.");
        return "redirect:/classes/" + id;
    }

    @PostMapping("/{id}/students/{enrollmentId}/release")
    public String release(@PathVariable Long id,
                          @PathVariable Long enrollmentId,
                          RedirectAttributes redirect) {
        classGroupService.releaseStudent(enrollmentId, LocalDate.now());
        // 잘못 눌렀을 때 되돌리는 길을 함께 알린다. 안 적으면 배정을 되살릴 방법을 찾다가 화면을 뜬다
        redirect.addFlashAttribute("message",
                "반에서 뺐습니다. 지난 기록은 그대로 남습니다. 다시 넣으려면 아래 '학생 추가' 에서 고르세요.");
        return "redirect:/classes/" + id;
    }
}
