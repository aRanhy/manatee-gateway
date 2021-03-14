package com.ranhy.framework.manatee.gateway.ratelimit.config.supports;

/**
 * @author	 hz18093501 hongyu.ran
 * @date	2019年8月8日
 */
public interface RateLimiterErrorHandler {

    void handleSaveError(String key, Exception e);

    void handleFetchError(String key, Exception e);

    void handleError(String msg, Exception e);

}


