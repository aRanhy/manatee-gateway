package com.ranhy.framework.manatee.gateway.ratelimit.config.keyresolver;

import com.ranhy.framework.manatee.gateway.common.protocol.Request;
import com.ranhy.framework.manatee.gateway.common.resolver.ManateeMessageResolver;
import com.ranhy.framework.manatee.gateway.ratelimit.config.properties.RateLimitType;
import com.ranhy.framework.manatee.gateway.ratelimit.config.supports.RateLimitUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.cloud.netflix.zuul.filters.Route;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Optional;
import java.util.stream.Collectors;

public class RpcMethodKeyResolver implements KeyResolver {

    public final static String BEAN_NAME="rpcMethodKeyResolver";

    @Override
    public boolean apply(HttpServletRequest request, Route route, RateLimitUtils rateLimitUtils,/*not null*/ String matcher) {

        return   StringUtils.isBlank(matcher) ||
                getManateeRequest(request)
                        .map(Request::getCommand)
                        .map(command -> StringUtils.isNotBlank(command) && command.trim().equalsIgnoreCase(matcher))
                        .orElse(false);
    }

    @Override
    public String key(HttpServletRequest request, Route route, RateLimitUtils rateLimitUtils, String matcher) {

        return   getManateeRequest(request)
                .map(Request::getCommand)
                .map(command -> command.trim())
                .get()  ;
    }

    @Override
    public String getLimitType() {
        return RateLimitType.RPC_METHOD.getLimitType();
    }

    public Optional<Request> getManateeRequest(HttpServletRequest request){

        String body = null;
        try {
            body = request.getReader().lines().collect(Collectors.joining(System.lineSeparator()));
        } catch (IOException e) {
            e.printStackTrace();
        }
         return Optional.ofNullable(ManateeMessageResolver.getInitialize().parseMessage(body)) ;

    }
}
