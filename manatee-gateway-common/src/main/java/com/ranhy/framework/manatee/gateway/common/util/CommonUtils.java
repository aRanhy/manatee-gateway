package com.ranhy.framework.manatee.gateway.common.util;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.text.SimpleDateFormat;
import java.util.*;

@SuppressWarnings({ "unchecked", "rawtypes" })
public class CommonUtils {

    private static final Set<Class<?>> WRAPPER_TYPES = getWrapperTypes();

    // 分隔符
    private static String SPLIT = ",  ";

    /**
     * 
     */
    public static String forJson(Object obj) {

	String value = null;

	if (obj instanceof Map || obj instanceof List) {
	    value = JsonUtils.beanToJson(obj);
	} else if (obj instanceof String[]) {
	    value = StringUtils.join((String[]) obj, CommonConstants.SPLIT_OF_COMMA);
	} else {
	    value = String.valueOf(obj);
	}
	return value;
    }

    /**
     * 获取报错的类、方法
     */
    public static String getTags(Exception ex) {
	if (ex == null)
	    return null;

	StringBuffer result = new StringBuffer();
	StackTraceElement[] stList = ex.getStackTrace();

	// 哪个类
	String exclass = stList[0].getClassName() + SPLIT;
	// 哪个方法
	String method = stList[0].getMethodName() + SPLIT;
	// 在第几行报错
	String lineNumber = stList[0].getLineNumber() + SPLIT;
	// 异常名称
	String exceptionName = ex.getClass().getName() + SPLIT;

	result.append("报错的类：").append(exclass).append("报错的方法：").append(method).append("第几行报错：").append(lineNumber).append("异常类型：").append(exceptionName);

	return result.toString();
    }

    /**
     * 获取当前时间
     * 
     * @param fileName
     * @param content
     */
    public static String getDateTime(String dateFormat) {
	SimpleDateFormat df = new SimpleDateFormat(dateFormat);
	String currentDay = df.format(new Date());
	return currentDay;
    }

    /**
     * <p>
     * 
     * 判断对象所有属性是否为空
     * 
     * </p>
     * 
     * @param obj
     * @param ignoreFields 忽略字段
     * @return
     * 
     * @author hz1411965
     * @date 2015-1-28 上午11:42:02
     * @version
     */
    public static boolean isNotNullProperties(Object obj, String... ignoreFields) {

	boolean isNotNull = false;
	try {
	    Map<String, Object> map = PropertyUtils.describe(obj);
	    map.remove("class");

	    if (ignoreFields != null) {
		for (String field : ignoreFields) {
		    map.remove(field);
		}
	    }

	    Collection<?> collection = map.values();
	    for (Object o : collection) {
		if (o != null && StringUtils.isNotBlank(String.valueOf(o).trim())) {
		    isNotNull = true;
		    break;
		}
	    }
	} catch (Exception e) {
	    throw new RuntimeException(e);
	}
	return isNotNull;
    }

    public static void copyProperties(Object toObject, Object fromObject) throws RuntimeException {
	try {
	    if (fromObject != null && toObject != null) {
		for (Field toField : toObject.getClass().getDeclaredFields()) {
		    for (Field fromField : fromObject.getClass().getDeclaredFields()) {

			if (fromField.getName().equals(CommonConstants.SERIALVERSIONUID)) {
			    continue;
			}
			if (toField.getName().equals(fromField.getName())) {
			    fromField.setAccessible(true);
			    toField.setAccessible(true);
			    Object fromValue = fromField.get(fromObject);

			    if (fromValue != null) {
				if (fromValue instanceof Collection) {
				    exchangeList(toObject, toField, fromValue);
				} else if (fromValue instanceof Map) {
				    exchangeMap(toObject, toField, fromValue);
				} else if (fromValue.getClass().isArray()) {
				    exchangeArray(toObject, toField, fromValue);
				} else {
				    toField.set(toObject, fromValue);
				}
			    }
			    break;
			}
		    }
		}
	    }
	} catch (IllegalAccessException e) {
	    throw new RuntimeException(e);
	}
    }

    private static void exchangeList(Object toObject, Field toField, Object fromValue) throws RuntimeException {

	try {
	    if (isWrapperTypeOrPrimitiveType(toField)) {
		toField.set(toObject, fromValue);
	    } else {
		Collection collection = null;
		if (fromValue instanceof Set) {
		    collection = Sets.newHashSet();
		}
		if (fromValue instanceof List) {
		    collection = Lists.newArrayList();
		}
		for (Object obj : (Collection<?>) fromValue) {
		    Object e = newInstanceOf(toField);
		    copyProperties(e, obj);
		    collection.add(e);
		}
		toField.set(toObject, collection);
	    }
	} catch (IllegalAccessException | InstantiationException e) {
	    throw new RuntimeException(e);
	}

    }

    private static void exchangeArray(Object toObject, Field toField, Object fromValue) throws RuntimeException {
	try {
	    if (isWrapperTypeOrPrimitiveType(toField)) {
		toField.set(toObject, fromValue);
	    } else {
		Object array = Array.newInstance(toField.getType().getComponentType(), ((Object[]) fromValue).length);
		for (int i = 0; i < ((Object[]) fromValue).length; i++) {
		    Object el = newInstanceOf(toField);
		    copyProperties(el, ((Object[]) fromValue)[i]);
		    ((Object[]) array)[i] = el;
		}
		toField.set(toObject, array);
	    }
	} catch (IllegalAccessException | InstantiationException e) {
	    throw new RuntimeException(e);
	}
    }

    private static void exchangeMap(Object toObject, Field toField, Object fromValue) throws RuntimeException {

	Map map = (Map) fromValue;
	for (Object key : map.keySet()) {
	    Object value = map.get(key);
	    if (!isWrapperTypeOrPrimitiveType(key.getClass())) {
		throw new RuntimeException(CommonConstants.KEY_OF_MAP_CONVERT_EXCEPTION);
	    }
	    if (value != null && !isWrapperTypeOrPrimitiveType(value.getClass())) {
		throw new RuntimeException(CommonConstants.VALUE_OF_MAP_CONVERT_EXCEPTION);
	    }
	}

	try {
	    toField.set(toObject, fromValue);
	} catch (IllegalAccessException e) {
	    throw new RuntimeException(e);
	}

    }

    public static Object newInstanceOf(Field field) throws InstantiationException, IllegalAccessException {

	Object reult = null;
	if (Collection.class.equals(field.getType())) {
	    ParameterizedType type = (ParameterizedType) field.getGenericType();
	    Class<?> typeClass = (Class<?>) type.getActualTypeArguments()[0];
	    reult = typeClass.newInstance();
	}

	if (field.getType().isArray()) {
	    reult = field.getType().getComponentType().newInstance();
	}
	return reult;
    }

    public static boolean isWrapperTypeOrPrimitiveType(Field field) {

	boolean result = false;
	if (field.getType().isAssignableFrom(Collection.class)) {
	    ParameterizedType type = (ParameterizedType) field.getGenericType();
	    result = WRAPPER_TYPES.contains(type.getActualTypeArguments()[0]);
	}

	if (field.getType().isArray()) {
	    result = WRAPPER_TYPES.contains(field.getType().getComponentType());
	}
	return result;
    }

    public static boolean isWrapperTypeOrPrimitiveType(Class<?> clazz) {
	return WRAPPER_TYPES.contains(clazz);
    }

    public static Set<Class<?>> getWrapperTypes() {
	Set<Class<?>> ret = new HashSet<Class<?>>();

	ret.add(String.class);
	ret.add(Boolean.class);
	ret.add(boolean.class);
	ret.add(Character.class);
	ret.add(char.class);
	ret.add(Byte.class);
	ret.add(byte.class);
	ret.add(Short.class);
	ret.add(short.class);
	ret.add(Integer.class);
	ret.add(int.class);
	ret.add(Long.class);
	ret.add(long.class);
	ret.add(Float.class);
	ret.add(float.class);
	ret.add(Double.class);
	ret.add(double.class);
	return ret;
    }
}
