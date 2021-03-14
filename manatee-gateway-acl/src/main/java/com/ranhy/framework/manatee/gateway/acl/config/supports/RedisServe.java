package com.ranhy.framework.manatee.gateway.acl.config.supports;

import com.ranhy.framework.manatee.gateway.common.util.JsonUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.Map;
import java.util.Set;


/**
 * @author	 hz18093501 hongyu.ran
 * @date	2019年8月8日
 */
@Slf4j
@RequiredArgsConstructor
public class RedisServe {

    final private StringRedisTemplate redisTemplate;

    public void save(String tableName, String key, Object value) {
        try {
            redisTemplate.opsForHash().put(tableName, key, JsonUtils.beanToJson(value));
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw e;
        }
    }

    public void saveBatch(String tableName, Map<String, Object> value) {
        try {
            redisTemplate.opsForHash().putAll(tableName, value);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw e;
        }
    }

    public void save(String tableName, String key, String value) {
        try {
            redisTemplate.opsForHash().put(tableName, key, value);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw e;
        }
    }

    public void deleteByKey(String tableName, String key) {
        try {
            redisTemplate.opsForHash().delete(tableName, key);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw e;
        }
    }

    /**
     * 是否存在此key
     */
    public boolean hasKey(String tableName, String key) {
        try {
            Boolean result = redisTemplate.opsForHash().hasKey(tableName, key);
            return result;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return false;
        }
    }

    public long size(String tableName) {
        try {
            Long size = redisTemplate.opsForHash().size(tableName);
            if (size == null) {
                return 0;
            }
            return size;
        } catch (Exception e) {
            return 0;
        }
    }

    public Object getValueByKey(String tableName, String key) {
        try {
            Object entity = redisTemplate.opsForHash().get(tableName, key);
            return entity;
        } catch (Exception e) {
            return null;
        }
    }



    /**
     * 删除缓存
     */
    public void remove(String table, String key) {
        try {
            redisTemplate.opsForHash().delete(table, key);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw e;
        }
    }

    /**
     * 清除redis上的hash表缓存数据
     */
    public void cleanHashTable(String table) {
        try {
            redisTemplate.delete(table);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw e;
        }
    }

    public String getValueByKey(  String key) {
        try {
            return redisTemplate.opsForValue().get( key);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return null;
        }
    }

    public Set<String> getSetByKey(String key) {
        try {
            return redisTemplate.opsForSet().members(key) ;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw e;
        }
    }

    public Map<Object,Object> getMapBykey(String key){
        try {
            return redisTemplate.opsForHash().entries(key)  ;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw e;
        }
    }


    public void setValueByKey(  String key,String value) {
        try {
              redisTemplate.opsForValue().set( key,value);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw e;
        }
    }

}
