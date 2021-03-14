package com.ranhy.framework.manatee.gateway.ratelimit.config.repository;

import com.ranhy.framework.manatee.gateway.ratelimit.config.entity.Rate;
import com.ranhy.framework.manatee.gateway.ratelimit.config.properties.RateLimitProperties;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author	 hz18093501 hongyu.ran
 * @date	2019年8月8日
 */
public abstract class AbstractCacheRateLimiter<E> extends AbstarctBase<E> {

    protected abstract Lock getKeyLock(String key);

    protected abstract Lock setKeyLock(String key,Lock  keyLock , Long refreshInterval);

    protected abstract void calcRemainingLimit(Long limit, Long refreshInterval,   String key, Rate rate);

    @Override
    public   Rate consume(RateLimitProperties.Policy policy, String key ) {

        final Long refreshInterval = policy.getRefreshInterval();

        final Rate rate = new Rate(key, policy.getLimit(), null, null);


        Lock lock=getKeyLock(key);
        if(lock == null){
            lock= setKeyLock(key,new ReentrantLock(true) , refreshInterval);
        }

        try {
            lock.lock();
            calcRemainingLimit(policy.getLimit(), refreshInterval , key, rate);
        }finally {
            lock.unlock();
        }

        return rate;
    }




}