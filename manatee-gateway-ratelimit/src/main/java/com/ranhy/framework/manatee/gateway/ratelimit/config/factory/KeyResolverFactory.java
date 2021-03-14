package com.ranhy.framework.manatee.gateway.ratelimit.config.factory;

import com.ranhy.framework.manatee.gateway.common.constants.RespCodeEnum;
import com.ranhy.framework.manatee.gateway.common.util.JsonUtils;
import com.ranhy.framework.manatee.gateway.ratelimit.config.exception.CatfishRateLimitException;
import com.ranhy.framework.manatee.gateway.ratelimit.config.keyresolver.KeyResolver;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author	 hz18093501 hongyu.ran
 * @date	2019年8月8日
 */
@NoArgsConstructor
@Slf4j
public class KeyResolverFactory  implements ApplicationContextAware, InitializingBean {

    final private Map<String, KeyResolver> keyResolverStore= new HashMap<>();
    private ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext=applicationContext;
    }

    @Override
    public void afterPropertiesSet() throws Exception {

        Map<String, KeyResolver> keyResolvers= applicationContext.getBeansOfType(KeyResolver.class);
        if(MapUtils.isNotEmpty(keyResolvers)){
            this.loadKeyResolver(keyResolvers.values().stream().collect(Collectors.toList()));
        }
    }


    public KeyResolverFactory loadKeyResolver(List<KeyResolver> keyResolverList)   {

        Optional.ofNullable(keyResolverList).ifPresent(keyResolvers->
                keyResolvers.forEach(keyResolver ->
                    Optional.ofNullable(keyResolver)
                    .map(key->StringUtils.isBlank(key.getLimitType())? null : StringUtils.trim(key.getLimitType()))
                    .map(limitType->{
                       Object o=  keyResolverStore.putIfAbsent(limitType,keyResolver); return o==null?true:null;}  )
                    .orElseThrow(()-> {
                        log.error("keyResolver and limitType can not be empty  or  keyResolver can not be repeat");
                        return new CatfishRateLimitException(RespCodeEnum.PARAM_INVALID);
                    })
        ));

        log.info("load KeyResolver success,keyResolverStore={}", JsonUtils.beanToJson(keyResolverStore));
        return this;
    }

    public Map<String, KeyResolver> getKeyResolverStore(){
        return this.keyResolverStore;
    }


    public Optional<KeyResolver> getKeyResolver(String key){
       return   Optional.ofNullable(this.keyResolverStore.get(key));
    }


}
