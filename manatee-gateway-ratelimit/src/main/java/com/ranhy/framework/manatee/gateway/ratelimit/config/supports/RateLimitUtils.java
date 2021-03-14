package com.ranhy.framework.manatee.gateway.ratelimit.config.supports;


import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * @author	 hz18093501 hongyu.ran
 * @date	2019年8月8日
 */
public interface RateLimitUtils {


    String getRemoteAddress(HttpServletRequest request);

    /**
     * 获取用户真实ip
     * @param request
     * @return
     */
    String getRealIp(HttpServletRequest request);


    /**
     * 判断ip是否处于本地配置的白名单中
     * @param ipList
     * @param ip
     * @return
     */
    boolean ipExist(List<String> ipList, String ip);

    /**
     * long 和 ip 相互转换
     * @param ip
     * @return
     */

    String inet_ntoa(long ip);

    /**
     * ip 和 long相互转换
     * @param ip
     * @return
     */
    long inet_aton(String ip);



}
