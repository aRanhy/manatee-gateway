package com.ranhy.framework.manatee.gateway.common.exception;


import com.ranhy.framework.manatee.gateway.common.constants.RespCodeEnum;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;

/**
 * @author	 hz18093501 hongyu.ran
 * @date	2019年8月8日
 */

@Getter
@Setter
public class CatfishGatewayException extends RuntimeException {


    private static final long serialVersionUID = -7737482858419075163L;


    private RespCodeEnum respCode;

    private String extraMsg;

    private int httpStatus;


    public CatfishGatewayException() {
    }


    public CatfishGatewayException(RespCodeEnum respCode ,int httpStatus) {
        super(respCode.getRespCode() + ":" + respCode.getDescription());
        this.respCode = respCode;
        this.httpStatus = httpStatus;
    }



    public CatfishGatewayException(RespCodeEnum respCode ,int httpStatus, String extraMsg) {
        super(respCode.getRespCode() + ":" + respCode.getDescription() + ". " + StringUtils.defaultString(extraMsg));
        this.respCode = respCode;
        this.extraMsg = extraMsg;
        this.httpStatus = httpStatus;
    }

    public CatfishGatewayException(RespCodeEnum respCode,int httpStatus, Throwable cause) {
        super(respCode.getRespCode() + ":" + respCode.getDescription(), cause);
        this.respCode = respCode;
        this.extraMsg = respCode.getDescription();
        this.httpStatus = httpStatus;
    }

    public CatfishGatewayException(RespCodeEnum respCode,int httpStatus, String extraMsg, Throwable cause) {
        super(respCode.getRespCode() + ":" + respCode.getDescription() + ". " + StringUtils.defaultString(extraMsg), cause);
        this.respCode = respCode;
        this.extraMsg = extraMsg;
        this.httpStatus = httpStatus;
    }

    public String getRespMsg() {
        return respCode.getDescription() + ". " + StringUtils.defaultIfBlank(extraMsg, "");
    }





}
