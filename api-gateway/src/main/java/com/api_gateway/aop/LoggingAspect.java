package com.api_gateway.aop;

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
 * Centralized Logging Aspect for method execution logging across all layers.
 * Provides null-safe logging, execution time tracking, and sanitization of sensitive data.
 */
@Aspect
@Component
public class LoggingAspect {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    /**
     * Pointcut that matches all classes in the controller packages.
     */
    @Pointcut("execution(* com.api_gateway..filter..*(..)) || execution(* com.api_gateway..controller..*(..))")
    public void controllerLayer() {
        // Given that API gateway uses Filter classes primarily
    }

    /**
     * Pointcut that matches all classes in the service packages.
     */
    @Pointcut("execution(* com.api_gateway..service..*(..))")
    public void serviceLayer() {
        // Pointcut definition
    }

    /**
     * Combined pointcut for application layer.
     */
    @Pointcut("controllerLayer() || serviceLayer()")
    public void applicationLayer() {
        // Pointcut definition
    }

    /**
     * Advice that logs methods throwing exceptions.
     *
     * @param joinPoint join point for advice
     * @param e         exception
     */
    @AfterThrowing(pointcut = "applicationLayer()", throwing = "e")
    public void logAfterThrowing(JoinPoint joinPoint, Throwable e) {
        log.error("[ERROR] {}.{} - Exception: {}",
                joinPoint.getSignature().getDeclaringTypeName(),
                joinPoint.getSignature().getName(),
                e.getMessage());
    }

    /**
     * Advice that logs method entry, exit, and execution time.
     *
     * @param joinPoint join point for advice
     * @return result
     * @throws Throwable throws IllegalArgumentException
     */
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

    /**
     * Helper to mask sensitive data like passwords or tokens.
     */
    private String maskSensitiveData(Object[] args) {
        if (args == null || args.length == 0) {
            return "{}";
        }
        
        try {
            return Arrays.stream(args)
                    .map(arg -> {
                        if (arg == null) {
                            return "null";
                        }
                        String argStr = arg.toString();
                        String lowerCaseArgStr = argStr.toLowerCase();
                        if (lowerCaseArgStr.contains("password") || 
                            lowerCaseArgStr.contains("token") || 
                            lowerCaseArgStr.contains("secret") ||
                            lowerCaseArgStr.contains("authorization")) {
                            return "*****";
                        }
                        return argStr;
                    })
                    .toList()
                    .toString();
        } catch (Exception e) {
            return "[Error Masking Data]";
        }
    }
}
