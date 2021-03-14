package com.ranhy.framework.manatee.gateway.ratelimit.config.keyresolver;

import com.ranhy.framework.manatee.gateway.ratelimit.config.properties.RateLimitType;
import com.ranhy.framework.manatee.gateway.ratelimit.config.supports.RateLimitUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.cloud.netflix.zuul.filters.Route;

import javax.servlet.http.HttpServletRequest;
import java.util.Optional;

public class ApplicationKeyResolver implements KeyResolver {

    public final static String BEAN_NAME="applicationKeyResolver";

    @Override
    public boolean apply(HttpServletRequest request, Route route, RateLimitUtils rateLimitUtils, String matcher) {
        return StringUtils.isBlank(matcher) || matcher.equals( Optional.ofNullable(route).map(Route::getId).orElse(StringUtils.EMPTY));
    }

    @Override
    public String key(HttpServletRequest request, Route route, RateLimitUtils rateLimitUtils, String matcher) {
        return     Optional.ofNullable(route).map(Route::getId).orElse(StringUtils.EMPTY);
    }

    @Override
    public String getLimitType() {
        return RateLimitType.APPLICATION.getLimitType();
    }
}
