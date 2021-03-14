/**
 * Copyright (c) 2006-2015 Hzins Ltd. All Rights Reserved. 
 *  
 * This code is the confidential and proprietary information of   
 * Hzins. You shall not disclose such Confidential Information   
 * and shall use it only in accordance with the terms of the agreements   
 * you entered into with Hzins,http://www.hzins.com.
 *  
 */
package com.ranhy.example.manatee.gateway.nginx.manage.domain;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * @author	 hz18093501 hongyu.ran
 * @date	2019年8月8日
 */

@Getter
@Setter
public class GatewayBaseDomain implements Serializable {


    private static final long serialVersionUID = 4392415824613083304L;

    private Integer userId;

    private String userIp;

    private String userName;

    protected GatewayPagination pagination;

}
