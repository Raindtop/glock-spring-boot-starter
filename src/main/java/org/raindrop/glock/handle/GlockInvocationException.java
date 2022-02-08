package org.raindrop.glock.handle;

/**
 * @author wanglaomo
 * @since 2019/4/16
 **/
public class GlockInvocationException extends RuntimeException {

    public GlockInvocationException() {
    }

    public GlockInvocationException(String message) {
        super(message);
    }

    public GlockInvocationException(String message, Throwable cause) {
        super(message, cause);
    }
}
