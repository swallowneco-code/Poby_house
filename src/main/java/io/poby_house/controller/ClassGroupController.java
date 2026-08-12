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
import java.util.LinkedHashMap;
import java.util.Map;

@Controller
@RequestMapping("/classes")
@RequiredArgsConstructor
public class ClassGroupController {

    private final ClassGroupService classGroupService;

    @GetMapping
    public String list(Model model) {
        var groups = classGroupService.findAll();
        Map<Long, Long> counts = new LinkedHashMap<>();
        for (ClassGroup g : groups) {
            counts.put(g.getId(), classGroupService.countCurrentStudents(g.getId()));
        }
        model.addAttribute("groups", groups);
        model.addAttribute("counts", counts);
        model.addAttribute("unassigned", classGroupService.unassignedCount());
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
                         @Valid @ModelAttribute("form") ClassGroupForm form,
                         BindingResult bindingResult,
                         Model model,
                         RedirectAttributes redirect) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("group", classGroupService.get(id));
            model.addAttribute("editing", true);
            return "classgroup/form";
        }
        classGroupService.update(id, form);
        redirect.addFlashAttribute("message", "수정했습니다.");
        return "redirect:/classes/" + id;
    }

    @PostMapping("/{id}/students")
    public String assign(@PathVariable Long id,
                         @RequestParam Long studentId,
                         RedirectAttributes redirect) {
        try {
            classGroupService.assignStudent(id, studentId, LocalDate.now());
            redirect.addFlashAttribute("message", "학생을 반에 넣었습니다.");
        } catch (IllegalStateException e) {
            redirect.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/classes/" + id;
    }

    @PostMapping("/{id}/students/{enrollmentId}/release")
    public String release(@PathVariable Long id,
                          @PathVariable Long enrollmentId,
                          RedirectAttributes redirect) {
        classGroupService.releaseStudent(enrollmentId, LocalDate.now());
        redirect.addFlashAttribute("message", "반에서 뺐습니다. 지난 기록은 그대로 남습니다.");
        return "redirect:/classes/" + id;
    }
}
