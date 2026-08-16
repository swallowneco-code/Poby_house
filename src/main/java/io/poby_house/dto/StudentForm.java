package io.poby_house.dto;

import io.poby_house.domain.Student;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

/**
 * 학생의 사실 정보만 다룬다.
 *
 * 상태(재원·휴원·퇴원)와 퇴원일·사유는 일부러 빼 놓았다.
 * 예전에는 이 폼의 드롭다운에서 '퇴원'을 고르면 보관 만료일까지 세워지고
 * 반 배정은 그대로 남아 반 목록에 유령 학생이 생겼다.
 * 되돌리기 어려운 조작이 "학교 이름 고치는 화면"에 숨어 있으면 안 된다.
 * 그 조작은 /students/{id}/leave · pause · resume 으로 나갔다.
 *
 * @Size 는 DB 의 VARCHAR 길이와 맞춘다. 이걸 빼면 긴 이름이 저장 시점에
 * MySQL 오류로 튀어서 입력한 것이 전부 날아간다.
 */
@Getter
@Setter
@NoArgsConstructor
public class StudentForm {

    @NotBlank(message = "이름을 입력하세요")
    @Size(max = 50, message = "이름은 50자까지 입력할 수 있습니다")
    private String name;

    @Size(max = 50, message = "학교는 50자까지 입력할 수 있습니다")
    private String school;

    @Size(max = 20, message = "학년은 20자까지 입력할 수 있습니다")
    private String grade;

    @NotNull(message = "등록일을 선택하세요")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate enrolledOn = LocalDate.now();

    private String memo;

    public static StudentForm from(Student student) {
        StudentForm form = new StudentForm();
        form.setName(student.getName());
        form.setSchool(student.getSchool());
        form.setGrade(student.getGrade());
        form.setEnrolledOn(student.getEnrolledOn());
        form.setMemo(student.getMemo());
        return form;
    }
}
