package io.rusleo.minirest.http;

public enum HttpMethod {
    GET, POST, PUT, DELETE, PATCH, OPTIONS, HEAD;

    public static HttpMethod from(String s) {
        try {
            return HttpMethod.valueOf(s.toUpperCase());
        } catch (Exception e) {
            return null;
        }
    }
}
