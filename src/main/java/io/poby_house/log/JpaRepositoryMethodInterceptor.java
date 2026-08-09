package io.poby_house.log;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

@Slf4j
@Aspect
@Component
public class JpaRepositoryMethodInterceptor {
    
    @Pointcut("execution(* io.poby_house..*.*(..))")
    public void jpaRepositoryMethods() {}
    
    @Before("jpaRepositoryMethods()")
    public void beforeQuery(JoinPoint joinPoint) {
        String methodSignature = joinPoint.getSignature().toShortString();
        JpaQueryContextHolder.set(methodSignature);
    }
    
    @After("jpaRepositoryMethods()")
    public void afterQuery() {
        JpaQueryContextHolder.clear();
    }
}