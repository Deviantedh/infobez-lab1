package itmo.security.lab1.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;

public class AuthDto {

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "Запрос на аутентификацию")
    public static class LoginRequest {
        @NotBlank(message = "Имя пользователя не должно быть пустым")
        @Size(min = 3, max = 50, message = "Логин должен быть от 3 до 50 символов")
        @Pattern(regexp = "^[a-zA-Z0-9_.-]+$", message = "Логин содержит недопустимые символы")
        @Schema(example = "admin")
        private String username;

        @NotBlank(message = "Пароль не должен быть пустым")
        @Size(min = 6, max = 100, message = "Длина пароля должна быть от 6 до 100 символов")
        @Schema(example = "admin123")
        private String password;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "Запрос на регистрацию пользователя")
    public static class RegisterRequest {
        @NotBlank(message = "Имя пользователя не должно быть пустым")
        @Size(min = 3, max = 50, message = "Логин должен быть от 3 до 50 символов")
        @Pattern(regexp = "^[a-zA-Z0-9_.-]+$", message = "Логин содержит недопустимые символы")
        @Schema(example = "user1")
        private String username;

        @NotBlank(message = "Пароль не должен быть пустым")
        @Size(min = 6, max = 100, message = "Длина пароля должна быть от 6 до 100 символов")
        @Schema(example = "password123")
        private String password;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "Ответ с токеном аутентификации")
    public static class AuthResponse {
        @Schema(example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
        private String token;

        @Schema(example = "Bearer")
        private String tokenType;

        @Schema(example = "admin")
        private String username;

        @Schema(example = "ROLE_ADMIN")
        private String role;

        @Schema(example = "86400")
        private Long expiresInSeconds;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "Профиль пользователя")
    public static class UserProfile {
        private Long id;
        private String username;
        private String role;
        private String createdAt;
    }
}
