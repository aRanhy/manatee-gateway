package com.ranhy.framework.manatee.gateway.acl.config.route;

import com.netflix.client.config.IClientConfig;
import org.springframework.cloud.client.loadbalancer.LoadBalancedRetryContext;
import org.springframework.cloud.client.loadbalancer.ServiceInstanceChooser;
import org.springframework.cloud.netflix.ribbon.RibbonLoadBalancedRetryPolicy;
import org.springframework.cloud.netflix.ribbon.RibbonLoadBalancerContext;
import org.springframework.http.HttpMethod;
import org.springframework.retry.context.RetryContextSupport;

import java.net.SocketTimeoutException;
import java.util.Optional;

public class AclRibbonLoadBalancedRetryPolicy extends RibbonLoadBalancedRetryPolicy {

    private String readTimedOut ="Read timed out";
    private RibbonLoadBalancerContext lbContext;
    public AclRibbonLoadBalancedRetryPolicy(String serviceId, RibbonLoadBalancerContext context, ServiceInstanceChooser loadBalanceChooser) {
        super(serviceId, context, loadBalanceChooser);
        this.lbContext=context;
    }

    public AclRibbonLoadBalancedRetryPolicy(String serviceId, RibbonLoadBalancerContext context, ServiceInstanceChooser loadBalanceChooser, IClientConfig clientConfig) {
        super(serviceId, context, loadBalanceChooser, clientConfig);
        this.lbContext=context;
    }

    @Override
    public boolean canRetry(LoadBalancedRetryContext context) {
        HttpMethod method = context.getRequest().getMethod();
        return (HttpMethod.GET == method || lbContext.isOkToRetryOnAllOperations()) && selfRetryCheck(context);
    }

    private boolean selfRetryCheck(LoadBalancedRetryContext context) {

//        Throwable lastThrowable= ((RetryContextSupport)context).getLastThrowable();
//        if(null != lastThrowable && lastThrowable instanceof SocketTimeoutException){
//            if(readTimedOut.equals(lastThrowable.getMessage())){
//                return Boolean.FALSE;
//            }
//        }
//        return Boolean.TRUE;
         return Optional.ofNullable((RetryContextSupport)context)
                 .map(RetryContextSupport::getLastThrowable)
                 .map(lastThrowable ->  lastThrowable instanceof SocketTimeoutException && readTimedOut.equals(lastThrowable.getMessage()) )
                 .orElse(Boolean.TRUE);

    }


}
