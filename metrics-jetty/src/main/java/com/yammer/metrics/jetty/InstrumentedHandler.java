package com.yammer.metrics.jetty;

import com.yammer.metrics.Metrics;
import com.yammer.metrics.core.*;
import com.yammer.metrics.util.RatioGauge;
import org.eclipse.jetty.continuation.Continuation;
import org.eclipse.jetty.continuation.ContinuationListener;
import org.eclipse.jetty.server.AsyncContinuation;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.HandlerWrapper;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import static org.eclipse.jetty.http.HttpMethods.*;

/**
 * A Jetty {@link Handler} which records various metrics about an underlying
 * {@link Handler} instance.
 */
public class InstrumentedHandler extends HandlerWrapper {
    private static final String PATCH = "PATCH";

    private final Timer dispatches;
    private final Meter requests;
    private final Meter resumes;
    private final Meter suspends;
    private final Meter expires;

    private final Counter activeRequests;
    private final Counter activeSuspendedRequests;
    private final Counter activeDispatches;

    private final Meter[] responses;

    private final Timer getRequests, postRequests, headRequests,
            putRequests, deleteRequests, optionsRequests, traceRequests,
            connectRequests, patchRequests, otherRequests;

    private final ContinuationListener listener;

    /**
     * Create a new instrumented handler.
     *
     * @param underlying the handler about which metrics will be collected
     */
    public InstrumentedHandler(Handler underlying) {
        this(underlying, Metrics.defaultRegistry());
    }

    /**
     * Create a new instrumented handler using a given metrics registry.
     *
     * @param underlying the handler about which metrics will be collected
     * @param registry the registry for the metrics
     */
    public InstrumentedHandler(Handler underlying, MetricsRegistry registry) {
        super();

        final MetricsGroup metrics = registry.group(underlying.getClass());

        this.dispatches = metrics.timer("dispatches").build();
        this.requests = metrics.meter("requests").measuring("requests").build();
        this.resumes = metrics.meter("resumes").measuring("requests").build();
        this.suspends = metrics.meter("suspends").measuring("requests").build();
        this.expires = metrics.meter("expires").measuring("requests").build();

        this.activeRequests = metrics.counter("active-requests").build();
        this.activeSuspendedRequests = metrics.counter("active-suspended-requests").build();
        this.activeDispatches = metrics.counter("active-dispatches").build();

        this.responses = new Meter[]{
                metrics.meter("1xx-responses").measuring("responses").build(), // 1xx
                metrics.meter("2xx-responses").measuring("responses").build(), // 2xx
                metrics.meter("3xx-responses").measuring("responses").build(), // 3xx
                metrics.meter("4xx-responses").measuring("responses").build(), // 4xx
                metrics.meter("5xx-responses").measuring("responses").build(), // 5xx
        };

        metrics.gauge("percent-4xx-1m").build(new RatioGauge() {
            @Override
            protected double getNumerator() {
                return responses[3].getOneMinuteRate();
            }

            @Override
            protected double getDenominator() {
                return requests.getOneMinuteRate();
            }
        });

        metrics.gauge("percent-4xx-5m").build(new RatioGauge() {
            @Override
            protected double getNumerator() {
                return responses[3].getFiveMinuteRate();
            }

            @Override
            protected double getDenominator() {
                return requests.getFiveMinuteRate();
            }
        });

        metrics.gauge("percent-4xx-15m").build(new RatioGauge() {
            @Override
            protected double getNumerator() {
                return responses[3].getFifteenMinuteRate();
            }

            @Override
            protected double getDenominator() {
                return requests.getFifteenMinuteRate();
            }
        });

        metrics.gauge("percent-5xx-1m").build(new RatioGauge() {
            @Override
            protected double getNumerator() {
                return responses[4].getOneMinuteRate();
            }

            @Override
            protected double getDenominator() {
                return requests.getOneMinuteRate();
            }
        });

        metrics.gauge("percent-5xx-5m").build(new RatioGauge() {
            @Override
            protected double getNumerator() {
                return responses[4].getFiveMinuteRate();
            }

            @Override
            protected double getDenominator() {
                return requests.getFiveMinuteRate();
            }
        });

        metrics.gauge("percent-5xx-15m").build(new RatioGauge() {
            @Override
            protected double getNumerator() {
                return responses[4].getFifteenMinuteRate();
            }

            @Override
            protected double getDenominator() {
                return requests.getFifteenMinuteRate();
            }
        });

        this.listener = new ContinuationListener() {
            @Override
            public void onComplete(Continuation continuation) {
                expires.mark();
            }

            @Override
            public void onTimeout(Continuation continuation) {
                final Request request = ((AsyncContinuation) continuation).getBaseRequest();
                updateResponses(request);
                if (!continuation.isResumed()) {
                    activeSuspendedRequests.dec();
                }
            }
        };

        this.getRequests = metrics.timer("get-requests").build();
        this.postRequests = metrics.timer("post-requests").build();
        this.headRequests = metrics.timer("head-requests").build();
        this.putRequests = metrics.timer("put-requests").build();
        this.deleteRequests = metrics.timer("delete-requests").build();
        this.optionsRequests = metrics.timer("option-requests").build();
        this.traceRequests = metrics.timer("trace-requests").build();
        this.connectRequests = metrics.timer("connect-requests").build();
        this.patchRequests = metrics.timer("patch-requests").build();
        this.otherRequests = metrics.timer("other-requests").build();

        setHandler(underlying);
    }

    @Override
    public void handle(String target, Request request,
                       HttpServletRequest httpRequest, HttpServletResponse httpResponse)
            throws IOException, ServletException {
        activeDispatches.inc();

        final AsyncContinuation continuation = request.getAsyncContinuation();

        final long start;
        final boolean isMilliseconds;

        if (continuation.isInitial()) {
            activeRequests.inc();
            start = request.getTimeStamp();
            isMilliseconds = true;
        } else {
            activeSuspendedRequests.dec();
            if (continuation.isResumed()) {
                resumes.mark();
            }
            isMilliseconds = false;
            start = System.nanoTime();
        }

        try {
            super.handle(target, request, httpRequest, httpResponse);
        } finally {
            if (isMilliseconds) {
                final long duration = System.currentTimeMillis() - start;
                dispatches.update(duration, TimeUnit.MILLISECONDS);
                requestTimer(request.getMethod()).update(duration, TimeUnit.MILLISECONDS);
            } else {
                final long duration = System.nanoTime() - start;
                dispatches.update(duration, TimeUnit.NANOSECONDS);
                requestTimer(request.getMethod()).update(duration, TimeUnit.NANOSECONDS);
            }

            activeDispatches.dec();
            if (continuation.isSuspended()) {
                if (continuation.isInitial()) {
                    continuation.addContinuationListener(listener);
                }
                suspends.mark();
                activeSuspendedRequests.inc();
            } else if (continuation.isInitial()) {
                updateResponses(request);
            }
        }
    }

    private Timer requestTimer(String method) {
        if (GET.equalsIgnoreCase(method)) {
            return getRequests;
        } else if (POST.equalsIgnoreCase(method)) {
            return postRequests;
        } else if (PUT.equalsIgnoreCase(method)) {
            return putRequests;
        } else if (HEAD.equalsIgnoreCase(method)) {
            return headRequests;
        } else if (DELETE.equalsIgnoreCase(method)) {
            return deleteRequests;
        } else if (OPTIONS.equalsIgnoreCase(method)) {
            return optionsRequests;
        } else if (TRACE.equalsIgnoreCase(method)) {
            return traceRequests;
        } else if (CONNECT.equalsIgnoreCase(method)) {
            return connectRequests;
        } else if (PATCH.equalsIgnoreCase(method)) {
            return patchRequests;
        }
        return otherRequests;
    }

    private void updateResponses(Request request) {
        final int response = request.getResponse().getStatus() / 100;
        if (response >= 1 && response <= 5) {
            responses[response - 1].mark();
        }
        activeRequests.dec();
        requests.mark();
    }
}
