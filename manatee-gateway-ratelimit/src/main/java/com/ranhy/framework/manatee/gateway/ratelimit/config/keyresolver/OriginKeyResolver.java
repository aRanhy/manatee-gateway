package com.ranhy.framework.manatee.gateway.ratelimit.config.keyresolver;

import com.ranhy.framework.manatee.gateway.ratelimit.config.properties.RateLimitType;
import com.ranhy.framework.manatee.gateway.ratelimit.config.supports.RateLimitUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.cloud.netflix.zuul.filters.Route;

import javax.servlet.http.HttpServletRequest;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author	 hz18093501 hongyu.ran
 * @date	2019年8月8日
 */
@Slf4j
public class OriginKeyResolver implements KeyResolver {

    public final static String BEAN_NAME="originKeyResolver";

    @Override
    public boolean apply(HttpServletRequest request, Route route, RateLimitUtils rateLimitUtils, String matcher) {
        String ip = rateLimitUtils.getRemoteAddress(request);
        //非法IP过滤
        if(!isIP(ip))
        {
            if(log.isWarnEnabled())
                log.warn("IP格式异常,本次过滤器将不执行.ip = {}",ip);
            return false;
        }
        return  StringUtils.isBlank(matcher) || matcher.equals(ip);
    }

    @Override
    public String key(HttpServletRequest request, Route route, RateLimitUtils rateLimitUtils, String matcher) {
        return  rateLimitUtils.getRemoteAddress(request);
    }

    @Override
    public String getLimitType() {
        return RateLimitType.ORIGIN.getLimitType();
    }

    public  boolean isIP(String addr)
    {
        if( StringUtils.isBlank(addr) || addr.length() < 7 || addr.length() > 15 ) {
            return false;
        }
        /**
         * 判断IP格式和范围
         */
        String rexp = "^([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
                "([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
                "([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
                "([01]?\\d\\d?|2[0-4]\\d|25[0-5])$";
        //"([1-9]|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])(\\.(\\d|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])){3}";
        Pattern pat = Pattern.compile(rexp);
        Matcher mat = pat.matcher(addr);
        return mat.find();
    }

}
