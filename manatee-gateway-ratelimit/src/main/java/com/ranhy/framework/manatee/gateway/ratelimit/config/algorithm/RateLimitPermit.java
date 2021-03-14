package com.ranhy.framework.manatee.gateway.ratelimit.config.algorithm;

import com.google.common.base.Preconditions;

import java.util.concurrent.locks.ReentrantLock;

import static java.util.concurrent.TimeUnit.SECONDS;


/**
 * 令牌桶算法
 * 支持短暂爆发流量后平滑处理请求，避免突刺现象。
 * @author	 hz18093501 hongyu.ran
 * @date	2019年8月8日
 */

@Deprecated
public class RateLimitPermit  extends AbstractGroupServeCount    {


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
     * 上一次投放许可证时间戳
     */
    private  volatile long  refreshPoint;

    /**
     * 互斥对象
     */
    private volatile Object mutexObject;

    /**
     * 重入锁（阻塞线程，阻塞的最大线程数量为单位窗口时间的许可证数量，主要用于短暂突发流量后平滑消耗许可证）
     */
    private ReentrantLock  lock ;


    RateLimitPermit(long refreshInterval,  long permitsRateLimit,long burstCapacity, long storePermits ,long refreshPoint){
        this.refreshInterval = refreshInterval;
        this.permitsRateLimit=permitsRateLimit;
        this.burstCapacity=burstCapacity;
        this.storePermits=storePermits;
        this.refreshPoint=refreshPoint;
    }


    public static RateLimitPermit create(long permitsRateLimit ){
        return create(1,permitsRateLimit,Math.max(permitsRateLimit/2,1) );
    }

    public static RateLimitPermit create( long refreshInterval,long permitsRateLimit ){
        return create(refreshInterval,permitsRateLimit,Math.max(permitsRateLimit/2,1) );
    }

    public static RateLimitPermit create(long refreshInterval, long permitsRateLimit , long burstCapacity){
        Preconditions.checkArgument(permitsRateLimit>=1 && burstCapacity>= 1 && permitsRateLimit >= burstCapacity ,"burstCapacity can not greater than permitsRateLimit");
        return new RateLimitPermit(refreshInterval,permitsRateLimit,burstCapacity,burstCapacity,System.nanoTime());
    }


    public long acquire() {
        return acquire(1);
    }


    public long acquire(long permits) {


        int blockCount= getLock().getQueueLength();
        //one check
        if(blockCount >= permitsRateLimit){
            return -1;
        }

        if(blockCount ==0 ){

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
     * 直到获取到指定许可证为止 或者 超出了阻塞队列(阻塞队列大小默认为 单位窗口时间产生的许可证数量，若超出可断定已超出限流速率)
     * @param permits
     * @return 当前剩余可以许可证
     */
    private long blockUntilHoldPermits(long permits){

        synchronized (mutex()){
            //double check
            if(getLock().getQueueLength() >= permitsRateLimit){
                return -1;
            }
        }

        long remaining;

        try {

            getLock().lock();
            while (true){
                remaining= consumeAndRefreshPermitsNolock(permits);
                if(remaining>= 0 ){
                    break;
                }
            }
        }finally {

            getLock().unlock();
        }

        return remaining;

    }


    /**
     * 消费指定数量的许可证（前提是没有阻塞线程，直接消费,若已有线程阻塞则进入阻塞队列公平竞争许可证）
     * @param permits
     * @return 当前剩余可以许可证
     */
    private long consumeAndRefreshPermits(long permits){

        synchronized (mutex()){

            //double check
            if(getLock().getQueueLength() == 0){
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
        long permits= ( currentPoint-refreshPoint ) * permitsRateLimit/  SECONDS.toNanos(refreshInterval)  ;
        if(permits >0){
            storePermits=Math.min(storePermits+permits,burstCapacity);
            refreshPoint=currentPoint;
        }
        if(permits<0){
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


    /**
     * 获取重入公平锁
     * @return
     */
    public ReentrantLock getLock(){
        ReentrantLock mutex = lock;
        if (mutex == null) {
            synchronized (this) {
                mutex = lock;
                if (mutex == null) {
                    lock = mutex = new ReentrantLock(true);
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
     *
    public static void catfishLimit(long permitsRatelimit) throws InterruptedException {


        RateLimitPermit rateLimitPermit =  RateLimitPermit.create(1,permitsRatelimit,1);

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
        RateLimitPermit rateLimitPermit = RateLimitPermit.create(1,permitsRatelimit,1);

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
        catfishLimit(100000);
        multCatfishLimit(2000,1000);

    }

    **/
}
