package com.ranhy.framework.manatee.gateway.common.constants;

import lombok.Getter;

/**
 *  响应码枚举类
 * @author	 hz18093501 hongyu.ran
 * @date	2019年8月8日
 */
@Getter
public enum RespCodeEnum {
    /**
     * 公共返回码 - 成功(主码：000，子码：XXX)
     **/
    SUCCESS("000", "000", "success"),

    /**
     * 公共返回码 - 错误(主码：100，子码：XXX)
     **/
    PARAM_INVALID("100", "000", "param invalid"),
    SERVICE_NOT_AVAILABLE("100", "101", "service not available"),
    ACCESS_DENIAL ("100", "002", "request method access denial"),
    TOO_MANY_REQUEST ("100", "003", "too many request"),
    SYS_FAIL("100", "004", "service temporarily unavailable");


    /**
     * 响应主码
     **/
    private String code;

    /**
     * 响应子码
     **/
    private String subCode;

    /**
     * 响应描述
     **/
    private String description;


    RespCodeEnum(String code, String subCode, String description) {
        this.code = code;
        this.subCode = subCode;
        this.description = description;
    }
    /**
     * 获取组合后的响应代码
     */
    public String getRespCode() {
        return getCode() + getSubCode();
    }
}
