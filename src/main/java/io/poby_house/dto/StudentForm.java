package io.poby_house.dto;

import io.poby_house.domain.Student;
import io.poby_house.domain.StudentStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
public class StudentForm {

    @NotBlank(message = "이름을 입력하세요")
    private String name;

    private String school;

    private String grade;

    @NotNull(message = "등록일을 선택하세요")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate enrolledOn = LocalDate.now();

    private StudentStatus status = StudentStatus.ACTIVE;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate leftOn;

    private String leaveReason;

    private String memo;

    public static StudentForm from(Student student) {
        StudentForm form = new StudentForm();
        form.setName(student.getName());
        form.setSchool(student.getSchool());
        form.setGrade(student.getGrade());
        form.setEnrolledOn(student.getEnrolledOn());
        form.setStatus(student.getStatus());
        form.setLeftOn(student.getLeftOn());
        form.setLeaveReason(student.getLeaveReason());
        form.setMemo(student.getMemo());
        return form;
    }
}
