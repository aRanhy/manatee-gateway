package com.ranhy.example.manatee.gateway.nginx.factory;

import com.ranhy.example.manatee.gateway.nginx.manage.domain.GatewayPagination;
import com.ranhy.example.manatee.gateway.nginx.manage.domain.TDwCodIpInfo;
import com.ranhy.example.manatee.gateway.nginx.manage.service.DwCodIpInfoService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.InitializingBean;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.CountDownLatch;

/**
 * @author	 hz18093501 hongyu.ran
 * @date	2019年8月8日
 */
@Slf4j
public class IpInfoFactory implements InitializingBean {

    final private TreeMap<Long, TDwCodIpInfo> ipInfoTreeMap= new TreeMap<>();

    @Resource
    DwCodIpInfoService dwCodIpInfoService;


    @Override
    public void afterPropertiesSet() throws Exception {

        //加载ip池
        loadIpInfo();

    }


    synchronized public IpInfoFactory loadIpInfo() throws InterruptedException {

        TDwCodIpInfo queryObj= new TDwCodIpInfo();
        queryObj.setCountry("中国");
        Integer count= dwCodIpInfoService.getTDwCodIpInfoCount(queryObj);
        final int size = 5000;
        final int threadCount = 30;

        int pageTotal=  count/size+(count%size>0?1:0);

        CountDownLatch latch = null;

        Object mutexObject= new Object();

        final Map<String,Integer> threadPage=new HashMap<>();

        if(null != count && count.intValue()>0){

            for (int i = 1; i <= pageTotal ; i++) {
                int currentPageNo = i;

                if(currentPageNo % threadCount == 1  ){

                    if( pageTotal-currentPageNo+1 >= threadCount)  {

                        latch = new CountDownLatch(threadCount);
                    }  else {
                        latch = new CountDownLatch(pageTotal - currentPageNo + 1);
                    }

                }

                CountDownLatch finalLatch = latch;

                String threadName= "asyn-pull-ip-info-"+currentPageNo;
                threadPage.put(threadName,currentPageNo);

                Thread thread=new Thread(()->{

                    TDwCodIpInfo query= new TDwCodIpInfo();
                    query.setCountry("中国");
                    GatewayPagination gatewayPagination=new GatewayPagination();
                    gatewayPagination.setPageSize(size);
                    gatewayPagination.setPageNo(threadPage.get(Thread.currentThread().getName()));
                    query.setPagination(gatewayPagination);

                    List<TDwCodIpInfo> list=dwCodIpInfoService.getTDwCodIpInfoList(query);

                    if(CollectionUtils.isNotEmpty(list)){
                        synchronized (mutexObject) {
                            list.forEach(item->
                                    ipInfoTreeMap.putIfAbsent(item.getStartIpNum(),item)
                            );

                        };

                    }

                    if(null!= finalLatch) {
                        finalLatch.countDown();
                    }

                },threadName);
                thread.start();

                if(latch!=null && (currentPageNo % threadCount==0 || currentPageNo==pageTotal)) {
                    latch.await();
                }

            }
            log.info("IpInfoFactory已加载ip池大小{}", ipInfoTreeMap.size());

            if(count.intValue() != ipInfoTreeMap.size()){
                log.error("IpInfoFactory加载ip池子出差,池子大小{},成功加载{}", count.intValue(), ipInfoTreeMap.size());
            }

        }

        return this;
    }

   public TreeMap<Long, TDwCodIpInfo> getTreeMap(){
        return ipInfoTreeMap;
     }

     public  TDwCodIpInfo getIpSegmentInfo(Long ipNum){

        return   Optional.ofNullable(ipNum)
                .map(key -> ipInfoTreeMap.floorEntry(key))
                .map(Map.Entry::getValue)
                .map(ipInfo ->  ipInfo.getEndIpNum()!=null && ipInfo.getEndIpNum().longValue() >= ipNum.longValue() ? ipInfo : null
                ).orElse(null)
                ;
     }


}
