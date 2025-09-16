package io.rusleo.minirest.http;

import java.util.*;

public final class PathPattern {
    private final String pattern;
    private final String[] segments;
    private final boolean[] isVar;
    private final String[] varNames;

    public PathPattern(String pattern) {
        if (!pattern.startsWith("/")) throw new IllegalArgumentException("Pattern must start with /");
        this.pattern = pattern;
        this.segments = pattern.split("/");
        this.isVar = new boolean[segments.length];
        this.varNames = new String[segments.length];
        for (int i = 0; i < segments.length; i++) {
            String s = segments[i];
            if (s.startsWith("{") && s.endsWith("}") && s.length() > 2) {
                isVar[i] = true;
                varNames[i] = s.substring(1, s.length() - 1);
            }
        }
    }

    public Map<String, String> match(String path) {
        String[] parts = path.split("/");
        if (parts.length != segments.length) return null;
        Map<String, String> result = new HashMap<>();
        for (int i = 0; i < segments.length; i++) {
            String seg = segments[i];
            String got = parts[i];
            if (seg.isEmpty() && got.isEmpty()) continue;
            if (isVar[i]) {
                result.put(varNames[i], got);
            } else if (!seg.equals(got)) {
                return null;
            }
        }
        return result;
    }

    @Override
    public String toString() {
        return pattern;
    }
}
