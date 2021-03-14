package com.ranhy.example.manatee.gateway.nginx.keyresolver;

import com.ranhy.framework.manatee.gateway.ratelimit.config.keyresolver.KeyResolver;
import com.ranhy.framework.manatee.gateway.ratelimit.config.properties.RateLimitType;
import com.ranhy.framework.manatee.gateway.ratelimit.config.supports.RateLimitUtils;
import com.ranhy.example.manatee.gateway.nginx.factory.IpInfoFactory;
import org.apache.commons.lang.StringUtils;
import org.springframework.cloud.netflix.zuul.filters.Route;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Optional;

/**
 * @author	 hz18093501 hongyu.ran
 * @date	2019年8月8日
 */


public class AreaKeyResolver implements KeyResolver {

    @Resource
    IpInfoFactory ipInfoFactory;

    @Override
    public boolean apply(HttpServletRequest request, Route route, RateLimitUtils rateLimitUtils, String matcher) {

        return StringUtils.isBlank(matcher) ||
        Optional.ofNullable(rateLimitUtils.getRemoteAddress(request))
                .map(ip -> rateLimitUtils.inet_aton(ip))
                .map(ipNum ->ipInfoFactory.getIpSegmentInfo(ipNum))
                .map(ipInfo -> ipInfo.getProvince())
                .orElse(StringUtils.EMPTY)
                .equalsIgnoreCase(matcher.trim());
    }

    @Override
    public String key(HttpServletRequest request, Route route, RateLimitUtils rateLimitUtils, String matcher) {

        return         Optional.ofNullable(rateLimitUtils.getRemoteAddress(request))
                .map(ip -> rateLimitUtils.inet_aton(ip))
                .map(ipNum ->ipInfoFactory.getIpSegmentInfo(ipNum))
                .map(ipInfo -> ipInfo.getProvince())
                .orElse(StringUtils.EMPTY)
                ;
    }

    @Override
    public String getLimitType() {
        return RateLimitType.AREA.getLimitType();
    }
}
