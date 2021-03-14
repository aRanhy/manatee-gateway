package com.ranhy.framework.manatee.gateway.ratelimit.config.exception;

import com.ranhy.framework.manatee.gateway.common.constants.RespCodeEnum;
import com.ranhy.framework.manatee.gateway.common.exception.ManateeGatewayException;
import org.springframework.http.HttpStatus;

/**
 * @author	 hz18093501 hongyu.ran
 * @date	2019年8月8日
 */

public class ManateeRateLimitException extends ManateeGatewayException {

    private static final long serialVersionUID = -6892989869833540741L;


    public ManateeRateLimitException(RespCodeEnum respCode) {

        super(respCode,HttpStatus.OK.value());
    }

    public ManateeRateLimitException(RespCodeEnum respCode, int httpStatus) {
        super(respCode,httpStatus);
    }

    public ManateeRateLimitException(RespCodeEnum respCode, String  extraMsg) {

        super(respCode,HttpStatus.OK.value(),extraMsg);
    }

    public ManateeRateLimitException(RespCodeEnum respCode, Throwable cause) {

        super(respCode,HttpStatus.OK.value(),cause);
    }

    public ManateeRateLimitException(RespCodeEnum respCode, String extraMsg, Throwable cause) {
        super(respCode,HttpStatus.OK.value(),extraMsg,cause);
    }



}
