package com.ranhy.framework.manatee.gateway.acl.config.util;

import com.netflix.zuul.context.RequestContext;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Enumeration;

/**
 * @author	 hz18093501 hongyu.ran
 * @date	2019年8月8日
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class HeaderUtils {

    public static String getHeaderJSON(HttpServletRequest request) {
        StringBuilder headerStr = new StringBuilder("{");
        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            headerStr.append("\"").append(headerName).append("\"")
                    .append(":\"").append(request.getHeader(headerName)).append("\",");
        }
        headerStr.deleteCharAt(headerStr.length() - 1);
        headerStr.append("}");

        return headerStr.toString();
    }

    public static String getHeaderJSON(HttpServletResponse response) {
        StringBuilder headerStr = new StringBuilder("{");
        for (String headerName : response.getHeaderNames()) {
            headerStr.append("\"").append(headerName).append("\"")
                    .append(":\"").append(response.getHeader(headerName)).append("\",");
        }
        headerStr.deleteCharAt(headerStr.length() - 1);
        headerStr.append("}");

        return headerStr.toString();
    }

    public static String getRequestReadTimeOut(){
        RequestContext ctx = RequestContext.getCurrentContext();
        HttpServletRequest request = ctx.getRequest();
        return request.getHeader(AclConstants.READ_TIMEOUT);
    }


}
