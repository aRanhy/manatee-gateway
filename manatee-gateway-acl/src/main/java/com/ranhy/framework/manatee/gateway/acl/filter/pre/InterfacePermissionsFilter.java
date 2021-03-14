package com.ranhy.framework.manatee.gateway.acl.filter.pre;

import com.ranhy.framework.manatee.gateway.acl.config.exception.ManateeAclException;
import com.ranhy.framework.manatee.gateway.acl.filter.AbstractAclFilter;
import com.ranhy.framework.manatee.gateway.acl.config.configuration.AclMarkConfiguration;
import com.ranhy.framework.manatee.gateway.acl.config.factory.AclConfigFactory;
import com.ranhy.framework.manatee.gateway.acl.config.properties.ManateeAclProperties;
import com.ranhy.framework.manatee.gateway.common.constants.GateWayConstants;
import com.ranhy.framework.manatee.gateway.common.constants.RespCodeEnum;
import com.ranhy.framework.manatee.gateway.common.protocol.AclConfig;
import com.ranhy.framework.manatee.gateway.common.protocol.Request;
import com.ranhy.framework.manatee.gateway.common.resolver.ManateeMessageResolver;
import com.ranhy.framework.manatee.gateway.common.util.JsonUtils;
import com.netflix.zuul.context.RequestContext;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.cloud.netflix.zuul.filters.RouteLocator;
import org.springframework.http.HttpStatus;
import org.springframework.web.util.UrlPathHelper;

import javax.servlet.http.HttpServletRequest;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.springframework.cloud.netflix.zuul.filters.support.FilterConstants.PRE_TYPE;

/**
 * @author	 hz18093501 hongyu.ran
 * @date	2019年8月8日
 */

@Slf4j
public class InterfacePermissionsFilter extends AbstractAclFilter {

    final private ManateeAclProperties properties;

    final private AclConfigFactory aclConfigFactory;

    public InterfacePermissionsFilter(final ManateeAclProperties properties, final AclMarkConfiguration.AclMark aclMark,
                                      final AclConfigFactory aclConfigFactory , final   RouteLocator routeLocator,
                                      final UrlPathHelper urlPathHelper) {
        super(properties, aclMark,routeLocator,urlPathHelper);
        this.properties=properties;
        this.aclConfigFactory=aclConfigFactory;
    }

    @Override
    public String filterType() {
        return PRE_TYPE;
    }

    @Override
    public int filterOrder() {
        return properties.getPreFilterOrder();
    }


    @Override
    public Object doRun() {
        RequestContext ctx = RequestContext.getCurrentContext();
        HttpServletRequest request = ctx.getRequest();
        String  body=null;String clientApplicationName=null;
        try {

            body = request.getReader().lines().collect(Collectors.joining(System.lineSeparator()));
            Request requestBody= ManateeMessageResolver.getInitialize().parseMessage(body);
            clientApplicationName=request.getHeader(GateWayConstants.CLIENT_APPLICATION_NAME);

            if(null != requestBody && StringUtils.isNotBlank(clientApplicationName)){

                Optional<AclConfig> aclConfig=aclConfigFactory.getAclConfig(requestBody.getServiceId(),requestBody.getCommand());

                if( !aclConfigFactory.validPermission(aclConfig,clientApplicationName) ){
                    log.debug("the caller {} has been intercepted, service={} interface={} ",clientApplicationName,requestBody.getServiceId(),requestBody.getCommand());
                    throw new ManateeAclException(RespCodeEnum.ACCESS_DENIAL, HttpStatus.FORBIDDEN.value());
                }
            }

        }catch (ManateeAclException e){
            throw e;
        }catch (Exception e){
            e.printStackTrace();
            log.error("interface authentication failed , Request cleared , errorMsg={}  client={} route={} body={} ",e.getMessage(),clientApplicationName, JsonUtils.beanToJson(route(request)),body);
        }

        return null;
    }
}
