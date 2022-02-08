package org.raindrop.glock.core;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.raindrop.glock.annotation.Glock;
import org.raindrop.glock.handle.GlockInvocationException;
import org.raindrop.glock.lock.Lock;
import org.raindrop.glock.lock.LockFactory;
import org.raindrop.glock.lock.LockRes;
import org.raindrop.glock.model.LockInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Aspect
@Component
public class GlockAspect {
    @Autowired
    LockFactory lockFactory;

    @Autowired
    private LockInfoProvider lockInfoProvider;

    private final Map<String,LockRes> currentThreadLock = new ConcurrentHashMap<>();

    @Around("@annotation(glock)")
    public Object around(ProceedingJoinPoint joinPoint, Glock glock) throws Throwable{
        LockInfo lockInfo = lockInfoProvider.get(joinPoint, glock);
        String curentLock = this.getCurrentLockId(joinPoint, glock);
        currentThreadLock.put(curentLock,new LockRes(lockInfo, false));
        Lock lock = lockFactory.getLock(lockInfo);
        boolean lockRes = lock.acquire();

        //如果获取锁失败了，则进入失败的处理逻辑
        if(!lockRes) {
            if(log.isWarnEnabled()) {
                log.warn("Timeout while acquiring Lock({})", lockInfo.getName());
            }
            //如果自定义了获取锁失败的处理策略，则执行自定义的降级处理策略
            if(!StringUtils.isEmpty(glock.customLockTimeoutStrategy())) {
                return handleCustomLockTimeout(glock.customLockTimeoutStrategy(), joinPoint);
            } else {
                //否则执行预定义的执行策略
                //注意：如果没有指定预定义的策略，默认的策略为静默啥不做处理
                glock.lockTimeoutStrategy().handle(lockInfo, lock, joinPoint);
            }
        }

        currentThreadLock.get(curentLock).setLock(lock);
        currentThreadLock.get(curentLock).setRes(true);

        return joinPoint.proceed();
    }

    @AfterReturning(value = "@annotation(glock)")
    public void afterReturning(JoinPoint joinPoint, Glock glock) throws Throwable {
        String curentLock = this.getCurrentLockId(joinPoint,glock);
        releaseLock(glock, joinPoint,curentLock);
        cleanUpThreadLocal(curentLock);
    }

    @AfterThrowing(value = "@annotation(glock)", throwing = "ex")
    public void afterThrowing (JoinPoint joinPoint, Glock glock, Throwable ex) throws Throwable {
        String curentLock = this.getCurrentLockId(joinPoint,glock);
        releaseLock(glock, joinPoint,curentLock);
        cleanUpThreadLocal(curentLock);
        throw ex;
    }

    /**
     *  释放锁
     */
    private void releaseLock(Glock glock, JoinPoint joinPoint,String curentLock) throws Throwable {
        LockRes lockRes = currentThreadLock.get(curentLock);
        if(Objects.isNull(lockRes)){
            throw new NullPointerException("Please check whether the input parameter used as the lock key value has been modified in the method, which will cause the acquire and release locks to have different key values and throw null pointers.curentLockKey:" + curentLock);
        }
        if (lockRes.getRes()) {
            boolean releaseRes = currentThreadLock.get(curentLock).getLock().release();
            // avoid release lock twice when exception happens below
            lockRes.setRes(false);
            if (!releaseRes) {
                handleReleaseTimeout(glock, lockRes.getLockInfo(), joinPoint);
            }
        }
    }

    /**
     *  处理释放锁时已超时
     */
    private void handleReleaseTimeout(Glock glock, LockInfo lockInfo, JoinPoint joinPoint) throws Throwable {

        if(log.isWarnEnabled()) {
            log.warn("Timeout while release Lock({})", lockInfo.getName());
        }

        if(!StringUtils.isEmpty(glock.customReleaseTimeoutStrategy())) {

            handleCustomReleaseTimeout(glock.customReleaseTimeoutStrategy(), joinPoint);

        } else {
            glock.releaseTimeoutStrategy().handle(lockInfo);
        }

    }

    /**
     * 处理自定义释放锁时已超时
     */
    private void handleCustomReleaseTimeout(String releaseTimeoutHandler, JoinPoint joinPoint) throws Throwable {

        Method currentMethod = ((MethodSignature)joinPoint.getSignature()).getMethod();
        Object target = joinPoint.getTarget();
        Method handleMethod = null;
        try {
            handleMethod = joinPoint.getTarget().getClass().getDeclaredMethod(releaseTimeoutHandler, currentMethod.getParameterTypes());
            handleMethod.setAccessible(true);
        } catch (NoSuchMethodException e) {
            throw new IllegalArgumentException("Illegal annotation param customReleaseTimeoutStrategy",e);
        }
        Object[] args = joinPoint.getArgs();

        try {
            handleMethod.invoke(target, args);
        } catch (IllegalAccessException e) {
            throw new GlockInvocationException("Fail to invoke custom release timeout handler: " + releaseTimeoutHandler, e);
        } catch (InvocationTargetException e) {
            throw e.getTargetException();
        }
    }

    // avoid memory leak
    private void cleanUpThreadLocal(String curentLock) {
        currentThreadLock.remove(curentLock);
    }

    /**
     * 处理自定义加锁超时
     */
    private Object handleCustomLockTimeout(String lockTimeoutHandler, JoinPoint joinPoint) throws Throwable {

        // prepare invocation context
        Method currentMethod = ((MethodSignature)joinPoint.getSignature()).getMethod();
        Object target = joinPoint.getTarget();
        Method handleMethod = null;
        try {
            handleMethod = joinPoint.getTarget().getClass().getDeclaredMethod(lockTimeoutHandler, currentMethod.getParameterTypes());
            handleMethod.setAccessible(true);
        } catch (NoSuchMethodException e) {
            throw new IllegalArgumentException("Illegal annotation param customLockTimeoutStrategy",e);
        }
        Object[] args = joinPoint.getArgs();

        // invoke
        Object res = null;
        try {
            res = handleMethod.invoke(target, args);
        } catch (IllegalAccessException e) {
            throw new GlockInvocationException("Fail to invoke custom lock timeout handler: " + lockTimeoutHandler ,e);
        } catch (InvocationTargetException e) {
            throw e.getTargetException();
        }

        return res;
    }

    /**
     * 获取当前锁在map中的key
     * @param joinPoint
     * @param glock
     * @return
     */
    private String getCurrentLockId(JoinPoint joinPoint , Glock glock){
        LockInfo lockInfo = lockInfoProvider.get(joinPoint, glock);
        String curentLock= Thread.currentThread().getId() + lockInfo.getName();
        return curentLock;
    }
}
