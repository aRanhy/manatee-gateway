package com.ranhy.framework.manatee.gateway.ratelimit.filters;

import com.ranhy.framework.manatee.gateway.common.constants.RespCodeEnum;
import com.ranhy.framework.manatee.gateway.common.util.JsonUtils;
import com.ranhy.framework.manatee.gateway.ratelimit.config.configuration.RateLimitMarkConfiguration;
import com.ranhy.framework.manatee.gateway.ratelimit.config.exception.ManateeRateLimitException;
import com.ranhy.framework.manatee.gateway.ratelimit.config.factory.KeyResolverFactory;
import com.ranhy.framework.manatee.gateway.ratelimit.config.factory.RateLimitPolicyFactory;
import com.ranhy.framework.manatee.gateway.ratelimit.config.properties.RateLimitProperties;
import com.ranhy.framework.manatee.gateway.ratelimit.config.supports.FirewallUtils;
import com.ranhy.framework.manatee.gateway.ratelimit.config.supports.RateLimitKeyGenerator;
import com.ranhy.framework.manatee.gateway.ratelimit.config.supports.RateLimitUtils;
import com.netflix.zuul.context.RequestContext;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.cloud.netflix.zuul.filters.Route;
import org.springframework.cloud.netflix.zuul.filters.RouteLocator;
import org.springframework.http.HttpStatus;
import org.springframework.web.util.UrlPathHelper;

import javax.servlet.http.HttpServletRequest;

import static org.springframework.cloud.netflix.zuul.filters.support.FilterConstants.PRE_TYPE;

@Slf4j
public class FirewallPreFilter extends AbstractRateLimitFilter{

    private final RateLimitProperties properties;

    private final FirewallUtils firewallUtils;


    public FirewallPreFilter(final RateLimitMarkConfiguration.RateLimitMark rateLimitMark,
                             final RateLimitProperties properties, final RouteLocator routeLocator,
                             final UrlPathHelper urlPathHelper,  final RateLimitKeyGenerator rateLimitKeyGenerator,final RateLimitUtils rateLimitUtils,
                             final KeyResolverFactory keyResolverFactory, final RateLimitPolicyFactory rateLimitPolicyFactory,
                             final FirewallUtils firewallUtils) {
        super(rateLimitMark, properties, routeLocator, urlPathHelper, rateLimitUtils, keyResolverFactory, rateLimitPolicyFactory);
        this.properties=properties;
        this.firewallUtils=firewallUtils;
    }


    @Override
    public boolean doShouldFilter() {
        return properties.isFireWall() && StringUtils.isNotBlank(realIp(RequestContext.getCurrentContext().getRequest()));
    }


    @Override
    public String filterType() {
        return PRE_TYPE;
    }

    @Override
    public int filterOrder() {
        return properties.getPreFilterOrder()-1;
    }

    @Override
    public String filterDesc() {
        return "firewall";
    }

    @Override
    public Object doRun() {


        final RequestContext ctx = RequestContext.getCurrentContext();
        final HttpServletRequest request = ctx.getRequest();
        final Route route = route(request);
        String realIp = realIp(request);
        try {

            if (StringUtils.isNotBlank(realIp) && firewallUtils.isBlockValue(realIp) && !firewallUtils.isWhiteValue(realIp)) {
                HttpStatus tooManyRequests = HttpStatus.FORBIDDEN;
                ctx.setResponseStatusCode(tooManyRequests.value());
                log.warn("black list detected ip={} , route={} , restricted request", realIp , JsonUtils.beanToJson(route));
                throw new ManateeRateLimitException(RespCodeEnum.ACCESS_DENIAL, HttpStatus.FORBIDDEN.value());
            }

        }catch (ManateeRateLimitException e){
            throw  e;
        }catch (Exception e){
            e.printStackTrace();
            log.error("check black and white list failed , request cleared , errorMsg={} route={} ",e.getMessage(), JsonUtils.beanToJson(route));
        }
        return null;
    }



}
