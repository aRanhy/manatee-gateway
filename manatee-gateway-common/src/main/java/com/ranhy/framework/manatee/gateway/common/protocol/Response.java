/**
 * Copyright (c) 2006-2015 Hzins Ltd. All Rights Reserved.
 * <p>
 * This code is the confidential and proprietary information of Hzins. You shall not disclose such Confidential Information and shall use it only in
 * accordance with the terms of the agreements you entered into with Hzins,http://www.hzins.com.
 */
package com.ranhy.framework.manatee.gateway.common.protocol;


/**
 * @author	 hz18093501 hongyu.ran
 * @date	2019年8月8日
 */
public class Response<T> {

    private T result;

    private String exception;

    /**
     * <p>
     *
     * 0 成功 大于0 错误
     *
     * </p>
     *
     * @author hz1411965
     * @date 2015-3-18 下午2:44:07
     * @version
     */
    private String status = "00000";


    public Response() {
    }

    public Response(T result) {
        this.result = result;
    }


    public T getResult() {
        return result;
    }

    public void setResult(T result) {
        if (result != null) {
            this.result = result;
        }
    }

    public String getException() {
        return exception;
    }

    public void setException(String exception) {
        this.exception = exception;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public static <T> Response<T> of(String exception, String status) {
        Response<T> response = new Response<T>();
        response.setException(exception);
        response.setStatus(status);
        return response;
    }
}
