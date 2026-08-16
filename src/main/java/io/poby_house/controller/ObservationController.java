package io.poby_house.controller;

import io.poby_house.domain.Observation;
import io.poby_house.domain.ObservationKind;
import io.poby_house.domain.Student;
import io.poby_house.dto.ObservationForm;
import io.poby_house.security.TeacherPrincipal;
import io.poby_house.service.ObservationService;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Optional;

/**
 * 관찰기록 화면.
 *
 * 목록은 학생 아래(/students/{id}/observations)에, 한 건 수정은 기록 자신의 주소
 * (/observations/{oid})에 둔다. 수정 주소에 학생 id 를 또 넣으면
 * 두 값이 어긋난 주소(다른 학생의 기록을 남의 학생 아래에서 저장하는 요청)가 만들어진다.
 * 그래서 클래스 단위 @RequestMapping 을 두지 않는다.
 */
@Controller
@RequiredArgsConstructor
public class ObservationController {

    private final ObservationService observationService;
    private final StudentService studentService;

    @GetMapping("/students/{id}/observations")
    public String list(@PathVariable Long id, Model model) {
        Observation baseline = observationService.baseline(id).orElse(null);
        Observation latest = observationService.latest(id).orElse(null);

        model.addAttribute("student", studentService.get(id));
        model.addAttribute("observations", observationService.history(id));
        model.addAttribute("baseline", baseline);
        model.addAttribute("latest", latest);
        // 기준선 하나뿐일 때는 비교를 그리지 않는다.
        // 같은 기록을 before/after 로 나란히 두면 "변화 없음" 으로 읽혀서 사실과 다른 인상을 준다.
        model.addAttribute("comparable",
                baseline != null && latest != null && !latest.getId().equals(baseline.getId()));
        return "observation/list";
    }

    /**
     * 학생 등록 직후 ?kind=BASELINE 으로 들어오는 길이다.
     * 이미 기준선이 있으면 두 번째를 적게 두지 않고, 있는 것을 고치는 화면으로 보낸다.
     * 여기서 폼을 열어 주면 다 적고 저장을 누른 순간 거절당해서 적은 것이 날아간다.
     */
    @GetMapping("/students/{id}/observations/new")
    public String newForm(@PathVariable Long id,
                          @RequestParam(required = false) ObservationKind kind,
                          Model model,
                          RedirectAttributes redirect) {
        if (kind == ObservationKind.BASELINE) {
            Optional<Observation> existing = observationService.baseline(id);
            if (existing.isPresent()) {
                redirect.addFlashAttribute("errorMessage",
                        "입회 기준선은 학생마다 하나입니다. 이미 있는 기준선을 여기서 고치세요.");
                return "redirect:/observations/" + existing.get().getId() + "/edit";
            }
        }
        ObservationForm form = observationService.newForm(id, kind);
        model.addAttribute("form", form);
        fillFormModel(model, studentService.get(id), false, null, form.getKind());
        return "observation/form";
    }

    @PostMapping("/students/{id}/observations")
    public String create(@PathVariable Long id,
                         @AuthenticationPrincipal TeacherPrincipal principal,
                         @Valid @ModelAttribute("form") ObservationForm form,
                         BindingResult bindingResult,
                         Model model,
                         RedirectAttributes redirect) {
        if (bindingResult.hasErrors()) {
            fillFormModel(model, studentService.get(id), false, null, form.getKind());
            return "observation/form";
        }
        Observation saved = observationService.create(id, form, principal == null ? null : principal.getId());
        redirect.addFlashAttribute("message", saved.getKind().getLabel() + " 기록을 남겼습니다.");
        return "redirect:/students/" + id + "/observations";
    }

    @GetMapping("/observations/{oid}/edit")
    public String editForm(@PathVariable Long oid, Model model) {
        Observation observation = observationService.get(oid);
        model.addAttribute("form", ObservationForm.from(observation));
        fillFormModel(model, observation.getStudent(), true, observation, observation.getKind());
        return "observation/form";
    }

    @PostMapping("/observations/{oid}")
    public String update(@PathVariable Long oid,
                         @Valid @ModelAttribute("form") ObservationForm form,
                         BindingResult bindingResult,
                         Model model,
                         RedirectAttributes redirect) {
        if (bindingResult.hasErrors()) {
            Observation observation = observationService.get(oid);
            fillFormModel(model, observation.getStudent(), true, observation, form.getKind());
            return "observation/form";
        }
        Observation saved = observationService.update(oid, form);
        redirect.addFlashAttribute("message", "관찰기록을 수정했습니다.");
        return "redirect:/students/" + saved.getStudent().getId() + "/observations";
    }

    /**
     * 폼이 필요한 것을 한 곳에서 채운다.
     * 검증 실패 경로에서 이 중 하나만 빠뜨리면 그때만 화면이 500 으로 터진다.
     * 실수하기 가장 쉬운 자리라 메서드로 묶어 둔다.
     */
    private void fillFormModel(Model model, Student student, boolean editing,
                               Observation observation, ObservationKind kind) {
        model.addAttribute("student", student);
        model.addAttribute("observation", observation);
        model.addAttribute("editing", editing);
        model.addAttribute("kinds", ObservationKind.values());
        model.addAttribute("baselineMode", kind == ObservationKind.BASELINE);
        // 정기 관찰을 적으러 온 사람에게도 기준선이 비었다는 사실을 알린다.
        // 목록을 거치지 않고 바로 폼으로 들어오는 길이 있어서, 여기서 알리지 않으면 영영 모른다.
        model.addAttribute("hasBaseline", observationService.hasBaseline(student.getId()));
    }
}
