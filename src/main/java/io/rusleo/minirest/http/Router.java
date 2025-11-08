package io.rusleo.minirest.http;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import io.rusleo.minirest.metrics.MetricsRegistry;

import java.io.IOException;
import java.util.*;

import static io.rusleo.minirest.http.HttpExchangeHelper.*;

public final class Router implements HttpHandler {
    private static final class Entry {
        final HttpMethod method;
        final PathPattern pattern;
        final Route route;

        Entry(HttpMethod m, String p, Route r) {
            this.method = m;
            this.pattern = new PathPattern(p);
            this.route = r;
        }
    }

    private final List<Entry> routes = new ArrayList<>();
    private final MetricsRegistry metrics;

    public Router(MetricsRegistry metrics) {
        this.metrics = metrics;
    }

    public Router get(String path, Route route) {
        routes.add(new Entry(HttpMethod.GET, path, route));
        return this;
    }

    public Router post(String path, Route route) {
        routes.add(new Entry(HttpMethod.POST, path, route));
        return this;
    }

    @Override
    public void handle(HttpExchange ex) throws IOException {
        HttpMethod method = HttpMethod.from(ex.getRequestMethod());
        if (method == null) {
            sendPlain(ex, 405, "Method Not Allowed");
            return;
        }

        String path = ex.getRequestURI().getPath();
        Entry match = null;
        Map<String, String> pathParams = null;

        for (Entry e : routes) {
            if (e.method != method) continue;
            Map<String, String> params = e.pattern.match(path);
            if (params != null) {
                match = e;
                pathParams = params;
                break;
            }
        }

        if (match == null) {
            sendPlain(ex, 404, "Not Found");
            return;
        }

        long start = System.nanoTime();
        metrics.incHttpInFlight();
        try {
            // ВАЖНО: выполняем хендлер СИНХРОННО в том потоке,
            // который выделил HttpServer из своего executor’а.
            match.route.handle(ex, pathParams);
            metrics.markHttpCompleted(System.nanoTime() - start);
        } catch (Throwable t) {
            safeSendError(ex, t);
        } finally {
            metrics.decHttpInFlight();
        }
    }
}
