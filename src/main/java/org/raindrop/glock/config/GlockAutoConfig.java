package org.raindrop.glock.config;

import io.netty.channel.nio.NioEventLoopGroup;
import org.raindrop.glock.core.GlockAspect;
import org.raindrop.glock.core.LockInfoProvider;
import org.raindrop.glock.core.SpelNameUtils;
import org.raindrop.glock.lock.LockFactory;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.client.codec.Codec;
import org.redisson.config.Config;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.util.ClassUtils;

import javax.annotation.Resource;

@Configuration
@ConditionalOnProperty(prefix = GlockConfig.PREFIX, name = "enable", havingValue = "true", matchIfMissing = true)
@AutoConfigureAfter(RedisAutoConfiguration.class)
@EnableConfigurationProperties(GlockConfig.class)
@Import({GlockAspect.class})
public class GlockAutoConfig {
    @Resource
    private GlockConfig glockConfig;

    @Bean(destroyMethod = "shutdown")
    @ConditionalOnMissingBean
    RedissonClient redisson() throws Exception {
        Config config = new Config();
        config.useSingleServer().setAddress(glockConfig.getAddress())
                .setDatabase(glockConfig.getDatabase())
                .setPassword(glockConfig.getPassword());
        Codec codec = (Codec) ClassUtils.forName(glockConfig.getCodec(), ClassUtils.getDefaultClassLoader()).newInstance();
        config.setCodec(codec);
        config.setEventLoopGroup(new NioEventLoopGroup());

        return Redisson.create(config);
    }

    @Bean
    @ConditionalOnMissingBean
    public SpelNameUtils spelNameUtils(){
        return new SpelNameUtils();
    }
}
