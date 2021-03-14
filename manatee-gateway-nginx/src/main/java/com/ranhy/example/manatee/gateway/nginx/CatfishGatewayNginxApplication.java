/**
 * Copyright (c) 2006-2016 Huize Ltd. All Rights Reserved. 
 *  
 * This code is the confidential and proprietary information of   
 * Hzins. You shall not disclose such Confidential Information   
 * and shall use it only in accordance with the terms of the agreements   
 * you entered into with Huize,http://www.huize.com.
 *  
 */   
package com.ranhy.example.manatee.gateway.nginx;

import com.ranhy.framework.manatee.gateway.ratelimit.config.annotation.EnableCatfishRateLimit;
import lombok.extern.slf4j.Slf4j;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.zuul.EnableZuulProxy;
import org.springframework.context.annotation.PropertySource;


/**
 * @author	 hz18093501 hongyu.ran
 * @date	2019年8月8日
 */
@MapperScan(basePackages = "com.huize.pluto.catfish.gateway.nginx.manage.persistence")
@PropertySource(value={"classpath:conf/env/datasource.properties"
 })
@EnableZuulProxy
@SpringBootApplication
@EnableCatfishRateLimit
@Slf4j
public class CatfishGatewayNginxApplication {


    public static void main(String[] args) {
	long startTime = System.currentTimeMillis();
	boolean isException = false;
	try
	{
	    System.setProperty("spring.config.location", "classpath:conf/env/server.properties");
	    SpringApplication.run(CatfishGatewayNginxApplication.class);
	}catch (Exception e) {
	    isException = true;
	    log.error(e.getMessage(), e);

	} finally {
	    long endTime = System.currentTimeMillis();
	    log.info("服务[{}]启动--{}, 耗时[{}]毫秒", CatfishGatewayNginxApplication.class.getSimpleName(), (isException ? "异常" : "完成"), (endTime - startTime));
	}
    }
}
 