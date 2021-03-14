package com.ranhy.framework.manatee.gateway.ratelimit.config.supports;

public interface FirewallUtils {

    /**
     * 黑名单
     * @param value 限流key
     */
    void pushBlockValue(String value);

    boolean isBlockValue(String value);

    /**
     * 是否存在白名单内
     * @param value  限流key
     */
    boolean isWhiteValue(String value);

}
