package io.poby_house.service;

import io.poby_house.domain.ProgressLog;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

/**
 * 리포트 전용 조회. 기존 리포지토리 인터페이스를 건드리지 않기 위해 따로 둔다.
 *
 * 리포트만 필요한 조건(기간으로 자르기, 반 소속 기간과 회차 날짜를 함께 보기)을
 * 공용 리포지토리에 밀어 넣으면, 다른 화면들이 쓰는 메서드 목록이 리포트 사정으로 늘어난다.
 * 인터페이스가 아니라 클래스인 이유는 EntityManager 를 직접 들고 JPQL 을 쓰기 때문이다.
 *
 * 두 메서드 모두 <b>숨긴 회차를 제외</b>한다. 잘못 열어 둔 회차가 섞이면 리포트의 숫자가 거짓말을 한다.
 */
@Repository
@RequiredArgsConstructor
public class ReportQueryRepository {

    private final EntityManager em;

    /**
     * 이 학생이 소속돼 있던 반이 그 기간에 열었던 회차 수.
     *
     * 학생의 출결 행 수와 다른 값이다. 그 차이가 "출결을 표시하지 않은 회차" 이고,
     * 리포트는 그 차이를 감추지 않는다. 감추면 출결을 반만 적어도 출석률이 100% 로 보인다.
     *
     * 소속 기간(startedOn ~ endedOn)을 회차 날짜와 함께 본다.
     * 이 조건을 빼면 반을 옮긴 학생에게 옮기기 전 반의 회차까지 붙는다.
     */
    public long countHeldSessions(Long studentId, LocalDate from, LocalDate to) {
        return em.createQuery("""
                        select count(distinct s.id)
                        from ClassSession s
                          join Enrollment e on e.classGroup = s.classGroup
                        where e.student.id = :studentId
                          and s.hiddenAt is null
                          and s.sessionDate between :from and :to
                          and s.sessionDate >= e.startedOn
                          and (e.endedOn is null or s.sessionDate <= e.endedOn)
                        """, Long.class)
                .setParameter("studentId", studentId)
                .setParameter("from", from)
                .setParameter("to", to)
                .getSingleResult();
    }

    /**
     * 기간 안의 진도기록. <b>날짜 오름차순</b>이다.
     *
     * 오름차순인 것이 중요하다. 진도 구간 압축은 앞에서 뒤로 읽으면서 교재가 바뀌는 지점을 끊는다.
     * 내림차순으로 받아 뒤집으면 정렬이 한 번 더 돌고, 뒤집는 것을 잊으면 구간이 거꾸로 나온다.
     *
     * 회차를 함께 가져온다(날짜를 쓴다). 이걸 빼면 기록 수만큼 회차 조회가 다시 나간다.
     */
    public List<ProgressLog> findProgressInPeriod(Long studentId, LocalDate from, LocalDate to) {
        return em.createQuery("""
                        select p
                        from ProgressLog p
                          join fetch p.session s
                        where p.student.id = :studentId
                          and s.hiddenAt is null
                          and s.sessionDate between :from and :to
                        order by s.sessionDate asc
                        """, ProgressLog.class)
                .setParameter("studentId", studentId)
                .setParameter("from", from)
                .setParameter("to", to)
                .getResultList();
    }
}
