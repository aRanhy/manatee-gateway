package com.ranhy.framework.manatee.gateway.ratelimit.config.properties;


import lombok.AllArgsConstructor;
import lombok.Getter;
/**
 * @author	 hz18093501 hongyu.ran
 * @date	2019年8月8日
 */
@Getter
@AllArgsConstructor
public enum RateLimitType   {

    /**
     * 限流维度
     */
    APPLICATION("application","应用名"),
    RPC_METHOD("rpc_method","rpc方法名"),
    AREA("area","ip区域"),
    URL("url","url"),
    HTTP_METHOD("http_method","http方法名"),
    ORIGIN("origin","ip");

    public String limitType;

    public String desc;

}
