   
package com.ranhy.framework.manatee.gateway.ratelimit.config.supports;

import com.ranhy.framework.manatee.gateway.ratelimit.config.factory.KeyResolverFactory;
import com.ranhy.framework.manatee.gateway.ratelimit.config.properties.RateLimitProperties;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang.StringUtils;
import org.springframework.cloud.netflix.zuul.filters.Route;

import javax.servlet.http.HttpServletRequest;
import java.util.Optional;
import java.util.StringJoiner;


/**
 * @author	 hz18093501 hongyu.ran
 * @date	2019年8月8日
 */
@RequiredArgsConstructor
public class DefaultRateLimitKeyGenerator implements RateLimitKeyGenerator{
    

    private final RateLimitProperties properties;
    private final RateLimitUtils rateLimitUtils;
    private final KeyResolverFactory keyResolverFactory;
    @Override
    public Optional<String> key(HttpServletRequest request, Route route, RateLimitProperties.Policy policy) {

        final StringJoiner joiner = new StringJoiner(":");
        joiner.add(properties.getKeyPrefix());

        policy.getConfigs().forEach(
                config -> joiner.add(keyResolverFactory.getKeyResolverStore().get(config.getRateLimitType()).key(request,route,rateLimitUtils,config.getMatch()))
        );
        if(properties.getKeyPrefix().equals(joiner.toString())){
            return Optional.empty();
        }else{
            joiner.add(policy.getRefreshInterval().toString());

            if(StringUtils.isNotBlank(policy.getExtendKey())){
                joiner.add(policy.getExtendKey());
            }
            return Optional.ofNullable(joiner.toString())  ;
        }

    }

}
 