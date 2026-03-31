package com.pacman.config;

import com.pacman.entity.OperationLog;
import com.pacman.mapper.OperationLogMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import java.time.LocalDateTime;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class OperationLogAspect {
    
    private final OperationLogMapper operationLogMapper;
    
    @Pointcut("execution(* com.pacman.controller..*.*(..)) && " +
              "(@annotation(org.springframework.web.bind.annotation.PostMapping) || " +
              "@annotation(org.springframework.web.bind.annotation.PutMapping) || " +
              "@annotation(org.springframework.web.bind.annotation.DeleteMapping))")
    public void logPointcut() {}
    
    @Around("logPointcut()")
    public Object around(ProceedingJoinPoint point) throws Throwable {
        long startTime = System.currentTimeMillis();
        Object result = point.proceed();
        long endTime = System.currentTimeMillis();
        
        try {
            saveLog(point, endTime - startTime);
        } catch (Exception e) {
            log.error("保存操作日志失败", e);
        }
        
        return result;
    }
    
    private void saveLog(ProceedingJoinPoint point, long time) {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) return;
        
        HttpServletRequest request = attributes.getRequest();
        Long userId = (Long) request.getAttribute("userId");
        
        String className = point.getTarget().getClass().getSimpleName();
        String methodName = point.getSignature().getName();
        
        OperationLog operationLog = new OperationLog();
        operationLog.setUserId(userId);
        operationLog.setModule(className.replace("Controller", ""));
        operationLog.setAction(methodName);
        operationLog.setContent("耗时: " + time + "ms");
        operationLog.setIp(getIpAddress(request));
        operationLog.setCreatedAt(LocalDateTime.now());
        
        operationLogMapper.insert(operationLog);
        log.info("操作日志: {} - {} - {}ms", className, methodName, time);
    }
    
    private String getIpAddress(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }
}
