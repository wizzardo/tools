package org.bordl.utils;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author Moxa
 */
public class TextUtils {

    public static boolean isBlank(String s) {
        return s == null || s.length() == 0;
    }

    public static int getInt(String s) {
        return getInt(s, -1);
    }

    public static float getFloat(String s) {
        return getFloat(s, -1);
    }

    public static float getFloat(String s, float def) {
        try {
            return Float.parseFloat(s);
        } catch (NumberFormatException ex) {
            try {
                return Float.parseFloat(s.replace(',', '.'));
            } catch (NumberFormatException e) {
            }
        }
        return def;
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

    public static int getInt(String s, int def) {
        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException ex) {
            System.out.println("error parsing int: \"" + s + "\"");
            for (char c : s.toCharArray()) {
                System.out.println(c + "  " + (int) c);
            }
        }
        return def;
    }

    public static long getLong(String s, long def) {
        try {
            return Long.parseLong(s);
        } catch (NumberFormatException ex) {
            System.out.println("error parsing long: \"" + s + "\"");
            for (char c : s.toCharArray()) {
                System.out.println(c + "  " + (int) c);
            }
        }
        return def;
    }

    public static double getDouble(String s) {
        return getDouble(s, -1);
    }

    public static double getDouble(String s, double def) {
        try {
            return Double.parseDouble(s);
        } catch (NumberFormatException ex) {
            try {
                return Double.parseDouble(s.replace(',', '.'));
            } catch (NumberFormatException e) {
            }
        }
        return def;
    }

    public static String removeAllNbsp(String s) {
        return s.replaceAll("\u00A0", "");
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

    public static String removeAllButNumber(String s) {
        return s.replaceAll("[^\\.\\d]+", "");
    }
}
