package com.ranhy.framework.manatee.gateway.acl.config.exception;

import com.ranhy.framework.manatee.gateway.common.constants.RespCodeEnum;
import com.ranhy.framework.manatee.gateway.common.exception.CatfishGatewayException;
import org.springframework.http.HttpStatus;

/**
 * @author	 hz18093501 hongyu.ran
 * @date	2019年8月8日
 */


public class CatfishAclException extends CatfishGatewayException {

    private static final long serialVersionUID = -6892989869833540741L;


    public CatfishAclException(RespCodeEnum respCode) {

        super(respCode,HttpStatus.OK.value());
    }

    public CatfishAclException(RespCodeEnum respCode, int httpStatus) {
        super(respCode,httpStatus);
    }

    public CatfishAclException(RespCodeEnum respCode, String  extraMsg) {

        super(respCode,HttpStatus.OK.value(),extraMsg);
    }

    public CatfishAclException(RespCodeEnum respCode, Throwable cause) {

        super(respCode,HttpStatus.OK.value(),cause);
    }

    public CatfishAclException(RespCodeEnum respCode, String extraMsg, Throwable cause) {
        super(respCode,HttpStatus.OK.value(),extraMsg,cause);
    }


}
