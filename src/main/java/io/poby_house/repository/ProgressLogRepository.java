package io.poby_house.repository;

import io.poby_house.domain.ProgressLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProgressLogRepository extends JpaRepository<ProgressLog, Long> {

    List<ProgressLog> findBySessionId(Long sessionId);

    Optional<ProgressLog> findBySessionIdAndStudentId(Long sessionId, Long studentId);

    /** 학생 상세에서 최근 기록을 보여줄 때 */
    List<ProgressLog> findByStudentIdOrderBySessionSessionDateDesc(Long studentId);
}
