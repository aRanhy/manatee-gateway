/**
 * Copyright (c) 2006-2015 Hzins Ltd. All Rights Reserved. 
 *  
 * This code is the confidential and proprietary information of   
 * Hzins. You shall not disclose such Confidential Information   
 * and shall use it only in accordance with the terms of the agreements   
 * you entered into with Hzins,http://www.hzins.com.
 *  
 */
package com.ranhy.framework.manatee.gateway.common.util;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.lang.reflect.Type;

public class HzObjectMapper extends ObjectMapper {

    /**
     * <p>
     * 
     * 
     * 
     * </p>
     * 
     * @author hz1411965
     * @date 2015-9-3 上午3:55:01
     * @version
     */
    private static final long serialVersionUID = 1761469313568633110L;

    @SuppressWarnings("unchecked")
    public <T> T readValue(String content, Type type) throws IOException, JsonParseException, JsonMappingException {
	return (T) _readMapAndClose(_jsonFactory.createParser(content), _typeFactory.constructType(type));
    }

}
