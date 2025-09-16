package io.rusleo.minirest.http;

import com.sun.net.httpserver.HttpExchange;

import java.io.*;
import java.nio.charset.StandardCharsets;

public final class HttpExchangeHelper {
    private HttpExchangeHelper() {
    }

    public static void sendPlain(HttpExchange ex, int code, String text) {
        sendBytes(ex, code, "text/plain; charset=utf-8", text.getBytes(StandardCharsets.UTF_8));
    }

    public static void sendJson(HttpExchange ex, int code, String json) {
        sendBytes(ex, code, "application/json; charset=utf-8", json.getBytes(StandardCharsets.UTF_8));
    }

    public static void safeSendError(HttpExchange ex, Throwable t) {
        try {
            String msg = t.getMessage() == null ? "" : t.getMessage().replace("\"", "'");
            sendJson(ex, 500, "{\"error\":\"" + t.getClass().getSimpleName() + "\",\"message\":\"" + msg + "\"}");
        } catch (Exception ignored) {
        }
    }

    public static String readBodyAsString(HttpExchange ex, int maxBytes) throws IOException {
        try (InputStream is = ex.getRequestBody()) {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            byte[] buf = new byte[4096];
            int r, total = 0;
            while ((r = is.read(buf)) != -1) {
                total += r;
                if (total > maxBytes) throw new IOException("Request body too large");
                bos.write(buf, 0, r);
            }
            return bos.toString(StandardCharsets.UTF_8);
        }
    }

    private static void sendBytes(HttpExchange ex, int code, String contentType, byte[] data) {
        try {
            ex.getResponseHeaders().set("Content-Type", contentType);
            ex.sendResponseHeaders(code, data.length);
            try (OutputStream os = ex.getResponseBody()) {
                os.write(data);
            }
        } catch (IOException ignored) {
        } finally {
            try {
                ex.close();
            } catch (Exception ignored) {
            }
        }
    }
}
