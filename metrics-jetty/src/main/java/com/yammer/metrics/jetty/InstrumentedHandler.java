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
        final Class<?> klass = underlying.getClass();
        this.dispatches = registry.add(MetricName.name(klass, "dispatches"), new Timer());
        this.requests = registry.add(MetricName.name(klass, "requests"), new Meter("requests"));
        this.resumes = registry.add(MetricName.name(klass, "resumes"), new Meter("requests"));
        this.suspends = registry.add(MetricName.name(klass, "suspends"), new Meter("requests"));
        this.expires = registry.add(MetricName.name(klass, "expires"), new Meter("requests"));

        this.activeRequests = registry.add(MetricName.name(klass, "active-requests"), new Counter());
        this.activeSuspendedRequests = registry.add(MetricName.name(klass, "active-suspended-requests"), new Counter());
        this.activeDispatches = registry.add(MetricName.name(klass, "active-dispatches"), new Counter());

        this.responses = new Meter[]{
                registry.add(MetricName.name(klass, "1xx-responses"), new Meter("responses")), // 1xx
                registry.add(MetricName.name(klass, "2xx-responses"), new Meter("responses")), // 2xx
                registry.add(MetricName.name(klass, "3xx-responses"), new Meter("responses")), // 3xx
                registry.add(MetricName.name(klass, "4xx-responses"), new Meter("responses")), // 4xx
                registry.add(MetricName.name(klass, "5xx-responses"), new Meter("responses"))  // 5xx
        };

        registry.add(MetricName.name(klass, "percent-4xx-1m"),
                     new RatioGauge() {
                         @Override
                         protected double getNumerator() {
                             return responses[3].getOneMinuteRate();
                         }

                         @Override
                         protected double getDenominator() {
                             return requests.getOneMinuteRate();
                         }
                     });

        registry.add(MetricName.name(klass, "percent-4xx-5m"),
                     new RatioGauge() {
                         @Override
                         protected double getNumerator() {
                             return responses[3].getFiveMinuteRate();
                         }

                         @Override
                         protected double getDenominator() {
                             return requests.getFiveMinuteRate();
                         }
                     });

        registry.add(MetricName.name(klass, "percent-4xx-15m"),
                     new RatioGauge() {
                         @Override
                         protected double getNumerator() {
                             return responses[3].getFifteenMinuteRate();
                         }

                         @Override
                         protected double getDenominator() {
                             return requests.getFifteenMinuteRate();
                         }
                     });

        registry.add(MetricName.name(klass, "percent-5xx-1m"),
                     new RatioGauge() {
                         @Override
                         protected double getNumerator() {
                             return responses[4].getOneMinuteRate();
                         }

                         @Override
                         protected double getDenominator() {
                             return requests.getOneMinuteRate();
                         }
                     });

        registry.add(MetricName.name(klass, "percent-5xx-5m"),
                     new RatioGauge() {
                         @Override
                         protected double getNumerator() {
                             return responses[4].getFiveMinuteRate();
                         }

                         @Override
                         protected double getDenominator() {
                             return requests.getFiveMinuteRate();
                         }
                     });

        registry.add(MetricName.name(klass, "percent-5xx-15m"),
                     new RatioGauge() {
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

        this.getRequests = registry.add(MetricName.name(klass, "get-requests"), new Timer());
        this.postRequests = registry.add(MetricName.name(klass, "post-requests"), new Timer());
        this.headRequests = registry.add(MetricName.name(klass, "head-requests"), new Timer());
        this.putRequests = registry.add(MetricName.name(klass, "put-requests"), new Timer());
        this.deleteRequests = registry.add(MetricName.name(klass, "delete-requests"), new Timer());
        this.optionsRequests = registry.add(MetricName.name(klass, "options-requests"), new Timer());
        this.traceRequests = registry.add(MetricName.name(klass, "trace-requests"), new Timer());
        this.connectRequests = registry.add(MetricName.name(klass, "connect-requests"), new Timer());
        this.patchRequests = registry.add(MetricName.name(klass, "patch-requests"), new Timer());
        this.otherRequests = registry.add(MetricName.name(klass, "other-requests"), new Timer());

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
