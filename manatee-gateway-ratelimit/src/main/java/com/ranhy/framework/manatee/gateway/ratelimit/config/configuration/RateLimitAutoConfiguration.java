/**
 * Copyright (c) 2006-2016 Huize Ltd. All Rights Reserved. 
 *  
 * This code is the confidential and proprietary information of   
 * Hzins. You shall not disclose such Confidential Information   
 * and shall use it only in accordance with the terms of the agreements   
 * you entered into with Huize,http://www.huize.com.
 *  
 */   
package com.ranhy.framework.manatee.gateway.ratelimit.config.configuration;

import com.netflix.zuul.ZuulFilter;
import com.ranhy.framework.manatee.gateway.ratelimit.config.factory.KeyResolverFactory;
import com.ranhy.framework.manatee.gateway.ratelimit.config.factory.RateLimitPolicyFactory;
import com.ranhy.framework.manatee.gateway.ratelimit.config.properties.RateLimitProperties;
import com.ranhy.framework.manatee.gateway.ratelimit.config.repository.MemoryRateLimiter;
import com.ranhy.framework.manatee.gateway.ratelimit.config.repository.RateLimiter;
import com.ranhy.framework.manatee.gateway.ratelimit.config.repository.RedisRateLimiter;
import com.ranhy.framework.manatee.gateway.ratelimit.config.supports.*;
import com.ranhy.framework.manatee.gateway.ratelimit.filters.FirewallPreFilter;
import com.ranhy.framework.manatee.gateway.ratelimit.filters.RateLimitPreFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.client.discovery.event.HeartbeatEvent;
import org.springframework.cloud.client.discovery.event.ParentHeartbeatEvent;
import org.springframework.cloud.netflix.zuul.filters.RouteLocator;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.util.UrlPathHelper;

import java.util.List;
import java.util.Optional;

/**
 * @author	 hz18093501 hongyu.ran
 * @date	2019年8月8日
 */
@Configuration
@AutoConfigureAfter(RateLimitMarkConfiguration.class)
@ConditionalOnProperty(prefix = RateLimitProperties.PREFIX, name = "enabled",matchIfMissing = true, havingValue = "true")
public class RateLimitAutoConfiguration {

    private final UrlPathHelper urlPathHelper = new UrlPathHelper();


    @Bean
    @ConditionalOnMissingBean(RateLimitPolicyFactory.class)
    public RateLimitPolicyFactory rateLimitPolicyFactory(final RateLimitProperties rateLimitProperties){
        return new RateLimitPolicyFactory(rateLimitProperties);
    }

    @Bean
    @ConditionalOnMissingBean(KeyResolverFactory.class)
    public KeyResolverFactory keyResolverFactory(){
        return new KeyResolverFactory()  ;
    }

    @Bean
    @ConditionalOnMissingBean(RateLimitUtils.class)
    public RateLimitUtils rateLimitUtils (final RateLimitProperties rateLimitProperties){
        return  new DefaultRateLimitUtils(rateLimitProperties);
    }

    @Bean
    @ConditionalOnMissingBean(RateLimitKeyGenerator.class)
    public RateLimitKeyGenerator ratelimitKeyGenerator(final RateLimitProperties properties,
                                                       final RateLimitUtils rateLimitUtils ,
                                                       final KeyResolverFactory keyResolverFactory) {
        return new DefaultRateLimitKeyGenerator(properties,rateLimitUtils,keyResolverFactory);
    }

    @Bean
    @ConditionalOnMissingBean(RateLimiterErrorHandler.class)
    public RateLimiterErrorHandler rateLimiterErrorHandler(){
        return new DefaultRateLimiterErrorHandler();
    }

    @Bean
    @ConditionalOnMissingBean(RateLimitPreFilter.class)
    public ZuulFilter rateLimiterPreFilter(final RateLimitMarkConfiguration.RateLimitMark rateLimitMark,
                                           /**final RateLimiter rateLimiter,**/
                                           final RateLimitProperties rateLimitProperties,
                                           final RouteLocator routeLocator,
                                           final RateLimitKeyGenerator rateLimitKeyGenerator ,
                                           final RateLimitUtils rateLimitUtils,
                                           final KeyResolverFactory keyResolverFactory,
                                           final RateLimitPolicyFactory rateLimitPolicyFactory
                                           ) {
        return new RateLimitPreFilter(rateLimitMark,rateLimitProperties, routeLocator, urlPathHelper,
                rateLimitKeyGenerator,rateLimitUtils,keyResolverFactory,rateLimitPolicyFactory );
    }


    @Configuration
    @ConditionalOnProperty(prefix = RateLimitProperties.PREFIX, name = "fireWall", havingValue = "true")
    public static class FirewallConfiguration {

        private final UrlPathHelper urlPathHelper = new UrlPathHelper();

        @ConditionalOnClass(RedisTemplate.class)
        @Bean("catfishRateLimiterRedisTemplate")
        public StringRedisTemplate redisTemplate(final RedisConnectionFactory connectionFactory) {
            return new StringRedisTemplate(connectionFactory);
        }

        @Bean
        @ConditionalOnMissingBean(FirewallUtils.class)
        FirewallUtils firewallUtils(@Qualifier("catfishRateLimiterRedisTemplate") final RedisTemplate redisTemplate){
            return new DefaultFirewallUtils(redisTemplate);
        }

        @Bean
        @ConditionalOnMissingBean(FirewallPreFilter.class)
        public ZuulFilter firewallPreFilter(final RateLimitMarkConfiguration.RateLimitMark rateLimitMark,
                               final RateLimitProperties rateLimitProperties,
                               final RouteLocator routeLocator,
                               final RateLimitKeyGenerator rateLimitKeyGenerator ,
                               final RateLimitUtils rateLimitUtils,
                               final KeyResolverFactory keyResolverFactory,
                               final RateLimitPolicyFactory rateLimitPolicyFactory,
                               final FirewallUtils firewallUtils
        ) {
            return new FirewallPreFilter(rateLimitMark,rateLimitProperties, routeLocator, urlPathHelper,
                    rateLimitKeyGenerator,rateLimitUtils,keyResolverFactory,rateLimitPolicyFactory,firewallUtils );
        }

    }



    @Configuration
    @ConditionalOnProperty(prefix = RateLimitProperties.PREFIX, name = "repository.redis", havingValue = "true", matchIfMissing = true)
   // @DependsOn("rateLimitProperties")
   // @ConditionalOnMyProperty(repositoryContain = "redis")
    public static class RedisConfiguration {

        @ConditionalOnClass(RedisTemplate.class)
        @Bean("catfishRateLimiterRedisTemplate")
        public StringRedisTemplate redisTemplate(final RedisConnectionFactory connectionFactory) {
            return new StringRedisTemplate(connectionFactory);
        }

        @Bean
        public RateLimiter redisRateLimiter(@Qualifier("catfishRateLimiterRedisTemplate") final RedisTemplate redisTemplate ,final RateLimiterErrorHandler rateLimiterErrorHandler) {
            return new RedisRateLimiter(redisTemplate,rateLimiterErrorHandler);
        }

    }
    @Configuration
    @ConditionalOnProperty(prefix = RateLimitProperties.PREFIX, name = "repository.memory", havingValue = "true", matchIfMissing = true)
   // @ConditionalOnMyProperty(repositoryContain = "memory")
    public static class InMemoryConfiguration {
        @Bean
        public RateLimiter inMemoryRateLimiter(final RateLimiterErrorHandler rateLimiterErrorHandler) {
            return new MemoryRateLimiter(rateLimiterErrorHandler);
        }
    }

    @Bean
    public ApplicationListener<ApplicationEvent> gatewayRatelimitDiscoveryRefreshListener() {
        return new  GatewayRatelimitDiscoveryRefreshListener();
    }


    public static class  GatewayRatelimitDiscoveryRefreshListener
            implements ApplicationListener<ApplicationEvent> {


        private static volatile int gatewayRatelimitServeCount=1;

        @Autowired(required = false)
        private DiscoveryClient discovery;

        @Value("${spring.application.name}")
        private String serviceId;

        @Override
        public void onApplicationEvent(ApplicationEvent event) {

            if ( event instanceof ParentHeartbeatEvent || event instanceof HeartbeatEvent ) {
                reset();
            }

        }

        private void reset() {

            int reFreshCount = Optional.ofNullable(discovery)
                    .map(discoveryClient -> discoveryClient.getInstances(serviceId))
                    .map(List::size)
                    .orElse(0);
            if(reFreshCount>0 && reFreshCount !=gatewayRatelimitServeCount){
                gatewayRatelimitServeCount=reFreshCount;
            }
        }

        public static int getServeCount(){
            return gatewayRatelimitServeCount;
        }

    }

}
 