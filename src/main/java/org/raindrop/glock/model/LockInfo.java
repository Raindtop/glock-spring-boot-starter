package org.raindrop.glock.model;

import lombok.Data;

/**
 * @author raindrop
 * @date 2022/2/6
 * @Description: 锁信息
 */
@Data
public class LockInfo {
    private LockType type;
    private String name;
    private long waitTime;
    private long leaseTime;

    public LockInfo(){

    }

    public LockInfo(LockType type, String name, long waitTime, long leaseTime) {
        this.type = type;
        this.name = name;
        this.waitTime = waitTime;
        this.leaseTime = leaseTime;
    }
}
