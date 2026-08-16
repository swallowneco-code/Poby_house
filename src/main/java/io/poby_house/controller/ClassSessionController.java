package io.poby_house.controller;

import io.poby_house.domain.AttendanceStatus;
import io.poby_house.domain.ClassSession;
import io.poby_house.domain.HomeworkStatus;
import io.poby_house.dto.SessionListRow;
import io.poby_house.dto.SessionRecordForm;
import io.poby_house.security.TeacherPrincipal;
import io.poby_house.service.ClassGroupService;
import io.poby_house.service.ClassSessionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
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

import java.time.LocalDate;
import java.util.List;

/**
 * 수업 회차 · 출결 · 진도.
 *
 * 회차 목록은 반에 딸린 화면이라 /classes/{id}/sessions 이고,
 * 회차 하나는 반과 무관하게 한 곳을 가리켜야 해서 /sessions/{id} 다.
 * 그래서 클래스 단위 @RequestMapping 을 두지 않았다.
 */
@Controller
@RequiredArgsConstructor
public class ClassSessionController {

    private final ClassSessionService classSessionService;
    private final ClassGroupService classGroupService;

    @GetMapping("/classes/{classId}/sessions")
    public String list(@PathVariable Long classId,
                       @RequestParam(required = false) String month,
                       Model model) {
        List<SessionListRow> recorded = classSessionService.recordedRows(classId);
        List<String> months = classSessionService.recordedMonths(recorded);
        // 링크가 오래돼서 없는 달을 가리키면 빈 화면을 보여 주지 않고 가장 최근 달을 연다
        String selected = (month != null && months.contains(month))
                ? month
                : (months.isEmpty() ? null : months.get(0));

        model.addAttribute("group", classGroupService.get(classId));
        model.addAttribute("rows", classSessionService.rowsIn(recorded, selected));
        model.addAttribute("months", months);
        model.addAttribute("selectedMonth", selected);
        model.addAttribute("recordedCount", recorded.size());
        model.addAttribute("today", LocalDate.now());
        model.addAttribute("todayOpened", classSessionService.isOpened(classId, LocalDate.now()));
        model.addAttribute("activeNav", "classes");
        return "session/list";
    }

    /**
     * 회차를 연다. 같은 반 같은 날짜는 하나뿐이라 이미 있으면 그 회차로 들어간다.
     * date 를 안 주면 오늘이다. 오늘 수업을 여는 것이 이 화면의 거의 모든 쓰임이므로 한 번 눌러 끝나야 한다.
     */
    @PostMapping("/classes/{classId}/sessions")
    public String open(@PathVariable Long classId,
                       @RequestParam(required = false)
                       @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
                       @AuthenticationPrincipal TeacherPrincipal principal,
                       RedirectAttributes redirect) {
        ClassSession session = classSessionService.open(classId, date, principal.getId());
        return "redirect:/sessions/" + session.getId();
    }

    @GetMapping("/sessions/{id}")
    public String record(@PathVariable Long id, Model model) {
        model.addAttribute("form", classSessionService.buildForm(id));
        addRecordView(model, classSessionService.get(id));
        return "session/record";
    }

    /** 전원을 한 번에 저장하고 이 회차로 되돌아온다. 방금 적은 것을 눈으로 확인해야 한다 */
    @PostMapping("/sessions/{id}")
    public String save(@PathVariable Long id,
                       @Valid @ModelAttribute("form") SessionRecordForm form,
                       BindingResult bindingResult,
                       @AuthenticationPrincipal TeacherPrincipal principal,
                       Model model,
                       RedirectAttributes redirect) {
        if (bindingResult.hasErrors()) {
            // 학생 이름은 폼으로 되돌아오지 않는다. 다시 붙이지 않으면 오류 화면이 빈 줄 목록이 된다
            classSessionService.restoreEntries(id, form);
            addRecordView(model, classSessionService.get(id));
            return "session/record";
        }
        classSessionService.save(id, form, principal.getId());
        redirect.addFlashAttribute("message", "저장했습니다.");
        return "redirect:/sessions/" + id;
    }

    /** 지우지 않고 목록에서만 뺀다. 같은 날짜로 다시 열면 되살아난다 */
    @PostMapping("/sessions/{id}/hide")
    public String hide(@PathVariable Long id, RedirectAttributes redirect) {
        ClassSession hidden = classSessionService.hide(id);
        redirect.addFlashAttribute("message",
                hidden.getSessionDate() + " 회차를 목록에서 숨겼습니다. 같은 날짜로 다시 열면 되살아납니다.");
        return "redirect:/classes/" + hidden.getClassGroup().getId() + "/sessions";
    }

    private void addRecordView(Model model, ClassSession session) {
        // 모델 이름을 session 으로 두면 안 된다.
        // Thymeleaf 의 session 은 HttpSession 을 가리키는 예약어라 조용히 덮어써진다.
        model.addAttribute("classSession", session);
        model.addAttribute("attendanceOptions", AttendanceStatus.values());
        model.addAttribute("homeworkOptions", HomeworkStatus.values());
        // 지난 회차의 과제를 보여 준다. 이게 없으면 지난 과제를 무엇에 대해 찍는지 알 수 없다
        model.addAttribute("prevSession", classSessionService
                .previousSession(session.getClassGroup().getId(), session.getSessionDate())
                .orElse(null));
        model.addAttribute("activeNav", "classes");
    }
}
