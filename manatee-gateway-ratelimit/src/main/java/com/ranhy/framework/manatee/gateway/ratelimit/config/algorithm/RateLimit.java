package com.ranhy.framework.manatee.gateway.ratelimit.config.algorithm;

public interface RateLimit {

    /**
     * 尝试获取许可证
     * @return
     */
    boolean tryAcquire() ;

    /**
     * 尝试获取指定数量许可证
     * @param permits
     * @return
     */
    boolean tryAcquire(long permits) ;


    /**
     * 获取许可证
     * @return 当前剩余可以许可证
     */
    long acquire() ;

    /**
     * 获取指定数量的许可证
     * @param permits
     * @return 当前剩余可以许可证
     */
    long acquire(long permits) ;

    /**
     * 获取限流集群服务个数
     * @return
     */
    int getGroupServeCount();

    /**
     * 设置限流集群服务个数
     * @return
     */
    void setGroupServeCount(int count);

    /**
     * 获取窗口滑动间隔
     * @return
     */
    long getRefreshInterval();

}
