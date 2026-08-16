package io.poby_house.dto;

import io.poby_house.domain.Scene;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
public class SceneForm {

    @NotNull(message = "있었던 날을 선택하세요")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate occurredOn = LocalDate.now();

    /**
     * DB 가 VARCHAR(500) 이다. 여기서 막지 않으면 저장 시점에 MySQL 오류로 튀고
     * 적어 둔 장면이 통째로 날아간다.
     */
    @Size(max = 500, message = "상황은 500자까지 적을 수 있습니다")
    private String situation;

    @NotBlank(message = "무엇을 했고 무슨 말을 했는지 적으세요")
    private String content;

    public static SceneForm from(Scene scene) {
        SceneForm form = new SceneForm();
        form.setOccurredOn(scene.getOccurredOn());
        form.setSituation(scene.getSituation());
        form.setContent(scene.getContent());
        return form;
    }
}
