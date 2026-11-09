# Лабораторная работа №1: Разработка защищенного REST API с интеграцией в CI/CD

> **Дисциплина:** Информационная безопасность  
> **Университет:** Федеральное государственное автономное образовательное учреждение высшего образования «Национальный исследовательский университет ИТМО»  
> **Факультет / Группа:** ПИиКТ / P3432  
> **Студент:** Носов Григорий Игоревич  
> **Преподаватель:** Маркина Т.А.  

---

## 1. Описание проекта

Проект представляет собой легковесный, высокозащищенный REST API сервис на базе **Java 21** и **Spring Boot 3.3.5**, разработанный с соблюдением стандартов безопасности **OWASP Top 10** и интегрированный с автоматизированными сканерами безопасности в **CI/CD Pipeline** (GitHub Actions).

Сервис реализует функционал безопасной аутентификации пользователей и управление защищенными корпоративными записями данных с различными уровнями конфиденциальности.

### Стек технологий:
* **Язык:** Java 21 LTS
* **Фреймворк:** Spring Boot 3.3.5
* **Безопасность:** Spring Security 6 (Stateless JWT, BCrypt, Security Headers)
* **JWT библиотека:** JJWT (`io.jsonwebtoken`) 0.12.6
* **База данных:** H2 (In-Memory / PostgreSQL mode) с поддержкой PostgreSQL
* **ORM:** Spring Data JPA / Hibernate (Prepared Statements)
* **Документация:** OpenAPI 3 / Swagger UI (`springdoc-openapi` 2.6.0)
* **SAST (Static Application Security Testing):** SpotBugs 4.8.6 + FindSecBugs 1.13.0
* **SCA (Software Composition Analysis):** OWASP Dependency-Check 10.0.4

---

## 2. Спецификация REST API

Интерактивная документация Swagger UI доступна по адресу:  
`http://localhost:8080/swagger-ui.html`  
Спецификация OpenAPI (JSON): `http://localhost:8080/v3/api-docs`

### Таблица эндпоинтов:

| Метод | Путь | Доступ | Назначение / Описание |
|---|---|---|---|
| `POST` | `/auth/login` | Публичный | **Метод 1:** Аутентификация пользователя (логин и пароль), выдача JWT Bearer токена |
| `POST` | `/auth/register` | Публичный | Регистрация нового пользователя с валидацией и хэшированием пароля |
| `GET` | `/auth/me` | JWT | Получение информации о профиле текущего пользователя |
| `GET` | `/api/data` | JWT | **Метод 2:** Получение списка защищенных записей данных |
| `POST` | `/api/data` | JWT | **Метод 3:** Добавление новой защищенной записи с санитизацией |
| `GET` | `/api/data/{id}` | JWT | Получение конкретной записи данных по первичному ключу |

---

## 3. Подробное описание реализованных мер защиты (OWASP Top 10)

### 3.1. Защита от Broken Authentication (A07:2021)
1. **Хэширование паролей с высокой стойкостью:**
   * В открытом виде пароли в системе **никогда не хранятся**.
   * Для хэширования используется алгоритм **BCrypt** (`BCryptPasswordEncoder`) с коэффициентом трудоемкости (work factor / strength) **12**.
   * BCrypt использует адаптивную функцию ключевого расписания (key derivation function) и криптографическую соль (salt), генерируемую индивидуально для каждого пароля. Это полностью нивелирует угрозу атак по предвычисленным радужным таблицам (Rainbow Tables) и аппаратному брутфорсу на GPU/ASIC.
2. **Аутентификация через JSON Web Token (JWT):**
   * После успешной проверки учетных данных клиенту выдается компактный криптографически подписанный токен JWT формата Bearer.
   * Для подписи используется алгоритм **HMAC-SHA256** с 256-битным криптостойким секретным ключом.
   * Токен имеет ограниченный срок действия (`expiration = 24 часа`).
3. **Безопасная архитектура сессий (Stateless):**
   * Серверная сессия отключена: `SessionCreationPolicy.STATELESS`.
   * Middleware `JwtAuthenticationFilter` наследует `OncePerRequestFilter`, перехватывает каждый входящий запрос, проверяет валидность цифровой подписи JWT и срока действия до передачи управления в контроллеры.
4. **Защита от перечисления пользователей (User Enumeration Prevention):**
   * При передаче несуществующего логина или неверного пароля возвращается стандартизированное одинаковое сообщение: *"Неверный логин или пароль"* с кодом HTTP `401 Unauthorized`. Время обработки запроса выравнивается проверкой хэша.

### 3.2. Защита от SQL-инъекций (SQL Injection, A03:2021)
1. **Параметризованные запросы через ORM:**
   * В проекте используется Spring Data JPA и Hibernate.
   * Конкатенация строк в SQL/HQL-запросах категорически исключена.
   * Все параметры запросов передаются через строго типизированные `Prepared Statements`, где СУБД разделяет компиляцию текста запроса и подстановку пользовательских литералов. Символы кавычек и спецсимволы синтаксиса SQL экранируются драйвером БД и не могут изменить логику исполнения запроса.
2. **Строгая валидация входных данных (Input Validation):**
   * На уровне DTO (`jakarta.validation`) применяются регулярные выражения, ограничивающие набор разрешенных символов (например, `@Pattern(regexp = "^[a-zA-Z0-9_.-]+$")`).

### 3.3. Защита от межсайтового скриптинга (Cross-Site Scripting, XSS, A03:2021)
1. **Санитизация и контекстное экранирование:**
   * При получении текстовых полей (`title`, `content`) сервис производит санитизацию через `HtmlUtils.htmlEscape(...)`, преобразуя потенциально опасные HTML-символы (`<`, `>`, `"`, `'`, `&`) в безопасные HTML-сущности (`&lt;`, `&gt;`, `&#39;`).
   * Это нейтрализует как отраженные (Reflected), так и хранимые (Stored) XSS-атаки.
2. **Защитные заголовки HTTP (Security Headers):**
   * В конфигурации `SecurityConfig` настроены строгие защитные заголовки браузера:
     * `Content-Security-Policy: default-src 'self'` — запрет загрузки внешних вредоносных скриптов.
     * `X-Frame-Options: SAMEORIGIN` — защита от атак Clickjacking.
     * `X-XSS-Protection: 1; mode=block` — принудительная блокировка страницы при обнаружении XSS.
     * `X-Content-Type-Options: nosniff` — предотвращение MIME-sniffing уязвимостей.

### 3.4. Защита от раскрытия чувствительной информации (Security Misconfiguration, A05:2021)
* Исключение утечки stack trace: `GlobalExceptionHandler` логирует полные технические стектрейсы только на защищенном сервере, отдавая наружу стандартизированные безопасные JSON-объекты ошибок без деталей внутренней инфраструктуры.

---

## 4. Настройка CI/CD Pipeline со сканерами безопасности

В корне репозитория настроен workflow для GitHub Actions: [`.github/workflows/ci.yml`](.github/workflows/ci.yml).

Pipeline автоматически запускается при каждом `push` и `pull_request` в ветки `main` и `master`, а также вручную через `workflow_dispatch`.

### Состав этапов (Jobs):
1. **Job 1: Build & Security Integration Tests**
   * Сборка проекта под JDK 21.
   * Выполнение набора из 8 сквозных интеграционных тестов безопасности (`SecurityIntegrationTest`), проверяющих:
     * Отказ в доступе без токена (HTTP 401).
     * Отказ при вводе неверного пароля (HTTP 401).
     * Корректную выдачу JWT при правильной аутентификации.
     * Доступ к закрытым данным по валидному Bearer токену.
     * Отклонение поддельного/поврежденного токена.
     * Нейтрализацию попыток SQL-инъекций.
     * Санитизацию вредоносных XSS-векторов.
     * Наличие защитных HTTP-заголовков.
2. **Job 2: SAST (SpotBugs + FindSecBugs)**
   * Статический анализ байткода приложения плагином `spotbugs-maven-plugin` с расширением `findsecbugs-plugin`.
   * Проверка на отсутствие уязвимостей криптографии, инъекций, небезопасного десериализования и утечек памяти.
   * Формирование и выгрузка артефакта отчета `spotbugsXml.xml`.
3. **Job 3: SCA (OWASP Dependency-Check)**
   * Анализ сторонних библиотек и транзитивных зависимостей на предмет известных уязвимостей (CVE) по базе NVD (National Vulnerability Database).
   * Формирование HTML-отчета `dependency-check-report.html` с оценкой рисков по шкале CVSS.

---

## 5. Инструкция по локальному запуску и тестированию

### Предварительные требования:
* Java 21+
* Maven 3.9+ (или использование `./mvnw`)

### Сборка и запуск тестов:
```bash
mvn clean test
```

### Запуск сканеров безопасности:
```bash
# Запуск SAST SpotBugs:
mvn spotbugs:check

# Запуск SCA OWASP Dependency-Check:
mvn org.owasp:dependency-check-maven:check
```

### Запуск приложения:
```bash
mvn spring-boot:run
```
Приложение запустится на порту `8080`. В базе данных автоматически будут созданы тестовые пользователи:
* **Администратор:** логин `admin`, пароль `admin123` (роль `ROLE_ADMIN`)
* **Пользователь:** логин `user`, пароль `user123` (роль `ROLE_USER`)

---

## 6. Примеры работы с API (cURL)

### 1) Аутентификация и получение JWT токена (`POST /auth/login`):
```bash
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username": "admin", "password": "admin123"}'
```
*Пример ответа (200 OK):*
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJhZG1pbiIsInJvbGUiOiJST0xFX0FETUlOIiwiaWF0IjoxNzAwMDAwMDAwLCJleHAiOjE3MDAwODY0MDB9...",
  "tokenType": "Bearer",
  "username": "admin",
  "role": "ROLE_ADMIN",
  "expiresInSeconds": 86400
}
```

### 2) Попытка доступа к закрытым данным БЕЗ токена (`GET /api/data`):
```bash
curl -i http://localhost:8080/api/data
```
*Результат: HTTP 401 Unauthorized*
```json
{
  "status": 401,
  "error": "Unauthorized",
  "message": "Для доступа требуется валидный JWT токен (Authorization: Bearer <token>)"
}
```

### 3) Успешный доступ к закрытым данным С токеном (`GET /api/data`):
```bash
curl -X GET http://localhost:8080/api/data \
  -H "Authorization: Bearer <ВАШ_ТОКЕН>"
```

### 4) Добавление защищенной записи с проверкой защиты от XSS (`POST /api/data`):
```bash
curl -X POST http://localhost:8080/api/data \
  -H "Authorization: Bearer <ВАШ_ТОКЕН>" \
  -H "Content-Type: application/json" \
  -d '{
    "title": "<script>alert(1)</script> Отчет",
    "content": "Секретный текст <img src=x onerror=alert(1)>",
    "classificationLevel": "CONFIDENTIAL"
  }'
```
*В ответе и базе данных опасные теги будут безопасно экранированы:*
```json
{
  "id": 3,
  "title": "&lt;script&gt;alert(1)&lt;/script&gt; Отчет",
  "content": "Секретный текст &lt;img src=x onerror=alert(1)&gt;",
  "classificationLevel": "CONFIDENTIAL",
  "createdBy": "admin"
}
```

---

## 7. Скриншоты отчетов SAST / SCA и Pipeline

> *Скриншоты успешного выполнения pipeline и генерации отчетов SpotBugs и Dependency-Check из вкладки **Actions** репозитория.*

* **Общий статус Pipeline:** `Actions -> CI/CD Security Pipeline (All checks passed)`
* **Отчет SAST (SpotBugs / FindSecBugs):** `target/spotbugsXml.xml` (0 критических уязвимостей).
* **Отчет SCA (OWASP Dependency-Check):** `target/dependency-check-report.html`.
