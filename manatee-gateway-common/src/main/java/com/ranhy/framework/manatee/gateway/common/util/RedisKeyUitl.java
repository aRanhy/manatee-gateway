package com.ranhy.framework.manatee.gateway.common.util;

import com.ranhy.framework.manatee.gateway.common.constants.GateWayConstants;
import com.ranhy.framework.manatee.gateway.common.constants.RedisPrefixKey;


/**
 * @author	 hz18093501 hongyu.ran
 * @date	2019年8月8日
 */
public class RedisKeyUitl {


    /**
     * 获取应用serviceId的接口访问配置的key
     * @param serviceId 服务名
     * @return
     */
    public static String getServiceAclConfigKey(String serviceId ){
        return RedisPrefixKey.SERVICE_ACL_CONFIG.getKey()+ GateWayConstants.REDIS_KEY_SPLIT+serviceId ;
    }

    /**
     * 获取分组gatewayApplicationName下所有配置了接口访问配置的服务列表的key
     * @param gatewayApplicationName 分组应用名
     * @return
     */
    public static String getAclServiceListKey(String gatewayApplicationName){

        return RedisPrefixKey.ACL_SERVICE_LIST.getKey()+GateWayConstants.REDIS_KEY_SPLIT+"catfish-gateway-group";

    }

    /**
     * 获取分组gatewayApplicationName下访问接口配置的版本号key
     * @param gatewayApplicationName 分组应用名
     * @return
     */
    public static String getAclConfigVersionKey(String gatewayApplicationName){

        return RedisPrefixKey.ACL_CONFIG_VERSION.getKey() ;

    }

    /**
     * 限流白名单key
     * @return
     */
    public static String getWhiteListKey(){

        return RedisPrefixKey.RATE_LIMIT_WHITE_LIST.getKey()  ;

    }

    /**
     * 限流黑名单key
     * @return
     */
    public static String getBlackListKey(){

        return RedisPrefixKey.RATE_LIMIT_BLACK_LIST.getKey()  ;

    }



}
