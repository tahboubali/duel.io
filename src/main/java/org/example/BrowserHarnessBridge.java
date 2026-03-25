package org.example;

import java.util.HashMap;
import java.util.Map;

public final class BrowserHarnessBridge {
    private static final Map<String, String> QUERY_CACHE = new HashMap<String, String>();
    private static Boolean available;

    private BrowserHarnessBridge() {
    }

    public static boolean isAvailable() {
        if (available != null) {
            return available.booleanValue();
        }
        try {
            available = Boolean.valueOf(nativeIsSupported());
        } catch (UnsatisfiedLinkError error) {
            available = Boolean.FALSE;
        }
        return available.booleanValue();
    }

    public static String getQueryParam(String key) {
        synchronized (QUERY_CACHE) {
            if (QUERY_CACHE.containsKey(key)) {
                return QUERY_CACHE.get(key);
            }
        }
        if (!isAvailable()) {
            return null;
        }
        String value = null;
        try {
            value = nativeGetQueryParam(key);
        } catch (UnsatisfiedLinkError error) {
            // Ignore when not running in the browser harness.
        }
        synchronized (QUERY_CACHE) {
            QUERY_CACHE.put(key, value);
        }
        return value;
    }

    public static boolean isEnabled(String key) {
        String value = getQueryParam(key);
        if (value == null) {
            return false;
        }
        String trimmed = value.trim();
        return "1".equals(trimmed)
                || "true".equalsIgnoreCase(trimmed)
                || "yes".equalsIgnoreCase(trimmed)
                || "on".equalsIgnoreCase(trimmed);
    }

    public static void reportStatus(String message) {
        if (!isAvailable()) {
            return;
        }
        try {
            nativeReportStatus(message);
        } catch (UnsatisfiedLinkError error) {
            // Ignore when not running in the browser harness.
        }
    }

    private static native boolean nativeIsSupported();

    private static native String nativeGetQueryParam(String key);

    private static native void nativeReportStatus(String message);
}
