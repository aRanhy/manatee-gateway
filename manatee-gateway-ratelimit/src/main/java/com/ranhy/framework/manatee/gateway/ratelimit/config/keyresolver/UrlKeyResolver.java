package com.ranhy.framework.manatee.gateway.ratelimit.config.keyresolver;

import com.ranhy.framework.manatee.gateway.ratelimit.config.properties.RateLimitType;

import com.ranhy.framework.manatee.gateway.ratelimit.config.supports.RateLimitUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.cloud.netflix.zuul.filters.Route;

import javax.servlet.http.HttpServletRequest;
import java.util.Optional;
/**
 * @author	 hz18093501 hongyu.ran
 * @date	2019年8月8日
 */
public class UrlKeyResolver implements KeyResolver {

    public final static String BEAN_NAME="urlKeyResolver";
    @Override
    public boolean apply(HttpServletRequest request, Route route, RateLimitUtils rateLimitUtils, String matcher) {
        return StringUtils.isBlank(matcher) || Optional.ofNullable(route).map(Route::getPath).orElse(StringUtils.EMPTY).startsWith(matcher);
    }

    @Override
    public String key(HttpServletRequest request, Route route, RateLimitUtils rateLimitUtils, String matcher) {
        return Optional.ofNullable(route).map(Route::getPath).orElse(StringUtils.EMPTY);
    }

    @Override
    public String getLimitType() {
        return RateLimitType.URL.getLimitType();
    }
}
