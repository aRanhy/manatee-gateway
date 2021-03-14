
package com.ranhy.framework.manatee.gateway.ratelimit.config.repository;

import com.ranhy.framework.manatee.gateway.ratelimit.config.algorithm.RateLimit;
import com.ranhy.framework.manatee.gateway.ratelimit.config.properties.RepositoryType;
import com.ranhy.framework.manatee.gateway.ratelimit.config.supports.RateLimiterErrorHandler;


/**
 * @author	 hz18093501 hongyu.ran
 * @date	2019年8月8日
 */
public class MemoryRateLimiter extends AbstractRateLimiter<RateLimit> {

    public MemoryRateLimiter(RateLimiterErrorHandler rateLimiterErrorHandler){
        super(rateLimiterErrorHandler );
    }

    @Override
    protected RateLimit  getPermit(String key) {
	   return getRepository().getAndUpdateElement(key);
    }

    @Override
    protected RateLimit  savePermit(String key, RateLimit rateLimit ) {
        return	getRepository().setAndDelExpireElement(key, rateLimit,getElementExpireTime(rateLimit.getRefreshInterval()));
    }

    @Override
    protected RateLimit replacePermit(String key, RateLimit rateLimit) {
        return	getRepository().replaceElement(key, rateLimit , getElementExpireTime(rateLimit.getRefreshInterval()));
    }

    @Override
    public RepositoryType getRepositoryType() {
        return RepositoryType.MEMORY;
    }

}
 