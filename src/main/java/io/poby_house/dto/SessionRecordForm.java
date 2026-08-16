package io.poby_house.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.util.AutoPopulatingList;

import java.util.List;

/**
 * 회차 한 건을 통째로 저장하는 폼.
 *
 * 학생 한 명 저장할 때마다 화면이 넘어가면 아무도 안 쓴다. 그래서 반 공통 진도와 학생 전원을 한 번에 담는다.
 *
 * 진도를 반 공통으로 한 번만 받는 이유:
 * 리포트는 학생 한 명의 진도 흐름을 요구하는데, 입력은 반 단위여야 30초에 끝난다.
 * 그래서 여기서 한 번 받아 저장할 때 학생별 ProgressLog 로 복사한다.
 * 둘 중 하나만 고르면 리포트가 비거나(반에만 남김) 아무도 기록을 안 한다(학생마다 재입력).
 */
@Getter
@Setter
@NoArgsConstructor
public class SessionRecordForm {

    /* ---- 반 공통 ---- */

    /** 수업 메모 - 그날 반 전체에 있었던 일. 개인에 대한 것은 학생 메모에 적는다 */
    private String dailyNote;

    @Size(max = 100, message = "교재명은 100자까지 적을 수 있습니다.")
    private String textbook;

    @Size(max = 100, message = "범위는 100자까지 적을 수 있습니다.")
    private String rangeText;

    /**
     * 오늘 한 것 · 오늘 내준 과제.
     * DB 가 VARCHAR(500) 이라 여기서 막지 않으면 저장 시점에 MySQL 오류로 튄다.
     */
    @Size(max = 500, message = "오늘 한 것은 500자까지 적을 수 있습니다.")
    private String content;

    @Size(max = 500, message = "과제는 500자까지 적을 수 있습니다.")
    private String homework;

    /* ---- 학생별 ---- */

    /**
     * AutoPopulatingList 여야 entries[3].note 같은 인덱스 바인딩이 된다.
     * 보통 List 로 두면 없는 인덱스에서 바인딩이 터진다.
     */
    @Valid
    private List<StudentEntryForm> entries = new AutoPopulatingList<>(StudentEntryForm.class);
}
