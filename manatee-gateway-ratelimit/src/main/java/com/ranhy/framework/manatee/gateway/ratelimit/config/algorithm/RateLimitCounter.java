package com.ranhy.framework.manatee.gateway.ratelimit.config.algorithm;

import static java.util.concurrent.TimeUnit.SECONDS;

/**
 * 计数器算法
 * 主要支持低频请求
 * @author	 hz18093501 hongyu.ran
 * @date	2019年8月8日
 */

public class RateLimitCounter extends AbstractGroupServeCount   {

    /**
     * 滑动窗口时间(单位:秒)
     */
    private long refreshInterval;


    /**
     * 产生许可证数量 (单位:一个窗口时间)
     */
    private  volatile long limit;

    /**
     * 剩余许可证
     */
    private  volatile long  storePermits;
    /**
     * 上一次滑动窗口的时间戳
     */
    private  volatile long  refreshPoint;

    /**
     * 互斥对象
     */
    private volatile Object mutexObject;


    public RateLimitCounter(long refreshInterval, long limit, long storePermits, long refreshPoint) {
        this.refreshInterval=refreshInterval;
        this.limit=limit;
        this.storePermits=storePermits;
        this.refreshPoint=refreshPoint;
    }


    public static RateLimitCounter create( long refreshInterval,long limit,long storePermits ){
        return new RateLimitCounter(refreshInterval,limit,storePermits,System.currentTimeMillis());
    }

    @Override
    public boolean tryAcquire() {
        return  tryAcquire(  1);
    }

    @Override
    public boolean tryAcquire(long permits) {

        synchronized (mutex()){

            refreshPermits();

            if(storePermits>= permits){
                storePermits -= permits;
                return true;
            }
            return false;

        }


    }

    @Override
    public long acquire() {
        return acquire(1);
    }

    @Override
    public long acquire(long permits) {

        synchronized (mutex()){

            refreshPermits();

            if(storePermits>= permits){
                return storePermits -= permits;

            }
            return -1;
        }
    }

    @Override
    public long getRefreshInterval() {
        return refreshInterval;
    }


    /**
     * 获取互斥对象
     * @return
     */
    private Object mutex() {

        Object mutex = mutexObject;
        if (mutex == null) {
            synchronized (this) {
                mutex = mutexObject;
                if (mutex == null) {
                    mutexObject = mutex = new Object();
                }
            }
        }
        return mutex;
    }

    /**
     * 投放令牌
     */
    private void refreshPermits() {

        long past =System.currentTimeMillis() - refreshPoint;

        if(past > SECONDS.toMillis(refreshInterval) || past<0 ){
            storePermits=limit;
            refreshPoint=System.currentTimeMillis();
        }

    }

}
