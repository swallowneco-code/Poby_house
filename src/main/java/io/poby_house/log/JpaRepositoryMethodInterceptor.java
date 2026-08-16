package io.poby_house.log;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * SQL 로그 앞에 "어느 기능에서 나간 쿼리인지"를 붙여 준다.
 *
 * 포인트컷을 service 로 잡는 이유가 둘이다.
 *   - Hibernate 는 트랜잭션이 끝날 때 flush 한다. 그건 리포지토리 메서드 <b>밖</b>이라,
 *     리포지토리에 걸면 INSERT/UPDATE 대부분이 이름표 없이 찍힌다.
 *   - 로그에는 StudentRepository.save 보다 StudentService.create 가 쓸모 있다.
 *
 * 예전에는 execution(* io.poby_house..*.*(..)) 이었다.
 * 이름은 JpaRepository 인데 실제로는 컨트롤러·SecurityConfig 같은 @Configuration 까지
 * 모든 빈을 프록시로 감쌌다. 이미 CGLIB 로 강화된 설정 클래스를 한 번 더 감싸는 것은
 * 알려진 위험 패턴이다.
 */
@Slf4j
@Aspect
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class JpaRepositoryMethodInterceptor {

    @Around("execution(* io.poby_house.service..*(..))")
    public Object tagQueries(ProceedingJoinPoint joinPoint) throws Throwable {
        // 서비스가 서비스를 부르면 안쪽이 끝날 때 바깥 이름표가 사라지면 안 된다.
        // 예전에는 @After 에서 무조건 지워서, 중첩 호출 뒤에 나간 쿼리는 이름표를 잃었다.
        String previous = JpaQueryContextHolder.get();
        JpaQueryContextHolder.set(joinPoint.getSignature().toShortString());
        try {
            return joinPoint.proceed();
        } finally {
            JpaQueryContextHolder.set(previous);
        }
    }
}
