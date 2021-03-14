package com.ranhy.framework.manatee.gateway.common.constants;


import lombok.Getter;

import static com.ranhy.framework.manatee.gateway.common.constants.GateWayConstants.REDIS_KEY_SPLIT;

/**
 * @author	 hz18093501 hongyu.ran
 * @date	2019年8月8日
 */
@Getter
public enum RedisPrefixKey {

    GATEWAY_GLOBAL_PREFIX("manatee-gateway"+REDIS_KEY_SPLIT),
    ACL_SERVICE_LIST("acl_service_list"),
    SERVICE_ACL_CONFIG("service_acl_config"),
    ACL_CONFIG_VERSION("acl_config_version"),
    INTERFACE_REFUSE_PREFIX("interface_permissions"),
    RATE_LIMIT_BLACK_LIST("rate_limit_black_list"),
    RATE_LIMIT_WHITE_LIST("rate_limit_white_list");

    private String val;

    RedisPrefixKey(String val) {
        this.val = val;
    }

    public String getKey(){
        return RedisPrefixKey.GATEWAY_GLOBAL_PREFIX.getVal()+val;
    }
}
