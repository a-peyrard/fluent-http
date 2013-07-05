package net.codestory.http;

import java.io.*;
import java.net.*;
import java.util.*;

import net.codestory.http.routes.*;

import com.sun.net.httpserver.*;

public class WebServer {
  private final HttpServer server;
  private final RouteCollection routes = new RouteCollection();

  public WebServer() {
    try {
      server = HttpServer.create();
    } catch (IOException e) {
      throw new IllegalStateException("Unable to create http server", e);
    }
  }

  public WebServer configure(Configuration configuration) {
    configuration.configure(routes);
    return this;
  }

  public WebServer start(int port) {
    try {
      server.bind(new InetSocketAddress(port), 0);
      server.createContext("/", this::handleRequest);
      server.start();
    } catch (IOException e) {
      throw new IllegalStateException("Unable to bind the web server on port " + port);
    }
    return this;
  }

  protected void handleRequest(HttpExchange exchange) {
    try {
      if (!routes.apply(exchange)) {
        exchange.sendResponseHeaders(404, 0);
      }
    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      exchange.close();
    }
  }

  public WebServer startOnRandomPort() {
    Random random = new Random();

    for (int i = 0; i < 20; i++) {
      try {
        int port = 8183 + random.nextInt(1000);
        start(port);
        return this;
      } catch (Exception e) {
        System.err.println("Unable to bind server: " + e);
      }
    }

    throw new IllegalStateException("Unable to start server");
  }

  public int port() {
    return server.getAddress().getPort();
  }

  public void stop() {
    server.stop(0);
  }
}
