
package com.ranhy.framework.manatee.gateway.ratelimit.filters;


import com.google.common.collect.Maps;
import com.ranhy.framework.manatee.gateway.common.constants.RespCodeEnum;
import com.ranhy.framework.manatee.gateway.common.util.JsonUtils;
import com.ranhy.framework.manatee.gateway.ratelimit.config.configuration.RateLimitMarkConfiguration;
import com.ranhy.framework.manatee.gateway.ratelimit.config.entity.Rate;
import com.ranhy.framework.manatee.gateway.ratelimit.config.exception.CatfishRateLimitException;
import com.ranhy.framework.manatee.gateway.ratelimit.config.factory.KeyResolverFactory;
import com.ranhy.framework.manatee.gateway.ratelimit.config.factory.RateLimitPolicyFactory;
import com.ranhy.framework.manatee.gateway.ratelimit.config.properties.RateLimitProperties;
import com.ranhy.framework.manatee.gateway.ratelimit.config.properties.RepositoryType;
import com.ranhy.framework.manatee.gateway.ratelimit.config.repository.RateLimiter;
import com.ranhy.framework.manatee.gateway.ratelimit.config.supports.FirewallUtils;
import com.ranhy.framework.manatee.gateway.ratelimit.config.supports.RateLimitKeyGenerator;
import com.ranhy.framework.manatee.gateway.ratelimit.config.supports.RateLimitUtils;
import com.netflix.zuul.context.RequestContext;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.netflix.zuul.filters.Route;
import org.springframework.cloud.netflix.zuul.filters.RouteLocator;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.http.HttpStatus;
import org.springframework.web.util.UrlPathHelper;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.ranhy.framework.manatee.gateway.ratelimit.config.util.RateLimitConstants.*;
import static org.springframework.cloud.netflix.zuul.filters.support.FilterConstants.PRE_TYPE;

/**
 * @author	 hz18093501 hongyu.ran
 * @date	2019年8月8日
 */
@Slf4j
public class RateLimitPreFilter extends AbstractRateLimitFilter implements ApplicationContextAware, InitializingBean {

    private final RateLimitProperties properties;
    private List<RateLimiter> rateLimiterList;
    private final RateLimitKeyGenerator rateLimitKeyGenerator;
    private ApplicationContext applicationContext;
    @Autowired(required = false)
    private FirewallUtils firewallUtils;

    public RateLimitPreFilter(final RateLimitMarkConfiguration.RateLimitMark rateLimitMark,
                              final RateLimitProperties properties, final RouteLocator routeLocator,
                              final UrlPathHelper urlPathHelper,/** final RateLimiter rateLimiter,**/
                              final RateLimitKeyGenerator rateLimitKeyGenerator, final RateLimitUtils rateLimitUtils,
                              final KeyResolverFactory keyResolverFactory, final RateLimitPolicyFactory rateLimitPolicyFactory

    ) {
        super(rateLimitMark,properties, routeLocator, urlPathHelper, rateLimitUtils, keyResolverFactory, rateLimitPolicyFactory);
        this.properties = properties;
        this.rateLimitKeyGenerator = rateLimitKeyGenerator;
    }

    @Override
    public String filterType() {
        return PRE_TYPE;
    }

    @Override
    public int filterOrder() {
        return properties.getPreFilterOrder();
    }

    @Override
    public boolean doShouldFilter() {
        final RequestContext ctx = RequestContext.getCurrentContext();
        final HttpServletRequest request = ctx.getRequest();
        final Route route = route(request);
        return CollectionUtils.isNotEmpty(policy(route,request));
    }

    @Override
    public String filterDesc() {
        return "rateLimit";
    }


    @Override
    public Object doRun() {
        final RequestContext ctx = RequestContext.getCurrentContext();
        final HttpServletResponse response = ctx.getResponse();
        final HttpServletRequest request = ctx.getRequest();
        final Route route = route(request);
        try {
            policy(route, request).forEach(policy ->

                    rateLimitKeyGenerator.key(request, route, policy).ifPresent(key ->

                            getRateLimiter(policy.getRepository()).ifPresent(rateLimiters -> rateLimiters.forEach(rateLimiter -> {

                                        Map<String, String> responseHeaders = Maps.newHashMap();

                                        final Rate rate = rateLimiter.consume(policy, key);

                                        final String httpHeaderKey = key.replaceAll("[^A-Za-z0-9-.]", "_").replaceAll("__", "_");

                                        final Long limit = policy.getLimit();
                                        final Long remaining = rate.getRemaining();
                                        if (limit != null) {
                                            responseHeaders.put(HEADER_LIMIT + httpHeaderKey, String.valueOf(limit));
                                            responseHeaders.put(HEADER_REMAINING + httpHeaderKey, String.valueOf(Math.max(remaining, 0)));
                                        }


                                        responseHeaders.put(HEADER_RESET + httpHeaderKey, String.valueOf(rate.getReset()));

                                        if (properties.isAddResponseHeaders()) {
                                            for (Map.Entry<String, String> headersEntry : responseHeaders.entrySet()) {
                                                response.setHeader(headersEntry.getKey(), headersEntry.getValue());
                                            }
                                        }

                                        if ((limit != null && remaining < 0) ) {

                                            HttpStatus tooManyRequests = HttpStatus.TOO_MANY_REQUESTS;
                                            ctx.setResponseStatusCode(tooManyRequests.value());
                                            String realIp =  realIp(request);
                                            if(properties.isFireWall() && firewallUtils!=null && StringUtils.isNotBlank(realIp)){
                                                firewallUtils.pushBlockValue(key);
                                            }
                                            log.warn("restricted request key={} ,policy={}",key, JsonUtils.beanToJson(policy));
                                            throw new CatfishRateLimitException(RespCodeEnum.TOO_MANY_REQUEST, HttpStatus.TOO_MANY_REQUESTS.value());

                                        }
                                    })

                            )

                    )
            );

        }catch (CatfishRateLimitException e){
            throw  e;
        }catch (Exception e){
            e.printStackTrace();
            log.error("current limit control failed , request cleared , errorMsg={} route={} ",e.getMessage(),JsonUtils.beanToJson(route));
        }
        return null;
    }



    @Override
    public void afterPropertiesSet() throws Exception {
        Map<String, RateLimiter> keyResolvers= applicationContext.getBeansOfType(RateLimiter.class);
        if(MapUtils.isNotEmpty(keyResolvers)){
            rateLimiterList=keyResolvers.values().stream().collect(Collectors.toList());
        }
    }



    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext=applicationContext;
    }

    private Optional< List<RateLimiter>> getRateLimiter(RepositoryType type){
        return  Optional.ofNullable(rateLimiterList)
                .map(list -> list.stream().filter(item -> item.getRepositoryType() == type).collect(Collectors.toList()) ) ;
    }

}

