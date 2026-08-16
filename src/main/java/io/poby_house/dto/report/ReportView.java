package io.poby_house.dto.report;

import java.util.List;

/**
 * 퇴원생 리포트 초안 한 장. 화면과 인쇄와 텍스트 복사가 모두 이 하나를 본다.
 *
 * 템플릿이 엔티티를 헤집지 않게 뷰 전용으로 만든다. 그래야
 * (1) 반출 모드에서 실명이 빠졌다는 것을 한 곳에서 보장할 수 있고
 * (2) 화면에 뭘 더 붙이다가 LAZY 연관을 건드려 쿼리가 늘어나는 일이 생기지 않는다.
 *
 * plainText 는 이 레코드의 각 섹션에서만 만든다. 엔티티를 다시 읽지 않는다.
 * 그래서 반출 모드에서 감춘 값은 복사한 텍스트에도 구조적으로 들어갈 수 없다.
 */
public record ReportView(
        ReportHeader header,
        TogetherTime togetherTime,
        WhatWeDid whatWeDid,
        WhatChanged whatChanged,
        InterventionGroups interventions,
        WeakSpots weakSpots,
        List<SceneItem> scenes,
        Promises promises,
        /** 문서 편집기로 옮기기 위한 평문 초안 */
        String plainText) {
}
