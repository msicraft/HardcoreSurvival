package me.msicraft.hardcoresurvival.Utils;

import java.text.SimpleDateFormat;

public class TimeUtil {

    private TimeUtil() {}

    private final static SimpleDateFormat SIMPLE_DATE_FORMAT = new SimpleDateFormat("yyyy년 MM월 dd일 HH:mm:ss");

    public static String getTimeToFormat(String format, long time) {
        return new SimpleDateFormat(format).format(time);
    }

    public static String getTimeToFormat(long time) {
        return SIMPLE_DATE_FORMAT.format(time);
    }

}
