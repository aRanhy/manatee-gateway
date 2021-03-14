package com.ranhy.framework.manatee.gateway.ratelimit.config.supports;


import com.google.common.collect.Lists;
import com.ranhy.framework.manatee.gateway.ratelimit.config.properties.RateLimitProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;

import javax.servlet.http.HttpServletRequest;

import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import static org.springframework.cloud.netflix.zuul.filters.support.FilterConstants.X_FORWARDED_FOR_HEADER;

/**
 * @author	 hz18093501 hongyu.ran
 * @date	2019年8月8日
 */
@RequiredArgsConstructor
@Slf4j
public class DefaultRateLimitUtils implements RateLimitUtils {

    private final RateLimitProperties properties;

    private  AtomicLong  interceptCount = new AtomicLong(0);

    public  long incrementAndGetInterceptCount()
    {
        return interceptCount.incrementAndGet();
    }

    @Override
    public String getRemoteAddress(final HttpServletRequest request) {

        String ip = null;
        if(properties.isBehindProxy()){
            ip = getRealIp(request);
        }
        if (StringUtils.isBlank(ip)  || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }

        if(StringUtils.isNotBlank(ip)){

            ip=ip.split(",")[0].trim();
            if (ip.indexOf("::ffff:") > 0) {
                ip = ip.substring(7);
            }
        }

        return ip ;
    }

    @Override
    public String getRealIp(HttpServletRequest request) {

        String  ip = request.getHeader("X-Real-IP");

        if (StringUtils.isBlank(ip) || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader(X_FORWARDED_FOR_HEADER);
        }
        if (StringUtils.isBlank(ip)  || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (StringUtils.isBlank(ip)  || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (StringUtils.isBlank(ip)  || "unknown".equalsIgnoreCase(ip)) {
            return null;
        }

        if(StringUtils.isNotBlank(ip)){
            ip=ip.split(",")[0].trim();
            if (ip.indexOf("::ffff:") > 0) {
                ip = ip.substring(7);
            }
        }
        return ip;
    }

    public   List<String> formatRemoteAddr(String ip)
    {
        if(null == ip || ip.length() < 1)
            return null;
        List<String> list = Lists.newArrayList();
        if(ip.indexOf(".") < 1){
            list.add(ip);
            return list;
        }

        String[] tmp = ip.split("\\.");
        list.add(ip);
        list.add(assemblyIp(tmp[0],"*","*","*"));
        list.add(assemblyIp(tmp[0],tmp[1],"*","*"));
        list.add(assemblyIp(tmp[0],tmp[1],tmp[2],"*"));
        list.add(assemblyIp(tmp[0],"*",tmp[2],tmp[3]));
        list.add(assemblyIp(tmp[0],"*","*",tmp[3]));
        list.add(assemblyIp(tmp[0],tmp[1],"*",tmp[3]));
        return list;
    }

    public String assemblyIp(String pos1, String pos2, String pos3, String pos4)
    {
        StringBuffer sb = new StringBuffer();
        sb.append(pos1).append(".").append(pos2).append(".").append(pos3).append(".").append(pos4);
        return sb.toString();
    }

    @Override
    public   boolean ipExist(List<String> ipList,String ip)
    {
        if(CollectionUtils.isEmpty(ipList))
            return false;
        if(null == ip || ip.length() < 1)
            return false;

        List<String> list = formatRemoteAddr(ip);
        if(CollectionUtils.isEmpty(list))
            return false;

        for(String str:list)
        {
            if(ipList.contains(str))
                return true;
        }
        return false;
    }


    @Override
    public String inet_ntoa(long add) {
        return ((add & 0xff000000) >> 24) + "." + ((add & 0xff0000) >> 16)
                + "." + ((add & 0xff00) >> 8) + "." + ((add & 0xff));
    }

    @Override
    public long inet_aton(String add) {

        long result = 0;
        // number between a dot
        long section = 0;
        // which digit in a number
        int times = 1;
        // which section
        int dots = 0;
        for (int i = add.length() - 1; i >= 0; --i) {
            if (add.charAt(i) == '.') {
                times = 1;
                section <<= dots * 8;
                result += section;
                section = 0;
                ++dots;
            } else {
                section += (add.charAt(i) - '0') * times;
                times *= 10;
            }
        }
        section <<= dots * 8;
        result += section;
        return result;

    }






}
