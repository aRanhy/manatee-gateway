# manatee-gateway

##目录对应说明
manatee-gateway-acl       网关鉴权模块
manatee-gateway-ratelimit 网关限流模块
manatee-gateway-group     服务组间鉴权案例
manatee-gateway-nginx     nginx下游实现区域限流案例
manatee-gateway-common    公共模块
manatee-gateway-parent    父模块

##网关鉴权设计
(https://github.com/aRanhy/manatee-gateway/doc/网关鉴权.png)

##网关限流功能对比
(https://github.com/aRanhy/manatee-gateway/doc/网关限流功能对比.png)

##限流中间件算法对比
(https://github.com/aRanhy/manatee-gateway/doc/限流中间件算法对比.png)

##限流锁优化
(https://github.com/aRanhy/manatee-gateway/doc/限流锁优化.png)

## 属性说明
manatee.zuul.ratelimit.policies 限流策略集合
manatee.zuul.ratelimit.policies[0].limit 限流速率（一个窗口时间）
manatee.zuul.ratelimit.policies[0].refreshInterval 限流窗口滑动间隔
manatee.zuul.ratelimit.policies[0].repository 基于memory、redis限流
manatee.zuul.ratelimit.policies[0].algorithm限流算法（漏桶、计数器，此配置仅限memory限流，redis只支持计数器）
manatee.zuul.ratelimit.policies[0].burstCapacity支持的瞬时爆破流量（仅限漏桶算法）

manatee.zuul.ratelimit.policies[0].configs 限流维度集合
manatee.zuul.ratelimit.policies[0].configs[0].rateLimitType 限流维度（目前支持application、url、ip、http_method 四个维度，维度可自由搭配组合使用 ，支持自定义限流维度）
manatee.zuul.ratelimit.policies[0].configs[0].match 匹配项（主要用于指定某一个特定值限流，如某一个ip或者url限流，不配置默认对该维度下所有请求限流）

##接入文档参考
https://github.com/aRanhy/manatee-gateway/doc/网关限流接入说明.doc

