package io.rusleo.minirest.util;

public final class Strings {
    private Strings() {
    }

    public static long parseLongOr(String s, long def) {
        try {
            return Long.parseLong(s);
        } catch (Exception e) {
            return def;
        }
    }

    public static int parseIntOr(String s, int def) {
        try {
            return Integer.parseInt(s);
        } catch (Exception e) {
            return def;
        }
    }
}
