package com.ranhy.framework.manatee.gateway.ratelimit.config.annotation;

import com.ranhy.framework.manatee.gateway.ratelimit.config.configuration.OnMyPropertiesCondition;
import org.springframework.context.annotation.Conditional;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE, ElementType.METHOD })
@Documented
@Conditional(OnMyPropertiesCondition.class)
public @interface ConditionalOnMyProperty {
    String[] repositoryContain() default {};
}