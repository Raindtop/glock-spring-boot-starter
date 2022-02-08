package org.raindrop.glock.lock;

import lombok.Data;
import org.raindrop.glock.model.LockInfo;

@Data
public class LockRes {

    private LockInfo lockInfo;
    private Lock lock;
    private Boolean res;

    public LockRes(LockInfo lockInfo, Boolean res) {
        this.lockInfo = lockInfo;
        this.res = res;
    }
}
