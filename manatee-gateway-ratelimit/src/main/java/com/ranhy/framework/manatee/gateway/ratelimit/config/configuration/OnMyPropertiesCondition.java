package com.ranhy.framework.manatee.gateway.ratelimit.config.configuration;


import com.ranhy.framework.manatee.gateway.ratelimit.config.annotation.ConditionalOnMyProperty;
import com.ranhy.framework.manatee.gateway.ratelimit.config.properties.RateLimitProperties;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.boot.autoconfigure.condition.ConditionOutcome;
import org.springframework.boot.autoconfigure.condition.SpringBootCondition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.context.annotation.ConfigurationCondition;
import org.springframework.core.annotation.Order;
import org.springframework.core.type.AnnotatedTypeMetadata;

@Order
public  class OnMyPropertiesCondition extends SpringBootCondition implements ConfigurationCondition {

    @Override
    public ConfigurationPhase getConfigurationPhase() {
        return ConfigurationPhase.REGISTER_BEAN;
    }


    @Override
    public ConditionOutcome getMatchOutcome(ConditionContext context, AnnotatedTypeMetadata metadata) {
        String[] propertiesName = (String[]) metadata.getAnnotationAttributes( ConditionalOnMyProperty.class.getName()).get("repositoryContain");
        ConfigurableListableBeanFactory beanFactory = context.getBeanFactory();
        RateLimitProperties properties = beanFactory.getBean(RateLimitProperties.class);
        if (propertiesName != null) {

            for (String repository : propertiesName){
                for (int i = 0; i <50 ; i++) {
                    String key= RateLimitProperties.PREFIX+".policies["+i+"].repository";
                    String value = context.getEnvironment().getProperty(key);
                    if (repository.equalsIgnoreCase(value)) {
                        return new ConditionOutcome(true, "get properties");
                    }
                }
            }


        }
        return new ConditionOutcome(false, "non get properties");
    }

}