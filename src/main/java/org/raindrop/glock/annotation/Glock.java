package org.raindrop.glock.annotation;

import org.raindrop.glock.handle.lock.LockTimeoutStrategy;
import org.raindrop.glock.handle.release.ReleaseTimeoutStrategy;
import org.raindrop.glock.model.LockType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author raindrop
 * @date 2022/2/6
 * @Description: 锁注解
 */
@Target(value = {ElementType.METHOD})
@Retention(value = RetentionPolicy.RUNTIME)
public @interface Glock {
    /**
     * 锁的名称
     *
     * @return
     */
    String name() default "";

    /**
     * 尝试加锁，最多等待时间
     *
     * @return
     */
    long waitTime() default Long.MIN_VALUE;

    /**
     * 锁释放时间
     *
     * @return
     */
    long leaseTime() default Long.MIN_VALUE;

    /**
     * 自定义业务key
     *
     * @return
     */
    String[] keys() default {};

    /**
     * 锁类型
     *
     * @return
     */
    LockType type() default LockType.Fair;

    /**
     * 加锁超时的处理策略
     * @return lockTimeoutStrategy
     */
    LockTimeoutStrategy lockTimeoutStrategy() default LockTimeoutStrategy.NO_OPERATION;

    /**
     * 自定义加锁超时的处理策略
     * @return customLockTimeoutStrategy
     */
    String customLockTimeoutStrategy() default "";

    /**
     * 释放锁时已超时的处理策略
     * @return releaseTimeoutStrategy
     */
    ReleaseTimeoutStrategy releaseTimeoutStrategy() default ReleaseTimeoutStrategy.NO_OPERATION;

    /**
     * 自定义释放锁时已超时的处理策略
     * @return customReleaseTimeoutStrategy
     */
    String customReleaseTimeoutStrategy() default "";
}
