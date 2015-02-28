package com.wizzardo.tools.misc;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Moxa
 */
public class TextTools {

    public static boolean isBlank(String s) {
        return s == null || s.length() == 0;
    }

    public static float asFloat(String s) {
        return asFloat(s, -1);
    }

    public static float asFloat(String s, float def) {
        try {
            return Float.parseFloat(s);
        } catch (NumberFormatException ex) {
            try {
                return Float.parseFloat(s.replace(',', '.'));
            } catch (NumberFormatException ignore) {
            }
        }
        return def;
    }

    public static int asInt(String s) {
        return asInt(s, -1);
    }


    public static int asInt(String s, int def) {
        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException ignore) {
        }
        return def;
    }

    public static long asLong(String s) {
        return asLong(s, -1);
    }

    public static long asLong(String s, long def) {
        try {
            return Long.parseLong(s);
        } catch (NumberFormatException ignore) {
        }
        return def;
    }

    public static double asDouble(String s) {
        return asDouble(s, -1);
    }

    public static double asDouble(String s, double def) {
        try {
            return Double.parseDouble(s);
        } catch (NumberFormatException ex) {
            try {
                return Double.parseDouble(s.replace(',', '.'));
            } catch (NumberFormatException ignore) {
            }
        }
        return def;
    }

    public static List<String> findAll(String s, Pattern p) {
        LinkedList<String> l = new LinkedList<String>();
        Matcher m = p.matcher(s);
        while (m.find()) {
            l.add(m.group());
        }
        return l;
    }

    public static String find(String s, Pattern p) {
        return find(s, p, 0);
    }

    public static String find(String s, Pattern p, int group) {
        Matcher m = p.matcher(s);
        if (m.find()) {
            return m.group(group);
        }
        return null;
    }

    public static List<String> asListAndTrim(String[] arr) {
        ArrayList<String> l = new ArrayList<String>(arr.length);
        for (String s : arr) {
            s = s.trim();
            if (s.length() > 0) {
                l.add(s);
            }
        }
        return l;
    }
}
