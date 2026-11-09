package itmo.security.lab1;

import com.fasterxml.jackson.databind.ObjectMapper;
import itmo.security.lab1.dto.AuthDto;
import itmo.security.lab1.dto.DataDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class SecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("1. Попытка несанкционированного доступа к /api/data без токена должна вернуть 401 Unauthorized")
    void testAccessDataWithoutTokenReturnsUnauthorized() throws Exception {
        mockMvc.perform(get("/api/data"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.error").value("Unauthorized"));
    }

    @Test
    @DisplayName("2. Попытка входа с неверным паролем должна вернуть 401 Unauthorized")
    void testLoginWithWrongPasswordReturnsUnauthorized() throws Exception {
        AuthDto.LoginRequest request = AuthDto.LoginRequest.builder()
                .username("admin")
                .password("wrong_password")
                .build();

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Неверный логин или пароль"));
    }

    @Test
    @DisplayName("3. Аутентификация с корректными данными должна выдать валидный JWT токен")
    void testLoginWithValidCredentialsReturnsJwtToken() throws Exception {
        AuthDto.LoginRequest request = AuthDto.LoginRequest.builder()
                .username("admin")
                .password("admin123")
                .build();

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.username").value("admin"))
                .andExpect(jsonPath("$.role").value("ROLE_ADMIN"));
    }

    @Test
    @DisplayName("4. Доступ к защищенному эндпоинту /api/data с валидным JWT токеном должен вернуть 200 OK")
    void testAccessDataWithValidTokenReturnsOk() throws Exception {
        String token = obtainJwtToken("admin", "admin123");

        mockMvc.perform(get("/api/data")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$[0].title").isNotEmpty())
                .andExpect(jsonPath("$[0].content").isNotEmpty());
    }

    @Test
    @DisplayName("5. Доступ с поддельным или поврежденным JWT токеном должен вернуть 401 Unauthorized")
    void testAccessWithForgedTokenReturnsUnauthorized() throws Exception {
        String forgedToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.e30.forgedSignature";

        mockMvc.perform(get("/api/data")
                        .header("Authorization", "Bearer " + forgedToken))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("6. Защита от SQL-инъекций: инъекция в логин не должна обходить аутентификацию")
    void testSqlInjectionInLoginFailsSafely() throws Exception {
        AuthDto.LoginRequest sqliRequest = AuthDto.LoginRequest.builder()
                .username("admin' OR '1'='1")
                .password("any")
                .build();

        // Валидация @Pattern перехватит недопустимые символы, возвращая 400 Bad Request
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sqliRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.details.username").exists());
    }

    @Test
    @DisplayName("7. Защита от XSS: HTML теги должны санитизироваться и экранироваться")
    void testXssPayloadSanitization() throws Exception {
        String token = obtainJwtToken("admin", "admin123");

        DataDto.CreateRequest createRequest = DataDto.CreateRequest.builder()
                .title("<script>alert('xss')</script>")
                .content("<img src=x onerror=alert(1)> Безопасный текст")
                .classificationLevel("INTERNAL")
                .build();

        mockMvc.perform(post("/api/data")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("&lt;script&gt;alert(&#39;xss&#39;)&lt;/script&gt;"))
                .andExpect(jsonPath("$.content").value("&lt;img src=x onerror=alert(1)&gt; Безопасный текст"));
    }

    @Test
    @DisplayName("8. Проверка защитных HTTP-заголовков (Content-Security-Policy, X-Frame-Options, X-XSS-Protection)")
    void testSecurityHeaders() throws Exception {
        mockMvc.perform(get("/auth/login"))
                .andExpect(header().string("Content-Security-Policy", "default-src 'self'"))
                .andExpect(header().string("X-Frame-Options", "SAMEORIGIN"))
                .andExpect(header().string("X-XSS-Protection", "1; mode=block"));
    }

    private String obtainJwtToken(String username, String password) throws Exception {
        AuthDto.LoginRequest request = AuthDto.LoginRequest.builder()
                .username(username)
                .password(password)
                .build();

        MvcResult result = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andReturn();

        AuthDto.AuthResponse response = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                AuthDto.AuthResponse.class
        );
        return response.getToken();
    }
}
