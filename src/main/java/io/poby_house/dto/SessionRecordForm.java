package io.poby_house.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.util.AutoPopulatingList;

import java.util.List;

/**
 * 회차 한 건을 통째로 저장하는 폼.
 * 학생 한 명 저장할 때마다 화면이 넘어가면 아무도 안 쓴다. 그래서 한 번에 담는다.
 */
@Getter
@Setter
@NoArgsConstructor
public class SessionRecordForm {

    /* ---- 반 공통 ---- */

    /** 수업 메모 - 그날 반 전체에 있었던 일 */
    private String dailyNote;

    private String textbook;
    private String rangeText;
    private String content;
    private String homework;

    /* ---- 학생별 ---- */

    /** AutoPopulatingList 여야 entries[3].note 같은 인덱스 바인딩이 된다 */
    private List<StudentEntryForm> entries = new AutoPopulatingList<>(StudentEntryForm.class);
}
