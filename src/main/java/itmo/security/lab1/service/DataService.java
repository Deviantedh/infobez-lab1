package itmo.security.lab1.service;

import itmo.security.lab1.dto.DataDto;
import itmo.security.lab1.entity.DataRecord;
import itmo.security.lab1.repository.DataRecordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.HtmlUtils;

import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DataService {

    private final DataRecordRepository dataRecordRepository;

    @Transactional(readOnly = true)
    public List<DataDto.Response> getAllRecords() {
        return dataRecordRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public DataDto.Response getById(Long id) {
        DataRecord record = dataRecordRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Запись с ID " + id + " не найдена"));
        return mapToResponse(record);
    }

    @Transactional
    public DataDto.Response createRecord(DataDto.CreateRequest request, String username) {
        // Защита от Stored XSS: санитизация/экранирование HTML-тегов перед сохранением
        String sanitizedTitle = HtmlUtils.htmlEscape(request.getTitle().trim());
        String sanitizedContent = HtmlUtils.htmlEscape(request.getContent().trim());

        DataRecord record = DataRecord.builder()
                .title(sanitizedTitle)
                .content(sanitizedContent)
                .classificationLevel(request.getClassificationLevel())
                .createdBy(username)
                .build();

        DataRecord saved = dataRecordRepository.save(record);
        return mapToResponse(saved);
    }

    private DataDto.Response mapToResponse(DataRecord entity) {
        return DataDto.Response.builder()
                .id(entity.getId())
                .title(entity.getTitle())
                .content(entity.getContent())
                .classificationLevel(entity.getClassificationLevel())
                .createdBy(entity.getCreatedBy())
                .createdAt(entity.getCreatedAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
                .build();
    }
}
