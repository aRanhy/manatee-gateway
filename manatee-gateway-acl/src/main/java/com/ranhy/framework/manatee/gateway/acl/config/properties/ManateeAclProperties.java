package com.ranhy.framework.manatee.gateway.acl.config.properties;


import static java.util.concurrent.TimeUnit.MINUTES;
import static org.springframework.cloud.netflix.zuul.filters.support.FilterConstants.PRE_DECORATION_FILTER_ORDER;
import lombok.Data;
import lombok.NoArgsConstructor;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * @author	 hz18093501 hongyu.ran
 * @date	2019年8月8日
 */

@Data
@Validated
@NoArgsConstructor
@ConfigurationProperties(ManateeAclProperties.PREFIX)
public class ManateeAclProperties {

    public static final String PREFIX="manatee.acl";

    private boolean enabled  = true;

    private int preFilterOrder = PRE_DECORATION_FILTER_ORDER+1;

    private long configSyncInterval = MINUTES.toSeconds(1L);


}
