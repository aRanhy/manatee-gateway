package com.ranhy.framework.manatee.gateway.acl.config.configuration;

import com.ranhy.framework.manatee.gateway.acl.config.factory.AclConfigFactory;
import com.ranhy.framework.manatee.gateway.acl.config.properties.ManateeAclProperties;
import com.ranhy.framework.manatee.gateway.acl.config.route.AclHttpClientRibbonCommandFactory;
import com.ranhy.framework.manatee.gateway.acl.config.route.AclRibbonLoadBalancedRetryPolicyFactory;
import com.ranhy.framework.manatee.gateway.acl.config.supports.AclConfigSync;
import com.ranhy.framework.manatee.gateway.acl.config.supports.AclSmartLifecycle;
import com.ranhy.framework.manatee.gateway.acl.config.supports.RedisServe;
import com.ranhy.framework.manatee.gateway.acl.filter.pre.InterfacePermissionsFilter;
import com.google.common.base.Preconditions;
import com.ranhy.framework.manatee.gateway.common.constants.GateWayConstants;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.bind.RelaxedPropertyResolver;
import org.springframework.cloud.client.loadbalancer.LoadBalancedRetryPolicyFactory;
import org.springframework.cloud.netflix.ribbon.RibbonAutoConfiguration;
import org.springframework.cloud.netflix.ribbon.SpringClientFactory;
import org.springframework.cloud.netflix.zuul.ZuulProxyAutoConfiguration;
import org.springframework.cloud.netflix.zuul.filters.RouteLocator;
import org.springframework.cloud.netflix.zuul.filters.ZuulProperties;
import org.springframework.cloud.netflix.zuul.filters.route.RibbonCommandFactory;
import org.springframework.cloud.netflix.zuul.filters.route.ZuulFallbackProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.PropertyResolver;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.util.UrlPathHelper;

import java.util.Collections;
import java.util.Set;

/**
 * @author	 hz18093501 hongyu.ran
 * @date	2019年8月8日
 */
@Configuration
@AutoConfigureAfter(AclMarkConfiguration.class)
@AutoConfigureBefore({ZuulProxyAutoConfiguration.class,RibbonAutoConfiguration.class})
@ConditionalOnProperty(prefix = ManateeAclProperties.PREFIX, name = "enabled",matchIfMissing = true, havingValue = "true")
public class AclAutoConfiguration {

    private final UrlPathHelper urlPathHelper = new UrlPathHelper();

    @Bean
    InterfacePermissionsFilter interfacePermissionsFilter(final ManateeAclProperties properties,
                                                          final AclMarkConfiguration.AclMark aclMark,
                                                          final AclConfigFactory aclConfigFactory,
                                                          final RouteLocator routeLocator){
        return new InterfacePermissionsFilter(properties,aclMark,aclConfigFactory,routeLocator,urlPathHelper);
   }


    @Bean("manateeAclRedisTemplate")
    public StringRedisTemplate redisTemplate(final RedisConnectionFactory connectionFactory) {
        return new StringRedisTemplate(connectionFactory);
    }

    @Bean("manateeAclRedisServe")
    RedisServe redisServe(@Qualifier("manateeAclRedisTemplate") final StringRedisTemplate redisTemplate){
        return new RedisServe(redisTemplate);
   }

   @Bean
    AclConfigFactory aclConfigFactory(@Qualifier("manateeAclRedisServe") RedisServe redisServe , ConfigurableEnvironment env){
       PropertyResolver environmentPropertyResolver = new RelaxedPropertyResolver(env);
       String applicationName=environmentPropertyResolver.getProperty(GateWayConstants.APPLICATION_NAME_KEY);
       Preconditions.checkArgument(StringUtils.isNotBlank(applicationName),GateWayConstants.APPLICATION_NAME_KEY+" can not be empty ");
       return new AclConfigFactory(redisServe,applicationName);
   }

   @Bean
   AclSmartLifecycle aclSmartLifecycle(final AclConfigSync aclConfigSync){
        return new AclSmartLifecycle(aclConfigSync);
   }

   @Bean
   AclConfigSync aclConfigSync(final ManateeAclProperties properties,
                               final AclConfigFactory aclConfigFactory  ){
        return new AclConfigSync(properties,aclConfigFactory);
   }

    @Autowired(required = false)
    private Set<ZuulFallbackProvider> zuulFallbackProviders = Collections.emptySet();

    @Bean
    @ConditionalOnMissingBean
    public RibbonCommandFactory<?> ribbonCommandFactory(
            SpringClientFactory clientFactory, ZuulProperties zuulProperties) {
        return new AclHttpClientRibbonCommandFactory(clientFactory, zuulProperties, zuulFallbackProviders);
    }

    @Bean
    @ConditionalOnClass(name = "org.springframework.retry.support.RetryTemplate")
    @ConditionalOnMissingBean
    public LoadBalancedRetryPolicyFactory loadBalancedRetryPolicyFactory(SpringClientFactory clientFactory) {
        return new AclRibbonLoadBalancedRetryPolicyFactory(clientFactory);
    }
}
