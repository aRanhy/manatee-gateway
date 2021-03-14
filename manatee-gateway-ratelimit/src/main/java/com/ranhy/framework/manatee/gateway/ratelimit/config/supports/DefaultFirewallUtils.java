package com.ranhy.framework.manatee.gateway.ratelimit.config.supports;

import com.ranhy.framework.manatee.gateway.common.util.RedisKeyUitl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;

@RequiredArgsConstructor
@Slf4j
public class DefaultFirewallUtils implements FirewallUtils{

    private final RedisTemplate redisTemplate;

    @Override
    public void pushBlockValue(String blockValue) {

        try {
            redisTemplate.opsForSet().add(RedisKeyUitl.getBlackListKey(),blockValue);
        }catch (Exception e){
            log.warn("设置黑名单失败{}", blockValue);
        }


    }

    @Override
    public boolean isBlockValue(String blockValue) {

        try {
           return redisTemplate.opsForSet().isMember(RedisKeyUitl.getBlackListKey(),blockValue);
        }catch (Exception e){
            log.warn("判断黑名单失败{}", blockValue);
        }
        return false;

    }

    @Override
    public boolean isWhiteValue(String whiteValue) {
        try {
           return   redisTemplate.opsForSet().isMember(RedisKeyUitl.getWhiteListKey(),whiteValue);
        }catch (Exception e){
            log.warn("判断是否存在白名单内失败{}", whiteValue);
        }
        return false;
    }
}
