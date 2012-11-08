package com.yammer.metrics.httpclient;

import com.yammer.metrics.Metrics;
import com.yammer.metrics.core.MetricRegistry;
import com.yammer.metrics.core.Timer;
import com.yammer.metrics.core.TimerContext;
import org.apache.commons.logging.Log;
import org.apache.http.*;
import org.apache.http.client.*;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.ConnectionKeepAliveStrategy;
import org.apache.http.conn.routing.HttpRoutePlanner;
import org.apache.http.impl.client.DefaultRequestDirector;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpProcessor;
import org.apache.http.protocol.HttpRequestExecutor;

import java.io.IOException;

class InstrumentedRequestDirector extends DefaultRequestDirector {
    private final static String GET = "GET", POST = "POST", HEAD = "HEAD", PUT = "PUT",
            OPTIONS = "OPTIONS", DELETE = "DELETE", TRACE = "TRACE",
            CONNECT = "CONNECT", MOVE = "MOVE", PATCH = "PATCH";

    private final Timer getTimer;
    private final Timer postTimer;
    private final Timer headTimer;
    private final Timer putTimer;
    private final Timer deleteTimer;
    private final Timer optionsTimer;
    private final Timer traceTimer;
    private final Timer connectTimer;
    private final Timer moveTimer;
    private final Timer patchTimer;
    private final Timer otherTimer;

    InstrumentedRequestDirector(MetricRegistry registry,
                                Log log,
                                HttpRequestExecutor requestExec,
                                ClientConnectionManager conman,
                                ConnectionReuseStrategy reustrat,
                                ConnectionKeepAliveStrategy kastrat,
                                HttpRoutePlanner rouplan,
                                HttpProcessor httpProcessor,
                                HttpRequestRetryHandler retryHandler,
                                RedirectStrategy redirectStrategy,
                                AuthenticationStrategy targetAuthStrategy,
                                AuthenticationStrategy proxyAuthStrategy,
                                UserTokenHandler userTokenHandler,
                                HttpParams params) {
        super(log,
              requestExec,
              conman,
              reustrat,
              kastrat,
              rouplan,
              httpProcessor,
              retryHandler,
              redirectStrategy,
              targetAuthStrategy,
              proxyAuthStrategy,
              userTokenHandler,
              params);
        getTimer = registry.add(Metrics.name(HttpClient.class, "get-requests"),
                                new Timer());
        postTimer = registry.add(Metrics.name(HttpClient.class, "post-requests"),
                                 new Timer());
        headTimer = registry.add(Metrics.name(HttpClient.class, "head-requests"),
                                 new Timer());
        putTimer = registry.add(Metrics.name(HttpClient.class, "put-requests"),
                                new Timer());
        deleteTimer = registry.add(Metrics.name(HttpClient.class, "delete-requests"),
                                   new Timer());
        optionsTimer = registry.add(Metrics.name(HttpClient.class, "options-requests"),
                                    new Timer());
        traceTimer = registry.add(Metrics.name(HttpClient.class, "trace-requests"),
                                  new Timer());
        connectTimer = registry.add(Metrics.name(HttpClient.class, "connect-requests"),
                                    new Timer());
        moveTimer = registry.add(Metrics.name(HttpClient.class, "move-requests"),
                                 new Timer());
        patchTimer = registry.add(Metrics.name(HttpClient.class, "patch-requests"),
                                  new Timer());
        otherTimer = registry.add(Metrics.name(HttpClient.class, "other-requests"),
                                  new Timer());
    }

    @Override
    public HttpResponse execute(HttpHost target, HttpRequest request, HttpContext context) throws HttpException, IOException {
        final TimerContext timerContext = timer(request).time();
        try {
            return super.execute(target, request, context);
        } finally {
            timerContext.stop();
        }
    }

    private Timer timer(HttpRequest request) {
        final String method = request.getRequestLine().getMethod();
        if (GET.equalsIgnoreCase(method)) {
            return getTimer;
        } else if (POST.equalsIgnoreCase(method)) {
            return postTimer;
        } else if (PUT.equalsIgnoreCase(method)) {
            return putTimer;
        } else if (HEAD.equalsIgnoreCase(method)) {
            return headTimer;
        } else if (DELETE.equalsIgnoreCase(method)) {
            return deleteTimer;
        } else if (OPTIONS.equalsIgnoreCase(method)) {
            return optionsTimer;
        } else if (TRACE.equalsIgnoreCase(method)) {
            return traceTimer;
        } else if (CONNECT.equalsIgnoreCase(method)) {
            return connectTimer;
        } else if (PATCH.equalsIgnoreCase(method)) {
            return patchTimer;
        } else if (MOVE.equalsIgnoreCase(method)) {
            return moveTimer;
        }
        return otherTimer;
    }
}
