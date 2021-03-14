package com.ranhy.framework.manatee.gateway.ratelimit.config.util;

import com.ranhy.framework.manatee.gateway.ratelimit.config.entity.Element;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import static java.util.concurrent.TimeUnit.*;

/**
 * @author	 hz18093501 hongyu.ran
 * @date	2019年8月8日
 */
@AllArgsConstructor
@NoArgsConstructor
@Slf4j
  public class Repository< T extends Map<String, Element<U>> , U > {

    /**
     * 仓库
     */
   private T repository;

    /**
     * 清理仓库阈值
     */
   private long maxSize;

    /**
     * 仓库元素有效期(仓库清理时间间隔) ,单位毫秒
     */
   private long expireTime;

    /**
     * 最近一次清理时间戳 ,单位毫秒
     */
   private volatile long clearTime;


   final private AtomicBoolean isClear=new AtomicBoolean(false);

    public static <T extends Map<String, Element<U>>,U> Repository<T, U > create(){

        return  new Repository(  new ConcurrentHashMap<>(1000),10000,MINUTES.toMillis(10),System.currentTimeMillis());
    }

    public static <T extends Map<String, Element<U>>,U> Repository<T, U > create(long expireTime){

        return  new Repository(  new ConcurrentHashMap<>(1000),10000,expireTime,System.currentTimeMillis());
    }

   public static <T extends Map<String, Element<U>>,U> Repository<T, U > create(T m){

       return new Repository<>(m,10000,MINUTES.toMillis(10),System.currentTimeMillis());

   }

    /**
     * 获取仓库中的元素，如到达仓库清理阀值，则清理过期元素
     * @param key 元素唯一标识
     * @return
     */
    public  U  getAndDelExpireElement(String key){

      return  getAndDelExpireElement(key ,System.currentTimeMillis());

    }

    public  U  getAndDelExpireElement(String key ,long refreshTime){

        if(   canClear()  ){

            if(isClear.compareAndSet(false,true)) {

                clearInvalid();
                return getAndUpdateElement(key,refreshTime);
            }else{
                return getAndUpdateElement(key,refreshTime);
            }


        }else{
            return getAndUpdateElement(key,refreshTime);
        }


    }

    public  U  getAndUpdateElement(String key  ){

        return getAndUpdateElement(key,System.currentTimeMillis());
    }


    /**
     * 获取仓库元素，并更新元素使用时间
     * @param key 元素唯一标识
     * @param refreshTime 元素使用时间
     * @return
     */


    public  U  getAndUpdateElement(String key ,long refreshTime){

        if(!isClear.get()){

            Element<U> e= repository.get(key);
            if(e!=null){
                e.setRefreshTime(refreshTime);
                return e.getElement();
            }
            return null;
        }else {

            Uninterruptibles.sleepUninterruptibly(100,MICROSECONDS);

            return getAndUpdateElement(key,  refreshTime);

        }

    }


    public U setAndDelExpireElement(String key , U e ){
        return  setAndDelExpireElement(key,e,expireTime);
    }

    /**
     * 设置元素，如到达仓库清理阀值，则清理过期元素
     * @param key 元素唯一标识
     * @param e 元素
     * @param expireTime 元素有效期
     * @return 已存在元素则返回存在的，反之返回新增的元素
     */

    public U setAndDelExpireElement(String key , U e ,Long expireTime){


        if(   canClear()  ){

            if(isClear.compareAndSet(false,true)) {

                clearInvalid();
                return setAndGetElement(key,e,expireTime);
            }else{
                return setAndGetElement(key,e,expireTime);
            }

        }else{
            return setAndGetElement(key,e,expireTime);
        }


    }


    public  U  setAndGetElement(String key , U e ){
        return   setAndGetElement(key,e,expireTime);
    }



    public  U  setAndGetElement(String key , U e ,long expireTime){

        return   setAndGetElement(key,e,System.currentTimeMillis(),expireTime);
    }

    /**
     * 设置并获取仓库中的元素
     * @param key 元素唯一key
     * @param e 元素
     * @param refreshTime 元素最近使用时间
     * @param expireTime 元素有效期
     * @return 已存在元素则返回存在的，反之返回新增的元素
     */
    public  U  setAndGetElement(String key , U e ,long refreshTime , long expireTime){

        if(!isClear.get()){

            Element<U> element=new Element( refreshTime,expireTime,e);
            Element<U> old= this.repository.putIfAbsent(key, element);
            if(old == null){
                return e;
            }else{
                return old.getElement();
            }
        }  else {
            Uninterruptibles.sleepUninterruptibly(100,MICROSECONDS);
            return setAndGetElement(key,e,expireTime);

        }

    }

    /**
     * 删除已经过期的元素
     */
    private void clearInvalid(){

        long start =System.currentTimeMillis();
        int oldSize =repository.size();
        repository.values().removeIf(value -> System.currentTimeMillis() - value.getRefreshTime() > value.getExpireTime());
        clearTime=System.currentTimeMillis();
        log.warn("clearInvalid clear {} Element , use time {} mi",oldSize-repository.size() ,(System.currentTimeMillis()-start));
        isClear.getAndSet(false);

    }


    private boolean canClear(){

        return  repository.size()> maxSize && !isClear.get() && (System.currentTimeMillis()-clearTime > expireTime || System.currentTimeMillis()-clearTime <0);
    }

    public U replaceElement(String key, U e ,Long expireTime ) {

        Element<U> old= repository.get(key);
        if(null == old){
            return setAndDelExpireElement(key,e,expireTime);
        }else{
            repository.put(key,new Element<>(System.currentTimeMillis(),expireTime,e));
        }
        return old.getElement();
    }

/**
 * ConcurrentHashMap.get方法是否同步、清理过期容器元素测试
 *

    public static void main(String[] args) throws InterruptedException {

      Repository<ConcurrentHashMap<String, Element<Integer>>,Integer> keyLockRepository = new Repository<>(new ConcurrentHashMap(),10000,MILLISECONDS.toMillis(10),System.currentTimeMillis()) ;

      long startTime = System.currentTimeMillis();

      CountDownLatch countDownLatch=new CountDownLatch(1000);

        for (int j = 0; j < 1000; j++) {
              keyLockRepository.setAndGetElement(j+"" ,j );

        }

        AtomicInteger getfailCount =new AtomicInteger(0);

        AtomicInteger setfailCount =new AtomicInteger(0);

        for (int i = 0; i < 1000; i++) {

            if(i%2 == 0){
                Thread thread=new Thread(()->{
                    for (int j = 0; j < 1000; j++) {
                        Integer e= keyLockRepository.getAndDelExpireElement(j+"",System.currentTimeMillis()+10000 );
                        if(e== null || e.intValue()!=j){
                            getfailCount.incrementAndGet();
                        }
                    }
                    countDownLatch.countDown();
                });
                thread.start();
            }else{
                Thread thread=new Thread(()->{
                    for (int j = 1000; j < 10001; j++) {

                        Integer e= keyLockRepository.setAndDelExpireElement(j+"",j);
                        if(e== null || e.intValue()!=j){
                            setfailCount.incrementAndGet();
                        }

                    }
                    countDownLatch.countDown();
                });
                thread.start();
            }

        }

        countDownLatch.await();
        long endTime = System.currentTimeMillis();
        log.info("fail set = {} , fail get = {} , use time {} mi",setfailCount.get(),getfailCount.get(),endTime-startTime);

    }

*/

}
