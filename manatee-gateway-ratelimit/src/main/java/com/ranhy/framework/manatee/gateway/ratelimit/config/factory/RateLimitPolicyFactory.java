package com.ranhy.framework.manatee.gateway.ratelimit.config.factory;


import com.ranhy.framework.manatee.gateway.common.util.JsonUtils;
import com.ranhy.framework.manatee.gateway.ratelimit.config.properties.RateLimitProperties;
import com.ranhy.framework.manatee.gateway.ratelimit.config.properties.RateLimitType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.InitializingBean;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
/**
 * @author	 hz18093501 hongyu.ran
 * @date	2019年8月8日
 */
@RequiredArgsConstructor
@Slf4j
public class RateLimitPolicyFactory implements InitializingBean {

    /**
     * 指定服务的限流策略集合
     */
    final private Map<String, List<RateLimitProperties.Policy>> servicePolicyStore= new ConcurrentHashMap<>();

    /**
     * 全局限流策略
     */
    final private List<RateLimitProperties.Policy>  defaultPolicyList=new ArrayList<>();

    final private RateLimitProperties rateLimitProperties;


    @Override
    public void afterPropertiesSet() throws Exception {
        loadPolicy();
    }

    synchronized public RateLimitPolicyFactory loadPolicy(){
        Optional.ofNullable(this.rateLimitProperties).map(RateLimitProperties::getPolicies).ifPresent(policies -> loadPolicy(policies));
        return this;
    }

    synchronized public RateLimitPolicyFactory loadPolicy(List<RateLimitProperties.Policy> policies){

         if(CollectionUtils.isNotEmpty(policies)){

             //获取有效的策略
             List<RateLimitProperties.Policy>  policyValid= policies.stream().filter(policy ->
                 validPolicy(policy)
             ).map(policy -> {
                 policy.getConfigs().forEach(config -> {
                     config.setMatch(StringUtils.trim(config.getMatch()));
                     config.setRateLimitType(StringUtils.trim(config.getRateLimitType()));
                 });
                 return  policy;
             }).collect(Collectors.toList());


             policyValid.forEach(policy -> {
                 for (RateLimitProperties.Policy.Config config :
                         policy.getConfigs()) {
                     if (RateLimitType.APPLICATION.getLimitType().equals(config.getRateLimitType()) && StringUtils.isNotBlank(config.getMatch())) {
                         List<RateLimitProperties.Policy> list = this.servicePolicyStore.get(config.getMatch());
                         if (CollectionUtils.isEmpty(list)) {
                             list = new ArrayList<>();
                         }
                         list.add(policy);
                         this.servicePolicyStore.put(config.getMatch(), list);
                         break;
                     }
                 }

             });

              this.defaultPolicyList.addAll(policyValid.stream().filter(policy ->
                      !policy.getConfigs().stream().anyMatch(config -> RateLimitType.APPLICATION.getLimitType().equals(config.getRateLimitType())  && StringUtils.isNotBlank(config.getMatch()))
                     ).collect(Collectors.toList())
             );

         }
         log.info("load default policy success,defaultPolicyList={}", JsonUtils.beanToJson(defaultPolicyList));
         log.info("load service policy success,servicePolicyStore={}", JsonUtils.beanToJson(servicePolicyStore));
         return this;
    }


    public Map<String, List<RateLimitProperties.Policy>> getServicePolicyStore(){
        return this.servicePolicyStore;
    }

    public List<RateLimitProperties.Policy> getDefaultPolicyList(){ return  this.defaultPolicyList;}


    public Optional<List<RateLimitProperties.Policy>> getPolicyList(String service){

            return Optional.ofNullable(service).map(servicePolicyStore::get) ;

    }

    /**
     * 验证限流策略数据有效性（读远程动态配置用）
     * @param policy
     * @return
     */
    public boolean validPolicy(RateLimitProperties.Policy policy){

       return     policy!=null &&  policy.getLimit() != null && policy.getLimit().longValue() >=1
                        &&  policy.getRefreshInterval() != null &&  policy.getRefreshInterval().longValue() >=1
                        &&  (policy.getMemory().getBurstCapacity() == null || (policy.getMemory().getBurstCapacity().longValue()>=1 && policy.getLimit().longValue() >= policy.getMemory().getBurstCapacity().longValue() ))
                        &&  CollectionUtils.isNotEmpty(policy.getConfigs()) && policy.getConfigs().stream().allMatch(config ->validPolicyConfig(config) );

    }

    public boolean validPolicyConfig(RateLimitProperties.Policy.Config config){

        return config!=null &&  StringUtils.isNotBlank(config.getRateLimitType());
    }

}
