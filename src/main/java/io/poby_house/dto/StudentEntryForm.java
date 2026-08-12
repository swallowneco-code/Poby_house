package io.poby_house.dto;

import io.poby_house.domain.AttendanceStatus;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 회차 화면의 학생 한 줄. 이름 · 학생 메모 · 출결이 전부다.
 * 진도는 반 공통이라 여기 없다. ClassSession 에 있다.
 */
@Getter
@Setter
@NoArgsConstructor
public class StudentEntryForm {

    private Long studentId;

    /** 화면 표시용. 저장에는 쓰지 않는다 */
    private String studentName;
    private String studentGrade;
    private String studentCode;

    /** 기본값을 두지 않는다. 아직 확인하지 않은 것과 출석은 다른 정보다 */
    private AttendanceStatus attendance;

    /** 학생 메모 - 그날 이 학생에게 눈에 띈 것 */
    private String note;
}
