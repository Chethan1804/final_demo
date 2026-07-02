package com.notification_service.aop;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Arrays;

/**
 * Centralized Logging Aspect for method execution logging.
 */
@Aspect
@Component
public class LoggingAspect {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    @Pointcut("execution(* com.notification_service..controller..*(..))")
    public void controllerLayer() {}

    @Pointcut("execution(* com.notification_service..adapter..*(..))")
    public void adapterLayer() {}

    @Pointcut("controllerLayer() || adapterLayer()")
    public void applicationLayer() {}

    @AfterThrowing(pointcut = "applicationLayer()", throwing = "e")
    public void logAfterThrowing(JoinPoint joinPoint, Throwable e) {
        log.error("[ERROR] {}.{} - Exception: {}",
                joinPoint.getSignature().getDeclaringTypeName(),
                joinPoint.getSignature().getName(),
                e.getMessage());
    }

    @Around("applicationLayer()")
    public Object logAround(ProceedingJoinPoint joinPoint) throws Throwable {
        String className = joinPoint.getSignature().getDeclaringTypeName();
        String methodName = joinPoint.getSignature().getName();

        if (log.isInfoEnabled()) {
            log.info("[START] {}.{} - Arguments: {}", className, methodName, maskSensitiveData(joinPoint.getArgs()));
        }

        long start = System.currentTimeMillis();
        
        try {
            Object result = joinPoint.proceed();
            long executionTime = System.currentTimeMillis() - start;

            if (log.isInfoEnabled()) {
                String resultStr = result != null ? "Success" : "null";
                log.info("[END] {}.{} - Result: {}", className, methodName, resultStr);
                log.info("[TIME] {}.{} - Execution time: {} ms", className, methodName, executionTime);
            }

            return result;
        } catch (IllegalArgumentException e) {
            log.error("[ILLEGAL_ARGUMENT] {}.{} - Illegal argument: {} in {}.{}()", 
                    className, methodName, Arrays.toString(joinPoint.getArgs()), className, methodName);
            throw e;
        }
    }

    private String maskSensitiveData(Object[] args) {
        if (args == null || args.length == 0) return "{}";
        try {
            return Arrays.stream(args).map(arg -> {
                if (arg == null) return "null";
                String argStr = arg.toString();
                String lower = argStr.toLowerCase();
                if (lower.contains("password") || lower.contains("token")) return "*****";
                return argStr;
            }).toList().toString();
        } catch (Exception e) { return "[Error Masking Data]"; }
    }
}
