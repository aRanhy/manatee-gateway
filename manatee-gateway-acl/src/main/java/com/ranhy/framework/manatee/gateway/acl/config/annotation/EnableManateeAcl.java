package com.ranhy.framework.manatee.gateway.acl.config.annotation;

import com.ranhy.framework.manatee.gateway.acl.config.configuration.AclAutoConfiguration;
import com.ranhy.framework.manatee.gateway.acl.config.configuration.AclMarkConfiguration;
import com.ranhy.framework.manatee.gateway.acl.config.properties.ManateeAclProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author	 hz18093501 hongyu.ran
 * @date	2019年8月8日
 */
@Configuration
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@EnableConfigurationProperties(ManateeAclProperties.class)
@Import({AclMarkConfiguration.class, AclAutoConfiguration.class})
public @interface EnableManateeAcl {
}
