package com.ranhy.framework.manatee.gateway.common.protocol;

import lombok.Getter;
import lombok.Setter;


/**
 * @author	 hz18093501 hongyu.ran
 * @date	2019年8月8日
 */
@Getter
@Setter
public class Request {

    private String serviceId;
    private String command;
    private Object[] parameters;
}
