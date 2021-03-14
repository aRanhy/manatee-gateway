package com.ranhy.framework.manatee.gateway.ratelimit.config.exception;

import com.ranhy.framework.manatee.gateway.common.constants.RespCodeEnum;
import com.ranhy.framework.manatee.gateway.common.exception.CatfishGatewayException;
import org.springframework.http.HttpStatus;

/**
 * @author	 hz18093501 hongyu.ran
 * @date	2019年8月8日
 */

public class CatfishRateLimitException extends CatfishGatewayException {

    private static final long serialVersionUID = -6892989869833540741L;


    public CatfishRateLimitException(RespCodeEnum respCode) {

        super(respCode,HttpStatus.OK.value());
    }

    public CatfishRateLimitException(RespCodeEnum respCode, int httpStatus) {
        super(respCode,httpStatus);
    }

    public CatfishRateLimitException(RespCodeEnum respCode, String  extraMsg) {

        super(respCode,HttpStatus.OK.value(),extraMsg);
    }

    public CatfishRateLimitException(RespCodeEnum respCode, Throwable cause) {

        super(respCode,HttpStatus.OK.value(),cause);
    }

    public CatfishRateLimitException(RespCodeEnum respCode, String extraMsg, Throwable cause) {
        super(respCode,HttpStatus.OK.value(),extraMsg,cause);
    }



}
