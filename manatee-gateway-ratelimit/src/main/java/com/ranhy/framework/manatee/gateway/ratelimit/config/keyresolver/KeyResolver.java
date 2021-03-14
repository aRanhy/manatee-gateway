package com.ranhy.framework.manatee.gateway.ratelimit.config.keyresolver;


import com.ranhy.framework.manatee.gateway.ratelimit.config.supports.RateLimitUtils;
import org.springframework.cloud.netflix.zuul.filters.Route;

import javax.servlet.http.HttpServletRequest;
/**
 * @author	 hz18093501 hongyu.ran
 * @date	2019年8月8日
 */
public interface KeyResolver {


    boolean apply(HttpServletRequest request, Route route,
                  RateLimitUtils rateLimitUtils, String matcher);

    String key(HttpServletRequest request, Route route,
                               RateLimitUtils rateLimitUtils, String matcher);

    String getLimitType();
}
