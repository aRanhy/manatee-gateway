

package com.ranhy.framework.manatee.gateway.ratelimit.config.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * @author	 hz18093501 hongyu.ran
 * @date	2019年8月8日
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class RateLimitConstants {

    public static final String HEADER_LIMIT = "X-RateLimit-Limit-";
    public static final String HEADER_REMAINING = "X-RateLimit-Remaining-";
    public static final String HEADER_RESET = "X-RateLimit-Reset-";

}
