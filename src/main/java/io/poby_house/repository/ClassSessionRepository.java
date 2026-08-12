package io.poby_house.repository;

import io.poby_house.domain.ClassSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ClassSessionRepository extends JpaRepository<ClassSession, Long> {

    List<ClassSession> findByClassGroupIdOrderBySessionDateDesc(Long classGroupId);

    /**
     * 실제로 뭔가 적힌 회차만. '기록하기'만 누르고 나온 빈 껍데기는 목록에 내보내지 않는다.
     * 같은 날짜로 다시 들어오면 그 회차를 그대로 이어서 쓰므로 사라져도 문제없다.
     */
    @Query("""
            select s from ClassSession s
            where s.classGroup.id = :classGroupId
              and s.hiddenAt is null
              and (
                   s.dailyNote is not null
                or s.textbook  is not null
                or s.rangeText is not null
                or s.content   is not null
                or s.homework  is not null
                or exists (select 1 from Attendance  a where a.session = s)
                or exists (select 1 from ProgressLog p where p.session = s)
              )
            order by s.sessionDate desc
            """)
    List<ClassSession> findRecordedByClassGroupId(@Param("classGroupId") Long classGroupId);

    Optional<ClassSession> findByClassGroupIdAndSessionDate(Long classGroupId, LocalDate sessionDate);

    boolean existsByClassGroupIdAndSessionDate(Long classGroupId, LocalDate sessionDate);

    /** 이 회차 직전 회차. 지난 진도와 과제를 불러올 때 쓴다 */
    Optional<ClassSession> findFirstByClassGroupIdAndSessionDateLessThanOrderBySessionDateDesc(
            Long classGroupId, LocalDate sessionDate);
}
