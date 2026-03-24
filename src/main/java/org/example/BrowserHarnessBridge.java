package org.example;

public final class BrowserHarnessBridge {
    private BrowserHarnessBridge() {
    }

    public static boolean isAvailable() {
        try {
            return nativeIsSupported();
        } catch (UnsatisfiedLinkError error) {
            return false;
        }
    }

    public static String getQueryParam(String key) {
        if (!isAvailable()) {
            return null;
        }
        try {
            return nativeGetQueryParam(key);
        } catch (UnsatisfiedLinkError error) {
            return null;
        }
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
