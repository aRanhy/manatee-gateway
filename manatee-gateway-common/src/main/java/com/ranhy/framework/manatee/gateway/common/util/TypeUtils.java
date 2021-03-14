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

import com.google.common.collect.Sets;

import java.util.Set;

public class TypeUtils {
    
    
    private final static Set<Class<?>> TYPES = Sets.newHashSet();
    
    
    static {
	TYPES.add(String.class);
	TYPES.add(Boolean.class);
	TYPES.add(boolean.class);
	TYPES.add(Character.class);
	TYPES.add(char.class);
	TYPES.add(Byte.class);
	TYPES.add(byte.class);
	TYPES.add(Short.class);
	TYPES.add(short.class);
	TYPES.add(Integer.class);
	TYPES.add(int.class);
	TYPES.add(Long.class);
	TYPES.add(long.class);
	TYPES.add(Float.class);
	TYPES.add(float.class);
	TYPES.add(Double.class);
	TYPES.add(double.class);
	
	
    }

    public static boolean isWrapperType(Class<?> clazz) {
	return TYPES.contains(clazz);
    }

}
 