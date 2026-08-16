package io.poby_house.dto;

import io.poby_house.domain.AttendanceStatus;
import io.poby_house.domain.HomeworkStatus;
import io.poby_house.domain.Student;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 회차 화면의 학생 한 줄. 출결 · 지난 과제 · 학생 메모가 전부다.
 *
 * 진도(교재/범위/오늘 한 것/과제)는 여기 없다. 반 공통이라 SessionRecordForm 위쪽에 한 벌만 있고,
 * 저장할 때 학생마다 ProgressLog 로 복사된다. 학생 수만큼 같은 문장을 다시 치게 하면 아무도 안 쓴다.
 */
@Getter
@Setter
@NoArgsConstructor
public class StudentEntryForm {

    private Long studentId;

    /* ---- 아래 셋은 화면 표시용이다. 저장에는 쓰지 않는다 ---- */

    private String studentName;
    private String studentGrade;
    private String studentCode;

    /**
     * 기본값을 두지 않는다. 아직 확인하지 않은 것과 출석은 다른 정보다.
     * null 이면 Attendance 행을 아예 만들지 않는다.
     */
    private AttendanceStatus attendance;

    /** 지난 시간에 내준 과제를 해 왔는가. 확인하지 않았으면 null 로 둔다 */
    private HomeworkStatus homeworkStatus;

    /**
     * 학생 메모 - 그날 이 학생에게 눈에 띈 것.
     * DB 가 VARCHAR(500) 이라 여기서 막지 않으면 저장 시점에 MySQL 오류로 튄다.
     */
    @Size(max = 500, message = "학생 메모는 500자까지 적을 수 있습니다.")
    private String note;

    /** 이름·학년·식별번호는 폼으로 되돌아오지 않는다. 화면을 그릴 때마다 명단에서 다시 붙인다 */
    public void applyStudent(Student student) {
        this.studentId = student.getId();
        this.studentName = student.getName();
        this.studentGrade = student.getGrade();
        this.studentCode = student.getCode();
    }
}
