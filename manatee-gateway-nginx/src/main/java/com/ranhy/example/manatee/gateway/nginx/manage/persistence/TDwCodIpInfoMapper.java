package com.ranhy.example.manatee.gateway.nginx.manage.persistence;

import com.ranhy.example.manatee.gateway.nginx.manage.domain.TDwCodIpInfo;

import java.util.List;

public interface TDwCodIpInfoMapper {

    TDwCodIpInfo selectByPrimaryKey(Integer id);


    List<TDwCodIpInfo> selectListByObj(TDwCodIpInfo domain);

    int selectCountByObj(TDwCodIpInfo domain);

}