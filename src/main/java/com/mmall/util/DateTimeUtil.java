package com.mmall.util;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.NameValuePair;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class DateTimeUtil {

    public static String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";
    //字符串转Date
    public  static Date strToDate(String dateStr ,String dateFormatStr)
    {
        DateTimeFormatter dateTimeFormatter = DateTimeFormat.forPattern(dateFormatStr);
        DateTime dateTime = dateTimeFormatter.parseDateTime(dateStr);
        return dateTime.toDate();
    }

    //字符串转Date
    public  static String dateToStr(Date date ,String dateFormatStr)
    {
        if(date == null)
        {
            return StringUtils.EMPTY;
        }
        DateTime dateTime = new DateTime(date);
        return  dateTime.toString(dateFormatStr);
    }

    //字符串转Date
    public  static Date strToDate(String dateStr)
    {
        DateTimeFormatter dateTimeFormatter = DateTimeFormat.forPattern(DATE_FORMAT);
        DateTime dateTime = dateTimeFormatter.parseDateTime(dateStr);
        return dateTime.toDate();
    }

    //字符串转Date
    public  static String dateToStr(Date date)
    {
        if(date == null)
        {
            return StringUtils.EMPTY;
        }
        DateTime dateTime = new DateTime(date);
        return  dateTime.toString(DATE_FORMAT);
    }
    //快捷键 psvm + Tab
    public static void main(String[] args) {
        //快捷键 sout + Tab
        System.out.println(DateTimeUtil.dateToStr(new Date(),"yyyy-MM-dd HH:mm:ss"));
        System.out.println(DateTimeUtil.strToDate("2019-08-01 20:25:25","yyyy-MM-dd HH:mm:ss"));
        System.out.println(DateTimeUtil.dateToStr(new Date()));
        System.out.println(DateTimeUtil.strToDate("2019-08-01 20:25:25"));
        List<NameValuePair> nvps = new ArrayList<NameValuePair>();
        CollectionUtils.isNotEmpty(nvps);
    }
}
