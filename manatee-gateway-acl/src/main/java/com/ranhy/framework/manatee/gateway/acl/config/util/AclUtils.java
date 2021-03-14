package com.ranhy.framework.manatee.gateway.acl.config.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.commons.lang.StringUtils;

import java.util.Optional;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class AclUtils {

    /**
     * 请求超时时间客户端是否已经自定义
     * @return
     */
    public static Boolean isSelfReadTimeOut(){
        return StringUtils.isNotBlank(HeaderUtils.getRequestReadTimeOut());
    }

    public static int getRibbonTimeout(int ribbonReadTimeout, int ribbonConnectTimeout, int maxAutoRetries, int maxAutoRetriesNextServer){
       return Optional.ofNullable(HeaderUtils.getRequestReadTimeOut())
                .map(requestReadTimeOut -> ribbonConnectTimeout+Integer.parseInt(requestReadTimeOut) )
                .orElse( (ribbonReadTimeout + ribbonConnectTimeout) * (maxAutoRetries + 1) * (maxAutoRetriesNextServer + 1));
    }
}
