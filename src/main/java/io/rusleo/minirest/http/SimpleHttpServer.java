package io.rusleo.minirest.http;

import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.Executor;

public final class SimpleHttpServer {
    private final HttpServer server;

    public SimpleHttpServer(InetSocketAddress address, Executor httpExecutor, Router router) throws IOException {
        this.server = HttpServer.create(address, 0);
        this.server.setExecutor(httpExecutor);
        // Один общий контекст на "/": дальше роутинг берёт на себя Router
        this.server.createContext("/", router);
    }

    public void start() {
        server.start();
    }

    public void stop(int delaySeconds) {
        server.stop(delaySeconds);
    }
}
