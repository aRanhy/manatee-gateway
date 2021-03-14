package com.ranhy.framework.manatee.gateway.ratelimit.config.supports;


import lombok.extern.slf4j.Slf4j;
/**
 * @author	 hz18093501 hongyu.ran
 * @date	2019年8月8日
 */
@Slf4j
public class DefaultRateLimiterErrorHandler implements RateLimiterErrorHandler {
    @Override
    public void handleSaveError(String key, Exception e) {
        log.error("Failed saving rate for " + key + ", returning unsaved rate", e);
    }

    @Override
    public void handleFetchError(String key, Exception e) {
        log.error("Failed retrieving rate for " + key + ", will create new rate", e);
    }

    @Override
    public void handleError(String msg, Exception e) {
        log.error(msg, e);
    }
}
