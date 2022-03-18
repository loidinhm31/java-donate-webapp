package com.donation.common.core.utils;

import java.util.Calendar;
import java.util.Date;

public class DateTimeUtils {
    public static Date getFromDate() {
        Calendar cal = Calendar.getInstance();
        System.out.println("TIME FROM: " + cal.getTime());
        cal.set(Calendar.DAY_OF_YEAR, 1);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        return cal.getTime();
    }

    public static Date getToDate() {
        Calendar cal = Calendar.getInstance();
        System.out.println("TIME TO: " + cal.getTime());

        cal.set(Calendar.DAY_OF_MONTH, 1);
        cal.add(Calendar.DAY_OF_MONTH, -1);
        cal.set(Calendar.HOUR_OF_DAY, 23);
        cal.set(Calendar.MINUTE, 59);
        cal.set(Calendar.SECOND, 59);

        return cal.getTime();
    }
}
