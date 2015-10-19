package com.erc.dal;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by einar on 10/18/2015.
 */
public class RegEx {
    public static final String PATTERN_EXTENSION = "\\.[^\\.]*$";

    public static String match(String myString, String pattern) {
        String match = "";
        Pattern regEx = Pattern.compile(pattern);
        Matcher m = regEx.matcher(myString);
        if (m.find()) {
            match = m.group(0);
        }
        return match;
    }
}
