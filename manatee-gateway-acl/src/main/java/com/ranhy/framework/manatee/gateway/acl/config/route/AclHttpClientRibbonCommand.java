package com.ranhy.framework.manatee.gateway.acl.config.route;

import com.ranhy.framework.manatee.gateway.acl.config.util.AclUtils;
import com.netflix.client.config.DefaultClientConfigImpl;
import com.netflix.client.config.IClientConfig;
import com.netflix.client.config.IClientConfigKey;
import com.netflix.config.DynamicIntProperty;
import com.netflix.config.DynamicPropertyFactory;
import com.netflix.hystrix.HystrixCommandGroupKey;
import com.netflix.hystrix.HystrixCommandKey;
import com.netflix.hystrix.HystrixCommandProperties;
import com.netflix.hystrix.HystrixThreadPoolKey;
import com.netflix.zuul.constants.ZuulConstants;
import org.springframework.cloud.netflix.ribbon.RibbonClientConfiguration;
import org.springframework.cloud.netflix.ribbon.apache.RibbonApacheHttpRequest;
import org.springframework.cloud.netflix.ribbon.apache.RibbonApacheHttpResponse;
import org.springframework.cloud.netflix.ribbon.apache.RibbonLoadBalancingHttpClient;
import org.springframework.cloud.netflix.zuul.filters.ZuulProperties;
import org.springframework.cloud.netflix.zuul.filters.route.RibbonCommandContext;
import org.springframework.cloud.netflix.zuul.filters.route.ZuulFallbackProvider;
import org.springframework.cloud.netflix.zuul.filters.route.support.AbstractRibbonCommand;

public class AclHttpClientRibbonCommand extends  AbstractRibbonCommand<RibbonLoadBalancingHttpClient, RibbonApacheHttpRequest, RibbonApacheHttpResponse>   {


    public AclHttpClientRibbonCommand(final String commandKey,
                                   final RibbonLoadBalancingHttpClient client,
                                   final RibbonCommandContext context,
                                   final ZuulProperties zuulProperties) {
        super(commandKey, client, context, zuulProperties);
    }

    public AclHttpClientRibbonCommand(final String commandKey,
                                   final RibbonLoadBalancingHttpClient client,
                                   final RibbonCommandContext context,
                                   final ZuulProperties zuulProperties,
                                   final ZuulFallbackProvider zuulFallbackProvider) {
        super(commandKey, client, context, zuulProperties, zuulFallbackProvider);
    }

    public AclHttpClientRibbonCommand(final String commandKey,
                                   final RibbonLoadBalancingHttpClient client,
                                   final RibbonCommandContext context,
                                   final ZuulProperties zuulProperties,
                                   final ZuulFallbackProvider zuulFallbackProvider,
                                   final IClientConfig config) {
        this(getSetter(commandKey,zuulProperties,config),client,context ,zuulFallbackProvider, config);
    }


    public AclHttpClientRibbonCommand(final Setter setter,final RibbonLoadBalancingHttpClient client,
                                      final RibbonCommandContext context,
                                      final ZuulFallbackProvider fallbackProvider,final IClientConfig config ) {
        super(setter, client, context, fallbackProvider, config);
    }



    @Override
    protected RibbonApacheHttpRequest createRequest() throws Exception {

        RibbonApacheHttpRequest ribbonApacheHttpRequest = new AclRibbonApacheHttpRequest(this.context);
        return ribbonApacheHttpRequest;
    }

    protected static Setter getSetter(final String commandKey,
                                      ZuulProperties zuulProperties, IClientConfig config) {

        // @formatter:off
        Setter commandSetter = Setter.withGroupKey(HystrixCommandGroupKey.Factory.asKey("RibbonCommand"))
                .andCommandKey(HystrixCommandKey.Factory.asKey(commandKey));
        final HystrixCommandProperties.Setter setter = createSetter(config, commandKey, zuulProperties);
        if (zuulProperties.getRibbonIsolationStrategy() == HystrixCommandProperties.ExecutionIsolationStrategy.SEMAPHORE){
            final String name = ZuulConstants.ZUUL_EUREKA + commandKey + ".semaphore.maxSemaphores";
            // we want to default to semaphore-isolation since this wraps
            // 2 others commands that are already thread isolated
            final DynamicIntProperty value = DynamicPropertyFactory.getInstance()
                    .getIntProperty(name, zuulProperties.getSemaphore().getMaxSemaphores());
            setter.withExecutionIsolationSemaphoreMaxConcurrentRequests(value.get());
        } else if (zuulProperties.getThreadPool().isUseSeparateThreadPools()) {
            final String threadPoolKey = zuulProperties.getThreadPool().getThreadPoolKeyPrefix() + commandKey;
            commandSetter.andThreadPoolKey(HystrixThreadPoolKey.Factory.asKey(threadPoolKey));
        }

        return commandSetter.andCommandPropertiesDefaults(setter);
        // @formatter:on
    }


    protected static HystrixCommandProperties.Setter createSetter(IClientConfig config, String commandKey, ZuulProperties zuulProperties) {
        int hystrixTimeout = getHystrixTimeout(config, commandKey);
        return HystrixCommandProperties.Setter().withExecutionIsolationStrategy(
                zuulProperties.getRibbonIsolationStrategy()).withExecutionTimeoutInMilliseconds(hystrixTimeout);
    }

    protected static int getHystrixTimeout(IClientConfig config, String commandKey) {
        int ribbonTimeout = getRibbonTimeout(config, commandKey);
        DynamicPropertyFactory dynamicPropertyFactory = DynamicPropertyFactory.getInstance();
        int defaultHystrixTimeout = dynamicPropertyFactory.getIntProperty("hystrix.command.default.execution.isolation.thread.timeoutInMilliseconds",
                0).get();
        int commandHystrixTimeout = dynamicPropertyFactory.getIntProperty("hystrix.command." + commandKey + ".execution.isolation.thread.timeoutInMilliseconds",
                0).get();
        int hystrixTimeout;

        if(AclUtils.isSelfReadTimeOut()){
            hystrixTimeout=ribbonTimeout;
        }
        else if(commandHystrixTimeout > 0) {
            hystrixTimeout = commandHystrixTimeout;
        }
        else if(defaultHystrixTimeout > 0) {
            hystrixTimeout = defaultHystrixTimeout;
        } else {
            hystrixTimeout = ribbonTimeout;
        }
//        if(hystrixTimeout < ribbonTimeout) {
//            LOGGER.warn("The Hystrix timeout of " + hystrixTimeout + "ms for the command " + commandKey +
//                    " is set lower than the combination of the Ribbon read and connect timeout, " + ribbonTimeout + "ms.");
//        }
        return hystrixTimeout;
    }



    protected static int getRibbonTimeout(IClientConfig config, String commandKey) {
        int ribbonTimeout;
        if (config == null) {
            ribbonTimeout = RibbonClientConfiguration.DEFAULT_READ_TIMEOUT + RibbonClientConfiguration.DEFAULT_CONNECT_TIMEOUT;
        } else {
            int ribbonReadTimeout = getTimeout(config, commandKey, "ReadTimeout",
                    IClientConfigKey.Keys.ReadTimeout, RibbonClientConfiguration.DEFAULT_READ_TIMEOUT);
            int ribbonConnectTimeout = getTimeout(config, commandKey, "ConnectTimeout",
                    IClientConfigKey.Keys.ConnectTimeout, RibbonClientConfiguration.DEFAULT_CONNECT_TIMEOUT);
            int maxAutoRetries = getTimeout(config, commandKey, "MaxAutoRetries",
                    IClientConfigKey.Keys.MaxAutoRetries, DefaultClientConfigImpl.DEFAULT_MAX_AUTO_RETRIES);
            int maxAutoRetriesNextServer = getTimeout(config, commandKey, "MaxAutoRetriesNextServer",
                    IClientConfigKey.Keys.MaxAutoRetriesNextServer, DefaultClientConfigImpl.DEFAULT_MAX_AUTO_RETRIES_NEXT_SERVER);

            ribbonTimeout= AclUtils.getRibbonTimeout(ribbonReadTimeout,ribbonConnectTimeout,maxAutoRetries,maxAutoRetriesNextServer);
        }
        return ribbonTimeout;
    }





    private static int getTimeout(IClientConfig config, String commandKey, String property, IClientConfigKey<Integer> configKey, int defaultValue) {
        DynamicPropertyFactory dynamicPropertyFactory = DynamicPropertyFactory.getInstance();
        return dynamicPropertyFactory.getIntProperty(commandKey + "." + config.getNameSpace() + "." + property, config.get(configKey, defaultValue)).get();
    }



}
