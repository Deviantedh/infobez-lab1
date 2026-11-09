package itmo.security.lab1.repository;

import itmo.security.lab1.entity.DataRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DataRecordRepository extends JpaRepository<DataRecord, Long> {
    List<DataRecord> findAllByOrderByCreatedAtDesc();
    List<DataRecord> findByCreatedBy(String createdBy);
}
