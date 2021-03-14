package com.ranhy.framework.manatee.gateway.common.util;

import com.google.common.base.Throwables;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DateUtils {

    public static String getToday(String format) {
	SimpleDateFormat SIMPLE_DATE_FORMAT = new SimpleDateFormat();
	SIMPLE_DATE_FORMAT.applyPattern(format);
	return SIMPLE_DATE_FORMAT.format(new Date());
    }

    public static Date strToDate(String str, String format) {
	SimpleDateFormat df = new SimpleDateFormat(format);
	Date date = null;
	try {
	    date = df.parse(str);
	} catch (ParseException e) {
	    throw Throwables.propagate(e);
	}
	return date;
    }

    public static Date getDate(String time) {

	Date date = null;
	try {
	    SimpleDateFormat SIMPLE_DATE_FORMAT = new SimpleDateFormat();
	    SIMPLE_DATE_FORMAT.applyPattern(CommonConstants.DATETIME_FORMAT);
	    date = SIMPLE_DATE_FORMAT.parse(time);
	} catch (ParseException e) {
	    e.printStackTrace();
	}
	return date;
    }

    public static long getTicks() {
	long milli = System.currentTimeMillis() + 8 * 3600 * 1000;
	long ticks = (milli * 10000) + 621355968000000000L;
	return ticks;
    }

}
