package itmo.security.lab1.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;

public class DataDto {

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "Запрос на сохранение защищенных данных")
    public static class CreateRequest {

        @NotBlank(message = "Заголовок не должен быть пустым")
        @Size(min = 3, max = 100, message = "Заголовок должен содержать от 3 до 100 символов")
        @Schema(example = "Отчет по инфобезу")
        private String title;

        @NotBlank(message = "Содержимое не должно быть пустым")
        @Size(min = 5, max = 1000, message = "Содержимое должно содержать от 5 до 1000 символов")
        @Schema(example = "Секретные данные проекта")
        private String content;

        @NotBlank(message = "Уровень секретности обязателен")
        @Pattern(regexp = "^(PUBLIC|INTERNAL|CONFIDENTIAL|RESTRICTED)$",
                message = "Уровень секретности должен быть одним из: PUBLIC, INTERNAL, CONFIDENTIAL, RESTRICTED")
        @Schema(example = "CONFIDENTIAL")
        private String classificationLevel;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "Защищенная запись данных")
    public static class Response {
        private Long id;
        private String title;
        private String content;
        private String classificationLevel;
        private String createdBy;
        private String createdAt;
    }
}
