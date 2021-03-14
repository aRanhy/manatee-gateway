package com.ranhy.framework.manatee.gateway.common.util;

import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Type;
import java.util.List;


/**
 * @author	 hz18093501 hongyu.ran
 * @date	2019年8月8日
 */
public class CatfishMessageUtils {

    public static String fetchInterfaceName(Class<?> clazz) {
        String result = null;
        Class<?>[] cs = clazz.getInterfaces();
        if ((cs != null) && (cs.length > 0)) {
            result = cs[0].getName();
        } else {
            result = clazz.getName();
        }
        return result;
    }

    public static String buildKey(String identity, Type[] genericParameterTypes) {
        int length = 0;
        if (genericParameterTypes != null) {
            length = genericParameterTypes.length;
        }
        return buildKey(identity, length);
    }

    public static String buildKey(String identity, int length) {
        String key = null;
        if (length > 0) {
            key = identity + "#" + length;
        } else {
            key = identity;
        }
        return key;
    }

    public static boolean isBooleanType(Class<?> type, Object parameterValue) {
        return (Boolean.class.equals(type)) && (!isNullString(parameterValue));
    }

    public static boolean isNumericType(Class<?> type, Object parameterValue) {
        return (ClassUtils.isPrimitiveWrapper(type)) && (StringUtils.isNumeric(String.valueOf(parameterValue)));
    }

    public static boolean isStringType(Class<?> type, Object parameterValue) {
        return String.class.equals(type);
    }

    public static boolean isNullString(Object value) {
        return "null".equals(String.valueOf(value));
    }

    public static boolean isJsonOfPropeties(String json, List<String> properties) {
        boolean result = true;
        if (StringUtils.isNotBlank(json)) {
            if ((json.startsWith("{")) && (json.endsWith("}"))) {
                for (String property : properties) {
                    if (!json.contains(property)) {
                        result = false;
                        break;
                    }
                }
            } else {
                result = false;
            }
        } else {
            result = false;
        }
        return result;
    }

    public static String wrapped(String string) {
        if (!JsonUtils.isJson(string)) {
            string = "\"" + string + "\"";
        }
        return string;
    }
}
