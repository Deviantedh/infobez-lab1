package itmo.security.lab1.bootstrap;

import itmo.security.lab1.entity.DataRecord;
import itmo.security.lab1.entity.Role;
import itmo.security.lab1.entity.User;
import itmo.security.lab1.repository.DataRecordRepository;
import itmo.security.lab1.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final DataRecordRepository dataRecordRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (userRepository.count() == 0) {
            log.info("Инициализация начальных пользователей и данных...");

            User admin = User.builder()
                    .username("admin")
                    .password(passwordEncoder.encode("admin123"))
                    .role(Role.ROLE_ADMIN)
                    .build();
            userRepository.save(admin);

            User user = User.builder()
                    .username("user")
                    .password(passwordEncoder.encode("user123"))
                    .role(Role.ROLE_USER)
                    .build();
            userRepository.save(user);

            DataRecord doc1 = DataRecord.builder()
                    .title("Конфигурация защищенного шлюза")
                    .content("Параметры TLS 1.3, разрешенные шифронаборы: TLS_AES_256_GCM_SHA384")
                    .classificationLevel("CONFIDENTIAL")
                    .createdBy("admin")
                    .build();
            dataRecordRepository.save(doc1);

            DataRecord doc2 = DataRecord.builder()
                    .title("Регламент реагирования на инциденты ИБ")
                    .content("Алгоритм изоляции скомпрометированных узлов сети и оповещения дежурного инженера")
                    .classificationLevel("INTERNAL")
                    .createdBy("user")
                    .build();
            dataRecordRepository.save(doc2);

            log.info("Тестовые пользователи созданы: admin (admin123), user (user123)");
        }
    }
}
