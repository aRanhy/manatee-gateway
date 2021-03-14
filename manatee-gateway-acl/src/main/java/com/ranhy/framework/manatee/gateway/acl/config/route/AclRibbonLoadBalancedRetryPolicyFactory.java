package com.ranhy.framework.manatee.gateway.acl.config.route;

import org.springframework.cloud.client.loadbalancer.LoadBalancedRetryPolicy;
import org.springframework.cloud.client.loadbalancer.ServiceInstanceChooser;
import org.springframework.cloud.netflix.ribbon.RibbonLoadBalancedRetryPolicyFactory;
import org.springframework.cloud.netflix.ribbon.RibbonLoadBalancerContext;
import org.springframework.cloud.netflix.ribbon.SpringClientFactory;

public class AclRibbonLoadBalancedRetryPolicyFactory extends RibbonLoadBalancedRetryPolicyFactory {

    private SpringClientFactory clientFactory;

    public AclRibbonLoadBalancedRetryPolicyFactory(SpringClientFactory clientFactory) {
        super(clientFactory);
        this.clientFactory=clientFactory;
    }

    @Override
    public LoadBalancedRetryPolicy create(String serviceId, ServiceInstanceChooser loadBalanceChooser) {
        RibbonLoadBalancerContext lbContext = this.clientFactory
                .getLoadBalancerContext(serviceId);
        return new AclRibbonLoadBalancedRetryPolicy(serviceId, lbContext, loadBalanceChooser, clientFactory.getClientConfig(serviceId));
    }
}
