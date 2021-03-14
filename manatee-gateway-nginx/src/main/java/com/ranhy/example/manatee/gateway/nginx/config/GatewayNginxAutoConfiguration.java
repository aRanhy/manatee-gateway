package com.ranhy.example.manatee.gateway.nginx.config;

import com.ranhy.framework.manatee.gateway.ratelimit.config.keyresolver.KeyResolver;
import com.ranhy.example.manatee.gateway.nginx.factory.IpInfoFactory;
import com.ranhy.example.manatee.gateway.nginx.keyresolver.AreaKeyResolver;
import com.ranhy.example.manatee.gateway.nginx.manage.service.DwCodIpInfoService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GatewayNginxAutoConfiguration {
    /**
     *地区 KeyResolver
     */
    @Bean
    KeyResolver areaKeyResolver(){
        return new AreaKeyResolver();
    }

    @Bean
    IpInfoFactory ipInfoFactory(){
        return new IpInfoFactory();
    }

    @Bean
    DwCodIpInfoService dwCodIpInfoService(){
        return new DwCodIpInfoService();
    }
}
