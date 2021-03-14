package com.ranhy.framework.manatee.gateway.acl.config.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class AclConstants {

    public static final String CONNECT_TIMEOUT = "X-Rpc-ConnectTimeoutMillis";
    public static final String READ_TIMEOUT = "X-Rpc-ReadTimeoutMillis";

}