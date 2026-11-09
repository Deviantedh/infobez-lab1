package itmo.security.lab1.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import itmo.security.lab1.dto.DataDto;
import itmo.security.lab1.service.DataService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/data")
@RequiredArgsConstructor
@SecurityRequirement(name = "BearerAuth")
@Tag(name = "Secure Data", description = "Защищенные эндпоинты, требующие валидный JWT токен")
public class DataController {

    private final DataService dataService;

    @GetMapping
    @Operation(summary = "Получение списка защищенных данных (Метод 2)",
            description = "Доступен ТОЛЬКО аутентифицированным пользователям с валидным JWT токеном")
    public ResponseEntity<List<DataDto.Response>> getAllData() {
        return ResponseEntity.ok(dataService.getAllRecords());
    }

    @PostMapping
    @Operation(summary = "Создание новой защищенной записи (Метод 3)",
            description = "Принимает валидированные данные, производит экранирование от XSS и сохраняет в БД через JPA")
    public ResponseEntity<DataDto.Response> createData(@Valid @RequestBody DataDto.CreateRequest request,
                                                       Authentication authentication) {
        DataDto.Response created = dataService.createRecord(request, authentication.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Получение записи по ID",
            description = "Параметризованный запрос через Spring Data JPA (защита от SQL-инъекций)")
    public ResponseEntity<DataDto.Response> getById(@PathVariable Long id) {
        return ResponseEntity.ok(dataService.getById(id));
    }
}
