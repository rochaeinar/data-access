package com.erc.dal;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by einar on 10/11/2015.
 */
public class HelperDate {
    public static final String ISO_FORMAT = "yyyy-MM-dd'T'HH:mm:ss";

    public static Date getDateFromFormat(String dateInFormat, String format) {

        if (!Util.isNullOrEmpty(dateInFormat)) {
            try {
                DateFormat df1 = new SimpleDateFormat(format);
                Date result1 = df1.parse(dateInFormat);
                return result1;
            } catch (Exception e) {
                Log.e("Error on getDateFromFormat", e);
            }
        }
        return null;
    }

    public static String getDateWithFormat(Date date, String format) {
        try {
            DateFormat dateFormat = new SimpleDateFormat(format);
            return dateFormat.format(date);
        } catch (Exception e) {
            Log.e("Error on getDateWithFormat", e);
        }
        return null;
    }
}
