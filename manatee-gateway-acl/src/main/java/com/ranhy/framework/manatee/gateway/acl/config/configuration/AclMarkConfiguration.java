package com.ranhy.framework.manatee.gateway.acl.config.configuration;

import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author	 hz18093501 hongyu.ran
 * @date	2019年8月8日
 */
@Configuration
@AutoConfigureBefore(AclAutoConfiguration.class)
public class AclMarkConfiguration {

    @Bean
    AclMark aclMark(){
        return new AclMark();
    }
    public class AclMark{
    }
}
