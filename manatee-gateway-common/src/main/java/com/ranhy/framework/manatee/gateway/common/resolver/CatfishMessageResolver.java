package com.ranhy.framework.manatee.gateway.common.resolver;

import com.ranhy.framework.manatee.gateway.common.constants.MessageConstants;
import com.ranhy.framework.manatee.gateway.common.protocol.Request;
import com.ranhy.framework.manatee.gateway.common.util.CatfishMessageUtils;
import com.ranhy.framework.manatee.gateway.common.util.JsonUtils;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;


/**
 * @author	 hz18093501 hongyu.ran
 * @date	2019年8月8日
 */
@NoArgsConstructor
@Slf4j
public class CatfishMessageResolver {

   public static  final   CatfishMessageResolver initialize=new CatfishMessageResolver();

    public   Boolean checkMessage(String message) {
        return StringUtils.isNotBlank(message) && message.startsWith("{") && message.endsWith("}")
                && message.contains(MessageConstants.KEY_SERVICE_ID)
                && message.contains(MessageConstants.KEY_COMMAND) ;
    }

    private   Request convertRequestMessage(String message) {

        Request request = JsonUtils.jsonToBean(message, Request.class);
        if (request.getCommand().contains(MessageConstants.DEFAULT_SEPARATOR)) {
            return request;
        }
        int length = 0;
        if (request.getParameters() != null) {
            length = request.getParameters().length;
        }
        request.setCommand(CatfishMessageUtils.buildKey(request.getCommand(), length));
        return request;
    }


    public   Request parseMessage(String message) {
        Request request=null;
        try {

            if( checkMessage(message)){
                request= convertRequestMessage(message);
            }

        }catch (Exception e){
            log.warn(e.getMessage());
        }
       return request;
    }

    public static CatfishMessageResolver getInitialize(){
        return initialize;
    }


}