package org.raindrop.glock.handle.lock;

import org.aspectj.lang.JoinPoint;
import org.raindrop.glock.lock.Lock;
import org.raindrop.glock.model.LockInfo;

/**
 * 获取锁超时的处理逻辑接口
 *
 * @author wanglaomo
 * @since 2019/4/15
 **/
public interface LockTimeoutHandler {

    void handle(LockInfo lockInfo, Lock lock, JoinPoint joinPoint);
}
