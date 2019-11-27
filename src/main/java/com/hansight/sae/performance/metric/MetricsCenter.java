package com.hansight.sae.performance.metric;

import com.codahale.metrics.Counter;
import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

public class MetricsCenter {
    private static final Logger logger = LoggerFactory.getLogger(MetricsCenter.class);

    private static MetricRegistry metricRegistry = new MetricRegistry();

    private static Slf4jScheduledReporter slf4jScheduledReporter;

    private static final MetricsCenter metricsCenter = new MetricsCenter();


    private MetricsCenter() {
        slf4jScheduledReporter = Slf4jScheduledReporter.forRegistry(metricRegistry)
                .outputTo(logger)
                .convertRatesTo(TimeUnit.SECONDS)
                .convertDurationsTo(TimeUnit.MILLISECONDS)
                .build();
    }

    public static MetricsCenter me() {
        return metricsCenter;
    }

    /**
     * Description: start metric reporter
     *
     * @Date: 2018/7/19
     */
    public void start(long period) {
        slf4jScheduledReporter.start(period, TimeUnit.SECONDS);
    }

    /**
     * Description: get meter for specific name
     *
     * @Date: 2018/7/19
     */
    public Meter getMeter(String name) {
        return metricRegistry.meter(name);
    }

    /**
     * Description: remove meter with specific name
     *
     * @Date: 2018/7/19
     */
    public void removeMeter(String name) {
        metricRegistry.remove(name);
        slf4jScheduledReporter.removeKey(name);
    }

    /**
     * Description: get counter for specific name
     *
     * @Date: 2018/7/19
     */
    public Counter getCounter(String name) {
        return metricRegistry.counter(name);
    }

    public static final String KAFKA_EVENT = "event";
    public static final String ALARM_TRIGGERRED = "alarm";

}

