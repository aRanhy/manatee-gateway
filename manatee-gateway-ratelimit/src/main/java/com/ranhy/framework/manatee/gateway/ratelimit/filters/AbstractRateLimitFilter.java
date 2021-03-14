
package com.ranhy.framework.manatee.gateway.ratelimit.filters;

import com.google.common.collect.Lists;
import com.ranhy.framework.manatee.gateway.ratelimit.config.configuration.RateLimitMarkConfiguration;
import com.ranhy.framework.manatee.gateway.ratelimit.config.factory.KeyResolverFactory;
import com.ranhy.framework.manatee.gateway.ratelimit.config.factory.RateLimitPolicyFactory;
import com.ranhy.framework.manatee.gateway.ratelimit.config.keyresolver.KeyResolver;
import com.ranhy.framework.manatee.gateway.ratelimit.config.properties.RateLimitProperties;
import com.ranhy.framework.manatee.gateway.ratelimit.config.properties.RateLimitProperties.Policy;
import com.ranhy.framework.manatee.gateway.ratelimit.config.properties.RateLimitType;
import com.ranhy.framework.manatee.gateway.ratelimit.config.supports.RateLimitUtils;
import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.cloud.netflix.zuul.filters.Route;
import org.springframework.cloud.netflix.zuul.filters.RouteLocator;
import org.springframework.web.util.UrlPathHelper;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author	 hz18093501 hongyu.ran
 * @date	2019年8月8日
 */
@RequiredArgsConstructor
@Slf4j
public abstract class AbstractRateLimitFilter extends ZuulFilter{

    private final RateLimitMarkConfiguration.RateLimitMark rateLimitMark;
    private final RateLimitProperties properties;
    private final RouteLocator routeLocator;
    private final UrlPathHelper urlPathHelper;
    private final RateLimitUtils rateLimitUtils;
    private final KeyResolverFactory keyResolverFactory;
    private final RateLimitPolicyFactory rateLimitPolicyFactory;

    @Override
    public boolean shouldFilter() {

        //白名单过滤,如果当前IP存在白名单列表中,则返回false
        String ip = rateLimitUtils.getRemoteAddress(RequestContext.getCurrentContext().getRequest());
        if(rateLimitUtils.ipExist(properties.getExempt(), ip))
        {
            if(log.isInfoEnabled())
                log.info("白名单中存在当前IP,本次过滤器将不执行.ip = {},exempt = {}",ip,properties.getExempt());
            return false;
        }
        return  Objects.nonNull(rateLimitMark) && properties.isEnabled() && doShouldFilter() ;
    }


    @Override
    public Object run() {

        Long startTime= System.currentTimeMillis();
        Object result= doRun();
        Long useTime = System.currentTimeMillis() - startTime;
        log.info("{} use time {} mi " , filterDesc(), useTime );
        if(useTime > 10){
            log.warn("{}Low use time {} mi" , filterDesc(), useTime);
        }

        return result;
    }




    Route route(HttpServletRequest request) {
        String requestURI = urlPathHelper.getPathWithinApplication(request);
        return routeLocator.getMatchingRoute(requestURI);
    }

    protected List<Policy> policy(Route route, HttpServletRequest request) {

        List<Policy> matchPolicy= Lists.newArrayList();

        Optional.ofNullable(rateLimitPolicyFactory.getDefaultPolicyList()).ifPresent(policyList ->
                matchPolicy.addAll(policyList.stream().filter(policy -> applyPolicy(request, route, policy)).collect(Collectors.toList()))
        );

        Optional.ofNullable(route).map(Route::getId).ifPresent(service->
                rateLimitPolicyFactory.getPolicyList(service).ifPresent(servicePolicyList->
                        matchPolicy.addAll(
                                servicePolicyList.stream().filter(policy -> applyPolicy(request,route,policy)).collect(Collectors.toList())
                        )
                )
        );

        return matchPolicy;

    }

    private boolean applyPolicy(HttpServletRequest request, Route route, RateLimitProperties.Policy policy) {

        List<Policy.Config> configs = policy.getConfigs();
        return CollectionUtils.isNotEmpty(configs) && configs.stream().allMatch(
                config -> {
                    Optional<KeyResolver> keyResolver= keyResolverFactory.getKeyResolver(config.getRateLimitType());
                    return  keyResolver.isPresent() && keyResolver.get().apply(request, route, rateLimitUtils,config.getMatch());
                }
        );
    }

    public boolean configContainIp(RateLimitProperties.Policy policy){
        return Optional.ofNullable(policy).map(RateLimitProperties.Policy::getConfigs).map(configs ->
                configs.stream().anyMatch(config -> config.getRateLimitType().equalsIgnoreCase(RateLimitType.ORIGIN.getLimitType()))
        ).orElse(false);
    }

    public String  realIp(HttpServletRequest request){
        return  rateLimitUtils.getRealIp(request);
    }

    abstract public Object doRun();

    abstract public boolean doShouldFilter();

    abstract public String  filterDesc();
}
 