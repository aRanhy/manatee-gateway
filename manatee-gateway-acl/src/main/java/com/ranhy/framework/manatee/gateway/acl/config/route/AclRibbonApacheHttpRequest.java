package com.ranhy.framework.manatee.gateway.acl.config.route;

import com.ranhy.framework.manatee.gateway.acl.config.util.HeaderUtils;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.entity.BasicHttpEntity;
import org.springframework.cloud.netflix.ribbon.apache.RibbonApacheHttpRequest;
import org.springframework.cloud.netflix.zuul.filters.route.RibbonCommandContext;

import java.net.URI;
import java.util.List;
import java.util.Optional;

import static org.springframework.cloud.netflix.ribbon.support.RibbonRequestCustomizer.Runner.customize;

public class AclRibbonApacheHttpRequest  extends RibbonApacheHttpRequest  {

    public AclRibbonApacheHttpRequest(RibbonCommandContext context) {
        super(context);
    }

    @Override
    public HttpUriRequest toRequest(final RequestConfig requestConfig) {
        final RequestBuilder builder = RequestBuilder.create(this.context.getMethod());
        builder.setUri(this.uri);
        for (final String name : this.context.getHeaders().keySet()) {
            final List<String> values = this.context.getHeaders().get(name);
            for (final String value : values) {
                builder.addHeader(name, value);
            }
        }

        for (final String name : this.context.getParams().keySet()) {
            final List<String> values = this.context.getParams().get(name);
            for (final String value : values) {
                builder.addParameter(name, value);
            }
        }

        if (this.context.getRequestEntity() != null) {
            final BasicHttpEntity entity;
            entity = new BasicHttpEntity();
            entity.setContent(this.context.getRequestEntity());
            // if the entity contentLength isn't set, transfer-encoding will be set
            // to chunked in org.apache.http.protocol.RequestContent. See gh-1042
            Long contentLength = this.context.getContentLength();
            if ("GET".equals(this.context.getMethod()) && (contentLength == null || contentLength < 0)) {
                entity.setContentLength(0);
            } else if (contentLength != null) {
                entity.setContentLength(contentLength);
            }
            builder.setEntity(entity);
        }

        customize(this.context.getRequestCustomizers(), builder);


//        final List<String> readTimeoutValues = this.context.getHeaders().get(AclConstants.READ_TIMEOUT);
//
//        if(  CollectionUtils.isNotEmpty(readTimeoutValues) && StringUtils.isNotBlank(readTimeoutValues.get(0))){
//            RequestConfig.Builder configBuilder = RequestConfig.copy(builder.getConfig());
//            configBuilder.setSocketTimeout(Integer.parseInt(readTimeoutValues.get(0)) );
//            builder.setConfig(configBuilder.build());
//        }else{
//            builder.setConfig(requestConfig);
//        }

        builder.setConfig(Optional.ofNullable(HeaderUtils.getRequestReadTimeOut())
                .map(readTimeout -> {
                    RequestConfig.Builder configBuilder = RequestConfig.copy(requestConfig);
                    configBuilder.setSocketTimeout(Integer.parseInt(readTimeout) );
                    return configBuilder.build();
                }).orElse(requestConfig));

        return builder.build();
    }

    public RibbonApacheHttpRequest withNewUri(URI uri) {
        return new AclRibbonApacheHttpRequest(newContext(uri));
    }

}
