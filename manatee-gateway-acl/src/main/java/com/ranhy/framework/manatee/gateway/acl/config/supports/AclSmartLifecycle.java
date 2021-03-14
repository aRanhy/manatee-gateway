package com.ranhy.framework.manatee.gateway.acl.config.supports;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.context.SmartLifecycle;

@RequiredArgsConstructor
public class AclSmartLifecycle  implements SmartLifecycle, DisposableBean {

    private boolean isRunning = false;
    final private AclConfigSync aclConfigSync;

    @Override
    public void start() {
        // TODO Auto-generated method stub
        isRunning = true;
        if (aclConfigSync != null) {
            aclConfigSync.start();
        }
    }

    @Override
    public void stop() {
        // TODO Auto-generated method stub
        isRunning = false;
        if (aclConfigSync != null) {
            aclConfigSync.stop();
        }
    }

    @Override
    public boolean isRunning() {
        // TODO Auto-generated method stub
        return isRunning;
    }

    @Override
    public int getPhase() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public void destroy() throws Exception {
        // TODO Auto-generated method stub

    }

    @Override
    public boolean isAutoStartup() {
        // TODO Auto-generated method stub
        return true;
    }

    @Override
    public void stop(Runnable callback) {
        // TODO Auto-generated method stub
        stop();
        callback.run();
        isRunning = false;
    }


}
