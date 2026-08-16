package io.poby_house.repository;

import io.poby_house.domain.Student;
import io.poby_house.domain.StudentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface StudentRepository extends JpaRepository<Student, Long> {

    List<Student> findByStatusOrderByNameAsc(StudentStatus status);

    /**
     * 어느 반에도 속하지 않은 재원생. 배정 후보다.
     * 예전에는 현재 배정 전체를 메모리로 끌어와 걸러 냈다.
     * Enrollment 는 student·classGroup·teacher 를 함께 물고 오므로
     * 인원수 하나 세려고 학생과 반 전체를 로딩하는 꼴이었다.
     */
    @Query("""
           select s from Student s
           where s.status = io.poby_house.domain.StudentStatus.ACTIVE
             and not exists (select 1 from Enrollment e where e.student = s and e.endedOn is null)
           order by s.name asc
           """)
    List<Student> findAssignable();

    @Query("""
           select count(s) from Student s
           where s.status = io.poby_house.domain.StudentStatus.ACTIVE
             and not exists (select 1 from Enrollment e where e.student = s and e.endedOn is null)
           """)
    long countAssignable();

    /**
     * 상태와 검색어를 함께 건다.
     * 둘을 따로 두면 검색할 때 상태 필터가 버려져서, 재원생을 찾는데 퇴원생까지 나온다.
     *
     * 검색 대상에 code 와 school 을 넣는다. 목록이 식별번호를 보여 주면서
     * 그 번호로는 못 찾는 것이 이상하다.
     *
     * 정렬은 status 의 선언 순서(재원 → 휴원 → 퇴원)를 따른다.
     * 그냥 order by status 로 두면 문자열 정렬이라 ACTIVE → LEFT → PAUSED,
     * 즉 퇴원생이 휴원생보다 위로 올라온다.
     *
     * 페이지로 돌려준다. 예전에는 전건을 List 로 올리고 화면이 #lists.size 로 총원을 셌다.
     * 학생이 몇 백 명이 되면 폰에서 목록 한 번 여는 데 전건이 다 올라온다.
     * 총원은 아래 countQuery 로 센다.
     *
     * countQuery 를 손으로 적는 이유: 본문에 order by 와 case 식이 있어
     * 자동 생성 count 쿼리를 신뢰할 수 없다. 정렬 조건이 없는 형태로 직접 준다.
     */
    @Query(value = """
           select s from Student s
           where (:status is null or s.status = :status)
             and (:keyword is null
                  or s.name   like concat('%', :keyword, '%')
                  or s.code   like concat('%', :keyword, '%')
                  or s.school like concat('%', :keyword, '%'))
           order by case s.status
                        when io.poby_house.domain.StudentStatus.ACTIVE then 0
                        when io.poby_house.domain.StudentStatus.PAUSED then 1
                        else 2
                    end,
                    s.name asc
           """,
           countQuery = """
           select count(s) from Student s
           where (:status is null or s.status = :status)
             and (:keyword is null
                  or s.name   like concat('%', :keyword, '%')
                  or s.code   like concat('%', :keyword, '%')
                  or s.school like concat('%', :keyword, '%'))
           """)
    Page<Student> search(@Param("status") StudentStatus status,
                         @Param("keyword") String keyword,
                         Pageable pageable);

    /**
     * 보관 만료일이 지난 퇴원생. 오래 방치된 것이 위로 온다.
     *
     * status 를 함께 거는 이유: 재원 복귀(resume)가 purgeAfter 를 지우므로 보통 LEFT 뿐이지만,
     * 손으로 손질한 데이터에 값이 남아 있을 수 있다.
     * 다니는 학생이 파기 목록에 오르는 것은 사고다.
     */
    @Query("""
           select s from Student s
           where s.status = io.poby_house.domain.StudentStatus.LEFT
             and s.purgeAfter is not null
             and s.purgeAfter <= :today
           order by s.purgeAfter asc
           """)
    List<Student> findPurgeTargets(@Param("today") LocalDate today);

    /**
     * 올해 발급된 마지막 일련번호. 다음 번호를 뽑을 때 쓴다.
     *
     * 문자열 정렬로 마지막 code 를 찾으면 999 다음이 깨진다.
     * S2026999 와 S20261000 을 문자로 비교하면 앞의 것이 더 크다고 나와
     * 같은 번호를 무한히 다시 발급하려 든다. 숫자로 뽑는다.
     */
    @Query("""
           select max(cast(substring(s.code, :from) as integer))
           from Student s
           where s.code like concat(:prefix, '%')
           """)
    Integer maxCodeSequence(@Param("prefix") String prefix, @Param("from") int from);
}
