package io.poby_house.controller;

import io.poby_house.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;

/**
 * 퇴원생 리포트 초안 화면.
 *
 * 읽기 전용이다. POST 가 없다. 리포트는 기록을 만들지 않고 이미 있는 기록을 배치한다.
 * 기간과 반출 모드는 전부 쿼리스트링으로 다룬다. 그래야 그 상태 그대로 주소를 남길 수 있고,
 * 인쇄·복사한 문서가 어느 조건으로 뽑은 것인지 주소만 봐도 다시 만들 수 있다.
 */
@Controller
@RequestMapping("/students/{id}/report")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    /**
     * @param from 기본은 학생 등록일. @DateTimeFormat 을 빼면 2026-08-18 을 못 읽고 400 이 난다
     *             (Boot 의 기본 날짜 변환은 ISO 가 아니다)
     * @param anon 외부 반출 모드. 실명·학교를 감춘다
     */
    @GetMapping
    public String draft(@PathVariable Long id,
                        @RequestParam(required = false)
                        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
                        @RequestParam(required = false)
                        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
                        @RequestParam(defaultValue = "false") boolean anon,
                        Model model) {
        model.addAttribute("report", reportService.draft(id, from, to, anon));
        // 화면이 링크를 만들 때 쓴다. 리포트 DTO 에 학생 id 를 담지 않는 이유는
        // 그 DTO 가 문서의 내용이고, 링크는 화면의 사정이기 때문이다
        model.addAttribute("studentId", id);
        return "report/draft";
    }
}
