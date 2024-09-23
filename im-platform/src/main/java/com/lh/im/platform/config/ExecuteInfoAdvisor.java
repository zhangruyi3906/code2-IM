package com.lh.im.platform.config;

import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

@Aspect
@Slf4j
@Component
public class ExecuteInfoAdvisor {

    @Pointcut("execution(* com.lh.im.platform.controller.*.*(..))")
    public void pointCut() {
    }

    @Around("pointCut()")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();
        Object res = joinPoint.proceed();
        long endTime = System.currentTimeMillis();
        if ((endTime - startTime) > 200) {
            log.info("接口执行时间过长, method:{}, args:{}",
                    joinPoint.getSignature().getName(), JSONUtil.toJsonStr(joinPoint.getArgs()));
        }
//        else {
//            log.info("接口执行时间time: {}ms, method:{}, args:{}",
//                    (endTime - startTime), joinPoint.getSignature().getName(), JSONUtil.toJsonStr(joinPoint.getArgs()));
//        }
        return res;
    }
}
