package io.poby_house.service;

import io.poby_house.domain.ObservationKind;
import io.poby_house.domain.StudentStatus;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

/**
 * 홈 대시보드 전용 집계 조회.
 *
 * 대시보드가 필요한 것은 엔티티 목록이 아니라 "밀린 것" 의 집계다.
 * 이걸 각 기능의 리포지토리에 끼워 넣으면 여섯 개 인터페이스가 대시보드 사정으로 오염되고,
 * 다른 에이전트가 같은 파일을 동시에 고치게 된다. 그래서 여기 한 곳에 모은다.
 *
 * 모든 메서드는 학생 수 · 반 수와 무관하게 쿼리 한 번이다.
 * 학생마다 조회를 돌리면 로그인 직후 착지 화면이 앱에서 가장 느린 화면이 된다.
 */
@Repository
@RequiredArgsConstructor
public class DashboardQueryRepository {

    private final EntityManager em;

    /**
     * 주어진 반들의 그 날짜 회차와 출결 개수.
     *
     * @return [classGroupId(Long), sessionId(Long), attendanceCount(Long)] / 회차가 없는 반은 행이 없다
     */
    public List<Object[]> sessionsOn(LocalDate date, Collection<Long> classGroupIds) {
        if (classGroupIds.isEmpty()) {
            // in () 는 문법 오류다. 오늘 수업이 없는 날에 홈 화면 전체가 500 이 되면 안 된다
            return List.of();
        }
        return em.createQuery("""
                        select s.classGroup.id, s.id,
                               (select count(a) from Attendance a where a.session = s)
                        from ClassSession s
                        where s.sessionDate = :date
                          and s.hiddenAt is null
                          and s.classGroup.id in (:ids)
                        """, Object[].class)
                .setParameter("date", date)
                .setParameter("ids", classGroupIds)
                .getResultList();
    }

    /**
     * 열렸지만 출결이 한 건도 없는 회차.
     *
     * 오래된 것이 위로 온다. 출결은 그날이 지나면 기억으로 복원할 수 없어서,
     * 가장 오래 방치된 회차가 가장 먼저 영구히 비어 버린다.
     *
     * @return [sessionId(Long), classGroupId(Long), className(String), sessionDate(LocalDate)]
     */
    public List<Object[]> sessionsWithoutAttendance(LocalDate from, LocalDate to) {
        return em.createQuery("""
                        select s.id, s.classGroup.id, s.classGroup.name, s.sessionDate
                        from ClassSession s
                        where s.hiddenAt is null
                          and s.sessionDate between :from and :to
                          and not exists (select 1 from Attendance a where a.session = s)
                        order by s.sessionDate asc
                        """, Object[].class)
                .setParameter("from", from)
                .setParameter("to", to)
                .getResultList();
    }

    /**
     * 입회 기준선을 가진 학생 id.
     *
     * "없는 쪽" 을 SQL 로 뽑지 않는 이유: 재원생 목록은 어차피 화면에 이름을 쓰려고 이미 읽는다.
     * 있는 쪽 id 만 받아 와 메모리에서 빼면 쿼리 한 번으로 끝난다.
     */
    public List<Long> studentIdsWithBaseline() {
        return em.createQuery("""
                        select distinct o.student.id
                        from Observation o
                        where o.kind = :kind
                        """, Long.class)
                .setParameter("kind", ObservationKind.BASELINE)
                .getResultList();
    }

    /**
     * 결과를 확인하지 않은 개입.
     *
     * result is null 만 본다. ONGOING 은 "확인했고 진행중" 이라 재촉할 대상이 아니다.
     * 퇴원생도 제외하지 않는다. 리포트를 쓰는 시점이 바로 퇴원 직후이므로
     * 그때 결과가 빈 개입은 리포트 3-1 에 한 줄도 못 나간다.
     *
     * @return [interventionId(Long), studentId(Long), studentName(String), problem(String), triedOn(LocalDate)]
     */
    public List<Object[]> unreviewedInterventions(LocalDate triedOnOrBefore) {
        return em.createQuery("""
                        select i.id, i.student.id, i.student.name, i.problem, i.triedOn
                        from Intervention i
                        where i.result is null
                          and i.triedOn <= :cutoff
                        order by i.triedOn asc
                        """, Object[].class)
                .setParameter("cutoff", triedOnOrBefore)
                .getResultList();
    }

    /**
     * 보관 만료가 임박했거나 이미 지난 퇴원생.
     *
     * StudentRepository.findPurgeTargets 는 이미 지난 것만 준다.
     * 대시보드는 "지나기 전에" 알려야 하므로 예고 기간까지 함께 본다.
     *
     * status 를 함께 거는 이유: 재원 복귀 처리가 purgeAfter 를 지우지 못한 데이터가 있으면
     * 다니는 학생이 파기 목록에 오른다. 그것은 사고다.
     *
     * @return [studentId(Long), name(String), code(String), purgeAfter(LocalDate)]
     */
    public List<Object[]> purgeDueStudents(LocalDate until) {
        return em.createQuery("""
                        select s.id, s.name, s.code, s.purgeAfter
                        from Student s
                        where s.status = :left
                          and s.purgeAfter is not null
                          and s.purgeAfter <= :until
                        order by s.purgeAfter asc
                        """, Object[].class)
                .setParameter("left", StudentStatus.LEFT)
                .setParameter("until", until)
                .getResultList();
    }

    /**
     * 그 기간에 장면 기록이 하나도 없는 재원생 수.
     * 이름 목록을 만들지 않고 센다. 화면에 필요한 것은 숫자와 링크 하나다.
     */
    public long countActiveStudentsWithoutScene(LocalDate from, LocalDate to) {
        return em.createQuery("""
                        select count(s)
                        from Student s
                        where s.status = :active
                          and not exists (select 1 from Scene sc
                                          where sc.student = s
                                            and sc.occurredOn between :from and :to)
                        """, Long.class)
                .setParameter("active", StudentStatus.ACTIVE)
                .setParameter("from", from)
                .setParameter("to", to)
                .getSingleResult();
    }
}
