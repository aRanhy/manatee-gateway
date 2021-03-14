package com.ranhy.framework.manatee.gateway.acl.config.factory;



import com.ranhy.framework.manatee.gateway.acl.config.supports.RedisServe;
import com.ranhy.framework.manatee.gateway.common.protocol.AclConfig;
import com.ranhy.framework.manatee.gateway.common.util.JsonUtils;
import com.ranhy.framework.manatee.gateway.common.util.RedisKeyUitl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.InitializingBean;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
public class AclConfigFactory implements InitializingBean {

    final private RedisServe redisServe;
    final private String gatewayApplicationName;
    /**
     * 服务接口授权集合
     */
    final private Map<String, Map<String,AclConfig>> aclConfigMap= new ConcurrentHashMap<>();

    final private ReentrantReadWriteLock lock = new ReentrantReadWriteLock(false);

    private String currentVersion= "";

    @Override
    public void afterPropertiesSet() throws Exception {
        loadConfig();
    }

    public void loadConfig(){

        String remoteVersion = getRemoteAclConfigVersion();

        if(null == remoteVersion ||  currentVersion.equalsIgnoreCase(remoteVersion)){
            return ;
        }

        Set<String> serviceSet=getRemoteServiceSet();

        Set<String> localServiceSet=  aclConfigMap.keySet();

        List<String> addList=   serviceSet.stream().filter(item->!localServiceSet.contains(item)).collect(Collectors.toList());
        List<String> delList=   localServiceSet.stream().filter(item->!serviceSet.contains(item)).collect(Collectors.toList());
        List<String> updList=   serviceSet.stream().filter(item->localServiceSet.contains(item)).collect(Collectors.toList());

        //所有远程配置都正确读取后再更新
        Map<String, Map<String,AclConfig>> addMap=getRemoteAclConfigByServiceIdList(addList);
        Map<String, Map<String,AclConfig>> updMap=getRemoteAclConfigByServiceIdList(updList);

        lock.writeLock().lock();
        try {
            addList.forEach(serviceId ->{

                        aclConfigMap.put(serviceId,addMap.get(serviceId));
                        // help GC
                        addMap.put(serviceId,null);
                    }
            );

            updList.forEach(serviceId -> {
                Map<String,AclConfig> localServiceConfig=aclConfigMap.get(serviceId);
                Map<String,AclConfig> remoteServiceConfig=updMap.get(serviceId);
                if( checkServiceConfigChange(localServiceConfig,remoteServiceConfig)){
                    aclConfigMap.put(serviceId,remoteServiceConfig);
                }
                // help GC
                updMap.put(serviceId,null);
            });

            delList.forEach(serviceId ->aclConfigMap.remove(serviceId));

            currentVersion=remoteVersion;
        }finally {
            lock.writeLock().unlock();
        }

    }


    /**
     * 获取远程配置的接口权限配置的版本号
     * @return
     */
    public String getRemoteAclConfigVersion(){

        return Optional.ofNullable(redisServe.getValueByKey(RedisKeyUitl.getAclConfigVersionKey(gatewayApplicationName)))
                .map(version -> version.trim() )
                .orElse( null);
    }


    /**
     * 获取远程配置的接口权限的服务列表
     * @return
     */
    public Set<String> getRemoteServiceSet(){

       return Optional.ofNullable(redisServe.getSetByKey(RedisKeyUitl.getAclServiceListKey(gatewayApplicationName)))
                .map(services -> services.stream().filter(serviceId ->StringUtils.isNotBlank(serviceId))
                        .map(serviceId-> StringUtils.trim(serviceId)).collect(Collectors.toSet()))
                .orElse( new HashSet<>());
    }

    private boolean checkServiceConfigChange(Map<String, AclConfig> localServiceConfig, Map<String, AclConfig> remoteServiceConfig) {

        if(MapUtils.isEmpty(localServiceConfig) && MapUtils.isEmpty(remoteServiceConfig)){
            return false;
        }else if(MapUtils.isNotEmpty(localServiceConfig) && MapUtils.isEmpty(remoteServiceConfig)){
            return true;
        }else if(MapUtils.isEmpty(localServiceConfig) && MapUtils.isNotEmpty(remoteServiceConfig)){
            return true;
        }else {
            Set<Map.Entry<String,AclConfig>> localEntrySet=localServiceConfig.entrySet();
            Set<Map.Entry<String,AclConfig>> remoteEntrySet=remoteServiceConfig.entrySet();
            if(localEntrySet.size()!=remoteEntrySet.size()){
                return true;
            }

            for (Map.Entry<String,AclConfig> localEntry:localEntrySet){
                boolean isSame=false;
                for (Map.Entry<String,AclConfig> remoteEntry:
                        remoteEntrySet ) {
                    if(localEntry.getKey().equals(remoteEntry.getKey()) ){
                        if(localEntry.getValue().equals(remoteEntry.getValue())){
                            isSame=true;
                            break;
                        }
                    }
                }

                if(!isSame){
                    return true;
                }
            }

        }

        return false;
    }


    /**
     * 获取指定服务列表的所有接口权限配置
     * @param serviceList
     * @return
     */
    public Map<String, Map<String,AclConfig>> getRemoteAclConfigByServiceIdList( List<String> serviceList){

        Map<String, Map<String,AclConfig>> serviceListConfig =new HashMap<>();
        if(CollectionUtils.isNotEmpty(serviceList)){
            serviceList.forEach(serviceId ->
                serviceListConfig.put(serviceId,getRemoteAclConfigByServiceId(serviceId))
            );
        }
        return serviceListConfig;
    }

    /**
     * 获取某个服务的接口权限配置
     * @param serviceId
     * @return
     */

    public Map<String,AclConfig> getRemoteAclConfigByServiceId(String serviceId){

        Map<String,AclConfig>  serviceConfig= new HashMap<>();

        if(StringUtils.isNotBlank(serviceId)){

            Map<Object,Object> redisConfig=   redisServe.getMapBykey(RedisKeyUitl.getServiceAclConfigKey(serviceId));
            Optional.ofNullable(redisConfig).map(Map::entrySet).map(entries ->
                    entries.stream().filter(entry->
                            StringUtils.isNotBlank((String) entry.getKey()) && null != entry.getValue()
                    ).collect(Collectors.toList())
            ).ifPresent(entries ->

                    entries.forEach(entry-> {

                        AclConfig aclConfig=null;
                        try {
                            aclConfig=   JsonUtils.jsonToBean((String) entry.getValue(), AclConfig.class);
                        }catch (Exception e){
                            e.printStackTrace();
                        }
                        if( validAclConfig(aclConfig) ){
                            serviceConfig.put(StringUtils.trim ((String) entry.getKey()), aclConfig);
                        }else{
                            log.warn("接口访问权限配置数据有误，已被过滤aclConfig = {}", entry.getValue());
                        }
                    })
            );
        }


        return serviceConfig;
    }

    public boolean validAclConfig(AclConfig aclConfig){

        return Objects.nonNull(aclConfig) && (CollectionUtils.isNotEmpty(aclConfig.getAccessList()) || CollectionUtils.isNotEmpty(aclConfig.getRefuseList()) )
                && StringUtils.isNotBlank(aclConfig.getProviderServiceId()) && StringUtils.isNotBlank(aclConfig.getInterfaceIdentity());
    }

    public Optional<AclConfig> getAclConfig(String serviceId,String interfaceIdentity){

        //获取读锁线程数量上限 1<<16
        lock.readLock().lock();
        Optional<AclConfig> result = Optional.empty();
        try{
            result= Optional.ofNullable(serviceId)
                    .map(aclConfigMap::get)
                    .map(serviceMap-> serviceMap.get(interfaceIdentity));
        }finally {
            lock.readLock().unlock();
        }
        return result;

    }

    public boolean validPermission (Optional<AclConfig> aclConfig ,String clientApplicationName){

        boolean  accept =  aclConfig.map(AclConfig::getAccessList).map(accessList ->
                CollectionUtils.isEmpty(accessList)? true: accessList.stream().anyMatch(item -> StringUtils.trim(item).equals(clientApplicationName))
        ).orElse(true);

        boolean  refuse =  aclConfig.map(AclConfig::getRefuseList).map(refuseList ->
                CollectionUtils.isEmpty(refuseList)? false: refuseList.stream().anyMatch(item -> StringUtils.trim(item).equals(clientApplicationName))
        ).orElse(false);

        return  accept && !refuse;
    }

}
