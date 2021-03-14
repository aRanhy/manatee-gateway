   
package com.ranhy.framework.manatee.gateway.ratelimit.config.supports;

import com.ranhy.framework.manatee.gateway.ratelimit.config.properties.RateLimitProperties;
import org.springframework.cloud.netflix.zuul.filters.Route;

import javax.servlet.http.HttpServletRequest;
import java.util.Optional;

/**
 * @author	 hz18093501 hongyu.ran
 * @date	2019年8月8日
 */
public interface RateLimitKeyGenerator {

    Optional<String> key(HttpServletRequest request, Route route, RateLimitProperties.Policy policy);
}
 