package io.poby_house.dto;

import io.poby_house.domain.ClassGroup;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalTime;

@Getter
@Setter
@NoArgsConstructor
public class ClassGroupForm {

    @NotBlank(message = "반 이름을 입력하세요")
    private String name;

    private String level;

    private String dayOfWeek;

    @DateTimeFormat(pattern = "HH:mm")
    private LocalTime startTime;

    @DateTimeFormat(pattern = "HH:mm")
    private LocalTime endTime;

    private boolean active = true;

    public static ClassGroupForm from(ClassGroup group) {
        ClassGroupForm form = new ClassGroupForm();
        form.setName(group.getName());
        form.setLevel(group.getLevel());
        form.setDayOfWeek(group.getDayOfWeek());
        form.setStartTime(group.getStartTime());
        form.setEndTime(group.getEndTime());
        form.setActive(group.isActive());
        return form;
    }
}
