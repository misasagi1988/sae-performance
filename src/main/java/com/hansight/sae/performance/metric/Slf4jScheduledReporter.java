package com.hansight.sae.performance.metric;

import com.codahale.metrics.*;
import org.slf4j.Logger;

import java.util.Map;
import java.util.SortedMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Slf4jScheduledReporter extends ScheduledReporter {

    private Logger logger;
    private String prefix;

    public static Builder forRegistry(MetricRegistry registry) {
        return new Builder(registry);
    }

    public static class Builder {
        private final MetricRegistry registry;
        private Logger logger;
        private String prefix;
        private TimeUnit rateUnit;
        private TimeUnit durationUnit;
        private MetricFilter filter;

        private Builder(MetricRegistry registry) {
            this.registry = registry;
            this.logger = null;
            this.prefix = "";
            this.rateUnit = TimeUnit.SECONDS;
            this.durationUnit = TimeUnit.MILLISECONDS;
            this.filter = MetricFilter.ALL;
        }

        public Builder outputTo(Logger logger) {
            this.logger = logger;
            return this;
        }

        public Builder prefixedWith(String prefix) {
            this.prefix = prefix;
            return this;
        }

        public Builder convertRatesTo(TimeUnit rateUnit) {
            this.rateUnit = rateUnit;
            return this;
        }

        public Builder convertDurationsTo(TimeUnit durationUnit) {
            this.durationUnit = durationUnit;
            return this;
        }

        public Builder filter(MetricFilter filter) {
            this.filter = filter;
            return this;
        }

        public Slf4jScheduledReporter build() {
            return new Slf4jScheduledReporter(registry, logger, prefix, rateUnit, durationUnit, filter);
        }
    }

    private Slf4jScheduledReporter(MetricRegistry registry,
                                   Logger logger,
                                   String prefix,
                                   TimeUnit rateUnit,
                                   TimeUnit durationUnit,
                                   MetricFilter filter) {
        super(registry, "logger-reporter", filter, rateUnit, durationUnit);
        this.logger = logger;
        this.prefix = prefix;
    }

    protected Slf4jScheduledReporter(MetricRegistry registry, String name, MetricFilter filter, TimeUnit rateUnit, TimeUnit durationUnit) {
        super(registry, name, filter, rateUnit, durationUnit);
    }

    protected Slf4jScheduledReporter(MetricRegistry registry, String name, MetricFilter filter, TimeUnit rateUnit, TimeUnit durationUnit, ScheduledExecutorService executor) {
        super(registry, name, filter, rateUnit, durationUnit, executor);
    }

    private static Map<String, Long> metricMap = new ConcurrentHashMap<>();

    /**
     * Description: remove meter in map
     *
     * @Date: 2018/7/19
     */
    public void removeKey(String name) {
        metricMap.remove(name);
    }

    /**
     * Description: override function, write statistics to file
     *
     * @Date: 2018/7/19
     */
    @Override
    public void report(SortedMap<String, Gauge> gauges, SortedMap<String, Counter> counters, SortedMap<String, Histogram> histograms, SortedMap<String, Meter> meters, SortedMap<String, Timer> timers) {
        if(meters == null || meters.isEmpty())
            return;
        StringBuilder stringBuilder = new StringBuilder("metrics info:");
        for (Map.Entry<String, Meter> entry : meters.entrySet()) {
            String name = entry.getKey();
            Meter meter = entry.getValue();
            if(!metricMap.containsKey(entry.getKey())) {
                stringBuilder.append("\n");
                stringBuilder.append(String.format(" name=%s, total_count=[%d], period_count=[%d]", name, meter.getCount(), meter.getCount()));
                metricMap.put(name, meter.getCount());

            } else {
                stringBuilder.append("\n");
                stringBuilder.append(String.format(" name=%s, total_count=[%d], period_count=[%d]", name, meter.getCount(), meter.getCount() - metricMap.get(name)));
                metricMap.put(name, meter.getCount());
            }
        }
        stringBuilder.append("\n");
        logger.info("{}", stringBuilder);
    }

    private void logMeter(String name, Meter meter) {

        if(!metricMap.containsKey(name)) {
            logger.info("name= {} , total_count=[ {} ], period_count=[ {} ]", prefix(name), meter.getCount(), meter.getCount());
            metricMap.put(name, meter.getCount());
        } else {
            logger.info("name= {} , total_count=[ {} ], period_count=[ {} ]", prefix(name), meter.getCount(), meter.getCount()-metricMap.get(name));
            metricMap.put(name, meter.getCount());
        }
    }

    private String prefix(String... components) {
        return MetricRegistry.name(prefix, components);
    }
}

