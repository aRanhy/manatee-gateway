package com.ranhy.framework.manatee.gateway.common.protocol;

import lombok.Data;

import java.util.List;

@Data
public class AclConfig {
    /**
     * 服务提供方应用应用名
     */
    private String  providerServiceId;
    /**
     * 服务提供方唯一接口标识
     */
    private String  interfaceIdentity;
    /**
     * 仅允许访问的服务列表（应用名）
     */
    private List<String> accessList;
    /**
     * 仅拒绝访问的服务列表（应用名）
     */
    private List<String> refuseList;
}
