package com.ranhy.framework.manatee.gateway.ratelimit.config.algorithm;

import com.google.common.base.Preconditions;
import com.ranhy.framework.manatee.gateway.ratelimit.config.util.Uninterruptibles;

import java.util.concurrent.atomic.AtomicLong;

import static java.util.concurrent.TimeUnit.*;


/**
 * 升级版令牌桶算法
 * 支持短暂爆发流量后平滑处理请求，避免突刺现象。
 * @author	 hz18093501 hongyu.ran
 * @date	2019年8月8日
 */


public class RateLimitBucket extends AbstractGroupServeCount    {


    /**
     * 滑动窗口时间(单位:秒)
     */
    private long refreshInterval;

    /**
     * 往桶投放许可证速率 (单位:一个窗口时间)
     */
    private  volatile long permitsRateLimit;
    /**
     * 桶大小
     */
    private  volatile long burstCapacity;
    /**
     * 剩余许可证
     */
    private  volatile long  storePermits;
    /**
     * 最近一次投放许可证时间戳
     */
    private  volatile long  refreshPoint;

    /**
     * 下一次可获取令牌的时间戳
     */
    private  volatile long  nextPermitPoint;

    /**
     * 互斥对象
     */
    private volatile Object mutexObject;


    /**
     * 产生令牌的时间间隔（单位:微秒）
     */
    private  long  stableIntervalMicros;


    /**
     * 休眠等待获取令牌数
     */
    private  final AtomicLong sleepWaitPermits = new AtomicLong(0);


    RateLimitBucket(long refreshInterval, long permitsRateLimit, long burstCapacity, long storePermits , long refreshPoint){
        this.refreshInterval = refreshInterval;
        this.permitsRateLimit=permitsRateLimit;
        this.burstCapacity=burstCapacity;
        this.storePermits=storePermits;
        this.refreshPoint=refreshPoint;
        this.stableIntervalMicros = SECONDS.toMicros(refreshInterval) /  permitsRateLimit;
    }


    public static RateLimitBucket create(long permitsRateLimit ){
        return create(1,permitsRateLimit,Math.max(permitsRateLimit/2,1) );
    }

    public static RateLimitBucket create(long refreshInterval, long permitsRateLimit ){
        return create(refreshInterval,permitsRateLimit,Math.max(permitsRateLimit/2,1) );
    }

    public static RateLimitBucket create(long refreshInterval, long permitsRateLimit , long burstCapacity){
        Preconditions.checkArgument(permitsRateLimit>=1 && burstCapacity>= 1 && permitsRateLimit >= burstCapacity ,"burstCapacity can not greater than permitsRateLimit");
        return new RateLimitBucket(refreshInterval,permitsRateLimit,burstCapacity,burstCapacity,System.nanoTime());
    }


    public long acquire() {
        return acquire(1);
    }


    public long acquire(long permits) {

        //one check
        if(sleepWaitPermits.get() >= permitsRateLimit){
            return -1;
        }

        if(sleepWaitPermits.get() ==0 ){

            long remaining = consumeAndRefreshPermits(permits);
            if(remaining >= 0){
                return remaining;
            }else{
                return blockUntilHoldPermits(permits);
            }

        }else{

           return blockUntilHoldPermits(permits);
        }
    }



    /**
     * 直到获取到指定许可证为止 或者 超出了休眠线程上限(休眠线程上限默认为 单位窗口时间产生的许可证数量，若超出可断定已超出限流速率)
     * @param permits
     * @return 当前剩余可以许可证
     */
    private long blockUntilHoldPermits(long permits){

        long microsToWait;

        synchronized (mutex()){
            //double check
            if(sleepWaitPermits.get() >= permitsRateLimit){
                return -1;
            }
            if(sleepWaitPermits.get() == 0 ){
                sleepWaitPermits.addAndGet(permits);
                nextPermitPoint = NANOSECONDS.toMicros(System.nanoTime())+ permits*stableIntervalMicros;
            }else{
                sleepWaitPermits.addAndGet(permits);
                nextPermitPoint = nextPermitPoint + permits*stableIntervalMicros;
            }

            microsToWait =Math.max(nextPermitPoint - NANOSECONDS.toMicros(System.nanoTime()),0) ;
        }


        if(microsToWait > 0){
            Uninterruptibles.sleepUninterruptibly(microsToWait,MICROSECONDS);
        }
        refreshPoint=System.nanoTime();
        sleepWaitPermits.addAndGet(-permits);
        return 0;

    }


    /**
     * 消费指定数量的许可证
     * @param permits
     * @return 当前剩余可以许可证
     */
    private long consumeAndRefreshPermits(long permits){

        synchronized (mutex()){

            //double check
            if(sleepWaitPermits.get() == 0){
                return consumeAndRefreshPermitsNolock(permits);
            }else{
                return -1;
            }

        }
    }

    private long consumeAndRefreshPermitsNolock(long permits){
        refreshPermits();

        if(permits>storePermits){
            return -1;
        }else {
            storePermits-=permits;
            return storePermits;
        }
    }

    public boolean tryAcquire() {
        return tryAcquire(1);
    }


    public boolean tryAcquire(long permits) {

        synchronized (mutex()){
            return tryAcquireNoLock(permits);
        }
    }

     private boolean tryAcquireNoLock(long permits) {

        refreshPermits();

        if(permits>storePermits){
            return false;
        }else {
            storePermits-=permits;
            return true;
        }
    }

    /**
     * 投放令牌
     */
    private void refreshPermits() {

        long currentPoint=System.nanoTime();
        long permits= NANOSECONDS.toMicros( currentPoint-refreshPoint ) / stableIntervalMicros ;
        if(permits >0){
            storePermits=Math.min(storePermits+permits,burstCapacity);
            refreshPoint=currentPoint;
        }
        if(permits< 0 ){
            resetPermits();
        }
    }

    /**
     * 复位
     */
    private void resetPermits(){

        storePermits=burstCapacity;
        refreshPoint=System.nanoTime();
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



    @Override
    public long getRefreshInterval() {
        return refreshInterval;
    }


    /**
     * 性能测试

    public static void catfishLimit(long permitsRatelimit) throws InterruptedException {


        RateLimitBucket rateLimitPermit =  RateLimitBucket.create(1,permitsRatelimit,1);

        int hastoken=0;
        long start =System.nanoTime();
        for(int i=0 ; i < permitsRatelimit*1 ; i++){
            if(rateLimitPermit.acquire()>=0){
                hastoken++;
            }
        }
        System.out.println("catfishLimit use time:"+(NANOSECONDS.toMillis(System.nanoTime()-start)  ) + " ms" );
        System.out.println("single thread hold Permit:"+hastoken);
    }

    public static void multCatfishLimit(int threadCount ,long permitsRatelimit) throws InterruptedException {

        CountDownLatch countDownLatch=new CountDownLatch(threadCount);
        AtomicInteger hastoken= new AtomicInteger(0);

        //不能精准控制多线程在同一个时间点同时执行
        CyclicBarrier cyclicBarrier= new CyclicBarrier(threadCount);
        RateLimitBucket rateLimitPermit = RateLimitBucket.create(1,permitsRatelimit,1);

        AtomicLong startTime= new AtomicLong(0);


        for (int i = 0; i < threadCount; i++) {
            Thread thread=new Thread(()->{
                try {
                    cyclicBarrier.await();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (BrokenBarrierException e) {
                    e.printStackTrace();
                }
                startTime.compareAndSet(0,System.nanoTime());
                for (int j = 0; j < 1; j++) {
                    if( rateLimitPermit.acquire()>=0){
                        hastoken.incrementAndGet();
                    }
                }

                countDownLatch.countDown();

            },"ratelimit-"+i);
            thread.start();

        }
        countDownLatch.await();
        System.out.println("catfishLimit use time:"+ NANOSECONDS.toMillis(System.nanoTime()-startTime.get()) +" ms");
        System.out.println("mult thread hold Permit:"+hastoken.get());

    }

    public static void main(String[] args) throws Exception {
        //catfishLimit(1000);
        multCatfishLimit(1000,2000);

    }
     **/
}
