/**
 * Copyright (c) 2006-2016 Huize Ltd. All Rights Reserved. 
 *  
 * This code is the confidential and proprietary information of   
 * Hzins. You shall not disclose such Confidential Information   
 * and shall use it only in accordance with the terms of the agreements   
 * you entered into with Huize,http://www.huize.com.
 *  
 */
package com.ranhy.framework.manatee.gateway.ratelimit.config.repository;

import com.ranhy.framework.manatee.gateway.ratelimit.config.entity.Rate;
import com.ranhy.framework.manatee.gateway.ratelimit.config.properties.RepositoryType;
import com.ranhy.framework.manatee.gateway.ratelimit.config.supports.RateLimiterErrorHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.Objects;
import java.util.concurrent.locks.Lock;

import static java.util.concurrent.TimeUnit.SECONDS;

/**
 * @author	 hz18093501 hongyu.ran
 * @date	2019年8月8日
 */
@Slf4j
@RequiredArgsConstructor
@SuppressWarnings("unchecked")
public class RedisRateLimiter extends AbstractCacheRateLimiter<Lock> {


	private final RedisTemplate redisTemplate;
	private final RateLimiterErrorHandler rateLimiterErrorHandler;

	@Override
	protected Lock getKeyLock(String key) {
		return getRepository().getAndUpdateElement(key);
	}

	@Override
	protected Lock setKeyLock(String key, Lock keyLock , Long refreshInterval) {
		return	getRepository().setAndDelExpireElement(key, keyLock ,getElementExpireTime(refreshInterval) );
	}


	@Override
	protected void calcRemainingLimit(final Long limit, final Long refreshInterval,
									  final String key, final Rate rate) {
		if (Objects.nonNull(limit)) {
			Long remaining = calcRemaining(limit, refreshInterval, 1, key, rate);
			rate.setRemaining(remaining);
		}
	}

	private Long calcRemaining(Long limit, Long refreshInterval, long usage,
							   String key, Rate rate) {
		rate.setReset(SECONDS.toMillis(refreshInterval));
		Long current = 0L;
		try {
			current = redisTemplate.opsForValue().increment(key, usage);
			// Redis returns the value of key after the increment, check for the first increment, and the expiration time is set
			if (current != null && current.equals(usage)) {
				handleExpiration(key, refreshInterval);
			}
		} catch (RuntimeException e) {
			String msg = "Failed retrieving rate for " + key + ", will return the current value";
			rateLimiterErrorHandler.handleError(msg, e);
		}
		return Math.max(-1, limit - current);
	}

	private void handleExpiration(String key, Long refreshInterval) {
		try {
			this.redisTemplate.expire(key, refreshInterval, SECONDS);
		} catch (RuntimeException e) {
			String msg = "Failed retrieving expiration for " + key + ", will reset now";
			rateLimiterErrorHandler.handleError(msg, e);
		}
	}

	@Override
	public RepositoryType getRepositoryType() {
		return RepositoryType.REDIS;
	}
}
