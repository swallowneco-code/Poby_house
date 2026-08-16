package io.poby_house.dto.report;

import java.time.LocalDate;
import java.util.List;

/**
 * 리포트 2. 무엇을 했나.
 *
 * 회차를 전부 나열하지 않는다. 3년 다닌 학생이면 300줄이 되고 아무도 읽지 않는다.
 * 교재가 바뀌는 지점에서만 끊어 "교재 · 시작범위 → 끝범위 · N회" 한 줄로 압축한다.
 * 교재가 바뀌는 지점은 진도가 넘어간 지점이라, 그 경계가 곧 이 학생의 진도 흐름이다.
 */
public record WhatWeDid(
        long loggedSessions,
        List<ProgressSpan> spans,
        /** 과제 수행 분포. 0건인 상태도 행을 남긴다 */
        List<HomeworkCount> homework,
        long homeworkRecorded,
        /** 과제 여부를 적지 않은 회차 */
        long homeworkUnrecorded,
        /** 그날 이 학생에게 눈에 띈 것(ProgressLog.note). 편지의 재료라 요약하지 않는다 */
        List<NoteItem> notes) {

    /**
     * @param textbook null 이면 교재를 적지 않은 구간이다. 빈 문자열로 두지 않는다
     * @param rangeText "p.1~10 → p.120~131" 또는 한 칸만 있으면 그 한 칸
     */
    public record ProgressSpan(String textbook, LocalDate from, LocalDate to,
                               String rangeText, int sessions) {
    }

    public record HomeworkCount(String code, String label, long count, int percent) {
    }

    public record NoteItem(LocalDate on, String note) {
    }
}
