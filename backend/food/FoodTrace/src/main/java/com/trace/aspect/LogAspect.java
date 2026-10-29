package com.trace.aspect;

import com.alibaba.fastjson.JSON;
import com.trace.common.UserContext;
import com.trace.dto.LoginDTO;
import com.trace.entity.SysOperationLog;
import com.trace.mapper.SysLogMapper;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import javax.servlet.http.HttpServletRequest;

@Slf4j
@Aspect
@Component
public class LogAspect {
    @Autowired
    private SysLogMapper sysLogMapper;

    @Pointcut("execution(* com.trace.controller..*.*(..))")
    public void controllerPointcut() {}

    @Around("controllerPointcut()")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = attributes.getRequest();

        Object result = null;
        try {
            result = joinPoint.proceed();
            return result;
        } finally {
            try {
                SysOperationLog sysLog = new SysOperationLog();
                sysLog.setRequestUrl(request.getRequestURL().toString());
                sysLog.setRequestMethod(request.getMethod());
                sysLog.setRequestParams(JSON.toJSONString(joinPoint.getArgs()));
                sysLog.setResponseResult(JSON.toJSONString(result));
                sysLog.setCostTime(System.currentTimeMillis() - startTime);
                sysLog.setClientIp(request.getRemoteAddr());
                LoginDTO user = UserContext.getUser();
                sysLog.setOperatorName(user != null ? user.getName() : "匿名");
                sysLog.setOperatorAddress(user != null ? user.getAddress() : "");
                sysLogMapper.insertLog(sysLog);
            } catch (Exception e) {
                log.error("AOP 日志入库失败: {}", e.getMessage());
            }
        }
    }
}