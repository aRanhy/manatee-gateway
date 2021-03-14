package com.ranhy.example.manatee.gateway.nginx.manage.service;

import com.ranhy.example.manatee.gateway.nginx.manage.domain.TDwCodIpInfo;
import com.ranhy.example.manatee.gateway.nginx.manage.persistence.TDwCodIpInfoMapper;

import javax.annotation.Resource;
import java.util.List;


public class DwCodIpInfoService {

    @Resource
    private TDwCodIpInfoMapper tDwCodIpInfoMapper;


    public List<TDwCodIpInfo> getTDwCodIpInfoList(TDwCodIpInfo tDwCodIpInfo){
       return tDwCodIpInfoMapper.selectListByObj(tDwCodIpInfo);
    }
    public int getTDwCodIpInfoCount(TDwCodIpInfo tDwCodIpInfo){
        return tDwCodIpInfoMapper.selectCountByObj(tDwCodIpInfo);
    }

}
