
package com.ranhy.framework.manatee.gateway.ratelimit.config.properties;

import com.google.common.collect.Lists;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.util.List;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.springframework.cloud.netflix.zuul.filters.support.FilterConstants.PRE_DECORATION_FILTER_ORDER;


/**
 * @author	 hz18093501 hongyu.ran
 * @date	2019年8月8日
 */
@Data
@Validated
@NoArgsConstructor
@ConfigurationProperties(prefix=RateLimitProperties.PREFIX )
public class RateLimitProperties {

    public static final String PREFIX = "catfish.zuul.ratelimit";

    /**
     * 限流策略集合
     */
    private List<Policy> policies = Lists.newArrayList();

    private boolean enabled  = true;

    private int preFilterOrder = PRE_DECORATION_FILTER_ORDER+2;


    private boolean behindProxy = true;

    /**
     * 是否启用ip防火墙
     */
    private boolean fireWall = false;

    /**
     * 限流响应头信息
     */
    private boolean addResponseHeaders = false;


    @NotNull
    @Value("${spring.application.name:catfish-rate-limit}")
    private String keyPrefix;

    /**
     * 白名单（处于白名单中的ip不限流）
     */
    @NotNull
    private List<String> exempt = Lists.newArrayList();


    @Data
    @NoArgsConstructor
    public static class Policy {

        /**
         * 限流窗口滑动间隔
         */
        @NotNull
        @Min(1)
        private Long refreshInterval = SECONDS.toSeconds(1L);

        /**
         * 限流速率（一个窗口时间）
         */
        @NotNull
        @Min(1)
        private Long limit;

        /**
         * 限流维度集合
         */
        @NotNull
        private List<Config> configs = Lists.newArrayList();

        /**
         * 扩展限流key
         */
        private String extendKey;

        /**
         *基于内存、redis限流
         */
        @NotNull
        private RepositoryType repository = RepositoryType.MEMORY;


        private RepositoryMemory memory=new RepositoryMemory();


        @Data
        @NoArgsConstructor
        public static class RepositoryMemory{
            /**
             * 桶大小（支持的瞬时爆破流量）
             */
            private Long burstCapacity;

            /**
             *限流算法（漏桶、计数器）
             */
            @NotNull
            private AlgorithmType algorithm = AlgorithmType.AUTO;

            /**
             * 低频阀值（qps）
             */
            @NotNull
            @Min(1)
            private Integer lowLimitThreshold = 50;

            /**
             * 集群限流，默认开启，false 网关单点限流
             */
            private boolean groupRateLimit = true;

        }


        @Data
        @NoArgsConstructor
        public static class Config {
            /**
             * 特定的匹配项（如针对某个应用、ip、url或者method等 限流，不设置默认对所有的ip、url或者method限流）
             */
            private String match;
            /**
             * 限流类型(application,ip、url、method等，支持自定义)
             */
            @NotNull
            private String rateLimitType;
        }
    }
}
 