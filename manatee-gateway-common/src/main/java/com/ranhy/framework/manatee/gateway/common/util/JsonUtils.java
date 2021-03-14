package com.ranhy.framework.manatee.gateway.common.util;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.PropertyNamingStrategy.PropertyNamingStrategyBase;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.joda.JodaModule;
import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Type;
import java.util.TimeZone;

@SuppressWarnings("unchecked")
public class JsonUtils {

    private final static HzObjectMapper JAVA_OBJECT_MAPPER = buildDefaultObjectMapper();

    private final static HzObjectMapper C_SHARP_OBJECT_MAPPER;

    static {
	C_SHARP_OBJECT_MAPPER = buildDefaultObjectMapper();
	C_SHARP_OBJECT_MAPPER.setPropertyNamingStrategy(new PropertyNamingStrategyBase() {

	    private static final long serialVersionUID = 1L;

	    @Override
	    public String translate(String propertyName) {

		String result = null;
		char firstWorld = propertyName.charAt(0);
		String first = String.valueOf(firstWorld);
		if (Character.isUpperCase(firstWorld)) {
		    result = first.toLowerCase() + propertyName.substring(1);
		} else {
		    result = first.toUpperCase() + propertyName.substring(1);
		}
		return result;

	    }
	});
    }

    /**
     * 忽略空字段 获取ObjectMapper实例
     * 
     * @param isCreateNewMapper 方式：true，新实例；false,存在的mapper实例
     * @return
     */
    public static HzObjectMapper getMapperInstance(boolean isCreateNewMapper) {

	HzObjectMapper objectMapper;
	if (isCreateNewMapper) {
	    objectMapper = buildDefaultObjectMapper();
	} else {
	    objectMapper = JAVA_OBJECT_MAPPER;
	}
	return objectMapper;
    }

    private static HzObjectMapper buildDefaultObjectMapper() {
	HzObjectMapper objectMapper = new HzObjectMapper();

	objectMapper.getFactory().enable(JsonFactory.Feature.INTERN_FIELD_NAMES);
	objectMapper.getFactory().enable(JsonFactory.Feature.CANONICALIZE_FIELD_NAMES);
	objectMapper.configure(JsonParser.Feature.ALLOW_COMMENTS, true);
	objectMapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);
	objectMapper.configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true);
	objectMapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_CONTROL_CHARS, true);
	objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
	objectMapper.configure(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT, true);

	objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
	objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
	objectMapper.setTimeZone(TimeZone.getTimeZone("GMT+8"));
	objectMapper.setDateFormat(new HzStdDateFormat());
	objectMapper.registerModule(new JodaModule());
	return objectMapper;
    }

    /**
     * 将java对象转换成json字符串
     * 
     * @param obj 准备转换的对象
     * @return json字符串
     * @throws Exception
     */
    public static String beanToJson(Object obj) {

	String json = null;
	try {
	    if (obj != null) {
		HzObjectMapper objectMapper = getMapperInstance(false);
		json = objectMapper.writeValueAsString(obj);
	    }
	} catch (Exception e) {
	    throw new RuntimeException(e);
	}
	return json;
    }

    /**
     * 将java对象转换成json字符串，忽略Annotaion
     * 
     * @param obj 准备转换的对象
     * @return json字符串
     * @throws Exception
     */
    public static String beanToJsonIgnoreAnnotaion(Object obj) {

	String json = null;
	try {
	    if (obj != null) {
		HzObjectMapper objectMapper = getMapperInstance(true);
		objectMapper.configure(MapperFeature.USE_ANNOTATIONS, false);
		json = objectMapper.writeValueAsString(obj);
	    }
	} catch (Exception e) {
	    throw new RuntimeException(e);
	}
	return json;
    }

    /**
     * 将java对象转换成json字符串
     * 
     * @param obj 准备转换的对象
     * @param createNew ObjectMapper实例方式:true，新实例;false,存在的mapper实例
     * @return json字符串
     * @throws Exception
     */
    public static String beanToJson(Object obj, Boolean createNew) {

	String json = null;
	try {
	    if (obj != null) {
		HzObjectMapper objectMapper = getMapperInstance(createNew);
		json = objectMapper.writeValueAsString(obj);
	    }
	} catch (Exception e) {
	    throw new RuntimeException(e);
	}
	return json;

    }

    /**
     * 将json字符串转换成java对象
     * 
     * @param json 准备转换的json字符串
     * @param cls 准备转换的类
     * @return
     * @throws Exception
     */
    public static <T> T jsonToBean(String json, Class<T> cls) {

	T t = null;
	try {
	    if (json != null) {
		if (String.class.equals(cls)) {
		    t = (T) json;
		} else {
		    HzObjectMapper objectMapper = getMapperInstance(false);
		    t = objectMapper.readValue(json, cls);
		}
	    }
	} catch (Exception e) {
	    throw new RuntimeException(e);
	}
	return t;
    }

    public static <T> T jsonToBean(String json, Type type) {

	T t = null;
	try {
	    if (json != null) {
		if (String.class.equals(type)) {
		    t = (T) json;
		} else {
		    HzObjectMapper objectMapper = getMapperInstance(false);
		    t = objectMapper.readValue(json, type);
		}
	    }
	} catch (Exception e) {
	    throw new RuntimeException(e);
	}
	return t;
    }

    public static <T> T jsonToBean(String json, Type type, String lang) {
	T t = null;
	try {
	    if (json != null) {
		if (String.class.equals(type)) {
		    t = (T) json;
		} else {
		    if (StringUtils.isNotBlank(json)) {
			HzObjectMapper objectMapper = JAVA_OBJECT_MAPPER;
			if (CommonConstants.LANG_OF_C_SHARP_AUTO_CONVERT.equals(lang)) {
			    objectMapper = C_SHARP_OBJECT_MAPPER;
			}
			t = objectMapper.readValue(json, type);
		    } else {
			t = (T) json;
		    }
		}
	    }
	} catch (Exception e) {
	    throw new RuntimeException(e);
	}
	return t;
    }

    public static <T> T jsonToBean(String json, TypeReference<T> cls) {

	T t = null;
	try {
	    if (json != null) {
		HzObjectMapper objectMapper = getMapperInstance(false);
		t = objectMapper.readValue(json, cls);
	    }
	} catch (Exception e) {
	    throw new RuntimeException(e);
	}
	return t;
    }

    /**
     * 将json字符串转换成java对象
     * 
     * @param json 准备转换的json字符串
     * @param cls 准备转换的类
     * @param createNew ObjectMapper实例方式:true，新实例;false,存在的mapper实例
     * @return
     * @throws Exception
     */
    public static <T> T jsonToBean(String json, Class<T> cls, Boolean createNew) {

	T t = null;
	try {
	    if (json != null) {
		if (String.class.equals(cls)) {
		    t = (T) json;
		} else {
		    HzObjectMapper objectMapper = getMapperInstance(createNew);
		    t = objectMapper.readValue(json, cls);
		}
	    }
	} catch (Exception e) {
	    throw new RuntimeException(e);
	}
	return t;
    }

    public static String beanToJson(Object obj, String lang) {

	String json = null;
	try {
	    if (obj != null) {
		HzObjectMapper objectMapper = JAVA_OBJECT_MAPPER;
		if (CommonConstants.LANG_OF_C_SHARP_AUTO_CONVERT.equals(lang)) {
		    objectMapper = C_SHARP_OBJECT_MAPPER;
		}
		json = objectMapper.writeValueAsString(obj);
	    }
	} catch (Exception e) {
	    throw new RuntimeException(e);
	}
	return json;
    }

    public static String beanToJson(Object... objects) {

	String json = null;
	if (objects != null) {
	    try {
		HzObjectMapper objectMapper = getMapperInstance(false);
		json = objectMapper.writeValueAsString(objects);
	    } catch (Exception e) {
		throw new RuntimeException(e);
	    }
	}
	return json;
    }

    public static boolean isJson(String str) {
	boolean isJson = false;
	if (StringUtils.isNotBlank(str)) {
	    isJson = (str.startsWith("{") && str.endsWith("}"))||(str.startsWith("\"")&&str.endsWith("\""));
	}
	return isJson;
    }
    
    
}
