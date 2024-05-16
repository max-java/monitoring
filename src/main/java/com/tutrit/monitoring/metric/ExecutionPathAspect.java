package com.tutrit.monitoring.metric;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Slf4j
@Aspect
@Component
@ConditionalOnProperty(name = "logging.level.com.tutrit.monitoring.metric.ExecutionPathAspect", havingValue = "DEBUG")
public class ExecutionPathAspect {
    @Before("execution(* com.tutrit..*(..))")
    public void logBeforeMethodExecution(JoinPoint joinPoint) {
        String className = joinPoint.getSignature().getDeclaringTypeName();
        String methodName = joinPoint.getSignature().getName();
        log.info("ExecutionPath: {}.{}", className, methodName);
        log.debug("ExecutionPath: {}.{}", className, methodName);
    }
}
