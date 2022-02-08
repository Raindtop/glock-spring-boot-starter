package org.raindrop.glock.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = GlockConfig.PREFIX)
public class GlockConfig {
    public static final String PREFIX = "spring.glock";
    //redisson
    private String address;
    private String password;
    private int database=15;
    private String codec = "org.redisson.codec.JsonJacksonCodec";
    //lock
    private long waitTime = Long.MIN_VALUE;
    private long leaseTime = Long.MIN_VALUE;
}
