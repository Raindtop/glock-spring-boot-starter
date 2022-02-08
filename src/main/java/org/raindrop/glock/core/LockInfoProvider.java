package org.raindrop.glock.core;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.raindrop.glock.annotation.Glock;
import org.raindrop.glock.config.GlockConfig;
import org.raindrop.glock.model.LockInfo;
import org.raindrop.glock.model.LockType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class LockInfoProvider {
    @Autowired
    private GlockConfig glockConfig;

    @Autowired
    private SpelNameUtils spelNameUtils;

    private static final String LOCK_NAME_PREFIX = "lock";
    private static final String LOCK_NAME_SEPARATOR = ":";


    LockInfo get(JoinPoint joinPoint, Glock glock) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        LockType type= glock.type();
        String businessKeyName=spelNameUtils.getKeyName(joinPoint, glock);
        //锁的名字，锁的粒度就是这里控制的
        String lockName = LOCK_NAME_PREFIX + LOCK_NAME_SEPARATOR + getName(glock.name(), signature) + businessKeyName;
        long waitTime = getWaitTime(glock);
        long leaseTime = getLeaseTime(glock);
        //如果占用锁的时间设计不合理，则打印相应的警告提示
        if(leaseTime == -1 && log.isWarnEnabled()) {
            log.warn("Trying to acquire Lock({}) with no expiration, " +
                    "Dlock will keep prolong the lock expiration while the lock is still holding by current thread. " +
                    "This may cause dead lock in some circumstances.", lockName);
        }
        return new LockInfo(type,lockName,waitTime,leaseTime);
    }

    /**
     * 获取锁的name，如果没有指定，则按全类名拼接方法名处理
     * @param annotationName
     * @param signature
     * @return
     */
    private String getName(String annotationName, MethodSignature signature) {
        if (annotationName.isEmpty()) {
            return String.format("%s.%s", signature.getDeclaringTypeName(), signature.getMethod().getName());
        } else {
            return annotationName;
        }
    }

    private long getWaitTime(Glock lock) {
        return lock.waitTime() == Long.MIN_VALUE ?
                glockConfig.getWaitTime() : lock.waitTime();
    }

    private long getLeaseTime(Glock lock) {
        return lock.leaseTime() == Long.MIN_VALUE ?
                glockConfig.getLeaseTime() : lock.leaseTime();
    }
}
