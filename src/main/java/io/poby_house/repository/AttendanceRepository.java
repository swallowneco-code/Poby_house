package io.poby_house.repository;

import io.poby_house.domain.Attendance;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface AttendanceRepository extends JpaRepository<Attendance, Long> {

    /** 회차 화면의 학생 한 줄씩. 이름을 바로 쓰므로 학생을 함께 가져온다 */
    @EntityGraph(attributePaths = "student")
    List<Attendance> findBySessionId(Long sessionId);

    /**
     * 학생의 출결 이력. 날짜와 반 이름을 화면에서 쓰므로 회차와 그 반을 함께 가져온다.
     * 이것을 빼면 행 수만큼 회차 조회가 다시 나간다.
     * 숨긴 회차의 출결도 함께 나온다. 화면에서 걸러 쓸지는 쓰는 쪽이 정한다.
     */
    @EntityGraph(attributePaths = {"session", "session.classGroup"})
    List<Attendance> findByStudentIdOrderBySessionSessionDateDesc(Long studentId);

    /**
     * 기간 안의 상태별 개수. 리포트 1-2 "함께한 시간" 이 이 집계를 쓴다.
     * 상태 5개마다 count 를 돌리는 대신 group by 한 방으로 센다.
     *
     * 숨긴 회차는 제외한다. 잘못 열어 둔 회차의 출결이 섞이면 출석률이 거짓말을 한다.
     *
     * @return [AttendanceStatus, Long] 행 목록. 0건인 상태는 아예 행이 없다
     */
    @Query("""
           select a.status, count(a)
           from Attendance a
           where a.student.id = :studentId
             and a.session.hiddenAt is null
             and a.session.sessionDate between :from and :to
           group by a.status
           """)
    List<Object[]> countByStatusForStudent(@Param("studentId") Long studentId,
                                          @Param("from") LocalDate from,
                                          @Param("to") LocalDate to);
}
