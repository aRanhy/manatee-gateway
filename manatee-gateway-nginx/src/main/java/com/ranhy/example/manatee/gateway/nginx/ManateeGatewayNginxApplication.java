   
package com.ranhy.example.manatee.gateway.nginx;

import com.ranhy.framework.manatee.gateway.ratelimit.config.annotation.EnableManateeRateLimit;
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
@MapperScan(basePackages = "com.ranhy.pluto.manatee.gateway.nginx.manage.persistence")
@PropertySource(value={"classpath:conf/env/datasource.properties"
 })
@EnableZuulProxy
@SpringBootApplication
@EnableManateeRateLimit
@Slf4j
public class ManateeGatewayNginxApplication {


    public static void main(String[] args) {
	long startTime = System.currentTimeMillis();
	boolean isException = false;
	try
	{
	    System.setProperty("spring.config.location", "classpath:conf/env/server.properties");
	    SpringApplication.run(ManateeGatewayNginxApplication.class);
	}catch (Exception e) {
	    isException = true;
	    log.error(e.getMessage(), e);

	} finally {
	    long endTime = System.currentTimeMillis();
	    log.info("服务[{}]启动--{}, 耗时[{}]毫秒", ManateeGatewayNginxApplication.class.getSimpleName(), (isException ? "异常" : "完成"), (endTime - startTime));
	}
    }
}
 