package io.poby_house.repository;

import io.poby_house.domain.Consultation;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ConsultationRepository extends JpaRepository<Consultation, Long> {

    /** 학생의 상담 이력. 카드마다 작성자를 쓰므로 함께 가져온다 */
    @EntityGraph(attributePaths = "createdBy")
    List<Consultation> findByStudentIdOrderByConsultedOnDesc(Long studentId);
}
