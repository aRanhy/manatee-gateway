
package com.ranhy.framework.manatee.gateway.ratelimit.config.repository;

import com.ranhy.framework.manatee.gateway.ratelimit.config.entity.Rate;
import com.ranhy.framework.manatee.gateway.ratelimit.config.properties.RateLimitProperties;
import com.ranhy.framework.manatee.gateway.ratelimit.config.properties.RepositoryType;

/**
 * @author	 hz18093501 hongyu.ran
 * @date	2019年8月8日
 */
public interface RateLimiter {

    Rate consume(RateLimitProperties.Policy policy, String key );

    RepositoryType getRepositoryType();
}
