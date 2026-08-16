package io.poby_house.controller;

import io.poby_house.domain.Student;
import io.poby_house.domain.StudentStatus;
import io.poby_house.dto.StudentForm;
import io.poby_house.service.StudentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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

@Controller
@RequestMapping("/students")
@RequiredArgsConstructor
public class StudentController {

    private final StudentService studentService;

    @GetMapping
    public String list(@RequestParam(required = false) StudentStatus status,
                       @RequestParam(required = false) String keyword,
                       Model model) {
        model.addAttribute("students", studentService.findAll(status, keyword));
        model.addAttribute("status", status);
        model.addAttribute("keyword", keyword);
        model.addAttribute("statuses", StudentStatus.values());
        model.addAttribute("primaryActionHref", "/students/new");
        model.addAttribute("primaryActionLabel", "+ 학생 등록");
        return "student/list";
    }

    @GetMapping("/new")
    public String newForm(Model model) {
        model.addAttribute("form", new StudentForm());
        model.addAttribute("statuses", StudentStatus.values());
        model.addAttribute("editing", false);
        return "student/form";
    }

    @PostMapping
    public String create(@Valid @ModelAttribute("form") StudentForm form,
                         BindingResult bindingResult,
                         Model model,
                         RedirectAttributes redirect) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("statuses", StudentStatus.values());
            model.addAttribute("editing", false);
            return "student/form";
        }
        Student saved = studentService.create(form);
        redirect.addFlashAttribute("message", saved.getName() + " 학생을 등록했습니다. (" + saved.getCode() + ")");
        return "redirect:/students/" + saved.getId();
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        model.addAttribute("student", studentService.get(id));
        model.addAttribute("enrollments", studentService.enrollmentHistory(id));
        return "student/detail";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        Student student = studentService.get(id);
        model.addAttribute("form", StudentForm.from(student));
        model.addAttribute("student", student);
        model.addAttribute("statuses", StudentStatus.values());
        model.addAttribute("editing", true);
        return "student/form";
    }

    @PostMapping("/{id}")
    public String update(@PathVariable Long id,
                         @Valid @ModelAttribute("form") StudentForm form,
                         BindingResult bindingResult,
                         Model model,
                         RedirectAttributes redirect) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("student", studentService.get(id));
            model.addAttribute("statuses", StudentStatus.values());
            model.addAttribute("editing", true);
            return "student/form";
        }
        studentService.update(id, form);
        redirect.addFlashAttribute("message", "수정했습니다.");
        return "redirect:/students/" + id;
    }
}
