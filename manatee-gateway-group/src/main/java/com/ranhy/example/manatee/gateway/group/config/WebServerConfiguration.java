package com.ranhy.example.manatee.gateway.group.config;

import org.apache.catalina.connector.Connector;
import org.apache.coyote.http11.Http11NioProtocol;
import org.springframework.boot.context.embedded.EmbeddedServletContainerFactory;
import org.springframework.boot.context.embedded.tomcat.TomcatConnectorCustomizer;
import org.springframework.boot.context.embedded.tomcat.TomcatEmbeddedServletContainerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author	 hz18093501 hongyu.ran
 * @date	2019年8月8日
 */

@Configuration
public class WebServerConfiguration {

	@Bean  
    public EmbeddedServletContainerFactory createEmbeddedServletContainerFactory()  
    {  
        TomcatEmbeddedServletContainerFactory tomcatFactory = new TomcatEmbeddedServletContainerFactory();  
        tomcatFactory.setPort(8081);
        tomcatFactory.addConnectorCustomizers(new MyTomcatConnectorCustomizer());  
        return tomcatFactory;  
    }
}
class MyTomcatConnectorCustomizer implements TomcatConnectorCustomizer  
{  
    public void customize(Connector connector)  
    {  
        Http11NioProtocol protocol = (Http11NioProtocol) connector.getProtocolHandler();  
        System.out.println("最大线程数 = "+protocol.getMaxThreads());
        System.out.println("最大连接数 = "+protocol.getMaxConnections());
        //设置最大连接数  
        protocol.setMaxConnections(20000);  
        //设置最大线程数  
        protocol.setMaxThreads(5000);
        protocol.setMinSpareThreads(300);
        protocol.setConnectionTimeout(30000);  
        
        System.out.println("最大线程数 = "+protocol.getMaxThreads());
        System.out.println("最大连接数 = "+protocol.getMaxConnections());
    }  
}
