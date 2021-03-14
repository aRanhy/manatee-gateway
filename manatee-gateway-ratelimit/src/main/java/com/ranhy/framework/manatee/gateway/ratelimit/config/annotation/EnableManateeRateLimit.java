
package com.ranhy.framework.manatee.gateway.ratelimit.config.annotation;

import com.ranhy.framework.manatee.gateway.ratelimit.config.configuration.KeyResolverConfiguration;
import com.ranhy.framework.manatee.gateway.ratelimit.config.configuration.RateLimitAutoConfiguration;
import com.ranhy.framework.manatee.gateway.ratelimit.config.configuration.RateLimitMarkConfiguration;
import com.ranhy.framework.manatee.gateway.ratelimit.config.properties.RateLimitProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author	 hz18093501 hongyu.ran
 * @date	2019年8月8日
 */
@Configuration
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@EnableConfigurationProperties(RateLimitProperties.class)
@Import({ KeyResolverConfiguration.class, RateLimitMarkConfiguration.class, RateLimitAutoConfiguration.class})
public @interface EnableManateeRateLimit {

}
 