package io.poby_house.repository;

import io.poby_house.domain.ClassSession;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ClassSessionRepository extends JpaRepository<ClassSession, Long> {

    /**
     * 반 x 날짜로 회차 하나. (classGroupId, sessionDate) 가 유니크라 하나뿐이다.
     * 숨긴 회차도 걸린다. 같은 날짜로 다시 열면 그 회차를 이어 쓰고 숨김만 풀어야 하기 때문이다.
     * 여기서 숨김을 걸러 내면 유니크 제약에 걸려 새 회차를 열 수 없다.
     */
    Optional<ClassSession> findByClassGroupIdAndSessionDate(Long classGroupId, LocalDate sessionDate);

    /** 이 반의 회차 목록. 화면에서 담당 강사를 함께 쓰므로 같이 가져온다 */
    @EntityGraph(attributePaths = "teacher")
    List<ClassSession> findByClassGroupIdAndHiddenAtIsNullOrderBySessionDateDesc(Long classGroupId);

    /** 회차가 이미 열려 있나. 존재 확인에 목록을 만들지 않는다 */
    long countByClassGroupIdAndSessionDate(Long classGroupId, LocalDate sessionDate);
}
