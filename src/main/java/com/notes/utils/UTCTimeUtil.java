package com.notes.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * UTC格式时间帮助类
 *
 * @author Liugl
 * @version 2020/2/17 15:40,Liugl,Ins
 * @copyright 2020/2/17 15:40
 */
public class UTCTimeUtil {

    public static final String SDF1 = "yyyy-MM-dd HH:mm:ss.SSS";

    public static final String buildRandom(boolean numberFlag, int length) {
        String strTable = numberFlag ? "123456789" : "123456789abcdefghijkmnpqrstuvwxyzABCDEFGHIJKMNPQRSTUVWXYZ";
        int len = strTable.length();
        StringBuffer retStr = new StringBuffer();
        String str;
        for (int i = 0; i < length; i++) {
            double dblR = Math.random() * len;
            str = String.valueOf(dblR);
            int intR = Integer.parseInt(str.substring(0, str.indexOf(".")));
            retStr.append(strTable.charAt(intR));
        }
        return retStr.toString();
    }
    /**
     * 获得世界协调时的近似值
     *
     * @return Date
     */
    public static Date getUTCTime() {
        Calendar cal = Calendar.getInstance();
        //获得时区和 GMT-0 的时间差,偏移量
        int offset = cal.get(Calendar.ZONE_OFFSET);
        //获得夏令时 时差
        int dstoff = cal.get(Calendar.DST_OFFSET);
        cal.add(Calendar.MILLISECOND, -(offset + dstoff));
        return cal.getTime();
    }

    /**
     * 获得世界协调时的近似值
     *
     * @param format 格式化的时间格式
     * @return String
     */
    public static String getUTCTime(String format) {
        return format(getUTCTime(), format);
    }

    /**
     * 根据传入得本地时间获得 获得 这个对应得UTC 时间
     *
     * @param localDate
     * @param format
     * @return
     */
    public static String getUTCTimeByLocalTime(String localDate, String format) {
        Calendar cal = Calendar.getInstance();
        //获得时区和 GMT-0 的时间差,偏移量
        int offset = cal.get(Calendar.ZONE_OFFSET);
        //获得夏令时  时差
        int dstoff = cal.get(Calendar.DST_OFFSET);
        //获得当前是UTC时区的时间毫秒值
        Date date = new Date(getMillSecond(format, localDate) - (offset + dstoff));
        return format(date, format);
    }


    /**
     * 根据utc时间的字符串形式,获得当前时区的本地时间
     *
     * @param utcTime 时间字符串形式
     * @param format  时间格式为:yyyyMMddHHmmssS   精确到毫秒值
     * @return
     */
    public static Date getLocalZoneTime(String utcTime, String format) {
        Calendar cal = Calendar.getInstance();
        //获得时区和 GMT-0 的时间差,偏移量
        int offset = cal.get(Calendar.ZONE_OFFSET);
        //获得夏令时  时差
        int dstoff = cal.get(Calendar.DST_OFFSET);
        //获得当前是时区的时间毫秒值
        Date date = new Date(getMillSecond(format, utcTime) + (offset + dstoff));
        return date;
    }

    /**
     * 根据utc时间的字符串形式,获得当前时区的本地时间
     *
     * @param utcTime 时间字符串形式
     * @param format  时间格式为:yyyyMMddHHmmssS   精确到毫秒值
     * @return
     */
    public static String getLocalZoneTimeString(String utcTime, String format) {
        return format(getLocalZoneTime(utcTime, format), format);
    }

    /**
     * 根据时间的字符串形式获得时间的毫秒值
     *
     * @param format 最好为yyyyMMddHHmmssS 精确到毫秒值,这样转换没有精度损失
     * @return
     */
    public static long getMillSecond(String format, String time) {
        Date parse = parse(time, format);
        return parse.getTime();
    }

    /**
     * 使用用户格式格式化日期
     *
     * @param date    日期
     * @param pattern 日期格式
     * @return
     */
    public static String format(Date date, String pattern) {
        String returnValue = "";
        if (date != null) {
            SimpleDateFormat df = new SimpleDateFormat(pattern);
            returnValue = df.format(date);
        }
        return (returnValue);
    }

    /**
     * 使用用户格式提取字符串日期
     *
     * @param strDate 日期字符串
     * @param pattern 日期格式
     * @return
     */
    public static Date parse(String strDate, String pattern) {
        SimpleDateFormat df = new SimpleDateFormat(pattern);
        try {
            return df.parse(strDate);
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }

}
