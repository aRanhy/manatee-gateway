package com.ranhy.example.manatee.gateway.nginx.manage.domain;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;

/**
 * @author 
 */
@Getter
@Setter
public class TDwCodIpInfo extends GatewayBaseDomain{

    private Integer id;

    /**
     * 起始IP,数字型
     */
    private Long startIpNum;

    /**
     * 结束IP, 数字型
     */
    private Long endIpNum;

    /**
     * 起始IP, 原始点分隔字符串型
     */
    private String startIp;

    /**
     * 结束IP, 原始点分隔字符串型
     */
    private String endIp;

    /**
     * 国家
     */
    private String country;

    /**
     * 省份
     */
    private String province;

    /**
     * 城市
     */
    private String city;

    /**
     * 运营商
     */
    private String isp;

    /**
     * 更新时间
     */
    private Date updateTime;

    private String srcDb;

    private Date etlUpdateTime;


}