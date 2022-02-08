package org.raindrop.glock.model;

/**
 * @author raindrop
 * @date 2022/2/6
 * @Description: 锁类型
 */
public enum LockType {
    /**
     * 可重入锁
     */
    Reentrant,
    /**
     * 公平锁
     */
    Fair,
    /**
     * 读锁
     */
    Read,
    /**
     * 写锁
     */
    Write;

    LockType() {
    }
}
