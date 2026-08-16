package io.poby_house.dto;

import io.poby_house.domain.ClassGroup;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalTime;

/**
 * @Size 는 DB 의 VARCHAR 길이와 맞춘다. 이걸 빼면 긴 값이 저장 시점에 MySQL 오류(1406)로 튀어서
 * 한국어 필드 오류 대신 500 화면이 뜨고 입력한 것이 전부 날아간다.
 * "중1 심화 - 개념원리 진행반(화목 저녁)" 처럼 설명을 붙인 반 이름은 50자를 쉽게 넘긴다.
 */
@Getter
@Setter
@NoArgsConstructor
public class ClassGroupForm {

    @NotBlank(message = "반 이름을 입력하세요")
    @Size(max = 50, message = "반 이름은 50자까지 입력할 수 있습니다")
    private String name;

    @Size(max = 50, message = "레벨은 50자까지 입력할 수 있습니다")
    private String level;

    @Size(max = 20, message = "요일은 20자까지 입력할 수 있습니다")
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
