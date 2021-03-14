package com.ranhy.framework.manatee.gateway.acl.config.supports;

import com.ranhy.framework.manatee.gateway.acl.config.factory.AclConfigFactory;
import com.ranhy.framework.manatee.gateway.acl.config.properties.ManateeAclProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.atomic.AtomicBoolean;

import static java.util.concurrent.TimeUnit.SECONDS;

@RequiredArgsConstructor
@Slf4j
public class AclConfigSync implements ConfigSync{


    final AtomicBoolean closed = new AtomicBoolean(false);
    private Thread configSyncThread = null;
    final ManateeAclProperties properties;
    final AclConfigFactory aclConfigFactory;

    @Override
    public void start() {
        configSyncThread=new Thread(()->{

            log.info("Acl-ConfigSync-Thread 线程已正常启动");
            while (!closed.get()) {
                try {
                    Thread.sleep(SECONDS.toMillis(properties.getConfigSyncInterval()) );
                } catch (Exception e) {
                    e.printStackTrace();
                }

                try {
                    aclConfigFactory.loadConfig();
                }catch (Exception e){
                    e.printStackTrace();
                }

            }

        },"Acl-ConfigSync-Thread");

        configSyncThread.setDaemon(true);
        configSyncThread.start();
    }

    @Override
    public void stop() {

        if(!closed.get() && configSyncThread!=null){
            closed.set(true);
        }
    }
}
