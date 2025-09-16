package io.rusleo.minirest.util;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

public final class QueryParams {
    private QueryParams() {
    }

    public static Map<String, String> parse(String raw) {
        if (raw == null || raw.isEmpty()) return Map.of();
        Map<String, String> m = new LinkedHashMap<>();
        for (String p : raw.split("&")) {
            int i = p.indexOf('=');
            if (i >= 0) {
                String k = urlDecode(p.substring(0, i));
                String v = urlDecode(p.substring(i + 1));
                m.put(k, v);
            } else {
                m.put(urlDecode(p), "");
            }
        }
        return m;
    }

    private static String urlDecode(String s) {
        return URLDecoder.decode(s, StandardCharsets.UTF_8);
    }
}
