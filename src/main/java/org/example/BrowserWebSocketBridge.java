package org.example;

public final class BrowserWebSocketBridge {
    private BrowserWebSocketBridge() {
    }

    public static boolean isAvailable() {
        try {
            return nativeIsSupported();
        } catch (UnsatisfiedLinkError error) {
            return false;
        }
    }

    public static void connect(String url) {
        nativeConnect(url);
    }

    public static boolean isOpen() {
        return nativeIsOpen();
    }

    public static String pollMessage() {
        return nativePollMessage();
    }

    public static String pollError() {
        return nativePollError();
    }

    public static void send(String message) {
        nativeSend(message);
    }

    public static void close() {
        nativeClose();
    }

    private static native boolean nativeIsSupported();

    private static native void nativeConnect(String url);

    private static native boolean nativeIsOpen();

    private static native String nativePollMessage();

    private static native String nativePollError();

    private static native void nativeSend(String message);

    private static native void nativeClose();
}
