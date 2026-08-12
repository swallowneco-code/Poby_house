package io.poby_house.controller;

import io.poby_house.domain.AttendanceStatus;
import io.poby_house.domain.ClassSession;
import io.poby_house.dto.SessionRecordForm;
import io.poby_house.security.TeacherPrincipal;
import io.poby_house.service.ClassSessionService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;

@Controller
@RequiredArgsConstructor
public class ClassSessionController {

    private final ClassSessionService classSessionService;

    /** 반 상세에서 날짜를 골라 회차를 연다. 같은 날짜가 이미 있으면 그 회차로 간다 */
    @PostMapping("/classes/{classId}/sessions")
    public String open(@PathVariable Long classId,
                       @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate sessionDate,
                       @AuthenticationPrincipal TeacherPrincipal principal,
                       RedirectAttributes redirect) {
        ClassSession session = classSessionService.createOrGet(classId, sessionDate, principal.getId());
        return "redirect:/sessions/" + session.getId();
    }

    @GetMapping("/sessions/{id}")
    public String record(@PathVariable Long id, Model model) {
        // 모델 이름을 session 으로 두면 안 된다.
        // Thymeleaf 에서 session 은 HttpSession 을 가리키는 예약어라 조용히 덮어써진다.
        ClassSession classSession = classSessionService.get(id);
        model.addAttribute("classSession", classSession);
        model.addAttribute("form", classSessionService.buildForm(id));
        model.addAttribute("attendanceOptions", AttendanceStatus.values());
        return "session/record";
    }

    /** 목록에서 숨긴다. 실제로 지우지는 않는다 */
    @PostMapping("/sessions/{id}/hide")
    public String hide(@PathVariable Long id, RedirectAttributes redirect) {
        ClassSession hidden = classSessionService.hide(id);
        redirect.addFlashAttribute("message",
                hidden.getSessionDate() + " 회차를 목록에서 숨겼습니다. 같은 날짜로 다시 열면 되살아납니다.");
        return "redirect:/classes/" + hidden.getClassGroup().getId();
    }

    @PostMapping("/sessions/{id}")
    public String save(@PathVariable Long id,
                       @ModelAttribute("form") SessionRecordForm form,
                       @AuthenticationPrincipal TeacherPrincipal principal,
                       RedirectAttributes redirect) {
        classSessionService.save(id, form, principal.getId());
        redirect.addFlashAttribute("message", "저장했습니다.");
        return "redirect:/sessions/" + id;
    }
}
