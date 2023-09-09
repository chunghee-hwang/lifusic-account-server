package com.chung.lifusic.account.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Aspect
@Component
@Slf4j
public class LoggingAspect {
    @AfterThrowing(pointcut = "execution(* com.chung.lifusic.account.controller.*.*(..))", throwing = "e")
    public void logAfterThrowing(JoinPoint joinPoint, Throwable e) {
        log.error("Controller error:: {}, signature: {}, target: {}", e.getMessage(), joinPoint.getSignature().getName(), joinPoint.getTarget());
    }

    @Around("execution(* com.chung.lifusic.account.controller.*.*(..))")
    public Object controllerAround(ProceedingJoinPoint joinPoint) throws Throwable {
        log.info("Controller before:: signature: {}, target: {}", joinPoint.getSignature().getName(), joinPoint.getTarget());
        Object result = joinPoint.proceed();
        log.info("Controller after:: signature: {}, target: {}", joinPoint.getSignature().getName(), joinPoint.getTarget());
        return result;
    }
}
