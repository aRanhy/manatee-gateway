
package com.ranhy.framework.manatee.gateway.ratelimit.config.repository;

import com.ranhy.framework.manatee.gateway.ratelimit.config.algorithm.RateLimit;
import com.ranhy.framework.manatee.gateway.ratelimit.config.algorithm.RateLimitBucket;
import com.ranhy.framework.manatee.gateway.ratelimit.config.algorithm.RateLimitCounter;
import com.ranhy.framework.manatee.gateway.ratelimit.config.configuration.RateLimitAutoConfiguration;
import com.ranhy.framework.manatee.gateway.ratelimit.config.entity.Rate;
import com.ranhy.framework.manatee.gateway.ratelimit.config.properties.AlgorithmType;
import com.ranhy.framework.manatee.gateway.ratelimit.config.properties.RateLimitProperties.Policy;
import com.ranhy.framework.manatee.gateway.ratelimit.config.supports.RateLimiterErrorHandler;
import lombok.RequiredArgsConstructor;

/**
 * @author	 hz18093501 hongyu.ran
 * @date	2019年8月8日
 */
@RequiredArgsConstructor
public abstract class AbstractRateLimiter<E> extends AbstarctBase<E>{

    private final RateLimiterErrorHandler rateLimiterErrorHandler;

    protected abstract RateLimit getPermit(String key);
    protected abstract RateLimit savePermit(String key,RateLimit  rateLimit );
    protected abstract RateLimit replacePermit(String key,RateLimit  rateLimit );


    @Override
    public   Rate consume(final Policy policy, final String key ) {
        Rate rate=  new Rate(key, policy.getLimit(),  null, null);
        try {
            int serveCount = RateLimitAutoConfiguration.GatewayRatelimitDiscoveryRefreshListener.getServeCount();

            RateLimit  permit=getPermit(key);
            if(null == permit  || (policy.getMemory().isGroupRateLimit() && permit.getGroupServeCount() != serveCount )){

                RateLimit newPermit;
                long limit= policy.getMemory().isGroupRateLimit() ?  policy.getLimit() / serveCount : policy.getLimit();
                if(AlgorithmType.COUNTER == getLimitStrategy(policy) ){
                    newPermit= RateLimitCounter.create(policy.getRefreshInterval(), limit, limit);
                }else{
                    if(policy.getMemory().getBurstCapacity() == null ){
                        newPermit= RateLimitBucket.create(policy.getRefreshInterval(), limit);
                    }else{
                        long capacity = policy.getMemory().isGroupRateLimit() ? policy.getMemory().getBurstCapacity() /serveCount :  policy.getMemory().getBurstCapacity();
                        newPermit= RateLimitBucket.create(policy.getRefreshInterval(), limit,capacity );
                    }
                }

                newPermit.setGroupServeCount(serveCount);

                if(null == permit){
                    permit= savePermit(key,newPermit);
                }else {
                    synchronized (permit){
                        RateLimit oldPermit=getPermit(key);
                        if(null == oldPermit || oldPermit.getGroupServeCount() != serveCount){
                            replacePermit(key,newPermit);
                        }
                        permit=newPermit;
                    }
                }
            }

            rate.setRemaining(permit.acquire());

        }catch (Exception e){
            String msg = "Failed retrieving rate for " + key + ", will return the current value";
            rateLimiterErrorHandler.handleError(msg,e);
            e.printStackTrace();
        }
        return  rate;
    }


    public AlgorithmType getLimitStrategy(Policy policy ){

        if(policy.getMemory().getAlgorithm() == AlgorithmType.AUTO){
            return policy.getLimit() / policy.getRefreshInterval() > policy.getMemory().getLowLimitThreshold()? AlgorithmType.LEAKY_BUCKET : AlgorithmType.COUNTER;
        }else{
            return policy.getMemory().getAlgorithm();
        }
    }


}
 