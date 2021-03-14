package com.ranhy.framework.manatee.gateway.ratelimit.config.repository;

import com.ranhy.framework.manatee.gateway.ratelimit.config.entity.Element;
import com.ranhy.framework.manatee.gateway.ratelimit.config.util.Repository;
import lombok.Getter;

import java.util.concurrent.ConcurrentHashMap;

import static java.util.concurrent.TimeUnit.MINUTES;
import static java.util.concurrent.TimeUnit.SECONDS;

@Getter
public abstract class AbstarctBase<E>  implements RateLimiter {
    /**
     * 默认仓库元素有效期
     */
    private final long defaultExpireTime = MINUTES.toMillis(10);

    /**
     * 分key限流器集合
     */
    private final Repository<ConcurrentHashMap<String, Element<E>>,E> repository  =  Repository.create(defaultExpireTime);

    public long getElementExpireTime( long refreshInterval){
        long elementExpireTime =  SECONDS.toMillis(refreshInterval);
        return elementExpireTime > defaultExpireTime ?  elementExpireTime : defaultExpireTime;
    }

}
