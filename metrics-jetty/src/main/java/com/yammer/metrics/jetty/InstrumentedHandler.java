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
    public InstrumentedHandler(Handler underlying, MetricRegistry registry) {
        super();
        final Class<?> klass = underlying.getClass();
        this.dispatches = registry.timer(Metrics.name(klass, "dispatches"));
        this.requests = registry.meter(Metrics.name(klass, "requests"));
        this.resumes = registry.meter(Metrics.name(klass, "resumes"));
        this.suspends = registry.meter(Metrics.name(klass, "suspends"));
        this.expires = registry.meter(Metrics.name(klass, "expires"));

        this.activeRequests = registry.counter(Metrics.name(klass, "active-requests"));
        this.activeSuspendedRequests = registry.counter(Metrics.name(klass, "active-suspended-requests"));
        this.activeDispatches = registry.counter(Metrics.name(klass, "active-dispatches"));

        this.responses = new Meter[]{
                registry.meter(Metrics.name(klass, "1xx-responses")), // 1xx
                registry.meter(Metrics.name(klass, "2xx-responses")), // 2xx
                registry.meter(Metrics.name(klass, "3xx-responses")), // 3xx
                registry.meter(Metrics.name(klass, "4xx-responses")), // 4xx
                registry.meter(Metrics.name(klass, "5xx-responses"))  // 5xx
        };

        registry.add(Metrics.name(klass, "percent-4xx-1m"),
                     new RatioGauge() {
                         @Override
                         protected Ratio getRatio() {
                             return Ratio.of(responses[3].getOneMinuteRate(),
                                             requests.getOneMinuteRate());
                         }
                     });

        registry.add(Metrics.name(klass, "percent-4xx-5m"),
                     new RatioGauge() {
                         @Override
                         protected Ratio getRatio() {
                             return Ratio.of(responses[3].getFiveMinuteRate(),
                                             requests.getFiveMinuteRate());
                         }
                     });

        registry.add(Metrics.name(klass, "percent-4xx-15m"),
                     new RatioGauge() {
                         @Override
                         protected Ratio getRatio() {
                             return Ratio.of(responses[3].getFifteenMinuteRate(),
                                             requests.getFifteenMinuteRate());
                         }
                     });

        registry.add(Metrics.name(klass, "percent-5xx-1m"),
                     new RatioGauge() {
                         @Override
                         protected Ratio getRatio() {
                             return Ratio.of(responses[4].getOneMinuteRate(),
                                             requests.getOneMinuteRate());
                         }
                     });

        registry.add(Metrics.name(klass, "percent-5xx-5m"),
                     new RatioGauge() {
                         @Override
                         protected Ratio getRatio() {
                             return Ratio.of(responses[4].getFiveMinuteRate(),
                                             requests.getFiveMinuteRate());
                         }
                     });

        registry.add(Metrics.name(klass, "percent-5xx-15m"),
                     new RatioGauge() {
                         @Override
                         protected Ratio getRatio() {
                             return Ratio.of(responses[4].getFifteenMinuteRate(),
                                             requests.getFifteenMinuteRate());
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

        this.getRequests = registry.timer(Metrics.name(klass, "get-requests"));
        this.postRequests = registry.timer(Metrics.name(klass, "post-requests"));
        this.headRequests = registry.timer(Metrics.name(klass, "head-requests"));
        this.putRequests = registry.timer(Metrics.name(klass, "put-requests"));
        this.deleteRequests = registry.timer(Metrics.name(klass, "delete-requests"));
        this.optionsRequests = registry.timer(Metrics.name(klass, "options-requests"));
        this.traceRequests = registry.timer(Metrics.name(klass, "trace-requests"));
        this.connectRequests = registry.timer(Metrics.name(klass, "connect-requests"));
        this.patchRequests = registry.timer(Metrics.name(klass, "patch-requests"));
        this.otherRequests = registry.timer(Metrics.name(klass, "other-requests"));

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
