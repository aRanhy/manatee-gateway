package com.ranhy.framework.manatee.gateway.ratelimit.config.configuration;

import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@AutoConfigureBefore(RateLimitAutoConfiguration.class)
public class RateLimitMarkConfiguration {
    @Bean
    RateLimitMark  rateLimitMark(){
        return new RateLimitMark();
    }
    public class RateLimitMark{
    }
}
