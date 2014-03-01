/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.wizzardo.tools.collections;

import java.util.Map;

/**
 * @author Moxa
 */
public class MapTools {

    public static Long getLong(Map map, String key) {
        Object ob = map.get(key);
        if (ob == null)
            return null;
        if (ob instanceof Long) {
            return (Long) ob;
        }
        if (ob instanceof String) {
            try {
                return Long.parseLong(ob.toString());
            } catch (NumberFormatException ignore) {
            }
        }
        return null;
    }

    public static Integer getInteger(Map map, String key) {
        Object ob = map.get(key);
        if (ob == null)
            return null;
        if (ob instanceof Integer) {
            return (Integer) ob;
        }
        if (ob instanceof String) {
            try {
                return Integer.parseInt(ob.toString());
            } catch (NumberFormatException ignore) {
            }
        }
        return null;
    }

    public static Double getDouble(Map map, String key) {
        Object ob = map.get(key);
        if (ob == null)
            return null;
        if (ob instanceof Double) {
            return (Double) ob;
        }
        if (ob instanceof String) {
            try {
                return Double.parseDouble(ob.toString());
            } catch (NumberFormatException ignore) {
            }
        }
        return null;
    }

    public static Boolean getBoolean(Map map, String key) {
        Object ob = map.get(key);
        if (ob == null)
            return null;
        if (ob instanceof Boolean) {
            return (Boolean) ob;
        }
        if (ob instanceof String) {
            return Boolean.parseBoolean(ob.toString());
        }
        return null;
    }

    public static Float getFloat(Map map, String key) {
        Object ob = map.get(key);
        if (ob == null)
            return null;
        if (ob instanceof Float) {
            return (Float) ob;
        }
        if (ob instanceof String) {
            try {
                return Float.parseFloat(ob.toString());
            } catch (NumberFormatException ignore) {
            }
        }
        return null;
    }

    public static String getString(Map map, String key) {
        Object ob = map.get(key);
        if (ob == null)
            return null;
        return ob.toString();
    }

    public static long getLong(Map map, String key, long def) {
        Long l = getLong(map, key);
        if (l != null) {
            return l;
        }
        return def;
    }

    public static int getInteger(Map map, String key, int def) {
        Integer l = getInteger(map, key);
        if (l != null) {
            return l;
        }
        return def;
    }

    public static double getDouble(Map map, String key, double def) {
        Double l = getDouble(map, key);
        if (l != null) {
            return l;
        }
        return def;
    }

    public static float getFloat(Map map, String key, float def) {
        Float l = getFloat(map, key);
        if (l != null) {
            return l;
        }
        return def;
    }

    public static boolean getBoolean(Map map, String key, boolean def) {
        Boolean l = getBoolean(map, key);
        if (l != null) {
            return l;
        }
        return def;
    }

    public static String getString(Map map, String key, String def) {
        String s = getString(map, key);
        if (s != null) {
            return s;
        }
        return def;
    }
}
