package io.poby_house.repository;

import io.poby_house.domain.ProgressLog;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProgressLogRepository extends JpaRepository<ProgressLog, Long> {

    /**
     * 회차 x 학생 기록 한 건. (sessionId, studentId) 가 유니크다.
     * 회차 화면을 다시 저장할 때 새로 만들지 말고 이걸로 찾아 고친다.
     * 새로 만들면 유니크 제약에 걸려 저장이 통째로 실패한다.
     */
    Optional<ProgressLog> findBySessionIdAndStudentId(Long sessionId, Long studentId);

    /**
     * 한 회차의 진도기록 전부. 회차 화면이 학생 줄을 그릴 때 쓴다.
     *
     * 학생마다 findBySessionIdAndStudentId 를 부르면 회차 화면 한 번에
     * 학생 수만큼 쿼리가 나간다. 출결이 findBySessionId 로 한 번에 끝내는 것과 같은 모양이다.
     * student 를 함께 가져오지 않는 이유: 쓰는 쪽은 학생 id 로만 맵을 만들고,
     * LAZY 프록시의 id 접근은 쿼리를 내지 않는다.
     */
    List<ProgressLog> findBySessionId(Long sessionId);

    /** 학생의 진도 이력. 날짜와 반 이름을 쓰므로 회차와 그 반을 함께 가져온다 */
    @EntityGraph(attributePaths = {"session", "session.classGroup"})
    List<ProgressLog> findByStudentIdOrderBySessionSessionDateDesc(Long studentId);
}
