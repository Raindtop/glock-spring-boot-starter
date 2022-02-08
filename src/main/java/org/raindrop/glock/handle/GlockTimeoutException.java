package org.raindrop.glock.handle;

/**
 * @author wanglaomo
 * @since 2019/4/16
 **/
public class GlockTimeoutException extends RuntimeException {

    public GlockTimeoutException() {
    }

    public GlockTimeoutException(String message) {
        super(message);
    }

    public GlockTimeoutException(String message, Throwable cause) {
        super(message, cause);
    }
}
