package com.ranhy.framework.manatee.gateway.acl.filter;

import java.util.Objects;
import java.util.Optional;

import javax.servlet.http.HttpServletRequest;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang.StringUtils;
import org.springframework.cloud.netflix.zuul.filters.Route;
import org.springframework.cloud.netflix.zuul.filters.RouteLocator;
import org.springframework.web.util.UrlPathHelper;

import com.ranhy.framework.manatee.gateway.acl.config.configuration.AclMarkConfiguration;
import com.ranhy.framework.manatee.gateway.acl.config.properties.ManateeAclProperties;
import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;

/**
 * @author	 hz18093501 hongyu.ran
 * @date	2019年8月8日
 */
@Slf4j
@RequiredArgsConstructor
abstract public class AbstractAclFilter extends ZuulFilter{

    final private ManateeAclProperties properties;
    final private AclMarkConfiguration.AclMark aclMark;
    final private  RouteLocator routeLocator;
    final private UrlPathHelper urlPathHelper;

    @Override
    public boolean shouldFilter() {
        return properties.isEnabled() && Objects.nonNull(aclMark) && StringUtils.isNotBlank(serviceId());
    }

    public Route route(HttpServletRequest request) {
        String requestURI = urlPathHelper.getPathWithinApplication(request);
        return routeLocator.getMatchingRoute(requestURI);
    }

    public String serviceId(){

        final RequestContext ctx = RequestContext.getCurrentContext();
        Route route=route(ctx.getRequest());
        return Optional.ofNullable(route).map(Route::getId).orElse(null);
    }



    @Override
    public Object run() {

        long startTime= System.currentTimeMillis();
        Object result= doRun();
        long useTime = System.currentTimeMillis() -startTime;
        if(useTime >10){
            log.warn("acl use time {} mi" ,useTime);
        }
        return result;
    }



    abstract public Object doRun();





}
