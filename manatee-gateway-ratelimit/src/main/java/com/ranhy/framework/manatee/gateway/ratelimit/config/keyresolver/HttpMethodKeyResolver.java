package com.ranhy.framework.manatee.gateway.ratelimit.config.keyresolver;

import com.ranhy.framework.manatee.gateway.ratelimit.config.properties.RateLimitType;
import com.ranhy.framework.manatee.gateway.ratelimit.config.supports.RateLimitUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.cloud.netflix.zuul.filters.Route;

import javax.servlet.http.HttpServletRequest;

/**
 * @author	 hz18093501 hongyu.ran
 * @date	2019年8月8日
 */
public class HttpMethodKeyResolver implements   KeyResolver {

    public final static String BEAN_NAME="httpMethodKeyResolver";

    @Override
    public boolean apply(HttpServletRequest request, Route route, RateLimitUtils rateLimitUtils,/*not null*/ String matcher) {
        return   StringUtils.isBlank(matcher)|| request.getMethod().equalsIgnoreCase(matcher);
    }

    @Override
    public String key(HttpServletRequest request, Route route, RateLimitUtils rateLimitUtils, String matcher) {
        return  request.getMethod()  ;
    }

    @Override
    public String getLimitType() {
        return RateLimitType.HTTP_METHOD.getLimitType();
    }
}
