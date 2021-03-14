
package com.ranhy.example.manatee.gateway.group;

import com.ranhy.framework.manatee.gateway.acl.config.annotation.EnableManateeAcl;
import lombok.extern.slf4j.Slf4j;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.cloud.netflix.zuul.EnableZuulProxy;


/**
 * @author	 hz18093501 hongyu.ran
 * @date	2019年8月8日
 */
@EnableZuulProxy
@EnableEurekaClient
@SpringBootApplication
@EnableManateeAcl
@Slf4j
public class ManateeGatewayGroupApplication {

    public static void main(String[] args) {
	long startTime = System.currentTimeMillis();
	boolean isException = false;
	try
	{
	    System.setProperty("spring.config.location", "classpath:conf/env/server.properties");
	    SpringApplication.run(ManateeGatewayGroupApplication.class);
	}catch (Exception e) {
	    isException = true;
	    log.error(e.getMessage(), e);

	} finally {
	    long endTime = System.currentTimeMillis();
	    log.info("服务[{}]启动--{}, 耗时[{}]毫秒", ManateeGatewayGroupApplication.class.getSimpleName(), (isException ? "异常" : "完成"), (endTime - startTime));
	}
    }
}
 