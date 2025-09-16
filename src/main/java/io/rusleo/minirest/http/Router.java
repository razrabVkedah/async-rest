package io.rusleo.minirest.http;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import io.rusleo.minirest.metrics.MetricsRegistry;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

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
    private final ThreadPoolExecutor worker;
    private final MetricsRegistry metrics;

    public Router(ThreadPoolExecutor worker, MetricsRegistry metrics) {
        this.worker = worker;
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

        final long start = System.nanoTime();
        metrics.incHttpInFlight();

        final Entry routeMatch = match;
        final Map<String, String> routeParams = pathParams;
        final HttpExchange exchange = ex;
        final long startTime = start;

        try {
            worker.execute(() -> {
                try {
                    routeMatch.route.handle(exchange, routeParams);
                    metrics.markHttpCompleted(System.nanoTime() - startTime);
                } catch (Throwable t) {
                    safeSendError(exchange, t);
                } finally {
                    metrics.decHttpInFlight();
                }
            });
        } catch (RuntimeException rejected) {
            metrics.decHttpInFlight();
            sendPlain(ex, 429, "Too Many Requests");
        }
    }
}
