package com.ch.aidu.utils;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 作者： ch
 * 时间： 2018/8/7 0007-下午 4:47
 * 描述：
 * 来源：
 */


public class DataUtils {


    //将时间转换成日期
    public static String dateConvert(long time, String pattern) {
        Date date = new Date(time);
        SimpleDateFormat format = new SimpleDateFormat(pattern);
        return format.format(date);
    }
}
