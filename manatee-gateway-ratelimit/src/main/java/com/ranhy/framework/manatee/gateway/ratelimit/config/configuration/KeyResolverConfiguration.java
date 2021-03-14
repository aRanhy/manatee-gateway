package com.ranhy.framework.manatee.gateway.ratelimit.config.configuration;

import com.ranhy.framework.manatee.gateway.ratelimit.config.keyresolver.*;
import com.ranhy.framework.manatee.gateway.ratelimit.config.properties.RateLimitProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
/**
 * @author	 hz18093501 hongyu.ran
 * @date	2019年8月8日
 */
@Configuration
@ConditionalOnProperty(prefix = RateLimitProperties.PREFIX, name = "enabled",matchIfMissing = true, havingValue = "true")
public class KeyResolverConfiguration {

    @Bean(name= OriginKeyResolver.BEAN_NAME)
    public OriginKeyResolver  originKeyResolver(){
        return new OriginKeyResolver();
    }

    @Bean(name= UrlKeyResolver.BEAN_NAME)
    public UrlKeyResolver  urlKeyResolver(){
        return new UrlKeyResolver();
    }

    @Bean(name= HttpMethodKeyResolver.BEAN_NAME)
    public HttpMethodKeyResolver httpMethodKeyResolver(){
        return new HttpMethodKeyResolver();
    }

    @Bean(name= ApplicationKeyResolver.BEAN_NAME)
    public ApplicationKeyResolver applicationKeyResolver(){
        return new ApplicationKeyResolver();
    }

    @Bean(name= RpcMethodKeyResolver.BEAN_NAME)
    public RpcMethodKeyResolver rpcMethodKeyResolver(){
        return new RpcMethodKeyResolver();
    }
}
